#!/usr/bin/env python3
"""fream-back 변형 생성기 (size-structure-variants).

변형:
  inflated            도메인 패키지를 접두어 5종(Admin·Settlement·Legacy·Partner·Internal)으로 복제한 미끼를 더한다(약 5배).
                      미끼는 자기들끼리만 참조하고 진짜 흐름에서는 호출되지 않는다.
  obfuscated          타입·도메인 패키지 이름을 무의미 이름으로 바꾸고(구조 접미어 Service·Repository 등은 유지),
                      Java 주석을 지운다(줄 수 보존 — 체크리스트의 파일:줄 근거가 그대로 맞는다).
  inflated-obfuscated 부풀린 뒤 난독화.

결과: variants/<name>/{target/fream-back, cases, oracle, MAPPING.json}
  - 정답 패치는 "원본에 fix.patch 적용 → 같은 변환" 결과와 변환된 원본의 diff 로 다시 만든다.
  - 매핑은 원본(부풀린 뒤) src/main 타입에서 한 번 만들고 모든 트리에 똑같이 쓴다(시드 고정, 결정적).
사용: python3 scripts/make_variant.py inflated|obfuscated|inflated-obfuscated
"""
import json
import random
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "target" / "fream-back"
CASES = ROOT / "cases"
ORACLE = ROOT / "oracle"
JAVA_ROOT = Path("src/main/java/com/fream/back")
DOMAIN = "com.fream.back.domain"
PREFIXES = ["Admin", "Settlement", "Legacy", "Partner", "Internal"]
SEED = 20261011
# 구조(역할) 단어: 난독화해도 남긴다. 업무 단어만 지운다.
ARCH_WORDS = {
    "Command", "Query", "Service", "Repository", "Impl", "Custom", "Controller", "Entity", "Dto", "DTO", "Request",
    "Response", "Exception", "Config", "Configuration", "Event", "Listener", "Consumer", "Producer", "Handler",
    "Status", "Type", "Util", "Utils", "Mapper", "Converter", "Validator", "Scheduler", "Job", "Processor", "Reader",
    "Writer", "Aspect", "Filter", "Interceptor", "Base", "Abstract", "Info", "Detail", "Search", "Create", "Update",
    "Delete", "List", "Page", "Batch", "Kafka", "Redis", "Elasticsearch", "Web", "Socket", "Api", "Client", "Error",
    "Code", "Strategy", "Level", "Action", "Result", "Factory", "Provider", "Properties", "Support", "Helper",
    "Annotation", "Advice", "Interface", "Document", "Index", "Cache", "Key", "Message", "Template", "Security",
}
ARCH_PKGS = {"service", "controller", "dto", "command", "query", "repository", "entity", "exception", "kafka", "aop",
             "security", "elasticsearch", "config", "annotation", "scheduler", "filter", "websocket", "redis", "api",
             "validate", "utils", "util", "log", "impl", "index", "aspect", "enumType", "file", "oauth2", "admin"}
TEXT_EXT = {".java", ".yml", ".yaml", ".xml", ".properties", ".md", ".txt", ".sh", ".patch"}


# ---------- 공통 텍스트 변환 ----------

def lower_first(s: str) -> str:
    return s[:1].lower() + s[1:]


def strip_comments(src: str) -> str:
    """Java 주석 제거(문자열·문자·텍스트 블록 안은 그대로). 블록 주석은 줄바꿈만 남겨 줄 번호를 보존한다."""
    out, i, n = [], 0, len(src)
    while i < n:
        c = src[i]
        if src.startswith('"""', i):
            j = src.find('"""', i + 3)
            while j != -1 and src[j - 1] == "\\":
                j = src.find('"""', j + 1)
            j = n if j == -1 else j + 3
            out.append(src[i:j]); i = j
        elif c == '"' or c == "'":
            j = i + 1
            while j < n and src[j] != c:
                j += 2 if src[j] == "\\" else 1
            out.append(src[i:j + 1]); i = j + 1
        elif src.startswith("//", i):
            j = src.find("\n", i)
            j = n if j == -1 else j
            i = j
        elif src.startswith("/*", i):
            j = src.find("*/", i + 2)
            j = n if j == -1 else j + 2
            out.append("\n" * src.count("\n", i, j)); i = j
        else:
            out.append(c); i += 1
    # 주석만 있던 줄의 꼬리 공백 정리(줄 수는 그대로)
    return "\n".join(l.rstrip() for l in "".join(out).split("\n"))


