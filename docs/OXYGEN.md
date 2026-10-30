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
