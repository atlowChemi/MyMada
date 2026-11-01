# Activation, dispatch and chat geometry

Refs #35, #26. This is a presentation correction above accepted #125; SMS
policy and chat first-open ordering are unchanged.

Evidence root: `~/.local/share/opencode/evidence/m2-integration/geometry/`.

## Root causes

- Material3 filled fields introduced 56dp minimums and padding where published
  EditText used 41dp plus TextInputLayout's label allowance. `IntegrationField`
  restores those metrics, underline colors and transparent background; text
  remains scalable and validation/editing semantics remain available.
  Activation retains the 100dp top offset, four weighted cells separated by
  8dp, digit masking/focus progression and saved state. Its submit text now
  explicitly uses uppercase 20sp OpenSans, rather than Material3 label defaults.
  The retained `visual-audit-121/pairs/en-activation.png` predates this fix.
  API37 production edit bounds are `[42,515][274,623]`; Compose accessibility
  exposes `[42,506][276,632]` because its touch target expands to 48dp around
  the tested 41dp layout. Underline position is judged from the opened image,
  not from equating an accessibility hit rectangle with the painted widget.
- Fragment-hosted Compose dialogs inherited wider content padding and narrower
  window sizing. The existing alert theme and shared native-alert spacing now
  apply to request, chat and dispatch confirmation dialogs.
- Dispatch tabs used mixed-case Material3 labels and indicator defaults instead
  of the native tab casing, white indicator and width-sensitive label sizing.
- Remaining chat work: the name row omitted its native card margin, radius,
  elevation, 19sp text and right alignment. Its composer omitted the native
  input height and rounded aqua send card. A separate follow-up verifies these
  presentation defaults; this layer corrects chat dialog geometry only.

## Retained verification and failures

`final/baseline125-geometry.txt` reproduces the activation regression against
frozen #125: expected 41dp, actual 56dp. The pre-card-correction candidate passed
56 focused checks across EN/HE, API24/37 and font scales 1.0/1.5. Its debug/test/
release builds, both lints and 20 JVM tests passed (`v3/build.log`).

The initial TabRow experiment used BoxWithConstraints, which cannot supply
intrinsic measurements. The API24 optimized crash is retained in
`v2/api24-crash.txt`; ordinary measured-width state replaces it. Earlier failed
test records retain the obsolete assumption that fixed dialog actions have a
scroll parent; checks now assert that those actions are displayed.

## Opened comparisons

All 20 pre-card-correction production/candidate pairs were opened, with
production on the left. API37 uses `final/pair-5960-{en,he}-{activation,request,
form,team,chat}.png`; API24 uses `final/pair-ime-hidden-5962-{en,he}-{activation,
request,form,team,chat}.png`. Each brace combination identifies one actual file.
Keyboard-mismatched initial API24 pairs remain retained but are excluded from
geometry acceptance. Recaptures dismiss only a visible IME before comparison.

Activation underline placement and submit sizing, dialog widths and dispatch
field pitch are corrected. The form's medical-code picker is clipped to a sliver
in the initial API24 viewport above Send; that image does not prove reachability
or a fully visible control. Scrolled-state verification is required separately.
Chat background controls prompted the subsequent card
correction. Live populated-chat/backend and physical-device acceptance remain
outside these isolated fixtures.

## Chat card follow-up above #127

Restored the native name-card margin, rounding, elevation, 19sp right-aligned
text, 72dp composer input and rounded aqua send card. The shared field retains
its 41dp default for activation/dispatch. Existing sequence, scalable text and
pending-send behavior are retained.

Completed `chat-cards` verification: **60/60 checks**, ten normal-font and five
large-font checks per EN/HE/API24/API37 combination, including pending-send
recreation at 1.5. Debug/test/release builds, both lints and 20 JVM tests passed.
Optimized APK SHA256:
`f850d8de366565bb961416f140c2560136569413b8e43e2736bbe6f057d8f1ad`.
These frozen artifacts predate the independent Home commit; Home has its own
verified artifact and policy evidence, not a claimed re-execution here.

