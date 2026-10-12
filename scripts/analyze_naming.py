#!/usr/bin/env python3
"""이름 후속 실험(naming-followup) 집계 → 마크다운 표(stdout).

A. 대응표 vs 지도(obfuscated): B·AC(results-var-obfuscated) + GD·GE·GA(results-gloss-obfuscated)
B. 틀린 이름(misnamed) B·AC vs original·obfuscated
과제 05 기전: 실행이 알림 종류 enum 파일(원래 NotificationType, 변형별 새 이름)을 도구 입력에 넣었는가.
사용: python3 scripts/analyze_naming.py > results-naming-analysis.md
"""
import json
import statistics as st
from collections import defaultdict

from analyze import TYPES, fmt, gmean, ratio_summary
from analyze_variants import ROOT, first_hidden_turn, hidden_relevant, load, load_dir


def enum_name(variant):
    if variant == "original":
        return "NotificationType"
    return json.loads((ROOT / "variants" / variant / "MAPPING.json").read_text())["types"]["NotificationType"]


def opened(r, name):
    for l in (ROOT / r["log"]).read_text(errors="ignore").splitlines():
        try:
            e = json.loads(l)
        except ValueError:
            continue
        if e.get("type") == "assistant":
            for c in e["message"].get("content", []):
                if c.get("type") == "tool_use" and f"{name}.java" in json.dumps(c["input"], ensure_ascii=False):
                    return True
    return False


def group(rows):
    by = defaultdict(list)
    for r in rows:
        by[(r["case"][:2], r["cond"])].append(r)
    return by


def row_line(label, v, rs, hid, ename, ratio, extra=""):
    fix = [r for r in rs if r["type"] != "question"]
    gs = [r["grade"] for r in rs if r["grade"] is not None]
    ft = [x for x in (first_hidden_turn(r, hid[r["case"][:2]]) for r in rs) if x is not None]
    c5 = [r for r in rs if r["case"][:2] == "05"]
    return (f"| {label} | {ratio} | {sum(bool(r['success']) for r in fix)}/{len(fix)}"
            f" | {sum(bool(r['success']) for r in c5)}/{len(c5)} · {sum(opened(r, ename) for r in c5)}/{len(c5)}"
            f" | {fmt(st.mean(gs)) if gs else '?'} | {st.median(r['num_turns'] for r in rs)}"
            f" | {fmt(st.median(ft), 1) if ft else '-'} | {sum(r['total_cost_usd'] or 0 for r in rs):.2f}{extra} |")


def main():
    cases = sorted(p.name for p in (ROOT / "cases").iterdir() if p.is_dir())
    ks = sorted({c[:2] for c in cases})
    orig = group(load("original"))
    orig_b = {k: st.median(r["total_tokens"] for r in orig[(k, "B")]) for k in ks}

    print("# 이름 후속 실험 집계 (scripts/analyze_naming.py 생성)\n")
    print("열: ÷B = 같은 변형 B 대비 기하평균 [95%] · 성공(수정 과제) · 과제 05 성공 · 알림 enum 열람 · 질문 평균/8 · 턴 중앙값 · 숨은 파일 첫 도달 턴 · 비용 $\n")

    print("## A. 대응표 vs 지도 (obfuscated)\n")
    obf = load("obfuscated") + load_dir(ROOT / "results-gloss-obfuscated")
    by = group(obf)
    hid = {c[:2]: hidden_relevant("obfuscated", c) for c in cases}
    en = enum_name("obfuscated")
    size = {"B": 0, "AC": (ROOT / "variants/obfuscated/graphs/auto/NAV.md").stat().st_size}
    for c, k in (("GD", "gloss-dom"), ("GE", "gloss-ent"), ("GA", "gloss-all")):
        size[c] = (ROOT / "glossaries/obfuscated" / f"{k}.md").stat().st_size
    print("| 조건 (CLAUDE.md 크기) | ÷B | 성공 | 05 성공 · enum 열람 | 질문 | 턴 | 숨은 파일 턴 | 비용 | 버그 | 패턴 | 변경 | 질문 ÷B |")
    print("|---" * 12 + "|")
    for c in ("B", "AC", "GD", "GE", "GA"):
        rs = [r for r in obf if r["cond"] == c]
        if c == "B":
            ratio, typ = "1.00", ["1.00"] * 4
        else:
            g, lo, hi = ratio_summary(by, ks, c)
            ratio = f"{g:.2f} [{lo:.2f}–{hi:.2f}]"
            typ = [f"{ratio_summary(by, t, c, boot=1)[0]:.2f}" for t in TYPES.values()]
        print(row_line(f"{c} ({size[c]:,}B)", "obfuscated", rs, hid, en, ratio, " | " + " | ".join(typ)))
    print()

    print("## B. 틀린 이름 (misnamed) vs 원본·난독화\n")
    print("| 변형·조건 | ÷원본B | 성공 | 05 성공 · enum 열람 | 질문 | 턴 | 숨은 파일 턴 | 비용 | AC÷B (같은 변형) |")
    print("|---" * 9 + "|")
    for v in ("original", "obfuscated", "misnamed"):
        rows = load(v) if v != "misnamed" else load_dir(ROOT / "results-var-misnamed")
        b = group(rows)
        h = {c[:2]: hidden_relevant(v, c) for c in cases}
        e = enum_name(v)
        g, lo, hi = ratio_summary(b, ks, "AC")
        for c in ("B", "AC"):
            rs = [r for r in rows if r["cond"] == c]
            ob = gmean([st.median(r["total_tokens"] for r in b[(k, c)]) / orig_b[k] for k in ks])
            print(row_line(f"{v} {c}", v, rs, h, e, f"{ob:.2f}",
                           f" | {g:.2f} [{lo:.2f}–{hi:.2f}]" if c == "AC" else " | -"))
    print()

    print("## 과제별 성공 수/5 (수정 과제)\n")
    sets = [("obf B", "obfuscated", "B"), ("obf AC", "obfuscated", "AC"), ("obf GD", "obfuscated", "GD"),
            ("obf GE", "obfuscated", "GE"), ("obf GA", "obfuscated", "GA"),
            ("orig B", "original", "B"), ("orig AC", "original", "AC"), ("mis B", "misnamed", "B"), ("mis AC", "misnamed", "AC")]
    data = {"obfuscated": by, "original": orig, "misnamed": group(load_dir(ROOT / "results-var-misnamed"))}
    print("| 과제 | " + " | ".join(s[0] for s in sets) + " |")
    print("|---" * (len(sets) + 1) + "|")
    for k in ks[:6]:
        print(f"| {k} | " + " | ".join(f"{sum(bool(r['success']) for r in data[v][(k, c)])}/{len(data[v][(k, c)])}"
                                      for _, v, c in sets) + " |")
    print()


if __name__ == "__main__":
    main()
