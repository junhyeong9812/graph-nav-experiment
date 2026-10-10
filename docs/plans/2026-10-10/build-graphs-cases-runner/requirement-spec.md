# 요구사항 명세서 (requirement-spec)

> 작성일: 2026-10-10 · 작업 폴더: `docs/plans/2026-10-10/build-graphs-cases-runner/`
> 설계 정본: `docs/adr/0001-graph-nav-experiment-design.md`(채택). 이 명세는 그 설계의 **준비물 구현과 파일럿**까지다. 본 실험 336회는 파일럿 게이트를 통과한 뒤 같은 작업 폴더에서 이어서 진행한다.

---

## 0. 요구사항 원문 (인터뷰 기록)

- 원문: "private으로 잠시 바꿔놓고 진행하자"(대상 준비 완료 후 실험 설계 진행)
- 인터뷰(ADR-0001 D1~D7과 확정 절):
  - 조건 7개(B, AF·AC·HF·HC·LF·LC)
  - 과제 8개(유형당 2)
  - 336회, $5 상한, 웹 도구 차단·감사
  - LLM 지도는 Opus 1회
  - 파일럿 4과제 × B·AF × 1회 Sonnet
  - javaparser, 러너 복사 후 수정
- 자율성: auto(확인 필요)

## 1. 목표·대상 (필수)

1. **자동 그래프 생성기** `tools/navgraph/`
   - javaparser 기반 Java CLI, 별도 Gradle 프로젝트
   - 입력은 `target/fream-back/src/main`
   - 출력은 `graphs/auto/NAV.md`(인덱스 ≤8KB)와 `graphs/auto/nav/<도메인>.md`(상세)
   - 상세 내용: 엔드포인트 → 컨트롤러 → 서비스 → 리포지토리 사슬, 클래스 의존, JPA 연관, 이벤트·Kafka 발행 → 구독, 테스트 위치
2. **사람 문서 그래프** `graphs/human/`
   - `target-docs/fream-back`을 그대로 쓴다.
   - `NAV.md`는 루트 README를 8KB에서 자르고 문서 목록을 붙인다.
   - `nav/`는 원문 문서다.
3. **LLM 지도** `graphs/llm/`
   - Opus가 `claude -p`로 1회 생성한다. 격리 HOME에서 그래프와 과제 없이 사본만 보고 쓴다.
   - 지시는 "다음 에이전트를 위한 인덱스(≤8KB)와 nav/ 상세 문서를 써라"이다.
   - 생성 토큰·비용을 `graphs/llm/GENERATION.md`에 기록한다.
4. **과제 8개** `cases/NN-*/`(공개)와 `oracle/NN-*/`(비공개). 유형당 2개다.
   - 버그 수정 2, 반복 패턴 2: `docs/target-suspected-bugs.md`에서 고른다.
   - 변경 요청 2, 이해 질문 2: 새로 설계한다.
   - 공개 쪽: `TASK.md`(증상 또는 요청 또는 질문), `test.sh`(보이는 테스트 + 기존 294 회귀)
   - 비공개 쪽:
     - hidden 판정
     - 정답 diff
     - "관련 파일" 목록(첫 관련 파일 도달 턴 지표용)
     - 이해 질문은 정답 체크리스트
   - 대상 코드 사본은 과제마다 `target/fream-back`에서 만들어 실행 때 복사한다. 과제별 변경(버그 그대로 두기 등)은 없다. 의심 지점은 이미 코드에 있다.
     - 변경 요청과 이해 질문도 같은 코드 위에서 한다.
5. **러너** `scripts/run_experiment.py`
   - compare-issue-archive 러너를 복사해 고친다.
   - 조건 7개: 사본에 NAV.md·nav/ 또는 CLAUDE.md·nav/를 넣고 프롬프트를 만든다.
   - Gradle 공유 캐시와 오프라인 강제를 넣는다.
   - 감사를 확장한다: 네트워크 명령, 로컬 원본 경로, 실험 저장소의 docs·target-docs.
   - 이해 질문은 Opus 채점 호출을 따로 둔다(`scripts/grade_answers.py`).
   - 지표를 추가한다: 첫 관련 파일 도달 턴, 그래프 열람.
6. **파일럿**: 4과제(유형별 1) × B·AF × 1회, Sonnet → 게이트(ADR D7) 판정.

## 2. 경계·불변식 (필수)