Opened all four `chat-cards/pair-{5960,5962}-{en,he}-chat.png` comparisons
(production left, IME hidden), and all four large-font candidate
`chat-cards/{5960,5962}-{en,he}-1.5-chat/empty-{en,he}.png` images (matching
directory/file language). Card/composer actions remain visible; English name
text wraps at large font. The API24 background composer retains focus behind
the dialog, so its floating hint/active underline differs from the unfocused
production background. This is not evidence of a smaller configured body font.
Normal/large fake-backend screenshots are functional fixtures, not live messages
or production matched populated-chat evidence.

## Form picker visibility

The initial API24 candidate image does show a clipped label immediately above
Send. The root is the finite scroll viewport, not a Send overlay: `FormScreen`
places the weighted scroll column and fixed Send button as siblings. Production
also clips its last EditText in the initial viewport (API37 bounds
`[42,1503][1038,1533]`), so initial screenshots alone cannot establish access.

Extended `MedicalPickerTest.realFormOpensCatalogAndRetainsSelectionAcrossSearch`
to dismiss the keyboard, scroll the semantic picker into view, assert its full
bottom is above Send, and compare unclipped and clipped heights within one
physical pixel before opening the catalog and exercising search/selection.
**8/8 runs passed** across API24/37, EN/HE, font1.0/1.5. All eight screenshots
`form-access/{5960,5962}-{en,he}-{1.0,1.5}-images/scrolled-{en,he}.png`
(matching directory/file language) were opened: complete label/button visible,
separated from Send. This proves semantic activation and visible hit-area
separation, not a physical TalkBack session. No production layout defect is
replicated and no application layout change was needed.

The first test compilation used a nonexistent DpRect height property; the
retained `form-access/test-build.log` records it. The corrected bottom-minus-top
assertion compiled successfully (`test-build-retry.log`). Existing app builds,
lints and business tests are reused; only the changed instrumentation APK was
rebuilt for this test-only layer.

Also opened all eight optimized pairs
`form-access/pair-{5960,5962}-{en,he}-{1.0,1.5}.png`, with IME hidden and both
screens swiped to the bottom. Both controls are fully exposed above Send.
Candidate uses an outlined semantic button rather than production's underlined
EditText picker; their scroll extents therefore differ. The 1.5 API24 production
content remains smaller while candidate honors system scaling (already approved).
These are matched bottom-of-scroll access comparisons, not equal pixel offsets
or a claim that the initial viewport is fully visible. Font scale was restored
to 1.0 on both owned emulators after capture; firewall records accompany each
capture directory.

## Picker presentation correction above #129

#129 resolved scroll access only. Its centered outlined pill, smaller text and
rounded border were **not approved production appearance**. Production uses the
same start-aligned underlined field presentation and pitch as the form inputs.
The follow-up reuses `IntegrationField` for the picker, retaining its 18sp text,
41dp input minimum, label allowance and RTL-aware start alignment. The field is
read-only and exposes an activation semantics action; a released touch opens the
existing catalog without editing text or opening an input keyboard. Other shared
field callers retain editable behavior.

The existing full-bounds/Send-separation check now performs a real touch click
instead of semantic-only activation. Search, reverse-order selection, recreation,
approved SMS preview and cleared billing assertions remain in the same test.
The current run also includes the shared geometry test at both font scales to
check the other field consumers. Verification results and optimized comparisons
are recorded below.

- Debug/test/release builds, both lints and 20 JVM tests passed (build 1m54s).
- 16/16 focused instrumentation checks passed: picker actual-touch/selection and
  shared geometry, API24/37 × EN/HE × font1.0/1.5. Full picker bounds remain above Send.
- Opened all eight optimized production/candidate comparisons in
  `/Users/chemiatlow/.local/share/opencode/evidence/m2-integration/geometry/form-presentation/pair-{5960,5962}-{en,he}-{1.0,1.5}.png`.
  The picker now matches the surrounding underlined fields, start alignment and
  normal-font pitch/type size. Bottom-scroll offsets differ; these are not
  pixel-identical whole-screen captures. Large-font differences retain the
  already approved Android font-scaling policy (production API24 ignores it).
