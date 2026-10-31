# Activation / FCM behavior slice (#26)

Base: PR77 `c2f85cffbc550b485d391c677df46dbbace7fd01`, above PR75 in
the single published stack76. Implementation `ad8bdfae4943baeccddb40b86274cbd5e64d4547`;
subsequent changes to this report and instrumentation synchronization do not change
the application artifact. No merges. See `integrations/README.md` for remaining #26 scope.

## Reproduction / artifact provenance

Persistent evidence: `~/.local/share/opencode/evidence/integrations/`.
JDK17 and SDK: `~/.cache/mymada-m2/{jdk-17.0.21+9/Contents/Home,sdk}`.

```
sh ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
  :app:assembleRelease :app:lintDebug -x :app:uploadCrashlyticsMappingFileRelease \
  '-Dorg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8' --no-daemon --max-workers=2
adb -s emulator-5980 shell am instrument -w -r \
  -e class atlow.chemi.mymada.integrations.ActivationIntegrationTest \
  atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
```

Only owned emulators5980/5982; no phone, global ADB reset or other owner's devices.
IPv4/IPv6 non-loopback OUTPUT rejected before any app startup, including after
locale/framework restarts. No production data writes, requests, messages or SMS.
Task-based fixture acknowledgements are not live SDK/server delivery evidence.

- Exact above77 debug/unit/release/lint build: **PASS** (`build-above77.log`).
  8 JVM tests, 0 failures (3 new contract tests); lint 0 errors / 311 warnings.
- Initial default2GiB build failed R8 with Java heap exhaustion; retained in
  `build-initial.log`. Local4GiB run succeeds; no shrinker-rule/config changes.
- API24 stock48MiB,1080×1920/420dpi: **6/6 PASS**,6.611s,
  `api24-tests-lifecycle.log`. Request validation/confirmation restoration,
  awaited token + write acknowledgement, retry, activation acknowledgement and
  rotation, old conversation identity on token refresh, cold activation and real
  launcher routing, distinct PendingIntents, invalid payloads, screen captures.
- First API24 test run stalled in ActivityScenario.recreate's waitForIdleSync:
  fixture intentionally held activation pending while the indeterminate progress
  dialog continuously animated. Main thread remained responsive/idle in native
  looper. Thread dump: `api24-stall-threads.txt`. Test now observes actual resumed
  lifecycle with a bounded latch, verifies retained model and one backend call,
  then acknowledges the fixture. No app logic changed to make the test pass.
- Additional Hebrew capture test **1/1 PASS**,3.903s (`api24-capture-he.log`).
- Locally debug-signed optimized release installed on API24; actual gate click
  reaches request screen and disclaimer renders. Installed bytes match artifact
  SHA256 `eb40258acd871dbcc5e1fe5324cef02dbe8c5bf3aef8499dab6eb79040f37b2e`.
  This is not production-signing/update acceptance or successful activation.

APKs, initial test APK, source and SHA256 manifest: `artifacts-ad8bdfae/`.
The final instrumentation APK is separately hashed with its test-only revision.

## Actual published-production comparison

Reused unmodified locally debug-signed universal APK generated from maintainer's
12822.aab. Version3.5.7/12822; APK SHA256
`b08c3d812f4781cfc667edf4240c917350c8083ea91cdb95bc971e0524c09179`.
This has user-supplied provenance, not independent Play-track verification.

New real API24 screenshots captured at1080×1920/420dpi/font1.0 with matched
system/app locale English then he-IL. Reference files `production-{gate,activation,
disclaimer,request}-{en,he}.png`, plus `production-request-full-{en,he}.png`.
Candidate files in `candidate-api24/` (English) and `candidate-api24-he/` (Hebrew).
Images were opened and visually inspected, not generated mockups.

Gate body wrapping, white rounded panel, backgrounds, button geometry/order and
RTL mirror match. Activation four LTR fields, underline geometry and footer match.
Request body labels, declaration wrapping, field underlines, send button/icon and
keyboard-resized geometry match in the inspected states.

**Not full visual acceptance:** toolbar title weight differs (also documented by
base77); Hebrew debug gate title intentionally uses debug branding. Disclaimer
body wrapping/height and Hebrew OK alignment differ from12822. Candidate's Hebrew
full-form toolbar back arrow is visible; one production full-form capture has no
arrow, while its disclaimer capture does. Focus/caret/scrollbar timing differs.
These are recorded differences, not silently accepted redesigns. XML preservation
alone does not establish pixel parity. Compose conversion and per-screen review
remain open; successful live activation/request states have no production capture.

## Live integration gate / remaining work

No claim that #26 is complete. Chat/name/unread/SeenByAdmin, fresh-install awaited
chat initialization, token-refresh backend delivery mapping, dispatch forms/team/
medical codes/exact SMS payloads, broader SDK events/consent/test ads, and Compose
conversion remain outstanding. Native time string/seconds versus RN numeric/ms
still needs sanitized backend fixtures. Existing activation read/write remains
non-transactional; backend security redesign belongs to #8. Real FCM/server writes,
offline-write process death, signed upgrade and full #27/#35 acceptance remain gated.
