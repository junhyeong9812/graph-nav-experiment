#!/usr/bin/env python3
"""크기·구조 분리 실험(size-structure-variants) 집계 → 마크다운 표(stdout).

변형 4종(original·obfuscated·inflated·inflated-obfuscated) × 조건 {B, AC} × 8과제 × 5회.
original 은 results-sonnet 의 B·AC r1~3 + results-var-original r4~5 를 합친다.
- AC÷B: 변형 안에서 과제별 (AC 중앙값 ÷ B 중앙값)의 기하평균, 부트스트랩 95% 구간(analyze.ratio_summary).
- B÷원본B: 변형 B 가 원본 B 보다 얼마나 비싼가(크기·이름이 탐색 비용 자체를 키웠나).
- 미끼 접촉: 도구 호출 입력에 미끼 도메인 패키지 경로(domain/<접두어…>)가 나온 호출 수(에이전트는 Bash 로 읽고 고치므로 근사).
사용: python3 scripts/analyze_variants.py > results-var-analysis.md
"""
import json
import re
import statistics as st
from collections import defaultdict
from pathlib import Path

from analyze import TYPES, fmt, gmean, ratio_summary

ROOT = Path(__file__).resolve().parent.parent
VARIANTS = ["original", "obfuscated", "inflated", "inflated-obfuscated"]
DECOY_PREFIX = ("admin", "settlement", "legacy", "partner", "internal")


def load_dir(d, keep=lambda r: True):
    rows = [json.loads(l) for l in (d / "runs.jsonl").read_text().splitlines() if l.strip()]
    grades = {}
    gf = d / "grades.jsonl"
    if gf.exists():
        for l in gf.read_text().splitlines():
            if l.strip():
                g = json.loads(l)
                grades[g["run_id"]] = g.get("total")
    out = []
    for r in rows:
        if keep(r):
            r["grade"] = grades.get(r["run_id"])
            out.append(r)
    return out


def load(variant):
    rows = load_dir(ROOT / f"results-var-{variant}")
    if variant == "original":
        rows += load_dir(ROOT / "results-sonnet", lambda r: r["cond"] in ("B", "AC") and r["rep"] <= 3)
    return rows


def base_dir(variant):
    return ROOT if variant == "original" else ROOT / "variants" / variant


def hidden_relevant(variant, case):
    b = base_dir(variant)
    rel = [l.strip() for l in (b / "oracle" / case / "relevant.txt").read_text().splitlines()
           if l.strip() and not l.startswith("#")]
    public = "".join(p.read_text(errors="ignore") for p in (b / "cases" / case).rglob("*") if p.is_file())
    return [f for f in rel if Path(f).stem not in public]


def decoy_pkgs(variant):
    """미끼 도메인 최상위 패키지 이름(변형 사본에서 보이는 이름)."""
    if "inflated" not in variant:
        return set()
    names = {p.name for p in (base_dir("inflated") / "target/fream-back/src/main/java/com/fream/back/domain").iterdir()
             if p.name.lower().startswith(DECOY_PREFIX)}
    if variant == "inflated":
        return names
    pk = json.loads((base_dir(variant) / "MAPPING.json").read_text())["pkgs"]
    return {pk[n] for n in names if n in pk}


def decoy_calls(r, pat):
    if pat is None:
        return 0
    n = 0
    for l in (ROOT / r["log"]).read_text(errors="ignore").splitlines():
        try:
            e = json.loads(l)
        except ValueError:
            continue
        if e.get("type") != "assistant":
            continue
        for c in e["message"].get("content", []):
            if c.get("type") == "tool_use" and pat.search(json.dumps(c["input"], ensure_ascii=False)):
                n += 1
    return n


def first_hidden_turn(r, hidden):
    ts = [t for f, t in (r.get("relevant_first_turns") or {}).items() if f in hidden and t is not None]
    return min(ts) if ts else None


