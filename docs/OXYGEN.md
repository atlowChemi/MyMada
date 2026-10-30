# Oxygen (#23) — draft acceptance

Base: PR #53 `b42820cc94c9417e8c3cd9f095fd24fa41ad01cc`, including #22 prerequisites.
Native `b36f4a9c` OxyCalc.java and RN `be72a163` Oxygen.tsx agree on 2.4/20 L tanks,
10 L/min flow and approximate 15 PSI per ATM. This is compatibility documentation,
not validation of clinical assumptions. #19 explicitly leaves rounding unresolved.

## Maintainer questions (acceptance blocked)

1. At 1499 PSI / 20 L should the result be native 198 min (truncate PSI/15) or RN
   200 min (round)? Confirm the 15:1 conversion, tank volumes and fixed 10 L/min.
2. What pressure bounds should apply? Native limits ATM to three characters even
   on linked updates: 99999 PSI → 666 ATM, rather than 6666. RN does not clip linked
   ATM updates. Neither is an approved pressure validation policy.
3. Clearing native PSI leaves ATM/result intact; clearing ATM shows zero but keeps
   PSI. RN clears both linked values and renders zero as blank. Which is intended?
4. Native landscape limits PSI to four characters versus portrait five. Should the
   limit be orientation-independent? Draft currently uses five in both; the native
   64%/36% side-by-side landscape layout is retained with scrollable content.

The draft explicitly retains native behavior for all three, pending approval;
it does not silently introduce RN rounding or infer a medical limit from maxLength.
Input is ASCII digits only, three ATM/five PSI characters, matching native UI.
Duration is truncation of pressure × tank liters / 10. Defaults are 100/1500/24.

## Implementation

Same OxyCalc Fragment identity and existing tools navigation/enable key `oxy`.
Compose owns layout and rememberSaveable input/tank state. Native input/spinner/text
widgets retain the production decoration/font. No shared navigation/build changes.
Activity recreation restores input/tank; returning to a removed tool creates a new
composition (cross-tool session persistence is not claimed).

Focused instrumented checks cover conversion thresholds, max input, invalid input,
both tanks, clearing, linked edits and Activity recreation. Process-death and
production comparisons must be recorded separately; recreation is not process death.

Evidence root: `~/.local/share/opencode/evidence/oxygen`.

## Verified code `6919c5ee`

- Debug/test APKs, optimized release and lint pass; lint0 errors/335 warnings.
  Build uses JDK17, SDK37, `-Dorg.gradle.jvmargs=-Xmx4g --max-workers=1` and
  `-x :app:uploadCrashlyticsMappingFileRelease`. No shared build edits.
- API24 and API37 (actual16384-byte pages):2/2 focused tests for each EN/HE ×
  font1.0/1.5. API24 landscape:2/2 in each language. Exact APK SHA256s/logs are
  in `artifact-sha256.txt`, `artifact-commit.txt` and `test*.log` in the evidence root.
- Actual API24 background process death: PID11809 killed after Home, restored
  PID12174 retains200 ATM/3000 PSI/48 min. `process-{before,after}.{png,xml}` and
  `process-{kill,restart}-actual.txt`. Ordinary `am kill` did not kill this process;
  the verified run explicitly killed that owned app PID, not an emulator/global service.
- Locally signed optimized release opens Oxygen/default24 on both APIs; screenshots
  opened. API24 install-r over the locally signed12822 reference succeeds. This is
  not production-signature upgrade verification.
- All four published/candidate normal/large-system-font comparison pairs opened;
  native suppresses font scaling, candidate honors it. Normal geometry/content and
  RTL retained; initial focus/caret/toolbar focus highlight differ. Landscape pairs
  opened, including mirrored64%/36% layout. Maintainer visual approval remains open.

Constraint deviation: the **first** release command accidentally invoked the existing
Crashlytics mapping-upload task. It executed; server-side storage was not verified.
That host run cannot be claimed backend-write-free. Subsequent builds exclude the
task. Owned emulator application traffic was blocked before startup throughout.
