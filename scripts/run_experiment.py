#!/usr/bin/env python3
"""탐색 그래프 실험 러너 (ADR-0001).

compare-issue-archive scripts/run_experiment.py(feat/case-build aea9131)를 복사해 고쳤다.
과제마다 조건(B·AF·AC·HF·HC·LF·LC)으로 `claude -p`를 격리 HOME(하네스 없음)에서 실행하고,
결과(usage·비용·판정·감사·탐색 지표)를 <results>/runs.jsonl 에 한 줄씩 append 한다.

사용:
  python3 scripts/run_experiment.py --model sonnet --results-dir results-sonnet \
      [--cases 01,02] [--conds B,AF] [--reps 1,2,3] [--parallel 5]
이미 runs.jsonl 에 (case, cond, rep) 유효 행이 있으면 건너뛴다(재개 가능). 무효(감사 위반) 행은 재실행 대상.
"""
import argparse
import concurrent.futures as cf
import fcntl
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import threading
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "target" / "fream-back"
CASES = ROOT / "cases"
ORACLE = ROOT / "oracle"
GRAPHS = ROOT / "graphs"
RESULTS = ROOT / "results"
RAW = RESULTS / "raw"
RUNS_FILE = RESULTS / "runs.jsonl"

HOST_RUNS = Path("/tmp/gne-runs")
REAL_HOME = Path.home()
CREDS = REAL_HOME / ".claude" / ".credentials.json"
JDK17 = REAL_HOME / ".sdkman" / "candidates" / "java" / "17.0.13-tem"
# 실험 전용 Gradle 홈: 의존성 캐시 + init.d/offline.gradle(오프라인 강제) + gradle.properties(데몬 끔)
GRADLE_HOME = REAL_HOME / ".cache" / "gne-gradle"

# 조건 = (그래프 종류, 제공 방식). F = 사본 루트 NAV.md + nav/ + "먼저 읽어라", C = 인덱스를 CLAUDE.md 로(프롬프트는 B와 같다)
CONDITIONS = {
    "B": (None, None),
    "AF": ("auto", "F"), "AC": ("auto", "C"),
    "HF": ("human", "F"), "HC": ("human", "C"),
    "LF": ("llm", "F"), "LC": ("llm", "C"),
}
INDEX_LIMIT = 8192

ENV_NOTE = "빌드·테스트는 ./gradlew 로 한다(JDK 17과 의존성은 준비돼 있고 네트워크는 없다).\n"
PROMPTS = {
    "bug": (
        "현재 디렉터리의 프로젝트(Spring Boot 백엔드)에 버그 보고가 들어왔다. TASK.md 를 읽고 문제의 근본 원인을 찾아 고쳐라.\n"
        "완료 기준: 현재 디렉터리에서 `bash test.sh` 가 통과한다. 테스트 파일과 test.sh 는 수정하지 마라.\n" + ENV_NOTE +
        "작업은 현재 디렉터리 안에서만 하라. 끝나면 원인과 수정 내용을 3줄 이내로 요약하라."
    ),
    "change": (
        "현재 디렉터리의 프로젝트(Spring Boot 백엔드)에 변경 요청이 들어왔다. TASK.md 를 읽고 요청을 구현하라.\n"
        "완료 기준: 현재 디렉터리에서 `bash test.sh` 가 통과한다. 테스트 파일과 test.sh 는 수정하지 마라.\n" + ENV_NOTE +
        "작업은 현재 디렉터리 안에서만 하라. 끝나면 구현 내용을 3줄 이내로 요약하라."
    ),
    "question": (
        "현재 디렉터리의 프로젝트(Spring Boot 백엔드)에 대한 질문이 들어왔다. TASK.md 를 읽고 코드를 조사해 답하라.\n"
        "코드는 수정하지 마라. 답은 TASK.md 가 정한 형식으로 현재 디렉터리의 ANSWER.md 에 써라.\n"
        "작업은 현재 디렉터리 안에서만 하라."
    ),
}
PROMPTS["pattern"] = PROMPTS["bug"]  # 반복 패턴은 TASK.md 가 "같은 원인을 모두 찾아 고쳐라"를 말한다
PROMPT_NAV_FIRST = (
    "\n\n작업 규칙: 이 코드베이스의 구조 지도가 NAV.md(인덱스)와 nav/(상세)에 있다."
    " 코드를 탐색하기 전에 **먼저 NAV.md 를 읽고**, 필요한 상세 문서만 열어라."
)

