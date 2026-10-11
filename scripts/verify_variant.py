#!/usr/bin/env python3
"""변형 검증(size-structure-variants 명세 §5): 과제 01~06 판정 + 난독화 누출 + 크기 + grep 잡음.

- 판정: 원본 상태(사본 그대로) → 1차 실패여야 한다. 정답 패치 적용 → 1차·회귀 통과여야 한다. 러너 judge 를 그대로 쓴다.
- 누출: 에이전트 사본(copy_target)에서 원래 타입 이름(MAPPING.json 키)이 단어로 남은 곳 수.
- 크기: src/main Java 줄 수.
- grep 잡음: 과제별 증상 단어(GREP_TERMS)로 src/main 을 grep 한 결과 줄 수.
사용: python3 scripts/verify_variant.py <변형|original> [--cases 01,02] [--skip-judge]
"""
import argparse
import concurrent.futures as cf
import json
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import run_experiment as R  # noqa: E402

# 과제 TASK.md 의 증상 서술에서 에이전트가 먼저 검색할 만한 단어(업무 용어 — 문자열·메서드에는 남는다)
GREP_TERMS = {"01": "address", "02": "card", "03": "bid", "04": "MATCHED", "05": "delivered|DELIVERED",
              "06": "interest", "07": "instant", "08": "tracking"}


def judge_case(case: str) -> dict:
    out = {"case": case}
    for tag, patch in (("orig", None), ("fix", R.ORACLE / case / "fix.patch")):
        d = Path(tempfile.mkdtemp(prefix=f"verify-{case}-{tag}-", dir=R.HOST_RUNS))
        try:
            w = d / "work"
            R.copy_target(w)
            shutil.copytree(R.CASES / case, w, dirs_exist_ok=True, ignore=shutil.ignore_patterns("TYPE"))
            if patch is not None:
                p = subprocess.run(["git", "apply", "-p1", str(patch)], cwd=w, capture_output=True, text=True)
                if p.returncode:
                    out[tag] = {"apply_error": p.stderr[-400:]}
                    continue
            j = R.judge(case, w)
            out[tag] = {k: j.get(k) for k in ("test_pass", "hidden_pass", "regression_pass", "regression_error",
                                               "hidden_failed_tests", "regression_failed_tests", "tampered")}
            if not j.get("hidden_pass") and not j.get("hidden_failed_tests"):
                out[tag]["hidden_tail"] = j.get("hidden_tail", "")[-400:]
        finally:
            shutil.rmtree(d, ignore_errors=True)
    o, f = out.get("orig", {}), out.get("fix", {})
    out["ok"] = (not o.get("hidden_pass")) and bool(f.get("test_pass")) and bool(f.get("hidden_pass")) \
        and bool(f.get("regression_pass")) and not f.get("tampered")
    return out


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("variant")
    ap.add_argument("--cases", default="01,02,03,04,05,06")
    ap.add_argument("--skip-judge", action="store_true")
    ap.add_argument("--parallel", type=int, default=3)
    a = ap.parse_args()
    R.set_variant(a.variant)
    R.HOST_RUNS.mkdir(exist_ok=True)
    report = {"variant": a.variant}

    d = Path(tempfile.mkdtemp(prefix="verify-copy-", dir=R.HOST_RUNS))
    try:
        R.copy_target(d / "t")
        main_java = list((d / "t" / "src/main/java").rglob("*.java"))
        report["main_java_files"] = len(main_java)
        report["main_java_lines"] = sum(len(f.read_text(errors="ignore").splitlines()) for f in main_java)
        mp = R.ROOT / "variants" / a.variant / "MAPPING.json"
        if mp.exists():
            old = json.loads(mp.read_text())["types"]
            pat = re.compile(r"\b(" + "|".join(sorted(map(re.escape, old), key=len, reverse=True)) + r")\b")
            leaks = {}
            for f in (d / "t").rglob("*"):
                if f.is_file() and f.suffix in (".java", ".yml", ".xml"):
                    for m in pat.finditer(f.read_text(errors="ignore")):
                        leaks[m.group(1)] = leaks.get(m.group(1), 0) + 1
            report["type_name_leaks"] = dict(sorted(leaks.items(), key=lambda x: -x[1])[:20])
            report["type_name_leak_total"] = sum(leaks.values())
            pk = json.loads(mp.read_text())["pkgs"]
            report["domain_dirs"] = sorted(p.name for p in (d / "t" / "src/main/java/com/fream/back/domain").iterdir())[:30]
            report["pkg_mapped"] = len(pk)
        report["grep_lines"] = {}
        for c, term in GREP_TERMS.items():
            p = subprocess.run(["grep", "-rniE", term, "src/main"], cwd=d / "t", capture_output=True, text=True)
            report["grep_lines"][c] = len(p.stdout.splitlines())
    finally:
        shutil.rmtree(d, ignore_errors=True)

    if not a.skip_judge:
        cases = sorted(c.name for c in R.CASES.iterdir() if c.is_dir() and c.name[:2] in a.cases.split(","))
        with cf.ThreadPoolExecutor(max_workers=a.parallel) as ex:
            report["judge"] = list(ex.map(judge_case, cases))
        report["judge_ok"] = all(j["ok"] for j in report["judge"])
    print(json.dumps(report, ensure_ascii=False, indent=1))
    return 0 if report.get("judge_ok", True) else 1


if __name__ == "__main__":
    sys.exit(main())
