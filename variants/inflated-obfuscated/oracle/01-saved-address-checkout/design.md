# 과제 01 설계 메모 — 저장한 배송지로 즉시 구매 실패 (bug)

## 의도

증상은 **주문(즉시 구매)** 과 마이페이지 배송지 목록에서 보이지만, 원인은 **배송지 조회 서비스**와 **전역 유틸(개인정보 암호화)** 두 곳에 겹쳐 있다. 증상 지점에서 출발해 order → address → global/utils, 그리고 비교를 위해 user 도메인(회원 이메일 저장 형태)·배송지 등록 서비스까지 읽어야 원인을 다 찾을 수 있다.

## 원인 (코드 기준 SHA 82ec907의 target/fream-back)

1. `domain/m53/service/query/Xms98QueryService.java:37,74,119,164` — 조회 4개 메서드가 이메일을 결정적 암호화한 값으로 `userRepository.findByEmail`을 부른다. 회원 이메일은 평문으로 저장되고(가입·`Xms98CommandService:32,81,137`·`Xdq35:176` 모두 평문), 등록 서비스도 평문으로 찾는다. 그래서 조회는 항상 `Xkk16Exception`.
2. `global/utils/Xhr91Util.java:101-107,154-166` — `deterministicEncrypt`는 키를 `SHA-256(secretKey + 입력)`으로 만들고, `deterministicDecrypt`는 `secretKey`로 푼다. 입력마다 키가 달라 복호화가 원리상 불가능하다. 1을 고쳐도 다음 단계에서 `ADDRESS_QUERY_ERROR`(원인: "결정적 복호화 처리 중 오류").

즉시 구매 경로: `Xhq12CommandService.createInstantOrder`(:238) → `Xms98QueryService.getAddress` → 위 1·2.

## 정답 (`fix.patch`)

- 조회 서비스 4곳: 평문 이메일로 회원 조회.
- 결정적 암호화: 복호화와 같은 고정 키(`secretKey`)로 AES/ECB. 같은 입력은 같은 암호문(검색 유지), 복호화 가능.
- 대안 정답도 hidden은 통과해야 한다: 키를 `SHA-256(secretKey)`처럼 입력과 무관하게 고정하는 방식, 또는 결정적 필드를 다른 가역·결정적 방식으로 바꾸는 방식. hidden은 서비스 결과(원문 일치·검색·소유 범위)와 "DB에 원문이 아님"만 본다. 유틸 메서드를 직접 검사하지 않는다.

## 왜 탐색이 필요한가

- TASK는 "즉시 구매·배송지 목록 오류"만 말한다. 보이는 테스트도 주문 서비스 진입점과 배송지 등록만 쓴다.
- 첫 실패 메시지는 "사용자를 찾을 수 없습니다" → 배송지 조회 서비스. 왜 못 찾는지 알려면 회원 이메일이 어떻게 저장되는지(user 도메인, 등록 서비스)와 비교해야 한다.
- 그것을 고치면 두 번째 실패(`ADDRESS_QUERY_ERROR`, cause "결정적 복호화 처리 중 오류")가 나와 global/utils까지 가야 한다. 증상 한 개에 원인 두 개가 직렬로 있다.

## 순진한 오답

- 조회 서비스의 이메일만 고치고 끝냄 → 보이는 테스트가 여전히 실패(실행 확인: `주소 목록 조회 중 오류` ← `결정적 복호화 처리 중 오류` ← `BadPaddingException`).
- 조회 서비스에서 결정적 복호화 대신 원문을 그대로 돌려줌, 또는 등록 때 원문 저장 → hidden `storedRowsAreNotPlainText` 실패.
- `deterministicDecrypt`에서 예외를 삼키고 암호문을 그대로 반환 → 보이는 테스트 실패(값 불일치).
- 등록 쪽도 암호화 이메일로 찾도록 "통일" → 회원은 평문 저장이라 등록·수정이 깨짐(hidden setUp 실패).

## regression-exclude

- `com.fream.back.domain.m53.service.query.AddressEncryptionRoundTripTest`: 결정적 복호화 실패와 실데이터 조회 `ADDRESS_QUERY_ERROR`를 고정한다(정답이 바꾸는 동작).
- `com.fream.back.domain.m53.service.query.AddressQueryServiceTest`: 조회가 암호화한 이메일로 회원을 찾고 평문으로는 찾지 않음을 고정한다(`verify(..., never()).findByEmail("me@fream.com")`). 정답이 바꾸는 동작.

## 보이는 테스트의 배선

보이는 테스트(`visible/c01/SavedAddressCheckoutTest`)는 원인 클래스 이름을 쓰지 않으려고, 배송지 도메인 패키지 의존성은 스프링 빈 팩토리로 실제 생성하고(유틸은 그 의존성으로 자동 생성), 나머지 협력자는 목으로 채운다. 암호화 키·IV는 테스트 속성으로 준다(`application-test.yml`에 없음).

## 남는 관련 결함(과제 범위 밖, 판정에 넣지 않음)

- `Xms98CommandService.createAddress`가 새 주소를 `user.getAddresses()`에 넣지 않는다. 같은 영속성 컨텍스트에서 바로 목록을 조회하면 비어 보인다. 테스트는 요청 사이에 flush/clear 해서 이 영향을 받지 않는다.