- Evidence root above retains `build.log`, `*-test.txt`, eight instrumentation
  image folders, `tested-source.patch`, `sha256.txt` and frozen APKs. Optimized APK
  SHA256: `9365007583a7bd3b89a604dcc7f49294b63d1244c11128de7e98e68d10d78ab2`.
- Initial optimized API24 Hebrew locale-selection failure is retained in shell
  `sh_19704d7a3001qWXyzefDb8GIC6`; successful normal-font retry is in sibling
  `form-presentation-candidate-he24-retry`. Other optimized captures are in
  `form-presentation-candidate-{1.0,1.5}`. Both devices restored to font1.0.
# Settings measured text geometry above #130

Replayed private owner commit `2fa9a360390ab26a68921d1c0537c520299fb395`
onto published #130 without Team's private changes. Scope is SettingsContent,
ModeDialog choice metrics, and SettingsTextGeometryTest.

The native TextView drawing leaf restores legacy StaticLayout-only fallback
spacing instead of modern BoringLayout fallback inflation. Wrapped/RTL text,
unbounded width and system font scaling remain supported. Compose retains row
interaction, switch state/action and merged text semantics; native drawing nodes
are accessibility-unimportant. Shared callers reviewed include headings, links,
toggle/plain rows, mode choices, Delay/Volume actions and audio access prompts.
No callback or persistence contract changed.

Owner verification: 48/48 affected checks across API24/37 and fonts1/1.5/2,
plus8/8 system-Hebrew API37 checks. Actual compressed accessibility nodes carry
single labels and checkable/checked/clickable rows. Uncompressed dumps include
unimportant native duplicates; this is not universal duplicate-free certification.

Evidence: `/Users/chemiatlow/.local/share/opencode/evidence/m2-integration/settings-measured-parity/PRIMARY-HANDOFF.md`.
Integrator opened API37 EN Advanced and HE Mode comparisons: EN row pitch/card
align with native; Hebrew Mode retains a small vertical offset. Owner subsequently
captured valid API24 Hebrew via system Language preferences; integrator opened
`pair-5940-he-mode.png`, confirming the small offset also exists on API24.
Earlier English-mislabeled captures remain archived as failures, not references.
Owner's raw interior-edge measurements: HE Mode API24 native602..<1255 versus
candidate605..<1252 (6px shorter); API37 native630..<1289 versus634..<1285
(8px shorter). Font mismatch is not established: historical `b36f4a9c` uses
monospace and Applicatio replaces default typefaces with the bundled font via
reflection. Current sans-serif styles are migration code, not native evidence.
Metrics investigation continues without a compensating padding adjustment.
DialogTitle's20sp-to18sp ellipsis fallback is confirmed, but the first native-host
prototype produced63px English titles instead of53px on both APIs. Native APK
theme typeface is monospace; migrated theme uses sans-serif. Title-fitting/font
resolution remains under diagnostic investigation. No locale-specific sizing or
unverified host-title change is included in the published primary layer.
Delay hint/height is a separate follow-up. These gaps remain explicit: this layer
does not claim complete Settings parity. Missing release/JVM/lint checks are
recorded in `integration-release-jvm-lints.log`: lint rejected the platform
TextView subclass (`AppCompatCustomView`). Integration now uses AppCompatTextView
with the same measurement policy; the owner's private commit is preserved.
Corrective debug/test/release builds, JVM tests and both lints passed in
`integration-appcompat-build.log`. Post-superclass runtime recheck passed10/10:
geometry, Advanced and Day/Night at font1.0 on API24/37, plus geometry at font2.0
on both. Artifacts/hashes/logs are frozen in `integration-appcompat-runtime/`.
Opened resulting API37 EN Advanced and API24 EN Mode images, retaining the
aligned row/card and mode presentation. Both integrator devices restored font1.0.
The owner's earlier matrix remains distinct from these corrected-artifact checks.
# Delay editor follow-up above #131

