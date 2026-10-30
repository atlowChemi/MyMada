# Contractions migration (#23)

Based on PR #53 `6cc97c09`, including #45's saved-history bridge and the shared
locale fix `9612a31b` / regression `bcb4b756`. The installed
`fragments.ContracTimer` identity and tools navigation remain intact. Kotlin owns
timekeeping and Compose owns the timer/history; the shared Java `Contraction`
record remains the #45 Gson interoperability model.
History labels use standard `AppCompatTextView` interop inside Compose rows:
preserving API29's layout-dependent fallback spacing matches 12822: enabled for
RTL/wrapped (`StaticLayout`) text, disabled for single-line Latin (`BoringLayout`)
text. API33+ otherwise enables both. The bundled font is byte-identical to 12822;
no locale-specific padding or font substitution is used.

## Preserved contracts

- Native `b36f4a9c`: end-to-next-start intervals, eligibility within 20 minutes of
  last completion, completed-record history, 180dp timer and split landscape UI.
- RN `be72a163`: start-to-start intervals, eligibility timestamp written on
  disposal, incomplete tail with its original number. Active-only deletion works.
- `ContractionHistory` still backs up original list/counter/timestamp before
  validation or expiry. This slice does not change its parser or backup keys.
- Saved Fragment state keeps completed and incomplete records and unanswered
  recovery/delete dialogs through recreation. Backgrounding does not reset time.
  Explicit delete clears both active and completed state; cancellation keeps both.
- Discard/delete starts a fresh native-default session; the cleared RN format is
  not retained as invisible state that changes again on recreation.
- New native completions persist immediately. RN background checkpoints preserve
  the previous disposal timestamp; destruction updates that timestamp.

## Product decisions still required

1. Should future sessions use native end-to-start or RN start-to-start intervals?
2. Should the 20-minute window start at last completion or page disposal?
3. Native active-only history has no last completion and was never written by
   the native writer. Should a cold relaunch recover it, and what expiry anchor
   should apply? Incomplete native sessions currently survive saved-instance
    recreation/backgrounding and drawer navigation, but not a fresh cold launch.
4. Confirm that explicit discard/delete/expiry should reset to native-default
   provenance, as implemented, rather than retain the cleared RN session format.

Until decided, provenance is preserved, not standardized. Published 12822
comparison and actual API24/API37 (16KB) evidence belongs under
`~/.local/share/opencode/evidence/contractions`; acceptance is not implied by a
successful build. The migration is a partial #23 slice, not issue closure.

Focused instrumentation: `ContractionRecoveryTest` (state/UI/lifecycle) and the
unchanged `ContractionHistoryTest` (native/RN parsing, precision, expiry/backups).

## Verified candidate

At initial migration commit `6e131bcc`, debug, AndroidTest, optimized release and
lint builds passed. The
`test-{24,37}-settled-{normal,large}.log` runs each passed 9 tests (36 total),
including asserted Activity/Compose font scales of 1.0 and 1.5. API37 uses actual
16384-byte pages. Optimized release install/recovery/completion/reopen and RN
active-only cancel/delete/reopen passed on both owned emulators, with exact raw
backup checks. Release is locally debug-key signed, not production-key proof.
The typography follow-up builds debug/AndroidTest and lint, and runs just the two
affected bilingual visual/lifecycle methods on both APIs at both font scales
(`test-visual{24,37}-{normal,large}.log`, 8 passing checks). Captures wait for the
current Activity's focus and rendered frames; owned emulators disable transitions.
`captures-visual*` replaces the earlier transitional API24 captures. Both native
and RN active/completed states, native history, and landscape are retained.
Production comparisons and APK/screenshot hashes are under `visual-final/`.
The earlier 36-test/release evidence is not a claim of rerunning it after this
UI-only change.

## Shared locale integration

The two feature commits were replayed onto published `6cc97c09` with an explicit
old-parent boundary; range-diff confirms the migration and typography patches
are unchanged. `ContractionNavigationTest` exercises real Settings language
selection → Home → Tools in both directions, RN/native recovery, active-state
recreation, completion and reopen. It asserts Activity/application/Compose
locale direction, font scale, numbering, source-specific intervals and raw backup
preservation. Native Gson date strings retain their existing second precision;
RN epoch records retain milliseconds. Integration artifacts are in
`~/.local/share/opencode/evidence/contractions/integration/`.

Integrated debug/AndroidTest/release/lint builds pass. On each API, validation
covers the 9 contraction regressions, the new navigation test, and shared
Settings choice/navigation regressions; two affected visual methods also pass
at 1.5 font scale. API37's initial new-test dialog-focus race was corrected in
the test and rerun on both APIs; unchanged passing tests were not repeated.
Optimized locally signed release recovery/deletion/backups pass on both APIs.
Refreshed screenshots retain the production row geometry and Hebrew typography.
Product choices above remain unresolved; this integration changes no behavior.