# 감사: 도구 호출 입력(명령·경로·패턴)에 나타나면 무효. 도구 결과(파일 내용)는 보지 않는다 —
# 대상 코드·사람 문서 안에 github 링크·도커 이미지 이름이 원래 들어 있어 결과까지 보면 오탐이 난다.
AUDIT_INPUT = [re.escape(str(REAL_HOME / "project")), r"study-note", r"graph-nav-experiment", r"oracle/",
               r"target-suspected", r"hidden\.sh", r"fix\.patch", r"MAPPING\.json", r"github\.com", r"/tmp/claude-\d+"]
AUDIT_BASH = [r"(^|[\s;&|(])(curl|wget|nc|ssh|scp|gh)\s", r"\bgit\s+(clone|fetch|pull|ls-remote|remote)\b"]
FORBIDDEN_TOOLS = {"WebFetch", "WebSearch"}
DENY_RULES = [f"{tool}(/{REAL_HOME}/project/**)" for tool in ("Read", "Edit", "Write", "Grep", "Glob")] + \
             [f"{tool}(//tmp/claude-*/**)" for tool in ("Read", "Grep", "Glob")] + ["WebFetch", "WebSearch"]

COPY_IGNORE = shutil.ignore_patterns("build", ".gradle", "logs", "*.log")
# 기존 동작 고정 테스트(293개)는 이름·기대값이 현재 버그 동작을 드러내므로 에이전트 사본에는 넣지 않고 판정 2차(회귀)에만 쓴다
CHAR_TESTS = Path("src/test/java/com/fream/back/domain")


def copy_target(dst: Path) -> None:
    """에이전트용 대상 사본: 빌드 산출물과 고정 테스트를 뺀다."""
    skip = TARGET / CHAR_TESTS

    def ignore(d, names):
        out = set(COPY_IGNORE(d, names))
        if Path(d) == skip.parent:
            out.add(skip.name)
        return out
    shutil.copytree(TARGET, dst, ignore=ignore)

_lock_path = HOST_RUNS / ".lock"


VARIANT = "original"


def set_variant(name: str) -> None:
    """변형(size-structure-variants): 대상·과제·oracle·지도 경로를 variants/<name>/ 로 바꾼다."""
    global TARGET, CASES, ORACLE, GRAPHS, VARIANT
    if name == "original":
        return
    base = ROOT / "variants" / name
    if not base.is_dir():
        raise SystemExit(f"변형 없음: {base}")
    TARGET, CASES, ORACLE, GRAPHS, VARIANT = base / "target" / "fream-back", base / "cases", base / "oracle", base / "graphs", name


def set_dirs(results: str) -> None:
    global RESULTS, RAW, RUNS_FILE
    RESULTS = ROOT / results
    RAW, RUNS_FILE = RESULTS / "raw", RESULTS / "runs.jsonl"


def case_type(case: str) -> str:
    t = (CASES / case / "TYPE").read_text().strip()
    if t not in PROMPTS:
        raise ValueError(f"{case}: 알 수 없는 TYPE {t!r}")
    return t


def cond_prompt(cond: str, case: str) -> str:
    _, mode = CONDITIONS[cond]
    return PROMPTS[case_type(case)] + (PROMPT_NAV_FIRST if mode == "F" else "")


def graph_index(kind: str) -> Path:
    p = GRAPHS / kind / "NAV.md"
    if not p.is_file() or not (GRAPHS / kind / "nav").is_dir():
        raise ValueError(f"그래프 없음: {GRAPHS / kind}")
    if len(p.read_bytes()) > INDEX_LIMIT:
        raise ValueError(f"인덱스 {INDEX_LIMIT}바이트 초과: {p}")
    return p


