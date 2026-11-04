# Shared helper Kotlin migration (#27)

This slice converts `FixedTextInputEditText`, `MyDrawerLayout`, `Event`,
`UtilsJava`, `rateUs`, `AnimatedColor`, and `Contraction` without changing their
feature ownership or installed class names.

## Caller and storage audit

- `FixedTextInputEditText` remains an XML view in activation, dictionary, medical,
  chat, and dispatch layouts, and a programmatic view in oxygen/pulse. All three
  constructors delegate to the same Material superclass overload as before.
- `MyDrawerLayout` remains the tools/settings XML view. The one-argument lock-mode
  override still suppresses interception only for `LOCK_MODE_LOCKED_OPEN`.
- `Event` is live: `MetroService` publishes start/stop callbacks, and `MetroActivity`
  registers/removes named listeners. Keep-first named registration (including null
  values), anonymous duplicates/order, and independent mutable snapshots remain.
- `UtilsJava` retains Java-callable static resource methods used by
  `CircularProgressButton`. `rateUs` retains the static `app_launched` entry point,
  preference file/keys, launch/day thresholds, dialog text/actions, and market URI.
- `AnimatedColor` is live in Glasgow's score-band animation. Endpoint alpha,
  interior opaque HSV interpolation, and float arithmetic remain unchanged.
- `Contraction` remains the legacy Gson Date record with fields `start`, `end`,
  `oldEnd`, `number`, `previous`, and `length`, both original constructors, and JVM
  getter/setter names. Nullable fields stay nullable; no `lateinit` or non-null
  getter is substituted. Kotlin properties expose the former package-private
  accessors publicly because Kotlin has no package visibility.
- The only caller changes are explicit non-null assertions where the existing
  validated history/store code already dereferenced Java platform Dates. The
  separate Long-based `contractions.session.v1` format is untouched.
- Whole-tree source/resource/test/reflection searches found no dynamic class-name
  loading of these helpers. Both initially suspected unused classes are live and
  retained. Existing Gson keep rules remain unchanged.

## Focused checks

`EventTest` checks registration, nulls, duplicate anonymous listeners, removal,
and snapshot isolation. `ContractionContractTest` checks JVM constructors and
accessors, field names, Date JSON, and missing-field nulls. `SharedHelpersTest`
checks XML/constructor identity, Java static bridges, drawer interception,
HSV arithmetic/alpha, and rating preference keys/counters/opt-out. Existing
`ContractionHistoryTest` and `ContractionStoreTest` cover both installed formats,
precision restoration, malformed data preservation, and the new storage boundary.

Evidence is retained outside the worktree at
`~/.local/share/opencode/evidence/shared-kotlin/`.

Verified on the private slice rebased onto #102
(`1fc56d4e4ada08f67e9274f00053a1b848a4a0ab`):

- `testDebugUnitTest --tests 'atlow.chemi.mymada.helpers.*'`: 2 tests passed.
- `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, `lintDebug`:
  passed. Lint reported 0 errors, 313 warnings, and 2 hints.
- The eight focused instrumentation checks above passed on API24 and API37
  (16-KB pages), using only private emulator ports 5620/5622. IPv4/IPv6 routing
  isolation was recorded; API24's Ethernet interface was explicitly disabled.
- Builds use cached SDK/JDK17 and command-line
  `-Dorg.gradle.jvmargs='-Xmx6g -XX:ActiveProcessorCount=2 -Dfile.encoding=UTF-8'`
  with `--max-workers=2`. The repository's 2-GiB default exhausted R8's heap;
  no build configuration was changed.
- APKs and SHA-256 hashes, JVM signatures, build/lint logs, instrumentation
  output, network state, and the remaining-Java inventory are saved in evidence.

The installed `avdmanager` generated `target=android-0` for new API37 minor-version
images, causing an offline guest with misleading HVF/mprotect errors. Correcting
only the private AVD's `.ini` to `target=android-37` restored boot; no shared
emulator, global adb reset, physical device, or production state was used.

## Remaining app-owned Java at this slice's base

`MokedPage`, `PrivacyActivity`, `UtillitiesAndTools`, `CircularAnimatedDrawable`,
`CircularProgressButton`, `CircularRevealAnimatedDrawable`, `MainActivity1`, and
`ActionReceiver` remain assigned to other owners. This slice does not close #27.
