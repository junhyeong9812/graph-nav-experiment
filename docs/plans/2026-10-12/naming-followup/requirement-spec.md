# 요구사항 명세서 — 이름 후속 실험 (대응표 vs 지도, 틀린 이름 변형)

> 작성일: 2026-10-12 · 작업 폴더: `docs/plans/2026-10-12/naming-followup/`

---

## 0. 요구사항 원문 (인터뷰 기록)

- **원문:** "후속 실험 진행해보자" (NEXT 7·8, 앞 결론 "아키텍처는 도메인 안 비용, 이름은 경계 넘기"의 후속)
- **Q/A:**
  - 범위 → **7·8 둘 다**
  - 대응표 내용 → **세 수준 모두, 조건으로 나눠 비교**
    - GD: 도메인 패키지만(19줄, 276B)
    - GE: 도메인 + 엔티티·enum 어간
    - GA: 업무 어간 전부(384개, 8.9KB)
  - 틀린 이름 → **도메인끼리 이름 맞바꾸기**
  - 규모 → **원래 크기(8만 줄)만, Sonnet 5회**

## 1. 목표·대상 (필수)

- **대상:** `graph-nav-experiment`, 새 브랜치 `feat/naming-followup`(`feat/size-structure-variants`에서 분기)
- **실험 7 — 대응표 vs 지도:** 난독화 변형(`obfuscated`)에 대응표 3종을 CLAUDE.md로 주는 조건 GD·GE·GA를 추가한다.
  - 규모: 8과제 × 3조건 × 5회 = 120회
  - 비교: 기존 `results-var-obfuscated` B·AC와 비교한다.
- **실험 8 — 틀린 이름:** 새 변형 `misnamed`를 만든다.
  - 도메인 업무어를 짝지어 서로 바꾼다. 예: `Notification` ↔ `Payment`이면 `NotificationType` → `PaymentType`, `domain/notification` → `domain/payment`가 되고, 반대쪽도 같이 바뀐다.
  - 조건 B·AC × 8과제 × 5회 = 80회. 자동 지도는 이 변형으로 다시 만든다.
- **끝의 정의:** 분석 문서에 다음을 쓴다.
  - (7) 대응표가 지도만큼 정답률을 되찾는가, 고정비는 얼마인가
  - (8) 틀린 이름이 의미 없는 이름(obfuscated)·원본 대비 비용과 성공을 어떻게 바꾸는가
  - 두 실험 최종 결론(README) 갱신

## 2. 경계·불변식 (필수)

- **동작 보존:** `misnamed` 변형은 원본과 동작이 같다.
  - 고정 테스트 294/294
  - 과제 01~06: 원본 상태에서는 hidden 실패, 정답 패치(매핑 적용본)를 적용하면 통과
  - 같은 시드로 다시 생성하면 diff 0
- **틀린 이름 규칙:**
  - 도메인 19개의 업무어를 시드 고정으로 짝짓는다. 홀수라 하나는 3자 순환이다.
  - 타입 이름의 CamelCase 단어열과 도메인 패키지 이름에서, 단어가 통째로 일치할 때만 동시에 치환한다(순열이므로 충돌 없음).
  - 메서드·필드·URL·문자열·enum 값은 유지한다(obfuscated와 같은 범위). 주석은 제거한다(업무 서술이 이름과 모순되는 단서가 되지 않게 — obfuscated와 같은 처리).
  - 라이브러리 타입과 비업무 공용 타입(global)은 바꾸지 않는다.
- **대응표:**
  - `variants/obfuscated/MAPPING.json`에서 결정적으로 생성한다. 형식은 `무의미 이름 = 원래 업무 이름`, 한 줄에 하나다.
  - GA(8.9KB)는 지도 인덱스 상한(8,192B) 대상이 아니다. 대응표는 지도가 아니므로 상한을 대응표 조건에만 풀고, 크기를 그대로 기록한다.
  - 대응표 조건은 nav 디렉터리 없이 CLAUDE.md 한 파일만 둔다. 프롬프트는 B와 같다.