def prepare_work(case: str, cond: str, work: Path) -> None:
    """대상 사본 + 과제 공개 파일 + 조건별 그래프."""
    copy_target(work)
    shutil.copytree(CASES / case, work, dirs_exist_ok=True, ignore=shutil.ignore_patterns("TYPE"))
    kind, mode = CONDITIONS[cond]
    if kind:
        idx = graph_index(kind)
        shutil.copy2(idx, work / ("NAV.md" if mode == "F" else "CLAUDE.md"))
        shutil.copytree(GRAPHS / kind / "nav", work / "nav")


def protected_files(case: str) -> dict:
    """에이전트가 바꾸면 안 되는 파일: test.sh·TASK.md·기존/보이는 테스트 → 기준 원본 경로."""
    out = {}
    for base in (TARGET, CASES / case):
        for f in base.rglob("*"):
            if not f.is_file():
                continue
            rel = f.relative_to(base).as_posix()
            if base == TARGET and rel.startswith(CHAR_TESTS.as_posix() + "/"):
                continue  # 에이전트 사본에 없는 고정 테스트
            if rel in ("test.sh", "TASK.md") or rel.startswith("src/test/"):
                out[rel] = f
    return out


def sha(p: Path) -> str:
    return hashlib.sha256(p.read_bytes()).hexdigest()


def creds_expiry(p: Path) -> float:
    try:
        return json.loads(p.read_text())["claudeAiOauth"]["expiresAt"] / 1000
    except Exception:
        return 0.0


def wait_fresh_creds(min_left_s: int = 45 * 60) -> None:
    """원본 토큰 만료가 가까우면 샌드박스가 갱신(회전)하지 않도록 원본이 갱신될 때까지 기다린다."""
    while creds_expiry(CREDS) - time.time() < min_left_s:
        left = creds_expiry(CREDS) - time.time()
        print(f"[creds] 만료까지 {left/60:.0f}분 — 원본 갱신 대기(일반 claude 세션이 갱신)", flush=True)
        if left < 0:
            subprocess.run(["claude", "-p", "ok", "--model", "haiku", "--setting-sources", "project"],
                           cwd=tempfile.gettempdir(), capture_output=True, timeout=120)
        time.sleep(60)


def writeback_creds(sb_creds: Path) -> bool:
    """샌드박스 안에서 토큰이 갱신됐으면(만료가 더 늦으면) 원본에 원자적으로 되쓴다."""
    with open(_lock_path, "w") as lk:
        fcntl.flock(lk, fcntl.LOCK_EX)
        if creds_expiry(sb_creds) > creds_expiry(CREDS):
            tmp = CREDS.with_suffix(".tmp-gne")
            shutil.copy2(sb_creds, tmp)
            os.chmod(tmp, 0o600)
            os.replace(tmp, CREDS)
            return True
    return False


def claude_cmd(prompt: str, model: str, budget: str) -> list:
    return [
        str(REAL_HOME / ".local" / "bin" / "claude"), "-p", prompt,
        "--model", model,
        "--output-format", "stream-json", "--verbose",
        "--max-budget-usd", budget,
        "--permission-mode", "bypassPermissions",
    ]


def build_env() -> dict:
    """테스트 실행(test.sh·hidden.sh)용: JDK 17 + 실험 전용 Gradle 홈."""
    env = dict(os.environ)
    env.update({"JAVA_HOME": str(JDK17), "GRADLE_USER_HOME": str(GRADLE_HOME)})
    return env


def sandbox_env(sb_home: Path, sb_tmp: Path) -> dict:
    env = {k: v for k, v in os.environ.items()
           if not k.startswith(("CLAUDE", "ANTHROPIC")) and k not in ("OLDPWD", "PWD")}
    env.update({"HOME": str(sb_home), "TMPDIR": str(sb_tmp), "DISABLE_AUTOUPDATER": "1",
                "JAVA_HOME": str(JDK17), "GRADLE_USER_HOME": str(GRADLE_HOME)})
    return env


def make_sandbox(run_dir: Path) -> tuple:
    sb_home = run_dir / "home"
    (sb_home / ".claude").mkdir(parents=True)
    (run_dir / "tmp").mkdir()
    (sb_home / ".claude" / "settings.json").write_text(json.dumps({"permissions": {"deny": DENY_RULES}}))
    wait_fresh_creds()
    sb_creds = sb_home / ".claude" / ".credentials.json"
    shutil.copy2(CREDS, sb_creds)
    os.chmod(sb_creds, 0o600)
    return sb_home, sb_creds


