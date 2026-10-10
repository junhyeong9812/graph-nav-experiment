# log — setup-target-and-tests (그래프 탐색 실험 준비: 대상 사본·테스트)

## 타임라인

| 시각 | 사건 | 결과/결정 |
|------|------|----------|
| 2026-10-10 | 사용자 "graph-nav-experiment로 project 폴더에 만들고 시작하자, fream-back을 복사해놔줘, 거기에 테스트를 만들자" | 작업 폴더 생성, 인터뷰 착수 |
| 2026-10-10 | 사전 측정(L0, compare-issue-archive 세션): fream-back 본 코드 Java 825파일 79,978줄·도메인 20, 테스트 11파일 666줄 `@Test` 23, 커밋 450, GitHub 공개·별 0, Boot 3.4.1·Java 17. 오프라인 빌드 실패(플러그인 캐시 없음) | 대상 후보 확정 |
| 2026-10-10 | 인터뷰 2회: 기준 커밋 = 4fd438f(리팩토링 직전, 사용자 "작년 6월 마무리 코드, 리팩토링 안 된 형태" 확인), 이력 없음, 문서 분리 보관, 빌드 green + 테스트 기반, H2·Mockito, 빌드 최소 수선, git init + 작업 브랜치, 낮음·auto → 명세 합의 | SPEC=1·MODE=auto |
| 2026-10-10 | task01: git init(main) + feat/setup-target. `git archive 4fd438f` 911파일 → 코드 837 / 문서 69(target-docs) / logs 5 버림. 합계 일치 | |
| 2026-10-10 | task02: 수선 전 compileJava(JDK 17.0.13) 실패 — OrderQueryController cannot find symbol 4. aaa7871 src/main diff(122줄)를 git apply → compileJava·compileTestJava 성공 | 가정 1 앞부분 실증 |
| 2026-10-10 | task03: 기존 `contextLoads` 실패 — prod 프로필 기본, `RedisConfig.stringRedisTemplate` 빈 이름 충돌(c91c9e9 2025-08부터, 리팩토링 main에서도 미수정). test 프로필 + 덮어쓰기 허용 env로 1회 재시도 → Kafka bootstrap-servers 미해결로 실패 | 전체 기동은 외부 서버 필요 → 확인 대상에서 제외하고 SOURCE.md에 기록. 명세 §5 "컨텍스트 로드" 항목을 JPA 슬라이스 스모크로 대체 |
| 2026-10-10 | aaa7871의 PersistenceSmokeTest(@DataJpaTest + test 프로필)를 domain 패키지로 옮겨 실행 → 1/1 통과(전체 엔티티 H2 스키마 생성) | 테스트 기반 실증 |
| 2026-10-10 | 테스트 실행이 logs/ 를 만들어 .gitignore 추가. 커밋 b16f4af(910파일 = 코드 837 + 문서 69 + SOURCE·patch·smoke·.gitignore) | |
| 2026-10-10 | task04 착수: worker-brief-tests.md, 워커 Opus 4 병렬(w1 user·address, w2 product·style, w3 order·payment, w4 sale·notification), 각자 scratchpad 사본에서 작업. scripts/check-target.sh 작성(기준선 1/1) | 회수 대기 |
| 2026-10-10 | w4 회수: 9클래스 77개, 깨 보기 9/9, 의심 8 | 메인: src/main·build.gradle diff 0, 병합 후 77/77 |
| 2026-10-10 | w3 회수: 10클래스 74개, 깨 보기 10/10, 의심 10 | 메인: diff 0, 병합 후 74/74. 의심 O1(계좌이체 PENDING→PENDING)·O9(PROCESSING 리터럴)·N4(unpaged) 코드로 확인 |
| 2026-10-10 | w2 회수: 11클래스 80개, 깨 보기 11/11, 의심 11 | 메인: diff 0, 병합 후 80/80. P6 위치 확인 |
| 2026-10-10 | w1 회수: 13클래스 62개, 깨 보기 13/13, 의심 11 | 메인: diff 0, 병합 |
| 2026-10-10 | task05: check-target.sh 2회 연속 tests=294 failures=0 errors=0(스모크 1 + 293). 메인 깨 보기 1건: Payment.java:62 `> 0`→`>= 0` → payment 테스트 exit=1 → git checkout 원복, src/main 변경 0 | 커밋 1d756dd(test 43클래스 + 스크립트) |
| 2026-10-10 | 의심 지점 41건을 docs/target-suspected-bugs.md로 모음(에이전트 사본 밖). 반복 패턴: 페이지 total 불일치·생성 즉시 MATCHED·암호화 왕복 불일치 | 다음 작업(과제 설계)의 재료 |

## 생략한 검증

없음. stakes 낮음이라 리뷰는 셀프체크로 했다. 명세 §5의 "컨텍스트 로드 확인"은 전체 기동이 외부 서버 없이 불가능해 JPA 슬라이스 스모크로 대체했다(타임라인 task03 행).

## 완료 요약

- 저장소 `/home/jun/project/graph-nav-experiment`, 브랜치 `feat/setup-target`(원격 없음).
- `target/fream-back`: fream-back `4fd438f` 스냅샷(이력 없음) + `aaa7871` 빌드 수선. 문서 69개는 `target-docs/`로 분리.
- 테스트: 외부 서버 없이 도는 기존 동작 고정 테스트 43클래스 293개 + 스모크 1. 도메인 8개(user·address·product·style·order·payment·sale·notification). 깨 보기 43/43 + 메인 1.
- `scripts/check-target.sh`: 빌드 + `com.fream.back.domain.*` 테스트, 2회 연속 294/294.
- 의심 지점 41건: `docs/target-suspected-bugs.md`.
- CS 이슈 아카이브: 0건(study-note 금지영역).