class Mapper:
    """타입 이름·도메인 패키지 경로 매핑을 텍스트에 적용한다."""

    def __init__(self, types: dict, pkgs: dict, keep_types: set = frozenset()):
        self.keep = keep_types    # FQ 참조의 끝 타입이 이 집합이면 패키지를 바꾸지 않는다(부풀림의 공유 타입)
        self.types = types        # 옛 단순 이름 → 새 단순 이름
        self.pkgs = pkgs          # 옛 도메인 하위 패키지 경로("order", "user.service.point") → 새 경로
        self.tok = re.compile(r"\b(Q?)([A-Z]\w*)\b")
        self.fq = re.compile(r"com([./])fream\1back\1domain((?:\1\w+)+)")
        self.short = re.compile(r"(?<![\w/.])domain/((?:\w+/)*\w+(?:\.java)?)")  # 체크리스트의 축약 경로

    def map_pkg_path(self, segs: list) -> list:
        """도메인 아래 세그먼트 목록(패키지들 + 끝의 타입·파일 이름 가능)을 매핑한다."""
        pk = []
        for s in segs:
            if s[:1].isupper() or s.endswith(".java"):
                break
            pk.append(s)
        rest = segs[len(pk):]
        if rest and (rest[0] in self.keep or rest[0].removesuffix(".java") in self.keep
                     or (rest[0][:1] == "Q" and rest[0][1:] in self.keep)):
            return segs
        best = None
        for k in range(len(pk), 0, -1):
            key = ".".join(pk[:k])
            if key in self.pkgs:
                best = self.pkgs[key].split(".") + pk[k:]
                break
        newpk = best if best is not None else pk
        newrest = []
        for s in rest:
            if s.endswith(".java"):
                stem = s[:-5]
                newrest.append(self.types.get(stem, stem) + ".java")
            elif s[:1] == "Q" and s[1:] in self.types:
                newrest.append("Q" + self.types[s[1:]])
            else:
                newrest.append(self.types.get(s, s))
        return newpk + newrest

    def _fq(self, m):
        sep = m.group(1)
        segs = m.group(2)[1:].split(sep)
        return f"com{sep}fream{sep}back{sep}domain{sep}" + sep.join(self.map_pkg_path(segs))

    def map_text(self, s: str) -> str:
        s = self.fq.sub(self._fq, s)
        s = self.short.sub(lambda m: "domain/" + "/".join(self.map_pkg_path(m.group(1).split("/"))), s)
        lib = set(re.findall(r"^import\s+(?:static\s+)?(?!com\.fream)[\w.]+\.(\w+);", s, re.M))
        out, last = [], 0
        for m in self.tok.finditer(s):
            q, name = m.group(1), m.group(2)
            suffix = ""
            if name not in self.types and name.endswith("Builder") and name[:-7] in self.types:
                name, suffix = name[:-7], "Builder"  # Lombok @Builder 가 만드는 <타입>Builder
            if name not in self.types or (not q and name in lib):
                continue
            # 앞쪽 한정자: 라이브러리 패키지(소문자 세그먼트로 시작하고 com.fream 이 아닌 것)면 건드리지 않는다
            j = m.start()
            k = j
            while k > 0 and (s[k - 1].isalnum() or s[k - 1] in "._$"):
                k -= 1
            qual = s[k:j]
            if qual and qual.endswith(".") and qual[:1].islower() and not qual.startswith("com.fream") \
                    and not qual.startswith("com/fream"):
                head = qual.split(".")[0]
                if head in {"java", "javax", "jakarta", "org", "com", "lombok", "io", "net", "reactor", "software"}:
                    continue
            out.append(s[last:m.start()])
            out.append(q + self.types[name] + suffix)
            last = m.end()
        out.append(s[last:])
        s = "".join(out)
        # QueryDSL 정적 필드: QNew.oldLower → QNew.newLower
        for old, new in self.types.items():
            if f"Q{new}." in s:
                s = re.sub(rf"\bQ{re.escape(new)}\.{re.escape(lower_first(old))}\b", f"Q{new}.{lower_first(new)}", s)
        return s

    def map_relpath(self, rel: str) -> str:
        p = rel.replace("\\", "/")
        m = re.search(r"com/fream/back/domain/(.+)$", p)
        if m:
            p = p[:m.start(1)] + "/".join(self.map_pkg_path(m.group(1).split("/")))
        elif p.endswith(".java"):
            d, f = p.rsplit("/", 1) if "/" in p else ("", p)
            f = self.types.get(f[:-5], f[:-5]) + ".java"
            p = f"{d}/{f}" if d else f
        return p