def tool_uses(log_path: Path):
    """(턴, 도구 이름, 입력 dict) — stream-json 은 content 블록마다 assistant 이벤트를 내므로 message id 단위로 턴을 센다."""
    turn, seen = 0, set()
    for line in log_path.read_text(errors="replace").splitlines():
        try:
            ev = json.loads(line)
        except json.JSONDecodeError:
            continue
        if ev.get("type") != "assistant":
            continue
        mid = ev.get("message", {}).get("id")
        # 하위 에이전트(Agent 도구) 메시지는 parent_tool_use_id 가 있다 — 턴은 본 대화만 세고, 그 도구 호출은 현재 본 턴에 귀속한다
        if mid not in seen and not ev.get("parent_tool_use_id"):
            seen.add(mid)
            turn += 1
        for blk in ev.get("message", {}).get("content", []) or []:
            if blk.get("type") in ("tool_use", "server_tool_use"):
                yield turn, blk.get("name", "?"), blk.get("input") or {}


def audit(log_path: Path, run_id: str) -> list:
    hits = set()
    own_task = re.compile(rf"/tmp/claude-\d+/-tmp-gne-runs-{re.escape(run_id)}-work/")
    other_runs = re.compile(rf"gne-runs/(?!{re.escape(run_id)}\b)\d\d-[A-Z]+-r\d+-\d+")
    for _, name, inp in tool_uses(log_path):
        if name in FORBIDDEN_TOOLS:
            hits.add(f"tool:{name}")
        text = json.dumps(inp, ensure_ascii=False)
        text = own_task.sub("<own-task-dir>/", text).replace(f"{HOST_RUNS}/{run_id}/tmp/", "<own-tmp>/")
        hits.update(p for p in AUDIT_INPUT if re.search(p, text))
        if other_runs.search(text):
            hits.add("other-run")
        if name.lower() == "bash":
            cmd = inp.get("command", "")
            hits.update(f"bash:{p}" for p in AUDIT_BASH if re.search(p, cmd))
    return sorted(hits)


def parse_log(log_path: Path, relevant: list) -> dict:
    out = {"result": None, "tool_calls": 0, "tools": {}, "first_edit_turn": None,
           "nav_reads": 0, "first_nav_turn": None, "first_relevant_turn": None, "relevant_first_turns": {}}
    names = {Path(r).name: r for r in relevant}
    for turn, name, inp in tool_uses(log_path):
        out["tool_calls"] += 1
        out["tools"][name] = out["tools"].get(name, 0) + 1
        text = json.dumps(inp, ensure_ascii=False)
        edit_like = name in ("Edit", "Write", "MultiEdit", "NotebookEdit") or (
            name.lower() == "bash" and re.search(r"\bsed -i|\bpatch\b|> *[\w./-]+\.(java|ya?ml|sql)\b|python3? - <<", text))
        if edit_like and out["first_edit_turn"] is None:
            out["first_edit_turn"] = turn
        if re.search(r"(^|[\s\"'/])(NAV\.md|nav/)", text):
            out["nav_reads"] += 1
            out["first_nav_turn"] = out["first_nav_turn"] or turn
        for base, rel in names.items():
            if re.search(rf"(^|[^\w]){re.escape(base)}", text) and rel not in out["relevant_first_turns"]:
                out["relevant_first_turns"][rel] = turn
    if out["relevant_first_turns"]:
        out["first_relevant_turn"] = min(out["relevant_first_turns"].values())
    for line in log_path.read_text(errors="replace").splitlines():
        try:
            ev = json.loads(line)
        except json.JSONDecodeError:
            continue
        if ev.get("type") == "result":
            out["result"] = ev
    return out


