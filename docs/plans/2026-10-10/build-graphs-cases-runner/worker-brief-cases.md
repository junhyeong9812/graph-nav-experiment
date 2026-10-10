# 워커 브리핑 — 실험 과제 작성 (유형별)

## 목표

`target/fream-back`(Java 약 8만 줄, 리팩토링 전 레거시)에서 헤드리스 에이전트에게 줄 과제를 담당 유형으로 **2개** 만든다.

이 과제로 재려는 것은 "탐색 그래프가 있으면 **어디를 봐야 하는지 찾는 비용**이 줄어드는가"이다. 그래서 좋은 과제는 다음 조건을 갖춘다.

- 정답 위치를 TASK.md나 보이는 테스트에서 바로 알 수 없다. 여러 파일·패키지를 거쳐야 도달한다.
- 그런데도 코드만 읽으면 풀 수 있다. 모델의 사전 지식이 필요 없다.

## 필독

1. `/home/jun/project/graph-nav-experiment/docs/adr/0001-graph-nav-experiment-design.md`
2. `docs/plans/2026-10-10/build-graphs-cases-runner/requirement-spec.md`
3. `docs/target-suspected-bugs.md`: 의심 지점 41건. 버그·패턴 유형의 재료다.
4. `target/SOURCE.md`, `scripts/check-target.sh`

## 폴더 계약 (러너가 이 형식을 읽는다)

```
cases/NN-slug/                 에이전트에게 보이는 쪽. 실행 때 target/fream-back 사본 위에 덮어씌운다.
  TASK.md                      증상 보고 / 변경 요청 / 질문
  TYPE                         bug | change | pattern | question (한 줄)
  test.sh                      (question 제외) 보이는 테스트만 실행
  src/test/java/com/fream/back/visible/NN/...Test.java   (question 제외) 보이는 테스트
oracle/NN-slug/                비공개
  fix.patch                    정답(target/fream-back 루트 기준, `patch -p1`). question은 없음
  hidden/hidden.sh             판정. 아래 "hidden.sh 계약"
  hidden/src/test/java/com/fream/back/hidden/NN/...Test.java
  regression-exclude.txt       정답이 의도적으로 바꾸는 동작을 고정한 기존 테스트 클래스(FQCN, 한 줄에 하나). 없으면 빈 파일
  relevant.txt                 정답에 필요한 핵심 파일(루트 기준 경로, 3~10개). "첫 관련 파일 도달 턴" 지표에 쓴다
  checklist.md                 (question만) 정답 항목 5~8개. 항목마다 근거 파일:줄
  design.md                    과제 설계 메모: 의도, 정답, 왜 탐색이 필요한지, 순진한 오답
```

- `NN`은 배정 번호를 쓴다.
- test.sh와 hidden.sh는 `JAVA_HOME`과 `GRADLE_USER_HOME`이 러너 환경에서 주어진다고 가정한다. 직접 실행할 때는 아래처럼 준다.
  ```
  JAVA_HOME=$HOME/.sdkman/candidates/java/17.0.13-tem GRADLE_USER_HOME=/home/jun/.cache/gne-gradle
  ```
  이 캐시는 오프라인 강제·데몬 끔으로 설정돼 있다.
- **test.sh 예:** `./gradlew test --tests 'com.fream.back.visible.NN.*'`. 보이는 테스트만 돌린다(빠르게).
- **hidden.sh 계약:**
  1. `regression-exclude.txt`에 적힌 테스트 파일을 지운다.
  2. 아래 명령을 실행하고 그 exit code를 그대로 낸다.
     ```
     ./gradlew test --tests 'com.fream.back.domain.*' --tests 'com.fream.back.hidden.*' --tests 'com.fream.back.visible.*'
     ```
  - 즉 판정 = hidden + 보이는 테스트 + 기존 고정 테스트 회귀다.
- question 유형은 test.sh·hidden이 없다.
  - 에이전트는 `ANSWER.md`를 쓴다.
  - 채점은 `checklist.md`로 따로 한다. `hidden/` 폴더도 만들지 않는다.

## 유형별 요건

- **bug(버그 수정):**
  - 의심 지점에서 고른다. **증상이 보이는 곳과 원인이 다른 패키지·도메인**인 것을 우선한다.
    - 예: 주소 조회 실패 → 원인은 `global/utils` 암호화
  - 반복 패턴 그룹(페이지 total 불일치 P1·O4·N2, 생성 즉시 MATCHED O2·N1)은 pattern 유형용이라 쓰지 않는다.
  - TASK.md는 사용자·운영 시점의 증상만 쓴다. 파일·클래스 이름은 쓰지 않는다.
  - 보이는 테스트는 증상 지점(서비스나 리포지토리 결과)에서 실패를 재현한다. 테스트 이름이 원인 위치를 가리키면 안 된다.
