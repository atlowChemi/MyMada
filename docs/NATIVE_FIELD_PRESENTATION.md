# Native field errors and input retention

Labeled fields use native TextInputLayout/EditText measurement; unlabeled
activation cells retain their existing renderer. Compatibility rules preserve
published Material1.0 indicator spacing and target29 caption fallback. Notes
expand beyond their11-line minimum (18-line check passes). Read-only code fields
retain the picker action and reject native edits. Billing contracts are unchanged.

A reproduced typing-loss bug in the first version was fixed at the shared root:
observe edit revisions in composition, so AndroidView cannot replay stale text
before the next composition. The paused-frame regression fails the old APK and
passes the corrected one; three actual Hebrew `bad` input runs retain all letters.
Evidence: `settings-measured-parity/error-inputloss/` (10 focused tests pass).

Combined7ec987ef Request/Form validation passes with five independently opened
matched pairs: initial Notes underline, floating required labels, retained bad
input, reachable code errors and intact partial billing. Communications combined
keyboard/rename/preview smoke passes three methods. Earlier images retain their
original artifact bindings.

Final16e90fd8 adds the relative-focus NEXT fix and tests the actual native IME:

- Native NEXT real-editor test passes1/1 on5942.
- Picker search/clear/no-match/selection/return/order/recreation test passes1/1
  on5982; six changed-state images opened, complete catalog and controls.
- Receipt: `parallel-visual-audit/picker-final-16e90fd8-resumed/RECEIPT.md`.
- Release assembly, lintDebug and lintRelease pass at the combined final tip:
  `parallel-visual-audit/team-field-post-cleanup-release-lints.log`.

Devices restored and transient snapshots deleted. Caption alignment/baseline
differences are accepted under the practical criterion; no pixel-identity claim.
