# log — rejudge-05

## 타임라인

| 시각 | 사건 | 결과 |
|---|---|---|
| 2026-10-12 | 사용자 "진행하자" + 선택 "죽은 경로 테스트만 빼고 재판정". 05 실패 행: sonnet 4, haiku 2, var-obfuscated 6, var-inflated 1, var-inflated-obfuscated 6, var-misnamed 4, gloss-obfuscated 3 — 전부 `failed/` 사본 있음 | 명세 작성 → 사용자 "진행하자" → SPEC=1·MODE=auto. 브랜치 `feat/rejudge-05`(main 511d2c7) |
| 2026-10-13 | `scripts/rejudge_05.py` 기록 기반: 05 실패 26 → v2 성공 4(Sonnet LF r1·r2, Haiku LF r1·r3), 나머지 22는 알림 테스트 실패 | 앞 분석의 보정값과 일치 |
| 2026-10-13 | 실측 대조(@Disabled 3개 v2 oracle, failed/ 사본 재판정) 6건: 후보 4 성공·유지 2 실패 — 기록 기반과 전부 일치 | load-bearing 가정(hidden_failed_tests = 전체 목록) 확인 |
| 2026-10-13 | 문서: analysis.md 판정 결함 절에 v2 결과, 변형·이름 분석에 "수치 불변" 1줄 | |

## 리뷰 ledger

## 생략한 검증

## 완료 요약

- v2(죽은 경로 hidden 3개 제외) 재판정: 26건 중 4건 성공 전환, 모두 LF. 실측 6건 일치. 원 runs.jsonl·oracle 무변경, 결과는 `results-*/rejudge-05.jsonl`.
