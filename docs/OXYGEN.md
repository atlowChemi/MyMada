# Oxygen (#23) — draft acceptance

Base: PR #53 `b42820cc94c9417e8c3cd9f095fd24fa41ad01cc`, including #22 prerequisites.
Native `b36f4a9c` OxyCalc.java and RN `be72a163` Oxygen.tsx agree on 2.4/20 L tanks,
10 L/min flow and approximate 15 PSI per ATM. This is compatibility documentation,
not independent validation of clinical assumptions.

## Approved application policy

Retain native truncation: 1499 PSI / 20 L gives 198 minutes, not RN's rounded 200.
Accept whole ASCII-digit pressure values from 0 through 999 ATM or 14985 PSI
(999 × 15), inclusive. An invalid/out-of-range edit or paste is rejected as a whole,
with a visible range error; the previous accepted values remain. No character
clipping turns an invalid pressure into a different valid pressure. These bounds
are the maintainer's app policy, not a newly inferred clinical recommendation.
Duration is truncation of pressure × tank liters / 10. Defaults are 100/1500/24.

## Approved clearing and orientation behavior

Clearing either pressure field clears both linked fields and displays zero minutes,
so no stale estimate remains. Portrait and landscape use the same numeric bounds
and calculation behavior. Rotation retains both values and
tank selection, including an empty pair. The 64%/36% landscape layout remains.
The regression exercises both clearing directions, nonempty and empty rotation,
inclusive endpoints, overflow/invalid paste rejection and unchanged truncation in
both orientations. Prior code/evidence below predates these approved changes.

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