def rewrite_static_q_imports(s: str, renamed: set) -> str:
    """`import static ...QFoo.foo;` 는 이름이 바뀌면 필드 이름도 바뀌어 깨진다.
    import 줄은 빈 줄로 두고(줄 수 보존) 클래스 여는 중괄호 뒤에 같은 이름의 정적 필드를 둔다."""
    decls = []
    def repl(m):
        t, f = m.group(1), m.group(2)
        if t in renamed and f == lower_first(t):
            decls.append(f"private static final Q{t} {f} = Q{t}.{f};")
            return ""
        return m.group(0)
    s2 = re.sub(r"^import\s+static\s+com\.fream\.back[\w.]*\.Q(\w+)\.(\w+);[ \t]*$", repl, s, flags=re.M)
    if not decls:
        return s
    # import 줄에서 쓰던 QFoo 를 일반 import 로 되살릴 필요는 없다: 같은 패키지가 아니면 FQ 로 선언한다
    pkgs = dict(re.findall(r"^import\s+static\s+(com\.fream\.back[\w.]*)\.Q(\w+)\.\w+;", s, re.M))
    full = []
    for d in decls:
        t = re.match(r"private static final Q(\w+)", d).group(1)
        pkg = {v: k for k, v in pkgs.items()}.get(t)
        full.append(d.replace(f"final Q{t} ", f"final {pkg}.Q{t} ").replace(f"= Q{t}.", f"= {pkg}.Q{t}.") if pkg else d)
    m = re.search(r"\b(class|interface)\s+\w+[^{]*\{", s2)
    return s2[:m.end()] + " " + " ".join(full) + s2[m.end():]


# ---------- 부풀림 ----------

DATA_DIRS = {"entity", "dto", "exception", "event", "enumType"}
# 행위 계층에 있지만 진짜 코드와 값을 주고받아 공유해야 하는 타입(컴파일 오류 반복으로 찾은 고정 목록)
SHARED_EXTRA = set(Path(__file__).with_name("variant-shared-extra.txt").read_text().split()) \
    if Path(__file__).with_name("variant-shared-extra.txt").exists() else set()


def scan_domain(tree: Path) -> tuple:
    """도메인 파일별 (선언 타입들, 공유 여부). 공유 = 데이터 타입(entity·dto·exception·event 디렉터리, enum)
    또는 global 코드가 참조하는 타입 — 미끼가 이것까지 복제하면 global·진짜 타입과 섞여 타입이 맞지 않는다.
    미끼는 행위 계층(서비스·컨트롤러·리포지토리 등)만 복제하고 데이터 타입은 진짜를 함께 쓴다."""
    base = tree / JAVA_ROOT
    gtext = " ".join(f.read_text() for f in sorted((base / "global").rglob("*.java")))
    files = {}
    for f in sorted((base / "domain").rglob("*.java")):
        t = f.read_text()
        names = {a or b for a, b in re.findall(r"\b(?:class|interface|enum|record)\s+([A-Z]\w*)|@interface\s+([A-Z]\w*)", t)}
        rel = f.relative_to(base / "domain").parts
        if not re.search(r"^package\s+[\w.]+;", t, re.M):  # 통째로 주석 처리된 파일: 복제하지 않는다
            files[f] = (set(), True)
            continue
        data = f.stem in SHARED_EXTRA or bool(DATA_DIRS & set(rel[1:-1])) or re.search(rf"\benum\s+{re.escape(f.stem)}\b", t) is not None \
            or re.search(rf"\b{re.escape(f.stem)}\b", gtext) is not None
        files[f] = (names, data)
    return files


