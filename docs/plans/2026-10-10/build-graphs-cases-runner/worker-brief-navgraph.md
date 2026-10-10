# 워커 브리핑 — 자동 탐색 그래프 생성기 (navgraph)

## 목표

javaparser 기반 Java CLI `tools/navgraph/`를 만든다.

- 입력: `target/fream-back/src/main`(Spring Boot 3.4, Java 17, 약 8만 줄)
- 출력: 에이전트용 **기계 추출 구조 지도**
  - `graphs/auto/NAV.md`: 진입 인덱스, **8,192바이트 이하**
  - `graphs/auto/nav/<도메인>.md`: 도메인별 상세. 그 밖에 `nav/global.md`

이 지도는 "탐색 그래프가 에이전트의 탐색 비용을 줄이는가" 실험의 한 조건(자동 생성)이다. 사람·LLM의 해석 없이 **코드에서 기계적으로 뽑을 수 있는 사실만** 담는다.

## 필독

- `/home/jun/project/graph-nav-experiment/docs/adr/0001-graph-nav-experiment-design.md`(D1·D2)
- `docs/plans/2026-10-10/build-graphs-cases-runner/requirement-spec.md`
- `target/SOURCE.md`

## 담을 것

### NAV.md (인덱스)

- 머리말 3~5줄: 무엇인지, 상세 파일 위치, 표기법
- 도메인별 한 줄:
  - 패키지 경로, 파일 수
  - 주요 진입점: 컨트롤러 클래스명과 base path
  - 핵심 엔티티
  - 의존하는 다른 도메인(서비스·리포지토리·엔티티 참조 기준)
  - 이벤트·Kafka 발행 → 구독 도메인
- global 패키지 요약 한 줄씩: config, security, utils 등
- 8KB 안에 들어가게 형식을 압축한다. 넘치면 덜 중요한 열(파일 수 등)부터 줄인다.

### nav/<도메인>.md (상세)

1. **엔드포인트 표:** HTTP 메서드, 경로, 컨트롤러#메서드, 직접 호출하는 서비스 메서드
2. **호출 사슬:** 서비스 메서드 → 호출하는 다른 서비스·리포지토리 메서드. 생성자 주입 필드 타입으로 해석한다(`@RequiredArgsConstructor` + `private final` 필드 포함).
3. **클래스 목록:** 역할별(controller, service command/query, repository, custom/Impl, entity, dto, event, config, aop)로 파일 경로와 한 줄 시그니처 요약(public 메서드 이름 목록)
4. **JPA 엔티티 연관:** `@OneToMany`·`@ManyToOne`·`@OneToOne`·`@ManyToMany`, mappedBy, 상속(`@Inheritance`)
5. **이벤트·메시징:**
   - `ApplicationEventPublisher.publishEvent(new X(...))` → `@EventListener`·`@TransactionalEventListener`의 X 구독자
   - `KafkaTemplate.send("topic")` → `@KafkaListener(topics=...)`
   - 토픽 이름은 상수·설정 참조를 가능한 만큼 해석하고, 해석하지 못하면 원문 표현을 적는다.
6. **AOP:** `@Aspect` 클래스와 pointcut 표현식 원문
7. **테스트 위치:** `src/test`에서 이 도메인 패키지 아래 테스트 클래스 목록

## 기술

- 별도 Gradle 프로젝트 `tools/navgraph/`(`settings.gradle`, `build.gradle`, application 플러그인)
- 의존성: `com.github.javaparser:javaparser-symbol-solver-core`(최신 안정 3.x)
- 다운로드는 Maven Central만 허용한다(이 도구 빌드용). 대상 코드에는 아무것도 추가하지 않는다.
- JDK 17: `$HOME/.sdkman/candidates/java/17.0.13-tem`
- 실행: `tools/navgraph/run.sh`
  - `target/fream-back/src/main/java`와 `src/test/java`를 읽어 `graphs/auto/`를 다시 만든다.
  - 같은 입력이면 같은 출력이 나와야 한다(정렬 고정).
- 심볼 해석이 실패하는 곳(Lombok 생성 메서드, QueryDSL Q 타입 등)은 이름 기반 추정으로 대체한다. 추정한 항목은 표에서 `~`로 표시한다.

## 확인 (직접 실행, 원문 보고)

1. `run.sh` 2회 실행 → 출력 diff 0
2. `wc -c graphs/auto/NAV.md` ≤ 8192
3. **표본 대조:** `order`, `payment`, `user` 도메인
   - 엔드포인트 표: 컨트롤러 파일을 직접 grep한 `@*Mapping` 수와 비교
   - 호출 사슬 5개: 실제 코드를 열어 맞는지 확인
   - 이벤트·Kafka 연결 전부: grep으로 대조
   - 누락·오탐을 표로 보고한다.
4. **누출 점검:** 출력에 "bug", "버그", "의심", "suspect", "TODO 해석" 같은 평가성 단어가 없다(코드 원문 주석을 옮기지 않는다).
   - 도구가 코드 주석을 출력에 넣지 않는지 확인한다.

## 하지 말 것

- `target/`, `target-docs/`, `cases/`, `oracle/`, `docs/` 수정. 쓰는 곳은 `tools/navgraph/`와 `graphs/auto/`뿐이다.
- 코드 주석·Javadoc을 지도에 옮기기. 기계 추출 사실만 담는다.
- `docs/target-suspected-bugs.md` 읽기(지도가 과제를 모르게 하기 위해서다).
- git commit
- study-note 읽기·수정
- 임시 파일은 `<SCRATCH>/ng/` 아래에만 둔다. `<SCRATCH>`는 `/tmp/claude-1000/-home-jun-project-study-note/bf758d37-a004-4bfb-9e08-9754e714280b/scratchpad`이다.

## 보고 (packet)

- 만든 파일 목록
- NAV.md 크기
- 도메인별 상세 파일 크기 합계
- 확인 1~4의 명령과 결과 원문
- 표본 대조 누락·오탐 표
- 해석 실패와 추정 처리 방식
- 실제 읽은 파일, 미완료 항목
- NAV.md 처음 40줄
