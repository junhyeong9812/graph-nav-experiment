# 워커 브리핑 — 대상 코드베이스 기존 동작 고정(characterization) 테스트

## 목표

`/home/jun/project/graph-nav-experiment/target/fream-back`(fream-back 리팩토링 전 스냅샷, Spring Boot 3.4.1·Java 17, 본 코드 약 8만 줄)에 **외부 서버 없이 도는 기존 동작 고정 테스트**를 담당 도메인에 추가한다.

이 코드베이스는 나중에 "탐색 그래프가 있으면 에이전트의 탐색 비용이 줄어드는가" 실험의 대상이 된다. 지금 만드는 테스트는 그 실험의 과제가 아니라, 과제를 만들 때 쓸 **안전망**이다. 과제가 들어갔을 때 다른 동작이 깨지지 않았는지 잡아내는 역할이다.

## 배경

- 필독:
  - `/home/jun/project/graph-nav-experiment/target/SOURCE.md`: 출처·수선·한계
  - `docs/plans/2026-10-10/setup-target-and-tests/requirement-spec.md`
- 기반 테스트 예시: `src/test/java/com/fream/back/domain/PersistenceSmokeTest.java`
  - `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@ActiveProfiles("test")` + `@Import(QueryDslConfig.class)` 조합이다.
  - 전체 엔티티 스키마가 H2(MySQL 호환 모드)에서 생성되는 것까지 확인됐다.
- 전체 앱 기동(`@SpringBootTest`)은 안 된다. Redis·Kafka·ES 설정이 필요하고, Redis 빈 이름 충돌도 있다. 쓰지 마라.
- JDK: `export JAVA_HOME=$HOME/.sdkman/candidates/java/17.0.13-tem`. 기본 java는 8이다.

## 작업 방식 (같은 폴더에서 동시에 Gradle을 돌리지 않기 위해)

1. 자기 사본을 만든다.
   ```
   cp -r /home/jun/project/graph-nav-experiment/target/fream-back <SCRATCH>/wN/fream-back
   ```
   `<SCRATCH>` = `/tmp/claude-1000/-home-jun-project-study-note/bf758d37-a004-4bfb-9e08-9754e714280b/scratchpad`
2. 그 사본에서 테스트를 쓰고 실행한다.
   ```
   ./gradlew test --tests 'com.fream.back.domain.<담당>.*'
   ```
3. 원본 `target/fream-back`에는 쓰지 않는다. 메인이 사본에서 테스트 파일만 가져가 합친다.

## 만들 것

- 위치: `src/test/java/com/fream/back/domain/<도메인>/...`. 담당 도메인 패키지 아래에만 둔다.
- 종류:
  - **리포지토리 슬라이스**(`@DataJpaTest` 조합): 커스텀 쿼리(JPQL·QueryDSL)의 필터·정렬·페이징·연관 조회 결과
  - **서비스 단위**(Mockito, `@ExtendWith(MockitoExtension.class)`): 상태 전이, 계산(금액·포인트·수량), 검증과 예외 분기, 다른 도메인 호출 여부(verify)
  - 필요하면 **엔티티 단위**: 도메인 메서드의 상태 변경
- 양: 도메인당 테스트 클래스 3~8개, `@Test` 15~40개. 많이 쓰기보다 **핵심 흐름을 넓게** 덮는다. 해당 도메인의 주요 서비스 명령(생성·변경·취소 등)과 주요 조회 쿼리를 먼저 고른다.
- **현재 동작을 고정한다.** 버그처럼 보여도 지금 나오는 값을 기대값으로 쓴다. 고치지 않는다. 그런 곳은 보고서의 "의심 지점"에 적는다.
- 테스트는 반복 실행해도 같은 결과가 나와야 한다.
  - 현재 시각에 의존하면 고정하거나 범위로 검사한다.
  - 테스트 간 순서 의존이 없어야 한다.
- 이름·주석은 무엇을 고정하는지만 쓴다. "나중에 바꿀 곳" 같은 힌트는 쓰지 않는다.

## 확인 (직접 실행, 원문 보고)

1. 담당 테스트 전체를 **2회 연속** 실행한다. 둘 다 통과해야 하고, 테스트 수를 보고한다.
2. **깨 보기:** 테스트 클래스마다 하나씩 대상 본 코드 한 줄을 일부러 바꾼다(예: 조건 반전, 상수 변경). 해당 테스트가 실패하는지 확인하고 원복한다. 바꾼 줄과 실패한 테스트 이름을 보고한다.
3. 원복 후 다시 green인지 확인한다(`diff -r`로 본 코드가 원본 `target/fream-back/src/main`과 같은지).

## 보고 (packet)

- 추가한 테스트 파일 목록(사본 경로), 클래스별 `@Test` 수
- 무엇을 고정했는지 클래스당 한 줄
- 확인 명령과 결과 원문 요약: 2회 실행, 깨 보기 표, `diff -r` 결과
- 의심 지점(버그로 보이는 현재 동작): 파일:줄 + 한 줄
- 테스트로 못 덮은 것과 이유(외부 서버 의존 등)
- 실제 읽은 파일, 미완료 항목

## 하지 말 것

- 본 코드(`src/main`)·`build.gradle`·`application*.yml` 수정. 깨 보기는 사본에서 하고 바로 원복한다.
- 새 의존성 추가(Mockito·AssertJ·JUnit 5·H2는 이미 있다)
- 원본 `target/fream-back`·`/home/jun/project/fream-back` 쓰기
- 담당 외 도메인 패키지에 테스트 두기
- git commit
- study-note 읽기·수정(`/home/jun/project/study-note` 접근 금지)
- 네트워크(Gradle 의존성은 이미 받아져 있다. `--offline`으로 실행해도 된다)
- 임시 파일은 `<SCRATCH>/wN/` 아래에만 둔다

## 배정

| 워커 | 도메인 패키지 |
|---|---|
| w1 | `user`, `address` |
| w2 | `product`, `style` |
| w3 | `order`, `payment` |
| w4 | `sale`, `notification` |
