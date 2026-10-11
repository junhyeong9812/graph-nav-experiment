# log — size-structure-variants (fream 변형 2×2)

## 타임라인

| 시각 | 사건 | 결과/결정 |
|------|------|----------|
| 2026-10-11 | 인터뷰 2회(2×2·B+자동·Sonnet 5회·접두어 미끼·타입+패키지+주석 제거) → 명세 합의 "진행하자" | SPEC=1·MODE=auto, 브랜치 feat/size-structure-variants |
| 2026-10-11 | 가정 1 스모크: obfuscated 변형 294/294 green. 수정 3회: Lombok <타입>Builder, Spring Data <저장소>Impl 이름 규칙(업무 어간 공유로 해결), 암묵 테이블 이름 고정(@Entity 같은 줄에 @Table, 단일 테이블 상속 하위는 제외) | 가정 1 확인 |


| 2026-10-11 | obfuscated 판정 검증: 01~06 원본 hidden 실패 / 정답 패치 test·hidden·회귀 통과(judge_ok). 누출 45건 = 전부 라이브러리 타입(querydsl Order·java.util.Collection·security User). grep 잡음 원본 대비 0.65~0.94배 | 구조 접미어만 남은 이름(EventSearchRepository 등)은 업무 단어 Event가 구조 단어와 겹쳐 남음 — 한계로 기록 |
| 2026-10-11 | 가정 2: 전 타입 복제 미끼는 global·진짜 타입과 섞여 컴파일 실패(AdminGender→Gender) → **미끼는 행위 계층만 복제, 데이터 타입(entity·dto·exception·event·enum·global 참조)은 공유**로 변경. 와일드카드 import 처리, 컴파일 반복으로 공유 추가 SortOption(scripts/variant-shared-extra.txt). 접두어 4→5종(4.34배 맞추려고) | inflated 294/294, 34.7만 줄(4.34배), 파일 2,426, grep 잡음 약 4.5~5배 |
| 2026-10-11 | inflated·inflated-obfuscated 판정 검증: 01~06 모두 원본 hidden 실패 / 정답 test·hidden·회귀 통과. obfuscated 재생성 diff 0(결정적, 테스트 산출물 제외) | |
| 2026-10-11 | 미끼만 고친 패치(06 → AdminSaleBidCommandService·AdminInterestRepository) → hidden 실패 확인. 05는 원인 클래스(OrderShipmentCommandService)가 global이 참조해 공유 타입 → 미끼 복제본 없음. 02·04 원인도 엔티티(공유) | 한계: 원인이 공유 타입인 과제(02·04·05 일부)는 미끼가 원인 파일을 복제하지 않고 호출 측 잡음만 늘린다 |
| 2026-10-11 | navgraph: 도메인 114개(부풀림)에서 NAV 8KB 초과 → 압축 단계 7~9 추가(의존 도메인은 개수만, msg 생략, 9는 api·ent 생략). 원본 지도 재생성 diff 0. NAV: obfuscated 6,344B, inflated 5,247B, inflated-obfuscated 3,879B, nav 115파일 | 큰 코드베이스일수록 인덱스가 거칠어진다 — 크기 효과의 일부로 해석 |
| 2026-10-11 | 대기 루프 오류: `until ! pgrep -f "verify_variant.py ..."`가 자기 명령줄과 일치해 끝나지 않음(검증은 이미 완료) → 대기 프로세스 종료 | 대기는 실행 명령을 run_in_background로 직접 걸 것 |
| 2026-10-11 | 코드 커밋 33f1056. 본 실행 연쇄 시작: original B·AC r4·r5(32) → obfuscated → inflated → inflated-obfuscated(각 80), Sonnet, 병렬 5 | |
| 2026-10-12 00:06 | 본 실행 272회 완료(original 32·obfuscated/inflated/inflated-obfuscated 각 80). 무효 0·감사 위반 0·회귀 판정 오류 0 | |
| 2026-10-12 | `grade_answers.py`가 원본 과제·체크리스트만 읽음 → 행의 `variant`로 `set_variant` 호출 1줄 추가(매핑 적용 체크리스트로 채점). `scripts/analyze_variants.py` 작성 | |
| 2026-10-12 | 과제 05 실패 추적: 실패 hidden 3개는 모두 혜택 알림(`NotificationType.BENEFIT`) 검사. 실행마다 알림 enum 파일을 열었는지 = 성패와 완전 일치. obfuscated B 0/5(enum `Xkh73Type`을 아무도 안 엶, 기존 BID 알림 재사용) vs AC 4/5(지도 `m15 … /notifications` URL로 알림 도메인 식별 → enum 열람) | 판정 결함 아님(진짜 실패). 난독화 효과·지도 효과의 구체적 기전 |
| 2026-10-12 | 채점을 변형별 병렬 4개로 재시작(직렬 연쇄 중지, 채점된 건 건너뜀). 이전 세션의 자기일치 대기 루프 3개 정리 | |
| 2026-10-12 | 채점 68건 완료(중복 0·파싱 오류 0, $10.88). 코드 커밋 f87f6bf(채점 변형 지원·analyze_variants.py·.gitignore `results-var-*/failed/`), 데이터 커밋 f36315a | |
| 2026-10-12 | 분석 문서 `docs/analysis-size-structure.md`, README 후속 절, NEXT, measurement-log 갱신. 문서 수치는 `results-var-analysis.md`·원자료와 대조(비용 합계 $112.53으로 정정, 원본 enum 열람 서술을 실측 범위로 좁힘) | |
| 2026-10-12 | 사이클 마감 ②: study-note 쓰기는 금지영역 → 이슈 아카이브 보류(앞 작업과 같은 경로) | |
| 2026-10-12 | 사용자와 해석 논의 → 분석 문서에 '두 실험의 결론: 아키텍처와 이름의 역할' 절 추가(지엽성 = 도메인 안 비용, 이름 = 경계 넘기, 토큰 단독 지표의 함정, 미측정 범위). README 최종 결론, NEXT 7·8 추가 | 사용자 결론: "네이밍과 아키텍처의 중요성" — 순위가 아니라 역할 분담으로 기록 |

## 생략한 검증

## 완료 요약

- **결과:** 272회 유효. 크기 4.3배 → B 비용 1.27배, AC÷B 0.96 [0.79–1.09]. 이름 제거 → B 비용 0.95, 성공 25 → 21(둘 다 19), AC로 25·23 회복.
- **기전:** 과제 05 성패 = 알림 종류 enum 열람 여부. 지도 인덱스의 URL(`/notifications`)이 난독화된 이름의 의미를 되살렸다.
- **변경:** `scripts/grade_answers.py`

  ```python
      for r in todo:
          R.set_variant(r.get("variant", "original"))  # 변형 실행은 매핑 적용된 과제·체크리스트로 채점
          case = r["case"]
  ```

  신규 `scripts/make_variant.py`·`verify_variant.py`·`analyze_variants.py`, `run_experiment.py --variant`, navgraph 압축 단계 7~9.
- **커밋:** 33f1056·be73433·37904c2·f87f6bf·f36315a + 이 docs 커밋. push 안 함.
