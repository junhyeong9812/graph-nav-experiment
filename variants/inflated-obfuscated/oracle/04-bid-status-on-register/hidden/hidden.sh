#!/usr/bin/env bash
# 판정: regression-exclude.txt에 적힌 기존 테스트를 지우고 hidden + 보이는 테스트 + 기존 고정 테스트를 돌린다.
# 작업 디렉터리: 대상 사본 루트(gradlew가 있는 곳)에서 실행한다. 아니면 이 스크립트가 있는 곳으로 간다.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
if [ ! -x ./gradlew ]; then cd "$HERE"; fi
EXCLUDE="${REGRESSION_EXCLUDE:-}"
if [ -z "$EXCLUDE" ]; then
  for c in "$HERE/../regression-exclude.txt" "$HERE/regression-exclude.txt" "./regression-exclude.txt"; do
    if [ -f "$c" ]; then EXCLUDE="$c"; break; fi
  done
fi
if [ -n "$EXCLUDE" ] && [ -f "$EXCLUDE" ]; then
  while IFS= read -r fqcn || [ -n "$fqcn" ]; do
    fqcn="$(echo "$fqcn" | tr -d '[:space:]')"
    [ -z "$fqcn" ] && continue
    rm -f "src/test/java/$(echo "$fqcn" | tr . /).java"
  done < "$EXCLUDE"
else
  echo "hidden.sh: regression-exclude.txt를 찾지 못함" >&2
  exit 2
fi
./gradlew test --tests 'com.fream.back.domain.*' --tests 'com.fream.back.hidden.*' --tests 'com.fream.back.visible.*'
exit $?