- **pattern(반복 패턴):**
  - 한 곳의 증상을 보고하고, TASK.md에 "같은 원인의 문제가 코드베이스의 다른 곳에도 있을 수 있으니 모두 찾아 고쳐라"를 쓴다.
  - 보이는 테스트는 보고된 한 곳만 검사한다. hidden은 **모든 지점**을 검사한다.
  - `design.md`에 지점 목록을 적는다. 담당 패턴 그룹의 지점을 코드 전체 grep으로 직접 다시 찾아 누락을 없앤다(의심 지점 목록이 전부가 아닐 수 있다).
  - 배정 패턴은 01·02가 각각 하나씩이다(아래 배정).
- **change(변경 요청):**
  - 기획·운영 요청서 톤으로 쓴다.
  - 구현이 **2개 이상 도메인**을 건드려야 한다.
    - 예: 주문 취소 시 포인트 환원 + 알림 + 판매 입찰 상태 복구
  - 기존 흐름(이벤트·서비스 호출 사슬)을 찾아 그 위에 얹어야 맞게 구현된다.
  - 보이는 테스트는 요청의 핵심 한두 가지를 검사한다. hidden은 요청 전체와 기존 동작 보존을 검사한다.
  - 원본에서 test.sh는 실패해야 한다.
  - 테스트는 H2·Mockito 범위 안에서 판정되게 설계한다. 외부 서버가 필요한 요청은 쓰지 않는다.
- **question(이해 질문):**
  - "X는 어디서 어떻게 처리되나?"처럼 **여러 도메인을 거치는 흐름**을 묻는다.
    - 예: 즉시 구매가 성공하면 어떤 엔티티의 상태가 어떤 순서로 바뀌고 누가 알림을 받나
  - TASK.md는 질문과 답 형식으로 쓴다. 답 형식은 `ANSWER.md`에 단계별 답을 쓰고 각 단계에 근거 파일:줄을 붙이는 것이다.
  - `checklist.md`: 채점 항목 5~8개. 각 항목은 "무엇을 맞혀야 하는지 + 근거 파일:줄 + 부분 점수 기준"을 담는다.
  - 정답은 반드시 코드에서 확인한다. 의심 지점처럼 "코드가 실제로는 다르게 동작하는" 부분이 있으면 실제 동작이 정답이다.

## 공통 금지

- `target/fream-back`, `target-docs/`, `graphs/` 수정 금지. 작업은 자기 사본에서 하고, 결과물은 `cases/`와 `oracle/`의 담당 번호 폴더에만 쓴다.
- TASK.md·보이는 테스트에 "의심"·"버그 목록" 같은 메타 단어, 의심 지점 번호, `docs/` 경로를 쓰지 않는다.
- 새 의존성, 본 코드 수정(`fix.patch`는 정답일 뿐 target에 적용하지 않음), git commit, 네트워크.
- study-note 읽기·수정(`/home/jun/project/study-note` 접근 금지).
- 임시 파일은 `<SCRATCH>/cN/` 아래에만 둔다. `<SCRATCH>`는 `/tmp/claude-1000/-home-jun-project-study-note/bf758d37-a004-4bfb-9e08-9754e714280b/scratchpad`이다.

## 확인 (직접 실행, 원문 보고)

과제마다 `target/fream-back`을 새로 복사한 사본 두 개로 확인한다.

1. **원본 사본** + `cases/NN` 덮어쓰기:
   - test.sh 실패. 실패 메시지를 첨부한다.
   - hidden 덮어쓰기 + hidden.sh 실패. 실패한 테스트를 첨부한다.
   - question은 해당 없음.
2. **정답 사본** + `cases/NN` + `fix.patch`:
   - test.sh 통과.
   - hidden.sh 통과. 테스트 수를 첨부한다.
3. **pattern 추가 확인:** 보고된 한 곳만 고친 부분 정답(`design.md`에 diff를 둔다)은 test.sh를 통과하고 hidden.sh에서 실패해야 한다.
4. **question 추가 확인:** 체크리스트 항목마다 근거 파일:줄을 다시 열어 확인한 기록을 남긴다.
5. **누출 grep:** TASK.md와 보이는 테스트에 `relevant.txt` 파일의 클래스명이 얼마나 나오는지 보고한다. 0이 이상적이다. 증상 지점의 서비스명 정도는 허용한다.

## 보고 (packet)

- 과제별로:
  - 유형, 한 줄 요약, 정답 한 줄
  - 왜 탐색이 필요한지(증상 위치 → 원인 위치 경로)
  - 확인 명령과 결과 원문
  - `regression-exclude` 목록
  - 바꾼·만든 파일
- 실제 읽은 파일, 미완료 항목

## 배정

| 워커 | 번호 | 유형 | 비고 |
|---|---|---|---|
| c1 | 01, 02 | bug | 서로 다른 도메인 |
| c2 | 03, 04 | pattern | 03 = 페이지 total 불일치(내용·count 쿼리 조인 불일치) 계열, 04 = 생성 즉시 MATCHED 계열 |
| c3 | 05, 06 | change | 서로 다른 도메인 조합 |
| c4 | 07, 08 | question | 서로 다른 흐름 |

- 폴더 이름은 `cases/01-<slug>`처럼 짓는다. slug는 영어 소문자이고 정답 위치를 드러내지 않는다(예: `address-lookup`은 되고 `encryption-key`는 안 된다).