def total_tokens(res: dict) -> dict:
    """총 토큰. result.usage 는 본 대화만 센다(하위 에이전트 제외) — modelUsage 합계를 주 지표로 쓴다."""
    u = res.get("usage") or {}
    main = sum(u.get(k) or 0 for k in ("input_tokens", "output_tokens", "cache_creation_input_tokens", "cache_read_input_tokens"))
    mu = res.get("modelUsage") or {}
    allm = sum((v.get("inputTokens") or 0) + (v.get("outputTokens") or 0) + (v.get("cacheReadInputTokens") or 0)
               + (v.get("cacheCreationInputTokens") or 0) for v in mu.values())
    return {"total_tokens": allm if mu else main, "main_tokens": main, "models_used": sorted(mu)}


def run_sh(script: str, cwd: Path, timeout: int = 1200) -> subprocess.CompletedProcess:
    return subprocess.run(["bash", script], cwd=cwd, env=build_env(), capture_output=True, timeout=timeout)


def gradle_test(d: Path, *patterns: str) -> subprocess.CompletedProcess:
    args = ["./gradlew", "test"] + [x for p in patterns for x in ("--tests", p)]
    return subprocess.run(args, cwd=d, env=build_env(), capture_output=True, timeout=1500)


def judge(case: str, work: Path) -> dict:
    """1차(성공): 보호 파일 복원 + hidden 테스트 덮어쓰기 → test.sh, hidden·보이는 테스트.
    2차(회귀, 성공과 별도): 고정 테스트 덮어쓰기 − regression-exclude → com.fream.back.domain.*.
    oracle/*/hidden/hidden.sh 는 과제 작성 때의 확인 스크립트이고, 판정은 이 함수가 한다."""
    prot = protected_files(case)
    tampered = sorted(rel for rel, src in prot.items()
                      if not (work / rel).is_file() or sha(work / rel) != sha(src))
    if case_type(case) == "question":
        ans = work / "ANSWER.md"
        return {"tampered": tampered, "answer_present": ans.is_file(),
                "answer_text": ans.read_text(errors="replace") if ans.is_file() else ""}
    jd = Path(tempfile.mkdtemp(prefix="judge-", dir=HOST_RUNS))
    try:
        shutil.copytree(work, jd, dirs_exist_ok=True, ignore=COPY_IGNORE)
        for rel in tampered:  # 변조 여부와 무관하게 원본 보호 파일로 복원한 뒤 판정한다
            (jd / rel).parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(prot[rel], jd / rel)
        shutil.copytree(ORACLE / case / "hidden" / "src", jd / "src", dirs_exist_ok=True)
        t = run_sh("test.sh", jd)
        h = gradle_test(jd, "com.fream.back.hidden.*", "com.fream.back.visible.*")
        out = {"tampered": tampered, "test_pass": t.returncode == 0, "hidden_pass": h.returncode == 0,
               "hidden_failed_tests": failed_tests(jd) if h.returncode else [],
               "hidden_tail": h.stdout.decode(errors="replace")[-600:] if h.returncode else ""}
        shutil.rmtree(jd / "build" / "test-results", ignore_errors=True)
        shutil.copytree(TARGET / CHAR_TESTS, jd / CHAR_TESTS, dirs_exist_ok=True)
        for fqcn in read_lines(ORACLE / case / "regression-exclude.txt"):
            (jd / "src/test/java" / (fqcn.replace(".", "/") + ".java")).unlink(missing_ok=True)
        g = gradle_test(jd, "com.fream.back.domain.*")
        fails = failed_tests(jd) if g.returncode else []
        out.update({"regression_pass": g.returncode == 0, "regression_failed_tests": fails,
                    "regression_error": (g.returncode != 0 and not fails)})  # 컴파일 실패 등
        return out
    finally:
        shutil.rmtree(jd, ignore_errors=True)


def failed_tests(d: Path) -> list:
    out = []
    for x in sorted((d / "build" / "test-results" / "test").glob("*.xml")):
        for m in re.finditer(r'<testcase name="([^"]+)" classname="([^"]+)"[^>]*>\s*<failure', x.read_text(errors="replace")):
            out.append(f"{m[2]}.{m[1]}")
    return out


def append_row(row: dict) -> None:
    with open(_lock_path, "w") as lk:
        fcntl.flock(lk, fcntl.LOCK_EX)
        with open(RUNS_FILE, "a", encoding="utf-8") as f:
            f.write(json.dumps(row, ensure_ascii=False) + "\n")