- **매핑표 비공개:** MAPPING.json 자체는 에이전트에게 주지 않는다(감사 패턴 유지). 대응표는 조건으로 의도해 주는 파일이다.
- **격리·감사·판정:** ADR-0001 D5·D6과 기존 러너를 그대로 쓴다.

## 3. 기준소스 (필수)

- `variants/obfuscated/`(대상·과제·oracle·MAPPING)
- `scripts/make_variant.py`(난독화 기계 재사용)
- 원본 `target/fream-back`, `cases/`, `oracle/`
- 기존 결과 `results-var-obfuscated`, `results-var-original` + `results-sonnet`(B·AC)

## 4. 금지영역 (필수)

- 수정 금지:
  - `target/fream-back`, `target-docs/`
  - `cases/`·`oracle/` 원본, `graphs/` 기존 지도
  - 기존 `variants/*`(obfuscated 포함), 기존 `results-*`
  - 대응표는 `variants/obfuscated/` 밖 새 경로에 둔다.
- 로컬 fream-back 원본, compare-issue-archive, study-note를 읽거나 수정하지 않는다.
- push·원격 생성 없음. main 직접 작업 금지.

## 5. 검증 방법 (필수)

- **misnamed:**
  - `verify_variant.py misnamed`: 판정 01~06 ok, 크기, grep 잡음
  - 고정 테스트 294/294
  - 재생성 diff 0
  - 표본 확인: `NotificationType` 등 과제 05 관련 이름이 다른 도메인 이름으로 바뀌었는지
- **대응표:** 행 수 확인(GD 19, GA 384), 무의미 이름이 실제 사본에 존재하는지 표본 대조
- **러너:** dry-run으로 GD·GE·GA 사본 구성 확인(CLAUDE.md만 있고 nav 없음)
- **실행:** 유효 행 수 대조, 무효 행 재실행, 질문 채점

## 6. stakes (필수)

- **판정: 낮음.** 실험 저장소 안의 새 파일·새 브랜치다. 원본과 기존 변형은 불변이다. 동작 보존은 고정 테스트와 정답 패치 판정이 막는다.

---

## 7. 자율성

- [x] auto (직전 작업과 동일, 합의 시)
- [ ] lazy

## 8. load-bearing 가정 (1~2개)

1. **단어 단위 맞바꾸기가 컴파일된다.** 순열 치환이라 이름 충돌이 없고, 기존 난독화 기계(Lombok Builder·Spring Data Impl·`@Table` 고정·Q타입)가 그대로 적용된다. 착수 직후 빌드와 294 테스트로 스모크한다.
2. **의미 없는 이름과 틀린 이름은 에이전트에게 다르게 작용한다.** 틀린 이름은 grep을 엉뚱한 도메인으로 안내할 것이라는 가정이다. 결과가 obfuscated와 같다면 그것도 결론이다(가정이 아니라 측정 대상).

## 9. task 분해

| task | 목표 | 의존 | acceptance |
|------|------|------|-----------|
| 01 | 대응표 3종 생성 스크립트 + 러너 조건 GD·GE·GA | — | dry-run 구성 확인 |
| 02 | `make_variant.py`에 misnamed 모드(도메인 단어 순열) | — | 재생성 diff 0 |
| 03 | misnamed 생성·검증 + 자동 지도 | 02 | 294 green, 판정 01~06 ok, NAV ≤ 8,192B |
| 04 | 본 실행: obfuscated GD·GE·GA 120 + misnamed B·AC 80 | 01·03 | 200행 유효 |
| 05 | 채점·분석·문서(README 최종 결론 갱신) | 04 | `docs/analysis-naming.md` |

---

## 승인 상태

- [x] 필수 6칸 전부 기입
- [ ] 사용자 합의 → SPEC=1
- [ ] 자율성 선택 → MODE 기록
