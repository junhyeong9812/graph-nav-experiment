#!/usr/bin/env bash
# navgraph: target/fream-back 소스를 읽어 graphs/auto/ 를 다시 만든다.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
export JAVA_HOME="${NAVGRAPH_JAVA_HOME:-$HOME/.sdkman/candidates/java/17.0.13-tem}"
export PATH="$JAVA_HOME/bin:$PATH"
SRC="${1:-$REPO/target/fream-back}"
OUT="${2:-$REPO/graphs/auto}"
cd "$HERE"
./gradlew -q --console=plain installDist
"$HERE/build/install/navgraph/bin/navgraph" "$SRC" "$OUT"
