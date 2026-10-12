# log — naming-followup

## 타임라인

| 시각 | 사건 | 결과 |
|---|---|---|
| 2026-10-12 | 사용자 "후속 실험 진행해보자" → 인터뷰 4문항(범위 7·8 둘 다, 대응표 3수준, 도메인끼리 맞바꾸기, 원래 크기 Sonnet 5회) | 대응표 크기 실측: GD 276B, GA 8,929B(지도 인덱스 6,344B보다 큼) |
| 2026-10-12 | 명세 작성 → 사용자 "진행하자" → SPEC=1·MODE=auto. 브랜치 `feat/naming-followup` | |
| 2026-10-12 | `make_variant.py misnamed`: 도메인 18개(event 제외 — 구조 단어 Event와 겹침) 9쌍 맞바꾸기, 타입 485·패키지 248 매핑. 예: notification↔payment(`NotificationType`→`PaymentType`), order↔faq, shipment↔address, product↔user | 짝수라 3자 순환 없음 |
| 2026-10-12 | 대응표 생성 `make_glossary.py obfuscated`: gloss-dom 539B(헤더 포함), gloss-ent 2,297B(어간 78), gloss-all 10,342B(어간 384). 러너 조건 GD·GE·GA(CLAUDE.md만, nav 없음) dry-run 확인 | |
| 2026-10-12 | 선행 연구 조사 워커(general-purpose, 웹 검색) 회수 — 사용자 질문 "이미 측정한 사람이 있어?" | 이름 제거→정답률 하락은 기존 연구 다수. 크기 통제 측정·이름 손실 vs 비용 분리·지도=이름 보상 해석은 선행 미발견 |
| 2026-10-12 | misnamed 판정 01~06 전부 실패: main 컴파일 오류. 원인 = `Mapper.map_text`가 FQ 경로 치환 결과를 타입 이름 단계에서 한 번 더 치환(FAQ→Order→Faq). 난독화는 새 이름이 옛 이름과 겹치지 않아 무해했음 | 경로 치환 결과를 자리표시자로 보호. obfuscated 재생성 diff 0(기존 변형 불변 확인) |
| 2026-10-12 | 재검증: JPA 테스트 대량 실패 — 이름 없는 `@Table(indexes=…)` 엔티티(Notice)가 WarehouseStorage로 바뀌며 암묵 테이블 이름이 진짜 warehouse_storage와 충돌 | misnamed에서만 이름 없는 @Table에 원래 name 삽입(같은 줄, 줄 수 보존). 첫 시도는 @Index(name=…)를 @Table name으로 오인 → `@Table\(\s*name` 으로 정정 |
| 2026-10-12 | misnamed 검증 통과: 판정 01~06 ok(원본 hidden 실패, fix 적용 시 visible·hidden·회귀 통과), 재생성 diff 0, 80,048줄. 자동 지도 NAV 7,616B·nav 20파일(예: `payment … api: PaymentCommandController /notifications`) | |
| 2026-10-12 | 대응표 실행 시작: obfuscated GD·GE·GA × 8 × 5 = 120, 병렬 5. 끝나면 misnamed B·AC 80 자동 연쇄 | |
| 2026-10-12 | 커밋 ed7cbbb(코드), bae5bb3(변형·대응표 데이터). 실행 시작은 커밋 전 작업 트리(내용 동일) | |
| 2026-10-12 11:11 | 대응표 120회 완료(무효 0·감사 0·회귀 오류 0). 채점 30건 | |
| 2026-10-12 13:46 | misnamed 80회 완료(무효 0·감사 0·회귀 오류 0). 채점 20건(파싱 오류 0) | |
| 2026-10-12 | 집계 `scripts/analyze_naming.py` → `results-naming-analysis.md`. 커밋 ab12412(코드), cdb60ed(데이터, `results-gloss-*/failed/` gitignore) | |
| 2026-10-12 | 분석 `docs/analysis-naming.md`. 대조 중 과장 2곳 정정: 토큰 범위 상한 1.27 → 1.36(부풀림+난독화 AC), "실패는 모두 경계 넘기" → "정답률 차이는 대부분"(03은 전 조건 실패) | |
| 2026-10-12 | 사이클 마감 ②: study-note 금지영역 → 이슈 아카이브 보류 | |

## 리뷰 ledger

## 생략한 검증

## 완료 요약

- **A 대응표:** GD 539B → 성공 24/30(05 5/5), AC 지도 25/30. GE 24·GA 24, GA 토큰 1.16 [1.01–1.34]. 어떤 조건도 B보다 싸지 않음.
- **B 틀린 이름:** B 토큰 ÷원본B 1.03, 성공 22/30(원본 25, 난독화 21), AC 24/30·AC÷B 0.97. 05 성패 = BENEFIT 발견 여부(난독화와 같은 기전).
- **변경:** `scripts/make_variant.py`

  ```python
          held = []
          def hold(t: str) -> str:
              held.append(t)
              return f"\x00{len(held) - 1}\x00"
          s = self.fq.sub(lambda m: hold(self._fq(m)), s)
  ```

  misnamed 모드(`build_misnamed_mapping`, 이름 없는 `@Table`에 name 고정), `make_glossary.py`, 러너 GD·GE·GA, `analyze_naming.py`.
- **커밋:** ed7cbbb·bae5bb3·ab12412·cdb60ed + 이 docs 커밋. push 안 함.