def read_lines(p: Path) -> list:
    return [l.strip() for l in p.read_text().splitlines() if l.strip()] if p.is_file() else []


def run_one(case: str, cond: str, rep: int, model: str, budget: str, timeout_s: int) -> dict:
    run_id = f"{case[:2]}-{cond}-r{rep}-{int(time.time())}"
    run_dir = HOST_RUNS / run_id
    sb_home, sb_creds = make_sandbox(run_dir)
    prepare_work(case, cond, run_dir / "work")

    prompt = cond_prompt(cond, case)
    log_path = RAW / f"{run_id}.jsonl"
    t0 = time.time()
    timed_out = False
    with open(log_path, "wb") as lf:
        try:
            p = subprocess.run(claude_cmd(prompt, model, budget), cwd=run_dir / "work",
                               env=sandbox_env(sb_home, run_dir / "tmp"),
                               stdin=subprocess.DEVNULL, stdout=lf, stderr=subprocess.STDOUT, timeout=timeout_s)
            rc = p.returncode
        except subprocess.TimeoutExpired:
            rc, timed_out = -1, True
    wall = time.time() - t0
    creds_written_back = writeback_creds(sb_creds)

    relevant = read_lines(ORACLE / case / "relevant.txt")
    parsed = parse_log(log_path, relevant)
    res = parsed["result"] or {}
    usage = res.get("usage") or {}
    violations = audit(log_path, run_id)
    j = judge(case, run_dir / "work")
    ctype = case_type(case)
    row = {
        "run_id": run_id, "variant": VARIANT, "case": case, "type": ctype, "cond": cond, "rep": rep, "model": model,
        "rc": rc, "timed_out": timed_out, "wall_s": round(wall, 1),
        "result_subtype": res.get("subtype"), "is_error": res.get("is_error"),
        "num_turns": res.get("num_turns"), "duration_ms": res.get("duration_ms"),
        "total_cost_usd": res.get("total_cost_usd"),
        "input_tokens": usage.get("input_tokens"), "output_tokens": usage.get("output_tokens"),
        "cache_creation_input_tokens": usage.get("cache_creation_input_tokens"),
        "cache_read_input_tokens": usage.get("cache_read_input_tokens"),
        **total_tokens(res), "subagent_calls": parsed["tools"].get("Agent", 0) + parsed["tools"].get("Task", 0),
        "tool_calls": parsed["tool_calls"], "tools": parsed["tools"], "first_edit_turn": parsed["first_edit_turn"],
        "nav_reads": parsed["nav_reads"], "first_nav_turn": parsed["first_nav_turn"],
        "first_relevant_turn": parsed["first_relevant_turn"], "relevant_first_turns": parsed["relevant_first_turns"],
        "relevant_total": len(relevant),
        "audit_violations": violations, "valid": not violations and parsed["result"] is not None,
        "tampered": j["tampered"], "creds_written_back": creds_written_back,
        "log": str(log_path.relative_to(ROOT)),
    }
    if ctype == "question":
        ans_dir = RESULTS / "answers"
        ans_dir.mkdir(exist_ok=True)
        (ans_dir / f"{run_id}.md").write_text(j["answer_text"], encoding="utf-8")
        row.update({"answer_present": j["answer_present"], "answer": str((ans_dir / f"{run_id}.md").relative_to(ROOT)),
                    "success": None})  # 채점은 scripts/grade_answers.py
    else:
        row.update({"test_pass": j["test_pass"], "hidden_pass": j["hidden_pass"],
                    "success": j["test_pass"] and j["hidden_pass"] and not j["tampered"],
                    "hidden_failed_tests": j["hidden_failed_tests"], "hidden_tail": j["hidden_tail"],
                    "regression_pass": j["regression_pass"], "regression_failed_tests": j["regression_failed_tests"],
                    "regression_error": j["regression_error"]})
    if not row["success"]:
        kept = RESULTS / "failed" / run_id
        shutil.copytree(run_dir / "work", kept, symlinks=True, ignore=COPY_IGNORE)
        row["kept_work"] = str(kept.relative_to(ROOT))
    append_row(row)
    shutil.rmtree(run_dir, ignore_errors=True)
    return row


