# log — build-graphs-cases-runner (ADR-0001 준비물·파일럿)

## 타임라인

| 시각 | 사건 | 결과/결정 |
|------|------|----------|
| 2026-10-10 | 사용자: fream-back GitHub private 전환 요청 → 실행(PRIVATE 확인). 설계 인터뷰 3회(조건 7·과제 8·336회·$5·Opus 지도·javaparser·러너 복사) → ADR-0001 채택(82ec907) → 구현 명세 합의 | SPEC=1·MODE=auto |
| 2026-10-10 | 가정 2 스모크: 격리 HOME(haiku)에서 작업 폴더 CLAUDE.md의 암호 단어를 도구 없이 답함(PELICAN-7342), creds 변경 없음 | CLAUDE.md 로드 확인 |
| 2026-10-10 | 대상 코드에 `junhyeong9812`(도커 이미지 이름)·사람 문서에 github 링크 존재 → 감사를 도구 입력만 보도록 설계(결과 내용 제외) | 오탐 방지 |
| 2026-10-10 | 실험 전용 Gradle 홈 `~/.cache/gne-gradle`(627M) 채움 + init.d 오프라인 강제 + 데몬 끔. 오프라인 전체 테스트 2분 4초 green | |
| 2026-10-10 | 워커 5(Opus) 병렬: ng(navgraph), c1 bug 01·02, c2 pattern 03·04, c3 change 05·06, c4 question 07·08 — 브리핑 worker-brief-navgraph.md·worker-brief-cases.md | 회수 대기 |
| 2026-10-10 | graphs/human: build_human_graph.py — NAV.md 8,131바이트(문서 목록 + 루트 README 앞 6,012바이트), nav 68파일. 2회 실행 동일 | |
| 2026-10-10 | 러너 작성(scripts/run_experiment.py). 감사·parse 합성 로그 검사: 정상 []·네트워크 3종·로컬 원본·다른 실행·/tmp/claude 검출, 오탐 사례(grep 'nc', Grep curl) [] | 커밋 8819dc9 |
| 2026-10-10 | LLM 지도 생성 시작(gen_llm_graph.py, opus, 상한 $20, 백그라운드) | |
| 2026-10-10 | c4 회수(07·08 question, 체크리스트 8항목씩, 누출 0). 메인: OrderStatus 전이표·SHIPMENT_STARTED 세팅 부재·ShipmentBatchScheduler 위치 확인. 채점 프롬프트의 근거 규칙을 체크리스트 머리말 규칙 따르도록 수정 | |
| 2026-10-10 | LLM 지도 1차 완료: $16.30, NAV 7,912B, nav 17, 감사 0. **result.usage(30만)와 modelUsage(2,632만) 불일치** — 하위 에이전트(Agent 7회) 토큰이 usage에 빠짐 | 러너 총 토큰 = modelUsage 합계, 턴은 본 대화만(parent_tool_use_id 제외). 8e38e0e |
| 2026-10-10 | compare-issue-archive 과거 1,875 로그 점검: 하위 에이전트 흔적 8, 그중 Sonnet 4회가 usage 기준 6.9~16.5배 과소 집계(v3 09-F r3, v4-stale 09-P r2, v5b 05-L r3 등) | 사용자 보고 대상(마감 프로젝트라 수정은 사용자 결정) |
| 2026-10-10 | **누출 발견:** LLM 지도 NAV.md에 "일부 테스트는 현재의 버그 동작을 고정(계좌결제 실패 등)", nav에 고정 테스트 메서드명 나열. 고정 테스트 이름·기대값이 버그를 드러냄 → 모든 조건(B 포함)에 누출 | 사용자 결정: 고정 테스트를 에이전트 사본에서 빼고 판정에만, LLM 지도 재생성. ng 워커에 테스트 입력 제외 지시 |
| 2026-10-10 | ng 회수: navgraph(javaparser 3.28.2), NAV 7,628B, nav 20파일 284KB, 2회 diff 0, 표본 order·payment·user 엔드포인트·사슬 5·Kafka 누락/오탐 0, 테스트명·평가 단어 0 | 수용 |
| 2026-10-10 | c3(05·06 change)·c1(01·02 bug)·c2(03·04 pattern) 회수. 패키지명 숫자 시작 불가로 visible.c01/t05 등. hidden.sh 형식 워커별 상이 | 판정을 러너가 직접: 1차 성공 = test.sh + hidden·visible 테스트(고정 테스트 없음), 2차 회귀 = 고정 테스트 − exclude(성공과 별도 기록) |
| 2026-10-10 | 메인 검수(러너 judge로 직접): 01~06 원본 test·hidden 실패 / 정답 test·hidden·회귀 통과, 변조 0. 공개 파일 메타 단어 0. dry-run B·AF·AC·HF·HC 사본 구성 확인, 에이전트 사본 테스트 = 원래 1 + 보이는 테스트 | 커밋 46b2a2d. 1차 LLM 지도는 graphs/llm-v0-leaked 보관 |
| 2026-10-10 | LLM 지도 재생성 시작(고정 테스트 없는 사본). 파일럿 시작: 01·03·05·07 × B·AF × 1, Sonnet, results-pilot | |

## 생략한 검증

## 완료 요약
