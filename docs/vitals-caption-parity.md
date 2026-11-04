# Vitals caption and calculated tint

This slice retains the native empty-caption reservation after a valid pulse,
including return to Adult and clearing the field. The caption uses the native
named sans-serif family and no Material-added indicator inset. Numeric editors
retain fitting single-line metrics; the slash uses the bundled native font.
Calculated underline colors use the public ViewCompat API.

Clean source replay:65f66267,6a49d488,a42c4425,38c46073. Final focused assertions
come from verified6a53612a Vitals tests only; Dictionary controls and native metric
probes are excluded. The integration additionally replaces the lint-restricted
AppCompat support-tint call and getter with ViewCompat.

Evidence under `parallel-visual-audit/medical-clearfix/`:

- `integration-build.log`: debug, AndroidTest, release and both lints passed.
- `integration-viewcompat/`: frozen debug/test APK hashes and four passing
  `captionAndTintMetrics` executions (API24/API37, fonts1.0/1.5), including
  rendered red-background pixels, native43px normal caption reservation and
  persistence through clear. Both normal calculated-tint screenshots opened.
- `pairs-api24-normal/`: retained opened native/current Adult and Baby pairs.
  Caption/field/slash geometry matches. Native Adult stale gray/aqua underlines
  differ from corrected calculated colors; focus-dependent thickness is excluded.

This is scoped local presentation/behavior verification, not approval of the
remote Vitals range contract or a claim that every native pixel is identical.
