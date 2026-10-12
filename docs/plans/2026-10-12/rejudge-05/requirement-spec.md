# 요구사항 명세서 — 과제 05 재판정 (죽은 경로 테스트 제외)

> 작성일: 2026-10-12 · 작업 폴더: `docs/plans/2026-10-12/rejudge-05/`

## 0. 요구사항 원문 (인터뷰 기록)

- **원문:** "진행하자" → 선택: "죽은 경로 테스트만 빼고 재판정"
- **배경:** 과제 05의 hidden 테스트 3개(`periodicTracking*`)는 `updateShipmentStatuses()`를 부른다. 이 메서드는 대상 코드에서 호출처가 0인 죽은 코드다(`docs/analysis.md` '판정 결함: 과제 05').

## 1. 목표·대상 (필수)

- **대상:** `graph-nav-experiment`, 새 브랜치 `feat/rejudge-05`(main에서 분기)
- **재판정 규칙:** 05 hidden에서 `periodicTrackingUpdateRewardsWhenDelivered`, `periodicTrackingUpdateRewardsEachDeliveredOrderSeparately`, `periodicTrackingUpdateDoesNotRewardBeforeDelivery` 3개를 뺀 판정(05 v2)을 정의한다.
- **적용 범위:** 모든 결과 폴더의 05 실행이다(본 실험·변형·이름 실험, 파일럿 제외).
- **산출물:**
  - 결과 폴더마다 `rejudge-05.jsonl`: run_id, 원 성공, v2 성공, 근거
  - 분석 문서 정정: `analysis.md` 보정값 확정, 변형·이름 분석의 05 수치 영향 여부
- **끝의 정의:** 모든 05 실패 행의 v2 판정이 기록되고, 문서의 05 관련 수치가 v2 기준으로 병기된다.

## 2. 경계·불변식 (필수)

- 원 판정(`runs.jsonl`)과 oracle 원본은 바꾸지 않는다. v2는 별도 파일·별도 열로만 둔다.
- **판정 방법:**
  - 1순위(기록 기반): 원 판정의 `hidden_failed_tests`가 위 3개뿐이고, `test_pass`·`regression_pass`가 참이면 v2 성공이다. 실패 목록이 비어 있으면(컴파일 오류 등) v2도 실패다.
  - 실제 재실행 대조: v2 oracle 사본(3개 제거)으로 `failed/` 보관 사본을 다시 판정해 기록 기반 결과와 일치하는지 확인한다. v2 성공 후보 전부와, 실패 유지 표본 2건이 대상이다.
- 성공 → 실패로 바뀌는 경우는 없다(테스트를 빼기만 하므로). 나오면 중단하고 보고한다.

## 3. 기준소스 (필수)

- `results-*/runs.jsonl`의 05 행, `results-*/failed/<run_id>/`(로컬, gitignore)
- `oracle/05-purchase-reward/hidden/`, 변형별 `variants/*/oracle/05-*/hidden/`

## 4. 금지영역 (필수)

- 기존 `results-*/runs.jsonl`·`grades.jsonl`, `oracle/`·`cases/`·`variants/*` 원본, `target/`은 수정하지 않는다.
- study-note는 읽지도 수정하지도 않는다. main에서 직접 작업하지 않는다. push는 확인 후에만 한다.

## 5. 검증 방법 (필수)

- 05 실패 행 수와 `rejudge-05.jsonl` 행 수를 대조한다.
- 실제 재실행 대조: 위 2의 표본에서 기록 기반 판정과 실제 판정이 같아야 한다.
- 문서 수치를 표에서 재계산한다(스크립트 출력에서 복사).

## 6. stakes (필수)

- **낮음.** 원 데이터를 바꾸지 않는 추가 판정이고, 결론의 방향을 바꿀 가능성이 낮다(05 실패는 대부분 알림 종류 문제라 v2에서도 실패).

## 7. 자율성

- [x] auto

## 8. load-bearing 가정

1. `hidden_failed_tests`는 실패한 hidden 테스트의 전체 목록이다. 실제 재실행 대조로 확인한다.

## 9. task 분해

| task | 목표 | acceptance |
|---|---|---|
| 01 | `scripts/rejudge_05.py`: 기록 기반 v2 판정 + 실제 재실행 대조 옵션 | 실패 행 수 = 출력 행 수 |
| 02 | 실제 재실행 대조 | 기록 기반과 일치 |
| 03 | 문서 정정·커밋 | 분석 문서에 v2 병기 |

## 승인 상태

- [x] 필수 6칸 전부 기입
- [ ] 사용자 합의 → SPEC=1
- [ ] 자율성 선택 → MODE 기록
