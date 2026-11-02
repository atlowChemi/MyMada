# Local activation errors

Manual incomplete input has its own native Hebrew message. Backend-invalid and
malformed-intent responses retain the existing invalid-code message. Local errors
use the existing AppCompat alert style, localized Error title and negative OK
action; composition disposal dismisses the window during recreation.

Source handoff: `7ba83034`, integrated as `f2306d8f` above overlay draft #146.
The focused activation test passed on API37, including incomplete `12`, fake
backend-invalid `1234`, and recreation. The integrator opened
`settings-measured-parity/error-fix-v1/pair-activation-invalid-12.png`: native and
candidate contain the complete Error title, exact Hebrew message and OK action.
Small vertical/font differences are within the approved practical criterion.
Release assembly and both lints pass (`activation-helper-release-lints-final.log`).
The first lint failure is retained: the native Hebrew-only incomplete-code copy
needed an explicit `translatable="false"` marker, without changing its text.

Request/Form field presentation and Team adoption are separate follow-ups; this
layer does not claim those states complete. All backend outcomes use local fakes.
