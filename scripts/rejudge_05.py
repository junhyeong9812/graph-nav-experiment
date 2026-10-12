#!/usr/bin/env python3
"""과제 05 재판정(rejudge-05): 죽은 경로 hidden 테스트 3개를 뺀 판정(v2).

`periodicTracking*` 3개는 호출처가 없는 updateShipmentStatuses() 를 부른다(docs/analysis.md '판정 결함: 과제 05').
- 기록 기반: 원 판정의 hidden 실패 목록이 이 3개뿐이고 test.sh·회귀가 통과했으면 v2 성공.
- --verify: v2 oracle 사본(3개에 @Disabled)으로 failed/<run_id> 보관 사본을 실제로 다시 판정해 기록 기반과 대조.
결과: <results>/rejudge-05.jsonl (05 실패 행마다 한 줄). 원 runs.jsonl 은 바꾸지 않는다.
사용: python3 scripts/rejudge_05.py [--verify RUN_ID,...]
"""
import argparse
import json
import re
import shutil
import sys
import tempfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import run_experiment as R  # noqa: E402

DEAD = {"periodicTrackingUpdateRewardsWhenDelivered", "periodicTrackingUpdateRewardsEachDeliveredOrderSeparately",
        "periodicTrackingUpdateDoesNotRewardBeforeDelivery"}
DIRS = ["results-sonnet", "results-haiku", "results-var-original", "results-var-obfuscated", "results-var-inflated",
        "results-var-inflated-obfuscated", "results-var-misnamed", "results-gloss-obfuscated"]


def method(t: str) -> str:
    return t.rsplit(".", 1)[1].split("(")[0]


def v2_from_record(r: dict) -> tuple:
    failed = {method(t) for t in r["hidden_failed_tests"]}
    if not r["test_pass"]:
        return False, "test.sh 실패"
    if not failed:
        return False, "실패 목록 없음(컴파일 오류 등)"
    if not failed <= DEAD:
        return False, "죽은 경로 밖 실패: " + ", ".join(sorted(failed - DEAD))
    if not r.get("regression_pass"):
        return False, "회귀 실패"
    return True, "죽은 경로 테스트만 실패"


def v2_oracle(case: str) -> Path:
    """현재 ORACLE 의 05 를 복사해 죽은 경로 테스트에 @Disabled 를 붙인다(줄 수 보존)."""
    root = Path(tempfile.mkdtemp(prefix="oracle-v2-", dir=R.HOST_RUNS))
    shutil.copytree(R.ORACLE / case, root / case)
    f = next((root / case / "hidden").rglob("PurchaseRewardHiddenTest.java"))
    s, n = f.read_text(), 0
    for m in DEAD:
        s, k = re.subn(rf"(\bvoid\s+{m}\s*\()", r"@org.junit.jupiter.api.Disabled \1", s)
        n += k
    if n != len(DEAD):
        raise SystemExit(f"@Disabled 삽입 {n}/{len(DEAD)}: {f}")
    f.write_text(s)
    return root


def verify(d: str, r: dict) -> dict:
    R.set_variant(r.get("variant", "original"))
    work = R.ROOT / d / "failed" / r["run_id"]
    orig_oracle, root = R.ORACLE, v2_oracle(r["case"])
    try:
        R.ORACLE = root
        j = R.judge(r["case"], work)
    finally:
        R.ORACLE = orig_oracle
        shutil.rmtree(root, ignore_errors=True)
    ok = bool(j.get("test_pass")) and bool(j.get("hidden_pass")) and bool(j.get("regression_pass")) and not j.get("tampered")
    return {"verified_v2_success": ok, "verified_hidden_failed": [method(t) for t in j.get("hidden_failed_tests") or []]}


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--verify", default="", help="실제 재판정할 run_id 목록(쉼표)")
    a = ap.parse_args()
    R.HOST_RUNS.mkdir(exist_ok=True)
    want = set(filter(None, a.verify.split(",")))
    for d in DIRS:
        rows = [json.loads(l) for l in (R.ROOT / d / "runs.jsonl").read_text().splitlines() if l.strip()]
        out = []
        for r in rows:
            if r["case"][:2] != "05" or r["success"] or not r["valid"]:
                continue
            v2, why = v2_from_record(r)
            row = {"run_id": r["run_id"], "cond": r["cond"], "rep": r["rep"], "orig_success": False,
                   "v2_success": v2, "reason": why}
            if r["run_id"] in want:
                row.update(verify(d, r))
            out.append(row)
        (R.ROOT / d / "rejudge-05.jsonl").write_text("".join(json.dumps(x, ensure_ascii=False) + "\n" for x in out))
        print(f"{d}: 05 실패 {len(out)} → v2 성공 {sum(x['v2_success'] for x in out)}"
              + "".join(f"\n  {x['run_id']} {x['v2_success']} {x['reason']}"
                        + (f" | 실측 {x['verified_v2_success']} {x['verified_hidden_failed']}" if 'verified_v2_success' in x else "")
                        for x in out))
    return 0


if __name__ == "__main__":
    sys.exit(main())
