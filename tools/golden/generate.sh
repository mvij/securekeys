#!/usr/bin/env bash
# Regenerates securekeys/src/test/resources/golden_extended.tsv from the original closed-source
# SecureKeysLib.aar, which was deleted in c5cbe2d but is still in git history. Needs a JDK.
set -euo pipefail
cd "$(dirname "$0")"
root=$(git rev-parse --show-toplevel)
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

git show c5cbe2d^:SecureKeysLib/SecureKeysLib.aar > "$work/lib.aar"
unzip -q -o "$work/lib.aar" classes.jar -d "$work"
javac -encoding UTF-8 -d "$work/out" -cp "$work/classes.jar" GoldenGen.java stubs/android/text/TextUtils.java stubs/android/util/Log.java
# The AAR calls String.toLowerCase() and getBytes() with platform defaults; pin them to what Android uses
# for an English-locale device (UTF-8, non-Turkish locale).
java -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -cp "$work/out:$work/classes.jar" \
    GoldenGen phrases.txt "$root/securekeys/src/test/resources/golden_extended.tsv"
echo "wrote $(wc -l < "$root/securekeys/src/test/resources/golden_extended.tsv") vectors"
