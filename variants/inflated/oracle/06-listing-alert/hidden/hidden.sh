#!/usr/bin/env bash
# 판정: hidden + 보이는 테스트 + 기존 고정 테스트 회귀. regression-exclude.txt에 적힌 테스트 클래스는 지운다.
set -uo pipefail
cd "$(dirname "$0")"
ROOT="$(pwd)"
EXCLUDE="${REGRESSION_EXCLUDE:-}"
if [ -z "$EXCLUDE" ]; then
  for c in "$ROOT/regression-exclude.txt" "$ROOT/hidden/regression-exclude.txt" "$(dirname "$ROOT")/regression-exclude.txt"; do
    if [ -f "$c" ]; then EXCLUDE="$c"; break; fi
  done
fi
if [ -n "$EXCLUDE" ] && [ -f "$EXCLUDE" ]; then
  while IFS= read -r fqcn || [ -n "$fqcn" ]; do
    fqcn="$(echo "$fqcn" | tr -d '[:space:]')"
    [ -z "$fqcn" ] && continue
    rm -f "src/test/java/$(echo "$fqcn" | tr . /).java"
  done < "$EXCLUDE"
fi
./gradlew test --tests 'com.fream.back.domain.*' --tests 'com.fream.back.hidden.*' --tests 'com.fream.back.visible.*'
exit $?
