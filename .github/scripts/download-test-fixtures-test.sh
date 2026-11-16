#!/usr/bin/env bash
set -euo pipefail
script="$(cd "$(dirname "$0")" && pwd)/download-test-fixtures.sh"
fixtures=${1:-}
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
mkdir "$tmp/bin"
cat > "$tmp/bin/curl" <<'CURL'
#!/usr/bin/env bash
# A successful HTTP response with substituted bytes must not enter the test APK.
printf 'wrong document\n' > "${!#}"
CURL
chmod +x "$tmp/bin/curl"
cd "$tmp"
if PATH="$tmp/bin:$PATH" bash "$script" > download.log 2>&1; then
  echo 'Fixture download incorrectly accepted substituted content' >&2
  exit 1
fi
grep -q 'FAILED' download.log
test ! -e 'app/src/androidTest/assets/ci-fixtures/bls[1].pdf'
test ! -e 'app/src/androidTest/assets/ci-fixtures/bls[1].pdf.download'

# A corrupt cached fixture must be checked again, without replacing it with
# unverified download bytes if the mirror response is also corrupt.
printf 'old corrupted cache\n' > 'app/src/androidTest/assets/ci-fixtures/bls[1].pdf'
if PATH="$tmp/bin:$PATH" bash "$script" > download.log 2>&1; then
  echo 'Fixture download incorrectly trusted a corrupt cache' >&2
  exit 1
fi
grep -q 'old corrupted cache' 'app/src/androidTest/assets/ci-fixtures/bls[1].pdf'
test ! -e 'app/src/androidTest/assets/ci-fixtures/bls[1].pdf.download'

# Pass an absolute directory of real fixtures to check the network-free path.
if [[ -n "$fixtures" ]]; then
  cp "$fixtures/"* app/src/androidTest/assets/ci-fixtures/
  printf '#!/usr/bin/env bash\nexit 99\n' > "$tmp/bin/curl"
  PATH="$tmp/bin:$PATH" bash "$script"
fi
