#!/usr/bin/env bash
# 대상 코드베이스(target/fream-back) 빌드 + 기존 동작 고정 테스트 확인.
# 전체 앱 기동 테스트(FreamBackApplicationTests)는 외부 서버가 필요해 제외한다(target/SOURCE.md).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="${TARGET:-$ROOT/target/fream-back}"
export JAVA_HOME="${JAVA_HOME_17:-$HOME/.sdkman/candidates/java/17.0.13-tem}"

cd "$TARGET"
./gradlew --offline clean compileJava compileTestJava -q
./gradlew --offline test --tests 'com.fream.back.domain.*'

python3 - "$TARGET/build/test-results/test" <<'PY'
import sys, glob, re
t = f = e = s = 0
for p in glob.glob(sys.argv[1] + "/*.xml"):
    h = open(p, encoding="utf-8").read(2000)
    m = re.search(r'tests="(\d+)" skipped="(\d+)" failures="(\d+)" errors="(\d+)"', h)
    t += int(m[1]); s += int(m[2]); f += int(m[3]); e += int(m[4])
print(f"check-target: tests={t} failures={f} errors={e} skipped={s}")
sys.exit(1 if (f or e or t == 0) else 0)
PY