- `target/fream-back`(본 코드·테스트)은 바꾸지 않는다. 과제는 실행 사본에서만 진행한다.
- 세 그래프의 인덱스(NAV.md 또는 CLAUDE.md 본문)는 각각 8KB 이하다.
- 그래프에는 과제·정답·의심 지점 정보가 없다.
  - 자동 그래프는 기계 추출만 담는다.
  - 사람 문서는 원문 그대로다.
  - LLM 지도는 과제를 모르는 상태에서 만든다.
- 조건 간 차이는 그래프 파일과 프롬프트 문구뿐이다. 모델, 예산, 도구 권한, 사본 코드는 같다.
- 과제 판정:
  - hidden은 결과로 판정하고 구현 방식에 기대지 않는다.
  - 기존 294개 고정 테스트 중 과제가 의도적으로 바꾸는 동작을 고정한 테스트는 과제별로 명시해 회귀에서 뺀다.
- 감사에서 무효가 나온 행은 보존하고 재실행한다.

## 3. 기준소스 (필수)

ADR-0001, `target/fream-back`(4fd438f + 수선 + 테스트), `docs/target-suspected-bugs.md`, compare-issue-archive `scripts/run_experiment.py`(feat/case-build `aea9131`).

## 4. 금지영역 (필수)

- `target/fream-back`, `target-docs/` 수정.
- 로컬 fream-back 원본, compare-issue-archive 수정.
- study-note 읽기·수정(워커 포함).
- push·외부 발행(사용자 확인 후).

## 5. 검증 방법 (필수)

- **그래프 생성기:**
  - 표본 3개 도메인에서 사슬·의존 항목을 실제 코드와 대조한다(누락·오탐 기록).
  - 인덱스 크기 ≤8KB를 확인한다.
  - 과제 정답 파일·정답 키워드가 그래프에 노출되는지 grep으로 본다. 정답 파일 경로가 구조상 그래프에 나오는 것은 정상이지만 "버그"·"의심" 같은 단어는 없어야 한다.
- **과제 8개(메인이 직접 실행):**
  - 원본 사본: 버그·패턴 과제는 hidden 실패, 변경 요청은 hidden 실패.
  - 정답 diff 적용 후: hidden과 회귀 모두 통과.
  - 이해 질문: 체크리스트 항목마다 코드 근거(파일:줄)를 확인한다.
- **러너:**
  - 조건 7개의 사본 구성과 프롬프트를 출력해 확인한다.
  - CLAUDE.md가 격리 HOME에서 로드되는지 스모크 1회 확인한다.
  - 감사 규칙을 확인한다: 위반 문자열을 넣은 가짜 로그로 무효 판정이 나오는지 본다.
  - Gradle 오프라인 강제를 확인한다.
- **LLM 지도:** 생성 로그에 과제·docs 접근이 없는지 감사한다. 인덱스 ≤8KB를 확인한다.
- **파일럿:** 게이트 표, 손 검산 1회.

## 6. stakes (필수)

- 판정: **낮음**
- 로컬 실험 준비이고, 재생성할 수 있다.
- 비용: 파일럿 8회와 LLM 지도 1회. 본 실험 336회는 파일럿 결과를 보고한 뒤 진행한다.

---

## 7. 자율성

- [x] auto
- [ ] lazy

## 8. load-bearing 가정

1. javaparser로 Spring 구조(어노테이션·생성자 주입·QueryDSL 구현체)를 추출해 8KB 인덱스로 요약할 수 있다. 첫 버전을 표본 도메인과 대조해 바로 확인한다.
2. 격리 HOME의 `claude -p`가 작업 폴더의 CLAUDE.md를 읽는다(`--setting-sources` 영향). 러너 스모크로 착수 직후 확인하고, 안 되면 C 조건 제공 방식을 사용자와 재합의한다.

## 9. task 분해

| task | 목표 | acceptance |
|------|------|-----------|
| 01 | 러너 복사·조건 7개·감사·Gradle 오프라인, CLAUDE.md 로드 스모크 | §5 러너 |
| 02 | navgraph 생성기 + graphs/auto | §5 생성기 |
| 03 | graphs/human, graphs/llm(Opus 1회) | 크기·감사 |
| 04 | 과제 8개(워커 Opus ≤4, 유형별) + 메인 검수 | §5 과제 |
| 05 | 파일럿 8회 → 게이트 판정 → 사용자 보고 | 게이트 표 |
