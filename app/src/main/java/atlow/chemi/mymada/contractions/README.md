# Contractions migration (#23)

Based on PR #53 `b42820cc`, including #45's saved-history bridge. The installed
`fragments.ContracTimer` identity and tools navigation remain intact. Kotlin owns
timekeeping and Compose owns the timer/history; the shared Java `Contraction`
record remains the #45 Gson interoperability model.
History labels use standard `AppCompatTextView` interop inside Compose rows:
disabling API33 fallback-font line spacing matches published API29-targeted
12822 English row geometry. Hebrew row height/baselines still differ and block
visual acceptance; this is not a pixel-exact parity claim.

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

Debug, AndroidTest, optimized release and lint builds passed. The final
`test-{24,37}-settled-{normal,large}.log` runs each passed 9 tests (36 total),
including asserted Activity/Compose font scales of 1.0 and 1.5. API37 uses actual
16384-byte pages. Optimized release install/recovery/completion/reopen and RN
active-only cancel/delete/reopen passed on both owned emulators, with exact raw
backup checks. Release is locally debug-key signed, not production-key proof.
Some API24 large-font screenshots captured transitional/non-app frames despite
passing assertions; those captures do not establish visual parity. Hebrew
geometry and replacement captures remain acceptance work under #35.
