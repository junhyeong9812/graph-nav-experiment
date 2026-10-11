# measurement-log

| 날짜 | 작업 | stakes | 워커(모델) | 결과 |
|---|---|---|---|---|
| 2026-10-10 | setup-target-and-tests | 낮음 | 4 (Opus, 도메인별 테스트) | fream-back 4fd438f 스냅샷 + 빌드 수선, 기존 동작 고정 테스트 293개(8도메인), check-target 2회 294/294, 의심 지점 41 |
| 2026-10-10~11 | build-graphs-cases-runner + 본 실험 | 낮음 | 5 (Opus: navgraph·과제 4) + 지도 생성 Opus·채점 Opus | 336회 유효, 가설 기각(÷B Sonnet 0.87~1.15, Haiku 1.10~1.22), 과제 05 판정 결함 |
| 2026-10-11~12 | size-structure-variants | 낮음 | 0 (메인 단독) + 실행 Sonnet·채점 Opus | 변형 3종(동작 보존 검증) × B·AC × 5회 + 원본 2회 추가 = 272회 유효. 크기 4.3배 → B 1.27배·AC÷B 0.96, 이름 제거 → 성공 25→21, 지도로 회복 |
