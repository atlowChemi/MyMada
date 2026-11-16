#!/usr/bin/env bash
set -euo pipefail

# Public versioned GitHub mirror; never contact production Firebase from CI.
# Hashes pin the actual documents/policies accepted by the local fixture tests.
out=app/src/androidTest/assets/ci-fixtures
mkdir -p "$out"
base='https://github.com/atlowChemi/MyMada/releases/download/ci-fixtures-v1'
while read -r hash object name; do
  if printf '%s  %s\n' "$hash" "$out/$name" | sha256sum -c - >/dev/null 2>&1; then
    continue
  fi
  temporary="$out/$name.download"
  trap 'rm -f "$temporary"' EXIT
  curl --fail --silent --show-error --location --max-time 120 "$base/$object" -o "$temporary"
  printf '%s  %s\n' "$hash" "$temporary" | sha256sum -c -
  mv "$temporary" "$out/$name"
done <<'FIXTURES'
e4523a1372eddbd880da69329dafcab154bd7749ce81e790296b3ff61bae1b01 bls.pdf bls[1].pdf
8400498ba9c85f5cca38c7a8c4a4a4029f26c5db656c5280ae815e04e758c7c1 als.pdf als[1].pdf
ce8bcfe674dc69d08ad29d6291ffcdba4cb3c782c127d262014b23a929dca7bd kuntres.pdf kuntres.pdf
9efca8dbd6eb632fc6f0027ca1da03f113b41130bad9f9dbbcdcb636b901e9ac policy-en.html policy-en.html
c9d22a2aa8313cf46451c36f56680e5b79b9b05fd6c00037e85a868f1c286a46 policy-he.html policy-he.html
FIXTURES
