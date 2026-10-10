# 대상 코드베이스 출처

- 원본: `/home/jun/project/fream-back` (GitHub `junhyeong9812/fream-back`, 공개·별 0)
- 기준 커밋: `4fd438f` (2025-11-04, 2026-06-13 모듈리스 리팩토링 직전의 마지막 커밋)
- 복사 방식: `git archive 4fd438f` — 파일만, git 이력 없음. 추적 파일 911개.

## 제외·분리

| 대상 | 처리 | 개수 |
|---|---|---|
| `docs/`, 루트 `README.md` (사람이 쓴 문서) | `target-docs/fream-back/`으로 분리 보관 | 69 |
| `logs/` (커밋돼 있던 실행 로그) | 버림 | 5 |
| 그 밖 (`src/`, `docker/`, `gradle/`, 빌드 파일 등) | `target/fream-back/` | 837 |

원본 작업 트리의 `.claude/`·`.idea/`·`build/`·`.gradle/`은 추적 파일이 아니라 처음부터 포함되지 않는다.

## 수선

- `patches/0001-build-fix.patch`: `git diff -M 4fd438f aaa7871 -- src/main`. 리팩토링 첫 커밋 `aaa7871`에서 컴파일·기동 차단 해소 부분만 옮겼다(Spring Modulith 의존성·`build.gradle` 변경·그 커밋의 테스트는 제외).
  - 패키지 선언과 위치가 다른 파일 5개 이동(accessLog 아스펙트 3, IpBlocking 2)
  - `OrderQueryController` 죽은 import 제거, `getUpdatedAt` → `getModifiedDate`
  - `AddressLoggingAspect` 람다 캡처(`final int index`)
  - `SecurityConfig` `LoginAuthenticationFilter(authService)` 와이어링
  - `PaymentRepository` JPQL `createdAt` → `createdDate`
  - `application-test.yml` H2 MySQL 호환 모드
- 수선 전 `compileJava`: `OrderQueryController` cannot find symbol 등 실패. 수선 후 성공(JDK 17.0.13).

## 알려진 한계

- 전체 앱 기동(`FreamBackApplicationTests.contextLoads`)은 외부 서버 없이 돌지 않는다. 그대로 두고 확인 스크립트에서 제외한다.
  - 기본 프로필(`prod`)에서 `RedisConfig.stringRedisTemplate`이 Boot 자동 설정 빈과 이름이 겹쳐 `BeanDefinitionOverrideException`(2025-08-17 `c91c9e9`부터, 리팩토링 브랜치에서도 미수정).
  - 덮어쓰기를 허용해도 Kafka `bootstrap-servers` 등 외부 설정이 없어 실패한다.
- 테스트 기반은 JPA 슬라이스(`@DataJpaTest` + `test` 프로필, H2 MySQL 모드)와 Mockito 단위 테스트다.