_case_locks: dict = {}
_case_locks_guard = threading.Lock()


def run_locked(case: str, *args) -> dict:
    """같은 과제 실행은 동시에 돌리지 않는다 — 고정 이름 /tmp 파일 공유 방지."""
    with _case_locks_guard:
        lock = _case_locks.setdefault(case, threading.Lock())
    with lock:
        return run_one(case, *args)


def done_keys() -> set:
    keys = set()
    if RUNS_FILE.exists():
        for line in RUNS_FILE.read_text().splitlines():
            r = json.loads(line)
            if r.get("valid"):
                keys.add((r["case"], r["cond"], r["rep"]))
    return keys


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--cases", default="")
    ap.add_argument("--conds", default=",".join(CONDITIONS))
    ap.add_argument("--reps", default="1,2,3")
    ap.add_argument("--parallel", type=int, default=5)
    ap.add_argument("--model", default="sonnet")
    ap.add_argument("--budget", default="5")
    ap.add_argument("--timeout", type=int, default=2700)
    ap.add_argument("--results-dir", default="results")
    ap.add_argument("--variant", default="original", help="variants/<이름> (inflated·obfuscated·inflated-obfuscated)")
    ap.add_argument("--dry-run", action="store_true", help="실행하지 않고 조건별 사본 구성·프롬프트만 출력")
    a = ap.parse_args()
    set_variant(a.variant)
    set_dirs(a.results_dir)

    cases = sorted(d.name for d in CASES.iterdir() if d.is_dir())
    if a.cases:
        want = a.cases.split(",")
        cases = [c for c in cases if c[:2] in want]
    conds = a.conds.split(",")
    for c in conds:
        if c not in CONDITIONS:
            raise SystemExit(f"알 수 없는 조건: {c}")
    reps = [int(r) for r in a.reps.split(",")]

    HOST_RUNS.mkdir(exist_ok=True)
    if a.dry_run:
        for case in cases:
            for cond in conds:
                d = Path(tempfile.mkdtemp(prefix="dry-", dir=HOST_RUNS))
                try:
                    prepare_work(case, cond, d / "work")
                    extra = sorted(p.name for p in (d / "work").iterdir() if p.name in ("NAV.md", "CLAUDE.md", "nav"))
                    print(f"== {case} {cond} type={case_type(case)} graph_files={extra}")
                    print(cond_prompt(cond, case))
                finally:
                    shutil.rmtree(d, ignore_errors=True)
        return 0

    RAW.mkdir(parents=True, exist_ok=True)
    done = done_keys()
    jobs = []
    for rep in reps:
        # 조건 순서를 과제·반복마다 돌려 같은 조건이 늘 먼저 돌지 않게 하고, 같은 과제의 조건이 연달아 동시에 잡히지 않게 섞는다
        orders = [conds[(i + rep) % len(conds):] + conds[:(i + rep) % len(conds)] for i in range(len(cases))]
        for k in range(len(conds)):
            for c, order in zip(cases, orders):
                if (c, order[k], rep) not in done:
                    jobs.append((c, order[k], rep))
    print(f"jobs={len(jobs)} parallel={a.parallel}", flush=True)
    fails = 0
    with cf.ThreadPoolExecutor(max_workers=a.parallel) as ex:
        futs = {ex.submit(run_locked, c, cond, rep, a.model, a.budget, a.timeout): (c, cond, rep)
                for c, cond, rep in jobs}
        for f in cf.as_completed(futs):
            c, cond, rep = futs[f]
            try:
                r = f.result()
                print(f"{c} {cond} r{rep}: success={r['success']} valid={r['valid']} cost={r['total_cost_usd']} "
                      f"turns={r['num_turns']} rel_turn={r['first_relevant_turn']} viol={r['audit_violations']}", flush=True)
            except Exception as e:  # 러너 오류도 숨기지 않는다
                fails += 1
                print(f"{c} {cond} r{rep}: RUNNER ERROR {e!r}", flush=True)
                append_row({"case": c, "cond": cond, "rep": rep, "valid": False, "runner_error": repr(e)})
    return 1 if fails else 0


if __name__ == "__main__":
    sys.exit(main())
