# Team member dialogs

The editor restores the native horizontal name/role arrangement and real role
spinner, with a stacked layout for enlarged text. The fragment uses the existing
alert style; its title explicitly uses the native asset font. The shared dialog
title override retains the previous default for other callers. Empty-team errors
adopt the already-verified native local-error host.

Implementation reuses the preserved469ff4ea/5410ecc2 work. Strict59px typography
experiments remain private and are not part of this fix. Model, four-member cap,
role order, saved-state restoration and approved dispatch body are unchanged.

Combined7ec987ef initial EN/HE checks passed editor validation, native role
selection, recreation, edit/delete/cap, literal preview and empty-team error.
Independent review opened the six matching-locale add/member/error frames in
`parallel-visual-audit/team-final-fix-20261102T130837Z/`, comparing applicable
native references. Final role-popup/edit capture points are in16e90fd8; their
separate final evidence is in
`settings-measured-parity/team-final-16e90fd8-5942-resume/` (EN/HE2/2 pass).
Eight critical blank-member/role-popup/restored-draft/edit frames were opened;
applicable retained native references were compared. All seven roles, saved role,
edited name, complete error caption and actions remain visible. Fixture names and
background member counts differ from references and are excluded from appearance
claims. Final source matches16e90fd8 exactly under `app/` after documentation-only
rebasing. Release assembly and both lints pass after disk cleanup.

Shared-field caller checks use source-bound artifacts rather than relabeling
earlier screenshots: Settings7ec987ef Form/readonly/billing checks and five opened
pairs; communications7ec987ef keyboard/rename/preview smoke (three methods).
The final native NEXT and picker IME checks exercise actual Android editors.

## Shared native-field dependency

Labeled fields use the installed native TextInputLayout/EditText for floating
errors and multiline measurement; unlabeled activation cells keep their existing
renderer. Compatibility rules reproduce the published Material1.0 indicator
padding and target29 caption fallback behavior. Notes retain an expanding11-line
minimum, verified with18 lines. Read-only code fields reject native edits while
retaining the picker action; billing validation and clearing remain unchanged.

The first native-field version exposed actual typing loss: its AndroidView update
could replay a stale value before composition caught up. Reading the edit revision
in composition fixes that shared root. A paused-frame test fails the old APK and
passes the correction; three actual Hebrew `bad` input runs retained all characters.
The corrected artifact passed10 focused tests. Evidence is in
`settings-measured-parity/error-inputloss/`; combined7ec987ef Request/Form five
matched pairs independently retain complete error labels, initial Notes underline,
reachable code errors and billing fields. Harmless caption alignment is accepted.

Native NEXT uses the parent ViewGroup relative-focus API; its added real-editor
IME test passes on final16e90fd8. Picker tests now press the actual native IME
action rather than relying on a Compose-only semantics action. Historical failures
and artifact bindings remain retained, including disk-exhaustion build/capture
failures; they are not relabeled as successful final execution.
