#!/usr/bin/env bash
set -euo pipefail
# setup-android's Node stdin writer can throw EPIPE when sdkmanager exits early.
# Ignore only the answer producer's SIGPIPE; sdkmanager failures still fail CI.
(yes || true) | sdkmanager --licenses
sdkmanager 'platforms;android-37.0' 'build-tools;37.0.0'