def inflate(tree: Path) -> None:
    files = scan_domain(tree)
    gnames = set()
    for f in (tree / JAVA_ROOT / "global").rglob("*.java"):
        gnames |= {a or b for a, b in re.findall(r"\b(?:class|interface|enum|record)\s+([A-Z]\w*)|@interface\s+([A-Z]\w*)", f.read_text())}
    shared = set().union(*(n for n, d in files.values() if d))
    behave = set().union(*(n for n, d in files.values() if not d)) - shared - gnames
    shared_pkg = {}  # 공유 타입 단순 이름 → 원래 패키지(복제본에서 import 를 덧붙일 때)
    for f, (n, d) in files.items():
        if d and n:
            pkg = re.search(r"^package\s+([\w.]+);", f.read_text(), re.M).group(1)
            for x in n:
                shared_pkg.setdefault(x, pkg)
    base = tree / JAVA_ROOT / "domain"
    doms = sorted(p.name for p in base.iterdir() if p.is_dir())
    behave_pkgs = set()
    for f, (n, d) in files.items():
        if not d:
            behave_pkgs.add(re.search(r"^package\s+([\w.]+);", f.read_text(), re.M).group(1))
    for pre in PREFIXES:
        types = {t: pre + t for t in behave}
        pkgs = {d: (pre + d).lower() for d in doms}
        mp = Mapper(types, pkgs, keep_types=shared)
        for f, (names, data) in sorted(files.items()):
            if data:
                continue
            src = f.read_text()
            own_pkg = re.search(r"^package\s+([\w.]+);", src, re.M).group(1)
            s = rewrite_static_q_imports(src, set(types))
            wild = re.findall(r"^import\s+(com\.fream\.back\.domain\.[\w.]+)\.\*;", src, re.M)
            s = mp.map_text(s)
            # 와일드카드 import: 공유 타입만 있는 패키지는 원래 패키지로 되돌리고, 섞인 패키지는 원래 것도 함께 연다
            extra = []
            for w in wild:
                if w.rsplit(".", 1)[1][:1].isupper():
                    continue  # 클래스의 중첩 타입 와일드카드(Foo.*) — 일반 매핑 그대로
                mapped = mp.map_text(w)
                if w not in behave_pkgs:
                    s = s.replace(f"import {mapped}.*;", f"import {w}.*;")
                elif mapped != w:
                    extra.append(f"import {w}.*;")
            # 원래 같은 패키지라 import 없이 쓰던 공유 타입은 복제본(다른 패키지)에서 import 가 필요하다
            imps = sorted(f"import {shared_pkg[x]}.{x};" for x in shared
                          if shared_pkg.get(x) == own_pkg and re.search(rf"\b{re.escape(x)}\b", s)) + extra
            if imps:
                s = re.sub(r"^(package\s+[\w.]+;)", lambda m: m.group(1) + " " + " ".join(imps), s, count=1, flags=re.M)
            s = re.sub(r'(@RequestMapping\(\s*(?:value\s*=\s*|path\s*=\s*)?\{?\s*")/', rf"\g<1>/{pre.lower()}/", s)
            dst = tree / mp.map_relpath(f.relative_to(tree).as_posix())
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_text(s)


# ---------- 난독화 ----------

def camel_words(name: str) -> list:
    return re.findall(r"[A-Z]+(?=[A-Z][a-z]|\d|\b)|[A-Z]?[a-z]+|[A-Z]+|\d+", name)


