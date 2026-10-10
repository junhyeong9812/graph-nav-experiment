# graph-nav-experiment

큰 레거시 코드베이스에서 **코드·파일 탐색 그래프를 미리 주면 헤드리스 코딩 에이전트(`claude -p`)의 탐색 비용이 줄고, 코드베이스를 더 빨리 이해하는가**를 재는 실험장이다.

[compare-issue-archive](https://github.com/junhyeong9812/compare-issue-archive)의 결론 4번(미검증 가설)을 이어받는다. 그 실험은 이슈·결정 기록의 가치를 쟀고, 이 실험은 구조 지도의 가치를 잰다.

## 구조

```
target/fream-back/   실험 대상 — fream-back 4fd438f(리팩토링 전) 스냅샷 + 빌드 수선 + 기존 동작 고정 테스트
target/SOURCE.md     출처·제외 목록·수선 내역·알려진 한계
target/patches/      빌드 수선 패치
target-docs/         원본에 있던 사람이 쓴 문서(조건 재료, 에이전트 기본 사본에는 없음)
scripts/check-target.sh   빌드 + 테스트 확인
docs/                작업 기록·의심 지점 목록(에이전트에게 주지 않음)
```

## 확인

```
bash scripts/check-target.sh   # JDK 17(~/.sdkman/candidates/java/17.0.13-tem), Gradle 의존성 캐시 필요
```

## 진행

- 2026-10-10 대상 준비 완료: 본 코드 Java 약 8만 줄, 고정 테스트 294개. 다음은 실험 설계(`docs/plans/NEXT.md`).