def main():
    cases = sorted(p.name for p in (ROOT / "cases").iterdir() if p.is_dir())
    data = {v: load(v) for v in VARIANTS}
    by = {v: defaultdict(list) for v in VARIANTS}
    for v, rows in data.items():
        for r in rows:
            by[v][(r["case"][:2], r["cond"])].append(r)
    ks = sorted({c[:2] for c in cases})
    orig_b = {k: st.median(r["total_tokens"] for r in by["original"][(k, "B")]) for k in ks}

    print("# 크기·구조 분리 실험 집계 (scripts/analyze_variants.py 생성)\n")
    print("| 변형 | 행 | 무효 | AC÷B 기하평균 [95%] | 버그 | 패턴 | 변경 | 질문 | B÷원본B | AC÷원본B |")
    print("|---" * 10 + "|")
    for v in VARIANTS:
        rows = data[v]
        g, lo, hi = ratio_summary(by[v], ks, "AC")
        typ = [f"{ratio_summary(by[v], t, 'AC', boot=1)[0]:.2f}" for t in TYPES.values()]
        bb = gmean([st.median(r["total_tokens"] for r in by[v][(k, "B")]) / orig_b[k] for k in ks])
        ab = gmean([st.median(r["total_tokens"] for r in by[v][(k, "AC")]) / orig_b[k] for k in ks])
        print(f"| {v} | {len(rows)} | {sum(not r['valid'] for r in rows)} | {g:.2f} [{lo:.2f}–{hi:.2f}] | "
              + " | ".join(typ) + f" | {bb:.2f} | {ab:.2f} |")
    print()

    print("### 변형 × 조건: 성공·턴·탐색\n")
    print("| 변형 | 조건 | 성공(수정 30) | 회귀 실패 | 질문 평균/8 | 턴 중앙값 | 숨은 파일 첫 도달 턴 중앙값 | 첫 수정 턴 중앙값 | 지도 열람 중앙값 | 미끼 접촉 호출(중앙값 · 접촉 실행 수) | 비용 $ |")
    print("|---" * 11 + "|")
    for v in VARIANTS:
        hid = {c[:2]: hidden_relevant(v, c) for c in cases}
        dp = decoy_pkgs(v)
        pat = re.compile(r"domain/(" + "|".join(map(re.escape, sorted(dp, key=len, reverse=True))) + r")/") if dp else None
        for cond in ("B", "AC"):
            rs = [r for r in data[v] if r["cond"] == cond]
            fix = [r for r in rs if r["type"] != "question"]
            gs = [r["grade"] for r in rs if r["grade"] is not None]
            ft = [x for x in (first_hidden_turn(r, hid[r["case"][:2]]) for r in rs) if x is not None]
            fe = [r["first_edit_turn"] for r in fix if r.get("first_edit_turn") is not None]
            dc = [decoy_calls(r, pat) for r in rs] if pat else None
            dcell = f"{st.median(dc)} · {sum(x > 0 for x in dc)}/{len(dc)}" if dc else "-"
            print(f"| {v} | {cond} | {sum(bool(r['success']) for r in fix)}/{len(fix)}"
                  f" | {sum(r.get('regression_pass') is False for r in fix)}"
                  f" | {fmt(st.mean(gs)) if gs else '?'} ({len(gs)})"
                  f" | {st.median(r['num_turns'] for r in rs)}"
                  f" | {fmt(st.median(ft), 1) if ft else '-'} ({len(ft)}/{len(rs)})"
                  f" | {fmt(st.median(fe), 1) if fe else '-'}"
                  f" | {st.median((r['nav_reads'] or 0) for r in rs)}"
                  f" | {dcell}"
                  f" | {sum(r['total_cost_usd'] or 0 for r in rs):.2f} |")
    print()

    print("### 과제별 B 중앙값(k 토큰)과 AC÷B — 성공 수/5\n")
    print("| 과제 | " + " | ".join(f"{v} B | {v} AC÷B" for v in VARIANTS) + " |")
    print("|---" * (2 * len(VARIANTS) + 1) + "|")
    for k in ks:
        cells = []
        for v in VARIANTS:
            b, a = by[v][(k, "B")], by[v][(k, "AC")]
            mb = st.median(r["total_tokens"] for r in b)
            ma = st.median(r["total_tokens"] for r in a)
            sb = "" if b[0]["type"] == "question" else f" · {sum(bool(r['success']) for r in b)}/{len(b)}"
            sa = "" if a[0]["type"] == "question" else f" · {sum(bool(r['success']) for r in a)}/{len(a)}"
            cells += [f"{mb / 1000:.0f}k{sb}", f"{ma / mb:.2f}{sa}"]
        print(f"| {k} | " + " | ".join(cells) + " |")
    print()


if __name__ == "__main__":
    main()
