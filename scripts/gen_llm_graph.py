#!/usr/bin/env python3
"""LLM 지도(L 조건) 1회 생성: 과제·그래프 없는 대상 사본에서 Opus 가 NAV.md + nav/ 를 쓴다.

러너와 같은 격리(HOME·TMPDIR·deny·감사)를 쓴다. 결과는 graphs/llm/, 생성 비용은 graphs/llm/GENERATION.md.
사용: python3 scripts/gen_llm_graph.py [--model opus] [--budget 20]
"""
import argparse
import filecmp
import json
import shutil
import subprocess
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import run_experiment as R  # noqa: E402

PROMPT = (
    "현재 디렉터리는 Spring Boot 백엔드 프로젝트다. 앞으로 이 코드베이스에서 버그 수정·기능 변경·코드 질문 같은 작업을 맡을 "
    "다른 AI 코딩 에이전트를 위해, 코드를 빨리 찾아갈 수 있는 구조 지도를 써라.\n"
    "- NAV.md: 진입 인덱스. **8,000바이트 이하를 지켜라**(쓰고 나서 `wc -c NAV.md` 로 확인하라). "
    "전체 구조, 도메인별 역할과 주요 진입점, 도메인 사이의 주요 흐름, nav/ 상세 문서 목록을 담는다.\n"
    "- nav/: 상세 문서(도메인별·흐름별 마크다운, 크기 제한 없음). 다음 에이전트가 바로 열 수 있게 실제 파일 경로를 적어라.\n"
    "지도에는 코드에서 확인한 사실만 담아라. 코드와 테스트는 수정하지 마라. 작업은 현재 디렉터리 안에서만 하라."
)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--model", default="opus")
    ap.add_argument("--budget", default="20")
    ap.add_argument("--timeout", type=int, default=5400)
    a = ap.parse_args()
    out = R.GRAPHS / "llm"
    if out.exists():
        raise SystemExit(f"이미 있음: {out} (다시 만들려면 먼저 옮겨 두기)")

    R.HOST_RUNS.mkdir(exist_ok=True)
    run_id = f"00-LLMGEN-r1-{int(time.time())}"
    run_dir = R.HOST_RUNS / run_id
    sb_home, sb_creds = R.make_sandbox(run_dir)
    work = run_dir / "work"
    shutil.copytree(R.TARGET, work, ignore=R.COPY_IGNORE)
    log_dir = R.ROOT / "graphs" / "llm-generation"
    log_dir.mkdir(parents=True, exist_ok=True)
    log_path = log_dir / f"{run_id}.jsonl"
    t0 = time.time()
    with open(log_path, "wb") as lf:
        p = subprocess.run(R.claude_cmd(PROMPT, a.model, a.budget), cwd=work, env=R.sandbox_env(sb_home, run_dir / "tmp"),
                           stdin=subprocess.DEVNULL, stdout=lf, stderr=subprocess.STDOUT, timeout=a.timeout)
    wall = time.time() - t0
    R.writeback_creds(sb_creds)

    parsed = R.parse_log(log_path, [])
    res = parsed["result"] or {}
    usage = res.get("usage") or {}
    violations = R.audit(log_path, run_id)
    # 코드·테스트를 건드리지 않았는지: 대상과 같은 파일 집합·내용
    changed = []
    for f in R.TARGET.rglob("*"):
        rel = f.relative_to(R.TARGET)
        if f.is_file() and not any(part in ("build", ".gradle", "logs") for part in rel.parts) and not f.name.endswith(".log"):
            w = work / rel
            if not w.is_file() or not filecmp.cmp(f, w, shallow=False):
                changed.append(rel.as_posix())
    nav = work / "NAV.md"
    size = len(nav.read_bytes()) if nav.is_file() else None
    summary = {
        "run_id": run_id, "model": a.model, "rc": p.returncode, "wall_s": round(wall, 1),
        "num_turns": res.get("num_turns"), "total_cost_usd": res.get("total_cost_usd"),
        "input_tokens": usage.get("input_tokens"), "output_tokens": usage.get("output_tokens"),
        "cache_creation_input_tokens": usage.get("cache_creation_input_tokens"),
        "cache_read_input_tokens": usage.get("cache_read_input_tokens"),
        **R.total_tokens(res), "tool_calls": parsed["tool_calls"], "audit_violations": violations, "changed_target_files": changed,
        "nav_md_bytes": size, "nav_dir_files": sum(1 for x in (work / "nav").rglob("*") if x.is_file()) if (work / "nav").is_dir() else 0,
    }
    print(json.dumps(summary, ensure_ascii=False, indent=1))
    if size is not None and (work / "nav").is_dir():
        out.mkdir(parents=True)
        shutil.copy2(nav, out / "NAV.md")
        shutil.copytree(work / "nav", out / "nav")
        total = R.total_tokens(res)["total_tokens"]
        (out / "GENERATION.md").write_text(
            "# LLM 지도 생성 기록\n\n"
            f"- 모델: {a.model}, 프롬프트: `scripts/gen_llm_graph.py` PROMPT, 로그: `graphs/llm-generation/{run_id}.jsonl`\n"
            f"- 총 토큰(modelUsage 합계, 하위 에이전트 포함): {total:,}, 비용 ${summary['total_cost_usd']}, 턴 {summary['num_turns']}, {summary['wall_s']}초\n"
            f"- NAV.md {size}바이트, nav/ 파일 {summary['nav_dir_files']}개, 감사 위반 {violations or '없음'}, 대상 파일 변경 {changed or '없음'}\n",
            encoding="utf-8")
    shutil.rmtree(run_dir, ignore_errors=True)
    return 0 if (size is not None and not violations and not changed) else 1


if __name__ == "__main__":
    sys.exit(main())
