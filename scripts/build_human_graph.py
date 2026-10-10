#!/usr/bin/env python3
"""사람이 쓴 문서 그래프(H 조건): target-docs/fream-back 원문을 graphs/human 으로 옮긴다.

- nav/  = target-docs/fream-back/docs 원문 그대로(구조 유지)
- NAV.md = 머리말 + 문서 목록(디렉터리별) + 루트 README 원문 앞부분(줄 단위로 잘라 전체 8,192바이트 이하)
요약·해석은 넣지 않는다. 같은 입력이면 같은 출력.
"""
import shutil
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "target-docs" / "fream-back"
OUT = ROOT / "graphs" / "human"
LIMIT = 8192

HEADER = (
    "# 프로젝트 문서 인덱스\n\n"
    "이 저장소 개발자가 쓴 문서다. 상세 문서는 `nav/` 아래에 있다"
    " (원문 링크의 `/docs/...` 는 `nav/...` 에 해당한다).\n\n"
)


def doc_list() -> str:
    groups = defaultdict(list)
    for f in sorted((SRC / "docs").rglob("*")):
        if f.is_file():
            rel = f.relative_to(SRC / "docs")
            groups[rel.parent.as_posix()].append(rel.name)
    lines = ["## 문서 목록 (`nav/` 기준)", ""]
    for d in sorted(groups):
        prefix = "./" if d == "." else f"{d}/"
        lines.append(f"- `{prefix}`: " + ", ".join(groups[d]))
    return "\n".join(lines) + "\n\n## 루트 README (원문, 길이 제한으로 앞부분만)\n\n"


def main() -> None:
    if OUT.exists():
        shutil.rmtree(OUT)
    shutil.copytree(SRC / "docs", OUT / "nav")
    head = HEADER + doc_list()
    budget = LIMIT - len(head.encode())
    body = []
    used = 0
    for line in (SRC / "README.md").read_text(encoding="utf-8").splitlines(keepends=True):
        n = len(line.encode())
        if used + n > budget:
            break
        body.append(line)
        used += n
    nav = head + "".join(body)
    (OUT / "NAV.md").write_text(nav, encoding="utf-8")
    print(f"NAV.md {len(nav.encode())} bytes, README {used} bytes, nav files {sum(1 for p in (OUT / 'nav').rglob('*') if p.is_file())}")


if __name__ == "__main__":
    main()
