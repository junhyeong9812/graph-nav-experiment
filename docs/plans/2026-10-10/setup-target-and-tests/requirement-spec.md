# 요구사항 명세서 (requirement-spec)

> 작성일: 2026-10-10 · 작업 폴더: `docs/plans/2026-10-10/setup-target-and-tests/`
> 상위 목표: "코드·파일 탐색 그래프가 있으면 큰 레거시 코드베이스에서 에이전트의 탐색 비용이 줄어드는가"(compare-issue-archive README 결론 4번에서 넘어온 주제). 이번 작업은 그 실험의 **대상 코드베이스 준비**만 한다.

---

## 0. 요구사항 원문 (인터뷰 기록)

- 원문: "graph-nav-experiment로 project 폴더에 만들고 시작하자 그리고 프로젝트 내 대상이 되는 프로젝트인 fream-back을 복사해놔줘. 그리고 거기에 테스트를 만들자."
- Q/A:
  - 기준 커밋 = "작년 6월에 마무리된 코드베이스, 리팩토링 안 된 형태" → 확인 결과 **4fd438f (2025-11-04, 2026-06 리팩토링 직전 마지막 커밋)**.
  - 이력 = 포함하지 않는다(파일만 복사, 출처 SHA 기록).
  - 기존 문서·설정 = **코드 사본에서 빼고 따로 보관**(사람이 쓴 구조 지도 조건의 재료). `.claude`·`.idea`·`logs`·`build`·`.gradle`은 버린다.
  - 테스트 범위 = **빌드 green + 테스트 기반**(H2만으로 도는 기존 동작 고정 테스트). 실험 과제 케이스·숨긴 판정 테스트는 다음 작업.
  - 외부 의존 = **H2·모의 객체로만**. 의존성 다운로드·JDK 17 설치 허용.
  - 빌드 수선 = **컴파일·기동 차단만 최소 수선**(aaa7871의 빌드 고침 부분만, Modulith 도입·그 테스트 제외).
  - 저장소 = git init + 작업 브랜치. GitHub 원격은 나중에 따로 확인.
  - stakes 낮음 · auto.

## 1. 목표·대상 (필수)

1. `/home/jun/project/graph-nav-experiment/` 저장소 생성(git init, 작업 브랜치 `feat/setup-target`).
2. `target/fream-back/`: fream-back `4fd438f`의 추적 파일 스냅샷(`git archive`). 아래를 제외한다.
   - 사람이 쓴 문서: `docs/`, 루트 `*.md`(README 등) → `target-docs/fream-back/`으로 따로 보관
   - 도구·산출물: `.claude/`, `.idea/`, `logs/`, `build/`, `.gradle/`, `*.log`
   - 출처는 `target/SOURCE.md`에 기록(원본 경로·SHA·날짜·제외 목록·수선 내역).
3. 빌드 수선: aaa7871 중 컴파일·기동 차단 해소 부분만 이식. 수선 diff는 `target/patches/0001-build-fix.patch`로도 보관.
4. 테스트 기반: 외부 서버 없이(H2·Mockito) 도는 기존 동작 고정(characterization) 테스트를 주요 도메인에 추가.
   - 대상 도메인(코드 규모 상위·핵심 흐름): user, product, order, payment, sale·trade, style, address, notification 중 **최소 6개**.
   - 도메인당 서비스·리포지토리·컨트롤러 슬라이스 중 실제 동작을 고정하는 테스트. 현재 동작이 버그처럼 보여도 **현재 동작을 고정**한다(고치지 않음, 의심 지점은 목록으로 보고).
5. `scripts/check-target.sh`: 스냅샷 빌드 + 전체 테스트를 한 번에 돌리는 확인 스크립트.

## 2. 경계·불변식 (필수)

- 원본 `/home/jun/project/fream-back`은 읽기만 한다(작업 트리·브랜치·커밋 변경 금지. 읽기는 `git archive`·`git show`로).
- 본 코드(`src/main`) 변경은 빌드 수선(aaa7871 해당 부분)뿐이다. 구조·동작 변경 금지.
- 테스트는 외부 네트워크·Redis·Kafka·ES·MySQL 없이 통과한다. 반복 실행에서 결과가 같다.
- 테스트 이름·주석에 "나중 실험 과제"에 대한 힌트를 넣지 않는다(과제는 아직 없다).
- 다운로드는 Gradle 의존성(Maven Central 등)과 JDK 17뿐이다.

## 3. 기준소스 (필수)

fream-back `4fd438f`(스냅샷), `aaa7871`(빌드 수선의 근거), 스냅샷 코드 자체(테스트가 고정할 현재 동작).

## 4. 금지영역 (필수)

- fream-back 원본 수정.
- study-note 읽기·수정(워커 포함).
- compare-issue-archive 수정.
- push·GitHub 저장소 생성(사용자 확인 후).

## 5. 검증 방법 (필수)

- 스냅샷: 원본 `git ls-tree 4fd438f`와 파일 목록 비교(제외 목록 외 누락 0).
- 빌드 수선: 수선 전 컴파일 실패 원문 → 수선 후 `compileJava` 성공. 수선 diff가 aaa7871 해당 hunk와 같은지 비교.
- 기동 차단 해소 확인: 기존 `FreamBackApplicationTests`(컨텍스트 로드)가 외부 서버 없이 도는지 확인. 안 되면 테스트 프로필 설정으로 해결하고 그 내용을 기록(본 코드 변경 아님).
- 테스트: `scripts/check-target.sh` 2회 연속 green, 테스트 수 보고. 각 도메인 테스트 1개 이상을 일부러 깨 보고(본 코드 한 줄 변경 → 실패 → 원복) 실제로 동작을 잡는지 확인.

## 6. stakes (필수)

- 판정: **낮음** — 로컬 실험 준비, 원본은 읽기만, 재생성 가능.

---

## 7. 자율성

- [x] auto
- [ ] lazy

## 8. load-bearing 가정

1. aaa7871의 빌드 고침만으로 4fd438f가 컴파일되고(그 사이 커밋이 없으므로), 테스트 프로필로 외부 서버 없이 컨텍스트가 뜬다. 착수 직후 빌드로 실증한다.
2. 주요 도메인 로직 상당수가 H2·Mockito로 고정 가능한 형태다(외부 서버 호출이 서비스 깊숙이 섞여 있으면 범위를 줄이고 보고).

## 9. task 분해

| task | 목표 | acceptance |
|------|------|-----------|
| 01 | 저장소·스냅샷·문서 분리·SOURCE.md | 파일 목록 비교 |
| 02 | JDK 17·빌드 수선 | compileJava 성공, 수선 diff 대조 |
| 03 | 컨텍스트 로드·테스트 프로필 | 기존 테스트 green |
| 04 | 도메인 특성화 테스트(워커 Opus 병렬 ≤5, 도메인 분담) | 도메인별 깨 보기 확인 |
| 05 | check-target.sh 2회 green, 커밋, log 완료 요약 | |
