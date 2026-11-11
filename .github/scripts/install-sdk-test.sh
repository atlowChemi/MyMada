#!/usr/bin/env bash
set -euo pipefail
# Exercise early stdin closure without downloading SDK packages.
sdkmanager() {
  if [[ $1 == --licenses ]]; then return "$license_status"; fi
  [[ $* == 'platforms;android-37.0 build-tools;37.0.0' ]]
}
export -f sdkmanager
for license_status in 0 42; do
  export license_status
  status=0
  bash "$(dirname "$0")/install-sdk.sh" || status=$?
  [[ $status == "$license_status" ]]
done
