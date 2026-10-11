#!/usr/bin/env bash
# 보이는 테스트만 실행한다.
set -uo pipefail
cd "$(dirname "$0")"
./gradlew test --tests 'com.fream.back.visible.c01.*'
