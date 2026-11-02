# Dictionary (#23)

The existing `DictionaryAdmin` Tools identity, `dict` visibility preference,
toolbar search/add actions and moderation workflow remain. Compose owns the list,
saveable details/editor state and dialog lifetime. Native AppCompat dialogs and
`new_word_dialog.xml` deliberately retain published 12822 geometry, colors and
Hebrew button/title wording (including in English). The replaced Java adapter,
model and Fragment and unused list layouts are removed.

Editable text retains platform fallback spacing: native API37 fields are108/340px
when empty and113/345px when populated at420dpi/font1.0. Do not apply the static
spinner-label fitting policy to editors. API35 locale minimum metrics are disabled
locally. Error captions use named system sans-serif, as the
native error appearance did, while fields/title/buttons retain the bundled font.
Dialog button minima use legacy screen-height qualifiers and both minimum-height
properties. `validationCaptionMetrics` checks native empty-editor/caption heights,
populated editable metrics and required/unchanged validation without backend writes.

## Remote contract / ownership

- Firestore `dictionary`, query `approved == true`; document ID is authoritative.
- New document: `key`, `name`, `definition`, `approved=false` only.
- Existing document: `update` of changed `newName` / `newDefinition` only. Never
  overwrite approved fields, approve locally, or recreate a removed document.
- Both references have no dictionary-specific volunteer or authentication gate.
  Client UI availability is not backend authorization. Rules, actual credentials
  and moderation service behavior require separate authenticated integration.
- Default Firestore disk caching/queued writes are retained. Success is shown
  only after the write task succeeds, not on local enqueue. Pending sends disable
  resubmission and field editing; dismiss/navigation remain available. Failure
  preserves the draft for retry. A new-term draft keeps its document ID across
  Activity recreation, so retrying that draft does not allocate another document.
- The ViewModel owns pending tasks across Activity recreation; listeners are
  removed on stop and stale callbacks ignored. Full replacement snapshots handle
  empty/removal/unapproval correctly, unlike RN's removed-entry reinsertion bug.
- Cached results remain visible on read failure, with retry; loading/cache/error
  status is explicit. Cache availability is not proof of network disconnection.
  No guaranteed offline dictionary download or process-death completion receipt
  is claimed. Firestore itself owns persisted pending writes.

## Explicit native/RN decisions awaiting maintainer acceptance

Compared native `b36f4a9c` and RN `be72a163` directly. Native names **and definitions**
search, case-insensitive lexical ordering, trimmed submission validation and
opened-entry edit comparison are retained provisionally. RN uses name-only search,
locale collation, untrimmed input and a document reread before proposing edits.
The conflict is raised on #23 (comment 6521204615); this draft does not silently
declare either reference authoritative. Search submit keeps the current filter
instead of reproducing native's accidental reset-to-all with a nonempty query.
Malformed remote fields are skipped rather than crashing the list.

## Focused checks

`DictionaryContractTest` is pure Kotlin/JUnit (no Android or Firebase): exact
submission maps, changed-field proposals, invalid/unchanged input, safe decoding,
document identity, ordering and English/Hebrew/definition search.

`DictionaryScreenTest` uses an in-memory source through the real Fragment,
ViewModel and UI. It checks delayed loading, empty/full/removal snapshots,
cached/error/retry states, add/edit validation, exact payloads, delayed success,
failure/retry with stable ID, pending-task/editor/search restoration and stale
listener cancellation in English and Hebrew. No fixture submission reaches
Firestore. Drawer and system Back retain the Tools behavior. Activity recreation
is not OS process death.

On an owned offline emulator, after installing debug and androidTest APKs:

```sh
adb -s "$SERIAL" shell am instrument -w \
  -e class atlow.chemi.mymada.dictionary.DictionaryContractTest,atlow.chemi.mymada.dictionary.DictionaryScreenTest \
  -e capture true atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
```

Optional `-e language en` / `he` controls capture locale. Repeat at font scale 1.5.
Persistent raw evidence and exact limitations are recorded in
`~/.local/share/opencode/evidence/dictionary/EVIDENCE.md`. #23 remains open; no
live backend writes, production signing continuity or maintainer approval implied.
