#!/usr/bin/env bash
set -euo pipefail
script="$(cd "$(dirname "$0")" && pwd)/download-test-fixtures.sh"
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