Replayed private `b3ac6db9db98a60a9634321b25a43d55ab36e3a2`; `git diff`
against that commit under `app/` is empty. Native inflated shabat_t using the
activity theme before creating its alert; restoring that context and gravityStart
restores aqua hints and field metrics. API35+ locale-preferred minimum line height
is disabled to match the native target29 EN/HE field height without padding hacks.

Owner's six focused runs cover both APIs, font1/2, EN/HE, overflow, drafts,
recreation, cancel/OK and volume policy. Reused after source-equivalence check.
Opened matched API37 HE and API24 EN pairs from
`settings-measured-parity/pair-delay-final-{5940,5942}-{en,he}.png`.
Exact native/candidate bounds: API24 y753..<1103, editor108px; API37
y782..<1137, editor113px. Follow-up details and invalid-reference history are in
`settings-measured-parity/FOLLOWUP.md`. Hebrew Mode's6px/8px offset is separate.
Release assembly and both lints passed in `delay-integration-release-lints.log`.
Owner's verified source tree matches this replay under `app/`; no unchanged
device matrix was rerun. Earlier private commits and failed captures are retained.
# Mode native-title follow-up above #132

Replayed `aa4f791d9bc9cb00a550c8dba928488d708cb578`; full `app/` tree is
identical to the owner's verified slice. Optional native title/theme arguments
leave other settingsDialog callers on their existing defaults. Mode alone uses
the bundled title font before native fitting, the existing8dp native divider,
StaticLayout-only fallback compatibility and target35 minimum-line-height opt-out.
No global AlertDialogStyle change or locale-specific font size is introduced.

Owner's16/16 bounded checks cover both APIs, fonts1/1.5/2 and matched systemHE;
reused after source-equivalence review. Integrator opened all four normal pairs:
`pair-mode-spacing-{5940,5942}-en.png` and
`pair-mode-final-{5940,5942}-he.png` in `settings-measured-parity/`.
Native/candidate modal interior bounds match: API24 EN605..<1252, HE602..<1255;
API37 EN636..<1282, HE630..<1289. Native fitting remains EN18sp/shortHE20sp;
large English titles wrap and choices/Cancel remain reachable. Important compressed
HE accessibility exposes title/choice/action text once. Failed prototypes retained.
Full provenance: `MODE-HANDOFF.md`; missing release and both lints passed in
`mode-integration-release-lints.log` (BUILD SUCCESSFUL). No unchanged runtime matrix rerun.

# Contractions shared alert host (verification pending)

Matched medical batch1 images show recovery, delete and empty-delete using an
840px candidate surface versus940px native at1080px/420dpi. Empty-delete gains
an extra wrapped line. All three resource states route through one renderer in
ContractionScreen and one callback in ContracTimer.

The correction restores AlertDialogStyle's native alert host for this renderer,
with composition-scoped disposal and the latest state callback. Recovery/delete
retain native explicit message/negative-button typefaces; empty-delete retains
theme defaults. Non-cancelability, saved dialog state, recovery/delete decisions
and expiry/persistence logic are unchanged. Settings files are not involved.

Existing recovery tests now target native dialog controls. A focused regression
checks all three states'940px content width at the matched configuration and
empty-delete single-line text. Debug/test/release/JVM/lints passed after correcting
a configuration-sensitive color lookup. Initial runtime passed5/7 recovery tests;
two failed on the obsolete native toolbar selector. Tests now use the existing
Compose shell-action tag. Corrected replay above #132 passed debug/test/release,
JVM/both lints and16/16 device checks (seven recovery tests at font1.0 plus the
three-dialog geometry test at font1.5 on both APIs). Opened empty-delete debug
images: width/wrapping corrected; API37 height still differs from retained native.
Failure logs remain retained; optimized comparisons and metric disposition remain
pending. The three medical rows remain D.
