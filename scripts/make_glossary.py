#!/usr/bin/env python3
"""이름 대응표 생성(naming-followup): 난독화 변형의 MAPPING.json → "무의미 이름 = 업무 이름" 표 3종.

  gloss-dom  도메인 최상위 패키지만(m15 = notification)
  gloss-ent  도메인 + 엔티티·enum 의 업무 어간(Xkh73 = Notification)
  gloss-all  도메인 + 업무 어간 전부
결과: glossaries/<변형>/<종류>.md (러너 조건 GD·GE·GA 가 CLAUDE.md 로 둔다)
사용: python3 scripts/make_glossary.py obfuscated
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
HEAD = "# 이름 대응표\n\n이 코드베이스의 타입·패키지 이름은 업무 용어 대신 코드 이름을 쓴다. 왼쪽이 코드 이름, 오른쪽이 업무 용어다.\n"
STEM = re.compile(r"^(X[a-z]{2}\d{2})(.*)$")


def main(variant: str) -> None:
    v = ROOT / "variants" / variant
    m = json.loads((v / "MAPPING.json").read_text())
    tops = sorted((new, old) for old, new in m["pkgs"].items() if "." not in old)
    # 업무 어간: 새 이름 = 무의미 어간 + 구조 접미어, 옛 이름 = 업무 어간 + 같은 접미어
    stems = {}
    for old, new in m["types"].items():
        g = STEM.match(new)
        if g and old.endswith(g.group(2)):
            stems.setdefault(g.group(1), old[:len(old) - len(g.group(2))] if g.group(2) else old)
    # 엔티티·enum 어간: 사본에서 @Entity 클래스나 enum 으로 선언된 타입의 어간
    src = v / "target" / "fream-back" / "src" / "main" / "java"
    ent = set()
    for f in src.rglob("*.java"):
        t = f.read_text(errors="ignore")
        if re.search(r"^\s*@Entity\b", t, re.M) or re.search(rf"\benum\s+{re.escape(f.stem)}\b", t):
            g = STEM.match(f.stem)
            if g:
                ent.add(g.group(1))
    out = ROOT / "glossaries" / variant
    out.mkdir(parents=True, exist_ok=True)
    dom = "\n## 도메인 패키지 (com.fream.back.domain.*)\n\n" + "".join(f"- {n} = {o}\n" for n, o in tops)
    def stem_part(keys):
        return "\n## 타입 이름 어간 (어간 뒤의 Service·Repository·Type 등은 그대로 역할을 뜻한다)\n\n" + \
            "".join(f"- {k} = {stems[k]}\n" for k in sorted(keys, key=lambda k: stems[k].lower()))
    files = {
        "gloss-dom": HEAD + dom,
        "gloss-ent": HEAD + dom + stem_part(ent & set(stems)),
        "gloss-all": HEAD + dom + stem_part(stems),
    }
    for k, text in files.items():
        (out / f"{k}.md").write_text(text)
        print(f"{k}: {len(text.encode())}B, 도메인 {len(tops)}, 어간 {text.count(chr(10) + '- X')}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit(__doc__)
    main(sys.argv[1])
