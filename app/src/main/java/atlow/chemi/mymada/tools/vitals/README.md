# Vitals — #23 migration (draft)

Based on #53 `b42820cc`. Existing `VitalsChecker` Fragment, Tools key `vital`,
navigation, title and Firebase Realtime Database `Vitals` read contract remain.
Compose owns layout/saveable inputs/category; native input/radio/text leaves retain
published fonts, underlines and rounded information cards. No backend writes.

## Exact source comparison

References: native `b36f4a9c95e9d1647f14bee672d45ef9e20adef9`,
RN `be72a1632e8d5137db09a80258e5a51cae3ae19c`, #19 and actual production 12822.
Native `fragments/VitalsChecker.java`; RN `src/containers/Tools/Vitals.tsx`
**and** `src/utils/index.ts:inRange` (the helper matters).

| Category | Pulse | Breathing | Systolic | Diastolic |
|---|---|---|---|---|
| Adult (default) | 60–100 | 12–20 | 90–140 | 60–90 |
| Child | 90–120 | 18–30 | 90–110 | 50–80 |
| Baby | **125–130 native / 126–130 RN** | 22–60 | 60–90 | 40–60 |

Neither source assigns numeric age cutoffs. Labels say beats per minute / breaths
per minute; pressure labels do not explicitly state units. No conversion occurs.

Unresolved clinical/product decisions (native behavior retained provisionally):

- Native bounds inclusive; RN helper is **lower-exclusive, upper-inclusive**.
  Adult pulse 60 is green native, abnormal RN. Baby 126 also fails RN's lower test.
- Native paired BP requires both ranges **and systolic−diastolic 30–60 inclusive**;
  RN evaluates each independently. Adult 90/90 is red native, independently green RN
  except RN's lower-exclusive systolic boundary. Adult 100/90 isolates the paired rule.
- Native diastolic-only stays neutral; RN evaluates it. Clearing systolic resets
  the pair to neutral. Clearing diastolic now resets its tint rather than retaining
  the previous paired color (stale UI defect, not a new threshold).
- Baby fallback 125 versus 126 still requires explicit confirmation.

Inputs remain three ASCII digits maximum, nonnegative integers, empty neutral.
Malformed pasted text is rejected, not silently converted (`-1` or `1.5` must not
become `1` or `15`; overlength paste must not turn `1000` into `100`). Rejected edits
retain the previous value. Native numeric keyboard/length constraints and RN digit-stripping
did not robustly validate arbitrary pasted input. Leading zeros remain accepted.
Systolic advances focus at three digits or two digits not starting with `1`.

## Remote data and lifecycle

One read of `Vitals`, with paths `Pulse`, `Breathing`, `BloodPressure/Systolic`,
`BloodPressure/Diastolic`; keys `Old_low/high`, `Child_low/high`, `Baby_low/high`.
Finite integral nonnegative numbers within Int range and low <= high are accepted.
Missing, string, fractional, reversed or overflowing endpoints fall back **as a
pair**, without combining half a remote range with half a static range. This is
structural validation, not certification that arbitrary remote values are clinical.
Native null-unboxing crashes and RN absent-bound behavior are not reproduced.
No new clinical maximum or persisted schema is introduced.

Fallback is shown during loading/cancellation/offline, like the original; there is
no invented loading/error banner. Remote completion recomposes immediately instead
of waiting for another native keystroke. Listener is removed with the view lifecycle
and late callbacks ignored. Inputs/category use rememberSaveable with a stable view
ID, including Activity recreation; no medical inputs are written to preferences.
Full-screen scrolling at large font/short heights keeps all controls/cards reachable.

## Validation

`VitalsTest`: every range endpoint ±1, empty/invalid input, pressure differences
29/30/60/61, partial fields, baby discrepancy and malformed remote pairs.
`VitalsScreenTest`: actual Tools entry, bilingual inputs/colors, category changes,
clearing and Activity recreation. External evidence is retained at
`~/.local/share/opencode/evidence/vitals/`. Actual OS process-death, production-signed
upgrade and live Firebase contract acceptance are separate from recreation/offline tests.