def build_obf_mapping(tree: Path) -> tuple:
    base = tree / JAVA_ROOT
    names = set()
    for f in base.rglob("*.java"):
        for a, b in re.findall(r"\b(?:class|interface|enum|record)\s+([A-Z]\w*)|@interface\s+([A-Z]\w*)", f.read_text()):
            names.add(a or b)
    names.discard("FreamBackApplication")
    rnd = random.Random(SEED)
    letters = "bcdfghjkmnpqrstvwxz"
    # 업무 어간(끝의 구조 단어를 뗀 앞부분)마다 무의미 어간 하나: OrderRepository·OrderRepositoryImpl·Order 가 같은 어간을 공유해야
    # Spring Data 의 <저장소>Impl 이름 규칙이 유지된다
    used, types, stems = set(names), {}, {}
    for n in sorted(names):
        w = camel_words(n)
        k = len(w)
        while k > 0 and w[k - 1] in ARCH_WORDS:
            k -= 1
        if k == 0:
            continue  # 전부 구조 단어(RedisConfig 등): 업무 정보 없음
        biz = "".join(w[:k])
        if biz not in stems:
            while True:
                stem = "X" + rnd.choice(letters) + rnd.choice(letters) + str(rnd.randint(10, 99))
                if stem not in stems.values():
                    break
            stems[biz] = stem
        new = stems[biz] + "".join(w[k:])
        if new in used:
            raise SystemExit(f"이름 충돌: {n} -> {new}")
        used.add(new)
        types[n] = new
    # 도메인 아래 패키지: 구조 이름이 아닌 세그먼트를 무의미 이름으로
    pkgs = {}
    dom = base / "domain"
    dirs = sorted({d.relative_to(dom).as_posix().replace("/", ".") for d in dom.rglob("*") if d.is_dir()})
    tops = sorted({d.split(".")[0] for d in dirs})
    order = tops[:]
    rnd.shuffle(order)
    topmap = {t: f"m{i + 1:02d}" for i, t in enumerate(order)}
    counter = [0]
    seg_new = {}
    for d in dirs:
        parts = d.split(".")
        new = [topmap[parts[0]]]
        for p in parts[1:]:
            if p in ARCH_PKGS:
                new.append(p)
            else:
                if p not in seg_new:
                    counter[0] += 1
                    seg_new[p] = f"p{counter[0]:02d}"
                new.append(seg_new[p])
        pkgs[d] = ".".join(new)
    return types, pkgs


def spring_table_name(cls: str) -> str:
    """Spring Boot 기본 물리 이름 규칙(CamelCaseToUnderscoresNamingStrategy)."""
    out = []
    for i, c in enumerate(cls):
        if 0 < i < len(cls) - 1 and cls[i - 1].islower() and c.isupper() and cls[i + 1].islower():
            out.append("_")
        out.append(c.lower())
    return "".join(out)


def pin_table_name(s: str, renamed: set, entities: set = frozenset()) -> str:
    """이름이 바뀌는 엔티티의 암묵 테이블 이름을 고정한다(같은 줄에 붙여 줄 수 보존). DB 이름은 코드 이름과 따로 남는다."""
    m = re.search(r"\bclass\s+(\w+)(?:\s+extends\s+(\w+))?", s)
    if m and m.group(2) in entities:  # 상속 계층의 하위 엔티티는 루트가 테이블을 정한다
        return s
    if not m or m.group(1) not in renamed or "@Table" in s or not re.search(r"^\s*@Entity\b", s, re.M):
        return s
    return re.sub(r"^(\s*@Entity\b(?:\([^)]*\))?)", rf'\1 @jakarta.persistence.Table(name = "{spring_table_name(m.group(1))}")',
                  s, count=1, flags=re.M)


def transform_tree(tree: Path, mp: "Mapper | None", strip: bool) -> Path:
    """tree 를 매핑·주석 제거한 새 트리로 바꾼다(제자리, 파일 이동 포함)."""
    if mp is None:
        return tree
    renamed = set(mp.types)
    files = sorted(p for p in tree.rglob("*") if p.is_file())
    entities = set()
    for f in files:
        if f.suffix == ".java":
            t = f.read_text(errors="surrogateescape")
            if re.search(r"^\s*@Entity\b", t, re.M):
                m = re.search(r"\bclass\s+(\w+)", t)
                if m:
                    entities.add(m.group(1))
    tmp = tree.with_name(tree.name + ".obf")
    for f in files:
        rel = f.relative_to(tree).as_posix()
        dst = tmp / mp.map_relpath(rel)
        dst.parent.mkdir(parents=True, exist_ok=True)
        if f.suffix in TEXT_EXT or f.name in ("gradlew",):
            s = f.read_text(errors="surrogateescape")
            if f.suffix == ".java":
                s = rewrite_static_q_imports(s, renamed)
                s = pin_table_name(s, renamed, entities)
                if strip:
                    s = strip_comments(s)
            s = mp.map_text(s)
            dst.write_text(s, errors="surrogateescape")
            shutil.copymode(f, dst)
        else:
            shutil.copy2(f, dst)
    shutil.rmtree(tree)
    tmp.rename(tree)
    return tree


