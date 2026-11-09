#!/usr/bin/env bash
set -euo pipefail

# Public read-only objects; only the host downloads them, never the emulator.
# Hashes pin the actual documents/policies accepted by the local fixture tests.
out=app/src/androidTest/assets/ci-fixtures
mkdir -p "$out"
base='https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o'
while read -r hash object name; do
  curl --fail --silent --show-error --max-time 120 "$base/$object?alt=media" -o "$out/$name"
  printf '%s  %s\n' "$hash" "$out/$name" | sha256sum -c -
done <<'FIXTURES'
e4523a1372eddbd880da69329dafcab154bd7749ce81e790296b3ff61bae1b01 bls%5B1%5D.pdf bls[1].pdf
8400498ba9c85f5cca38c7a8c4a4a4029f26c5db656c5280ae815e04e758c7c1 als%5B1%5D.pdf als[1].pdf
ce8bcfe674dc69d08ad29d6291ffcdba4cb3c782c127d262014b23a929dca7bd kuntres.pdf kuntres.pdf
9efca8dbd6eb632fc6f0027ca1da03f113b41130bad9f9dbbcdcb636b901e9ac privacy-en.html policy-en.html
c9d22a2aa8313cf46451c36f56680e5b79b9b05fd6c00037e85a868f1c286a46 privacy.html policy-he.html
FIXTURES
