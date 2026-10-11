#!/usr/bin/env python3
"""본 실험 집계: results-<model>/runs.jsonl + grades.jsonl → 마크다운 표(stdout).

주 지표는 총 토큰 ÷B(과제별 B 중앙값 기준)와 성공률(ADR-0001 D6).
조건 요약의 ÷B 는 과제별 비율(조건 중앙값 ÷ B 중앙값)의 기하평균이고, 95% 구간은 과제 안에서 회차를 복원 추출한 부트스트랩이다.
사용: python3 scripts/analyze.py > results-analysis.md
"""
import json
import math
import random
import statistics as st
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODELS = ["sonnet", "haiku"]
CONDS = ["B", "AF", "AC", "HF", "HC", "LF", "LC"]
TYPES = {"bug": ["01", "02"], "pattern": ["03", "04"], "change": ["05", "06"], "question": ["07", "08"]}
# 패턴 과제 지점 = hidden 테스트 클래스(oracle/<case>/design.md)
SITES = {
    "03": ["PurchaseBidListPagingHiddenTest", "SaleBidListPagingHiddenTest",
           "ProductSearchPagingHiddenTest", "ProductNameSearchPagingHiddenTest"],
    "04": ["PurchaseBidRegistrationHiddenTest", "SaleBidRegistrationHiddenTest"],
}
LLM_GEN_TOKENS = 34_740_000  # graphs/llm/GENERATION.md (재생성본)
LLM_GEN_COST = 18.93


def load(model):
    d = ROOT / f"results-{model}"
    rows = [json.loads(l) for l in (d / "runs.jsonl").read_text().splitlines() if l.strip()]
    grades = {}
    gf = d / "grades.jsonl"
    if gf.exists():
        for l in gf.read_text().splitlines():
            if l.strip():
                g = json.loads(l)
                grades[g["run_id"]] = g.get("total")
    for r in rows:
        r["grade"] = grades.get(r["run_id"])
        r["site_score"] = site_score(r)
    return rows


def site_score(r):
    sites = SITES.get(r["case"][:2])
    if not sites:
        return None
    if r["hidden_pass"]:
        return 1.0
    failed = {t.rsplit(".", 1)[0].rsplit(".", 1)[1] for t in r["hidden_failed_tests"]}
    if not failed:  # 실패 테스트 없이 실패 = 컴파일 오류 → 0
        return 0.0
    return sum(s not in failed for s in sites) / len(sites)


def hidden_relevant(case):
    """relevant.txt 중 공개 과제 파일(cases/NN)에 이름이 나오지 않는 파일 = 스스로 찾아야 하는 파일."""
    rel = [l.strip() for l in (ROOT / "oracle" / case / "relevant.txt").read_text().splitlines()
           if l.strip() and not l.startswith("#")]
    public = "".join(p.read_text(errors="ignore") for p in (ROOT / "cases" / case).rglob("*") if p.is_file())
    return [f for f in rel if Path(f).stem not in public]


def first_hidden_turn(r, hidden):
    ts = [t for f, t in (r.get("relevant_first_turns") or {}).items() if f in hidden and t is not None]
    return min(ts) if ts else None


def gmean(xs):
    return math.exp(sum(math.log(x) for x in xs) / len(xs))


def ratio_summary(by, cases, cond, boot=2000, seed=7):
    pts = [st.median([r["total_tokens"] for r in by[(c, cond)]]) / st.median([r["total_tokens"] for r in by[(c, "B")]])
           for c in cases]
    rnd = random.Random(seed)
    bs = []
    for _ in range(boot):
        v = []
        for c in cases:
            x = [r["total_tokens"] for r in by[(c, cond)]]
            b = [r["total_tokens"] for r in by[(c, "B")]]
            v.append(st.median(rnd.choices(x, k=len(x))) / st.median(rnd.choices(b, k=len(b))))
        bs.append(gmean(v))
    bs.sort()
    return gmean(pts), bs[int(0.025 * boot)], bs[int(0.975 * boot)]


def fmt(x, nd=2):
    return "-" if x is None else f"{x:.{nd}f}"