# ---------- 조립 ----------

def git_apply(tree: Path, patch: Path) -> None:
    subprocess.run(["git", "apply", "--unsafe-paths", "-p1", str(patch)], cwd=tree, check=True)


def diff_trees(a: Path, b: Path) -> str:
    p = subprocess.run(["diff", "-ruN", "a", "b"], cwd=a.parent, capture_output=True, text=True)
    if p.returncode not in (0, 1):
        raise RuntimeError(p.stderr)
    return re.sub(r"^((?:---|\+\+\+) \S+)\t.*$", r"\1", p.stdout, flags=re.M)


def build(name: str) -> None:
    do_inflate = name.startswith("inflated")
    do_obf = name.endswith("obfuscated")
    out = ROOT / "variants" / name
    if out.exists():
        shutil.rmtree(out)
    (out / "target").mkdir(parents=True)
    work = Path(tempfile.mkdtemp(prefix=f"variant-{name}-"))
    ign = shutil.ignore_patterns("build", ".gradle", "logs", "*.log")

    def fresh(tag: str, patch: "Path | None" = None) -> Path:
        t = work / tag
        shutil.copytree(TARGET, t, ignore=ign)
        if do_inflate:
            inflate(t)  # 패치보다 먼저: 미끼는 고치기 전 코드의 복제여야 한다
        if patch is not None:
            git_apply(t, patch)
        return t

    base = fresh("base")
    mp, mapping = None, {"types": {}, "pkgs": {}}
    if do_obf:
        types, pkgs = build_obf_mapping(base)
        mp = Mapper(types, pkgs)
        mapping = {"types": types, "pkgs": pkgs}
    transform_tree(base, mp, strip=do_obf)
    shutil.copytree(base, out / "target" / "fream-back")

    for case in sorted(p.name for p in CASES.iterdir() if p.is_dir()):
        # 공개 과제 파일
        cdst = out / "cases" / case
        shutil.copytree(CASES / case, cdst)
        transform_tree(cdst, mp, strip=do_obf)
        # oracle: hidden 테스트·체크리스트·relevant·exclude 는 매핑만(주석은 hidden 에서도 지운다 — 판정 전용이라 무관)
        odst = out / "oracle" / case
        shutil.copytree(ORACLE / case, odst, ignore=shutil.ignore_patterns("fix.patch"))
        transform_tree(odst, mp, strip=False)
        rel = odst / "relevant.txt"
        if rel.exists() and mp:
            rel.write_text("".join(mp.map_relpath(l) + "\n" for l in rel.read_text().splitlines() if l.strip()))
        fix = ORACLE / case / "fix.patch"
        if fix.exists():
            fixed = fresh(f"fix-{case}", fix)
            transform_tree(fixed, mp, strip=do_obf)
            a, b = work / "a", work / "b"
            for p in (a, b):
                if p.exists():
                    shutil.rmtree(p)
            shutil.copytree(base, a)
            shutil.copytree(fixed, b)
            (odst / "fix.patch").write_text(diff_trees(a, b))
            shutil.rmtree(fixed)
    (out / "MAPPING.json").write_text(json.dumps(mapping, ensure_ascii=False, indent=1, sort_keys=True))
    shutil.rmtree(work)
    print(f"{name}: {out}")


if __name__ == "__main__":
    if len(sys.argv) != 2 or sys.argv[1] not in ("inflated", "obfuscated", "inflated-obfuscated"):
        raise SystemExit(__doc__)
    build(sys.argv[1])
