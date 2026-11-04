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