def main():
    cases = sorted({p.name for p in (ROOT / "cases").iterdir() if p.is_dir()})
    short = {c[:2]: c for c in cases}
    hid = {c[:2]: hidden_relevant(c) for c in cases}
    print("# 본 실험 집계 (scripts/analyze.py 생성)\n")
    print("스스로 찾아야 하는 관련 파일(공개 과제 파일에 이름 없음): " +
          "; ".join(f"{k}: {len(v)}개" for k, v in hid.items()) + "\n")
    for model in MODELS:
        rows = load(model)
        by = defaultdict(list)
        for r in rows:
            by[(r["case"][:2], r["cond"])].append(r)
        ks = sorted({k[0] for k in by})
        print(f"## {model} ({len(rows)}행, 무효 {sum(not r['valid'] for r in rows)})\n")

        print("### 과제 × 조건: 총 토큰 ÷B (성공 수/3 · 패턴은 지점 점수 평균 · 질문은 채점 평균/8)\n")
        print("| 과제 | " + " | ".join(CONDS) + " |")
        print("|---" * (len(CONDS) + 1) + "|")
        for k in ks:
            b = st.median([r["total_tokens"] for r in by[(k, "B")]])
            cells = []
            for c in CONDS:
                rs = by[(k, c)]
                m = st.median([r["total_tokens"] for r in rs])
                head = f"{m / 1000:.0f}k" if c == "B" else f"{m / b:.2f}"
                if rs[0]["type"] == "question":
                    gs = [r["grade"] for r in rs if r["grade"] is not None]
                    tail = fmt(st.mean(gs)) if gs else "?"
                elif rs[0]["site_score"] is not None:
                    tail = f"{sum(bool(r['success']) for r in rs)}/3 · {st.mean(r['site_score'] for r in rs):.2f}"
                else:
                    tail = f"{sum(bool(r['success']) for r in rs)}/3"
                cells.append(f"{head} · {tail}")
            print(f"| {short[k]} | " + " | ".join(cells) + " |")
        print()

        print("### 조건 요약\n")
        print("| 조건 | ÷B 기하평균 [95%] | 버그 | 패턴 | 변경 | 질문 | 성공(수정 과제 18) | 회귀 실패 | 질문 평균/8 | 턴 중앙값 | 숨은 파일 첫 도달 턴 중앙값 | 지도 열람 중앙값 | 하위 에이전트 | 비용 $ |")
        print("|---" * 14 + "|")
        for c in CONDS:
            rs = [r for r in rows if r["cond"] == c]
            if c == "B":
                tot = "1.00"
                typ_cells = ["1.00"] * 4
            else:
                g, lo, hi = ratio_summary(by, ks, c)
                tot = f"{g:.2f} [{lo:.2f}–{hi:.2f}]"
                typ_cells = [f"{ratio_summary(by, v, c, boot=1)[0]:.2f}" for v in TYPES.values()]
            fix = [r for r in rs if r["type"] != "question"]
            gs = [r["grade"] for r in rs if r["grade"] is not None]
            ft = [first_hidden_turn(r, hid[r["case"][:2]]) for r in rs]
            ft = [x for x in ft if x is not None]
            print(f"| {c} | {tot} | " + " | ".join(typ_cells) +
                  f" | {sum(bool(r['success']) for r in fix)}/{len(fix)}"
                  f" | {sum(r.get('regression_pass') is False for r in fix)}"
                  f" | {fmt(st.mean(gs)) if gs else '?'}"
                  f" | {st.median(r['num_turns'] for r in rs)}"
                  f" | {fmt(st.median(ft), 1) if ft else '-'} ({len(ft)}/{len(rs)})"
                  f" | {st.median((r['nav_reads'] or 0) for r in rs)}"
                  f" | {sum(r['subagent_calls'] or 0 for r in rs)}"
                  f" | {sum(r['total_cost_usd'] or 0 for r in rs):.2f} |")
        print()

        # LLM 지도 손익분기: 실행당 절감 토큰(과제별 B 중앙값 − L 중앙값의 과제 평균)
        print("### LLM 지도 손익분기 (생성 34.74M 토큰 / $18.93, Opus)\n")
        for c in ("LF", "LC"):
            save = st.mean(st.median([r["total_tokens"] for r in by[(k, "B")]]) -
                           st.median([r["total_tokens"] for r in by[(k, c)]]) for k in ks)
            csave = st.mean(st.median([r["total_cost_usd"] for r in by[(k, "B")]]) -
                            st.median([r["total_cost_usd"] for r in by[(k, c)]]) for k in ks)
            be = f"{LLM_GEN_TOKENS / save:.0f}회" if save > 0 else "회수 불가(절감 없음)"
            bec = f"{LLM_GEN_COST / csave:.0f}회" if csave > 0 else "회수 불가"
            print(f"- {c}: 실행당 평균 절감 {save / 1000:+.0f}k 토큰 · ${csave:+.3f} → 토큰 기준 {be}, 비용 기준 {bec}")
        print()


if __name__ == "__main__":
    main()
