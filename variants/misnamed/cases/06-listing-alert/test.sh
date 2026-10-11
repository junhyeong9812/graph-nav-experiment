#!/usr/bin/env bash
# 보이는 테스트만 실행한다.
set -euo pipefail
cd "$(dirname "$0")"
./gradlew test --tests 'com.fream.back.visible.t06.*'
