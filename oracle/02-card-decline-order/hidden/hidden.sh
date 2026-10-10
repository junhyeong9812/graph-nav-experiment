#!/usr/bin/env bash
# 판정: 정답이 의도적으로 바꾸는 동작을 고정한 기존 테스트를 지우고
#       기존 고정 테스트 + hidden + 보이는 테스트를 돌린 exit code를 그대로 낸다.
# 지울 목록은 oracle 쪽 regression-exclude.txt와 같다(사본에 덮어씌워져도 동작하도록 여기에 그대로 둔다).
set -uo pipefail
ROOT="$(pwd)"
if [ ! -x "$ROOT/gradlew" ]; then ROOT="$(cd "$(dirname "$0")" && pwd)"; fi
cd "$ROOT"
EXCLUDE=(
  com.fream.back.domain.payment.PaymentCommandServiceTest
  com.fream.back.domain.payment.PaymentEntityStateTest
  com.fream.back.domain.payment.PaymentRepositoryTest
)
for fqcn in "${EXCLUDE[@]}"; do
  rm -f "src/test/java/${fqcn//.//}.java"
done
./gradlew test --tests 'com.fream.back.domain.*' --tests 'com.fream.back.hidden.*' --tests 'com.fream.back.visible.*'
exit $?
