# Remaining dispatch contracts (#19 / #26)

Current status: all remaining UI layers are published in #113–116, Circular cleanup
in #117 and component bridges in #118. The private/pending statements below are
chronological evidence at each snapshot, not current implementation blockers.
Final combined verification and remaining acceptance are reconciled in
[the acceptance matrix](FINAL_KOTLIN_INVENTORY.md). The maintainer subsequently
approved the following SMS policy; earlier provisional-native results below are
historical, not current expected payloads.

## Approved SMS policy (follow-up above #119)

- Form uses RN label punctuation and trims every body field except ambulance.
  Optional visa/address are included when nonblank, including one-character values.
  Required fields and paired billing validate trimmed/nonblank content; clearing
  both billing fields still removes billing mode.
- Medical codes retain **native catalog order**, not selection insertion order.
- Team header always uses the full **Hebrew** region name, regardless of UI locale,
  without a colon: `צוות אמבולנס ירושלים 1234`. Hebrew roles, member order, four cap,
  recipient numbers and existing member-name handling are unchanged.
- Call chain: `FormScreen → FormContract.body` / `MainActivity2 → TeamContract.body`
  → `DispatchConfirmation.show` snapshots the region/body → `intent` retains the
  literal `sms_body` extra. Custom messages also use confirmation but do not pass
  through either formatter. No trimming is added to shared confirmation/custom text.
- `DispatchContract` maps all 12 indices to the existing RN/native recipient list;
  its Hebrew names supply team headers, with invalid indices falling back to Jerusalem.
  `MedicalPickerDialog.checked()` filters the catalog in its original array order.

Reference trace: RN `be72a163` FormDictation.onSend, TeamReport.onSend and
DispatchService. The approved Hebrew-only team header and native ordering are
explicit departures from RN's locale-sensitive header and insertion ordering.

Local checks: exact JVM bodies under EN/HE default locales, every Hebrew regional
name, one-character/blank optional fields, whitespace-only required/paired fields,
all roles/cap/recipients. Updated real-picker regression selects5000 before2000 and
expects2000,5000 through recreation and confirmation. Device execution is separately
reported; compiling that regression does not count as a runtime pass. Evidence:
`~/.local/share/opencode/evidence/m2-integration/approved-policy/`.

This follow-up passed 20 JVM tests, debug/test/optimized unsigned release assembly
and both lints (1m36s). The new exact EN/HE locale and all-region payload checks ran
on JVM. The user clarified that owned disposable emulators remain authorized.
Reverse-selection and updated UI confirmation assertions then passed **4/4 each**:
API24 EN12.879s/HE12.908s; API37 EN14.757s/HE14.256s. These use the frozen #120
debug/test APKs, with real picker/recreation/confirmation and no SMS approval.
Unsigned release SHA256:
`44b177410432fff12666413981402ba5631b4de06304d214a8569d2bb7855db4`.
The subsequent local-upgrade check found missing RN activity identities; its
correction/evidence is documented in [local upgrades](LOCAL_UPGRADE_VERIFICATION.md).

## Approved accessibility policy

Maintainer approved Android font scaling across migrated screens instead of
reproducing production API24's suppression. Font-size differences caused by
that policy are intentional, not a product blocker. Readability, reachability,
clipping and RTL still require verification; this does not approve other
geometry differences or restored-title behavior.

## Private follow-ons awaiting coordinated attachment

Chat content, saved draft, seen status, WhatsApp handoff and name/disclaimer/
error dialogs are now Compose on the private chat layer (`106f303e`). API24
chat fixture passed 3/3 in 3.782s, including pending send/rename recreation,
acknowledged clearing and listener cleanup. Raw log: `chat-dialog-compose-5980.log`.
Live backend identity mapping, queued-write process death and production visual
parity remain unclaimed; final combined/release/API37 verification is pending.

The next private layer converts member name/role entry, empty-team feedback
and shared dispatch confirmation to Compose. API24 combined dispatch/team/picker
fixture passed 6/6 in 16.07s (`team-dialog-compose-5980.log`). Seven role choices
are retained. The fixture verifies saved member draft/role editing, four cap,
literal recipient/body snapshots, action ordering, and blocked SMS approval.
Final combined/release/API37 and production visual verification remain pending.
These private layers do not reserve an append window or modify shared shell APIs.

The private full-form layer replaces MainActivity1.java with Kotlin/Compose and
an explicit native payload contract. All 161 catalog names/IDs/categories were
compared against the pre-conversion arrays and match exactly in original order
(`form-catalog-before.json`, `form-catalog-parity.txt`). An initial transcription
omitted a word in code8006; the comparison caught it before build and it was
corrected. Unit checks cover optional length>1, whitespace, paired billing and
cleared billing mode. API24 dispatch/team/picker passed 6/6 in 14.977s, including
restored form values/selection and literal preview (`full-form-compose-5980.log`).
Full combined release/API37/production visual and killed-process checks remain
pending. Native SMS policy is still provisional; this conversion does not
approve native/RN differences or claim whole #26 completion.
API37 follow-up dispatch/team/picker/chat passed 9/9 in 24.504s
(`full-form-chat-compose-final-5982.log`). The first run timed out inside
Espresso's keyboard-dismiss helper after recreation; the form's Compose submit
already dismisses its keyboard, so that redundant fixture action was removed.
The original failed log is retained; this is not final combined-release evidence.

Private Moked host now uses Kotlin with Compose header, tab controls and region
prompt; native ViewPager remains the swipe/Fragment saved-state bridge. The
pre-R inset boundary is retained. Detection still calls DetectionControls with
the existing menu model and observes async preference updates. API24 dispatch/
team/picker passed 7/7 in 16.779s (`moked-host-compose-5980.log`), including
tab/pager synchronization, selected-page recreation and prompt dismissal state.
Final combined/API37/release and production parity remain pending.

Private visual follow-up compares the retained production request/catalog
captures with Compose: removes new purple/filled-field styling, restores native
accent/dialog palette and zero letter spacing, adjusts request action margins,
and restores catalog separators/right-aligned confirmation. This is a shared
integration theme, not a change to the approved Android font-scaling policy.
Chat/member dialogs scroll at enlarged font sizes.

Normal-size private integration tests passed 17/17 on API24 (30.406s) and API37
(42.838s). At 2× Hebrew font, initial runs exposed off-screen chat action taps
and a medical selection attempted beneath the keyboard. The fixture now scrolls
to dialog actions; medical search exposes a Done action to hide the IME and
selection is asserted before recreation. A mistaken chained fixture call caused
one compile failure and was corrected. Focused medical/chat reruns passed 5/5
on both APIs (12.751s / 19.95s). Logs are `ui-parity-*.log`; font scales were
restored afterward. These checks are not landscape or final production-parity
acceptance and do not replace final combined installed-release verification.
Follow-up chat landscape checks at 2× Hebrew font passed 3/3 on API24 (5.785s)
and API37 (6.986s); captured name-dialog images are 1920×1080. Evidence is under
`ui-parity-large/chat-landscape-*`. Other integration activities retain their
existing manifest portrait lock. Original font/rotation settings were restored;
API24's originally absent user_rotation required deleting the temporary setting
after a rejected attempt to write the literal `null` value.
The first full private build caught two Compose lint errors in chat locale/
fallback-name reads. Both now use observable Compose locale/resource APIs;
the subsequent unit/debug/test APK/release/lintDebug/lintRelease build passed
(`owned-compose-private-locale-build.log`). This private artifact includes all
later layers and is distinct from the coordinator's shell/request integration.
That private artifact subsequently passed all 17 tests on API24 EN29.288s /
HE29.647s and API37 EN41.239s / HE40.651s. Its optimized runtime check then
exposed an API24 RTL accessibility defect: mirrored pager/fragment transforms
produced zero input bounds despite correct rendering. The next private fix
reverses physical page order in RTL instead, preserving logical tab/swipe order
without transformed content. UIAutomator now reports real visible input bounds;
the host regression passed 1/1 in 1.902s after waiting for accessibility events
to settle. Earlier test failure and zero-bound XML are retained. The RTL fix
still requires its final combined/release matrix; earlier 17/17 evidence does
not establish those new bytes.

RTL source `2931c87b0d51527aa097c8ff6f07a44a5d3dc74b` subsequently passed
unit/debug/test APK/release/both lints (2m16s), and 17/17 on API24 EN29.653s /
HE29.454s and API37 EN39.772s / HE39.282s. Signed optimized APK SHA256:
`9dab59fbf0830dee6b5a92ea896d8e85190ffde895aeb07170f25a507b071d71`.
Pulled installed bytes matched on both devices. Actual background SIGKILL
restored three form fields and an unsent chat draft on both APIs; chat used
local offline fixture identity with original preferences restored afterward.
Evidence: `rtl-accessibility-final/`; no fatal exception/signal matched the
collected release form/chat logs.

Direct gesture coverage added afterward FAILED on API24 EN: swipe gestures did
not reliably change the expected logical page. Center/slow/lower-content and
accessibility-idle attempts are retained as `swipe-*.log`, with the pending
regression in `pending-swipe-regression.patch`. The passing 17-test matrix covers
tab navigation, programmatic pager selection and recreation, not actual swipe
acceptance. Root cause and API37 gesture verification remain open; this blocks
host/RTL acceptance. No application bytes changed during these fixture trials.

### Direct-swipe investigation resolved (private follow-up)

The subsequent investigation traced the apparent API24 failure to input targeting,
not ViewPager's RTL mapping. The pager measured `[0,369]`, size `1080×1425`, while
the IME covered the lower screen. Its center/lower drag coordinates landed in the
keyboard. A temporary Window.Callback trace recorded no activity touch events for
the failing drag; `swipe-before.png` captures the occlusion. Closing the keyboard
only once was insufficient: moving to another input-bearing page reopened it.
ADB swipes on unobscured content worked, including cold-start Hebrew in the
optimized application. No host or shared-shell change was needed for this failure.

The native production12822 baseline was installed locally on owned API24, then the
candidate and original preferences were restored. With a single unambiguous EN
locale preference, real swipes reached form → team → custom; the reverse gesture
worked after hiding the custom-page IME. Initial native captures had duplicate
locale preferences and a keyboard contacts-permission dialog (denied), so those
attempts are retained but excluded from parity conclusions. This is gesture-flow
evidence, not broad production visual acceptance.

The host regression now dismisses the IME before **each** real Espresso drag and
asserts logical tab selection for 0→1→2→1→2, then recreation. It replaces the old
programmatic page jump; it does not synthesize tab clicks for the swipe assertions.
Accessibility-event idle synchronization remains at the existing 500/5000ms bounds;
no timeout increase or application gesture workaround was introduced.

Evidence: `~/.local/share/opencode/evidence/integrations/swipe-rootcause/`.
Affected dispatch/team/picker suites passed 7/7 on API24 EN22.585s / HE22.077s and
API37 EN28.05s / HE27.468s. The actual-swipe/accessibility/recreation regression also
passed at 2× font: API24 EN8.648s / HE7.873s, API37 EN8.422s / HE8.441s. Original
font settings were restored. Unit/debug/test APK/release/both lints passed in 1m59s.

The rebuilt optimized artifact (separate bytes from the earlier preserved release)
has SHA256 `745e0415f51cc99297ecb9bb4e7367194e71c9d75d35a9046366893fe3e2025c`;
pulled installed APKs match on both APIs. Independent ADB cold-start gestures
0→1→2→1→0 passed EN/HE at 1× and 2× on both APIs (eight combinations). The script
checks the IME is hidden before dragging and checks selected labels in UIAutomator
XML. One initial API24 2× run raced asynchronous IME dismissal; that failure is
retained separately. The final script waits for UIAutomator's idle hierarchy and
asserts hidden IME before injection (`release-settled-*`), without retrying gestures.
It restores original preferences, app locales and font settings in finally.

Application source remains `2931c87b`; the earlier 17-test/release/process-death
results and raw swipe failures remain intact. This follow-up resolves the direct
swipe acceptance blocker only; other production/live acceptance gates and the
serialized publication queue remain unchanged.

### #27: obsolete Circular Java widgets (private)

Whole-tree tracked source/resource/XML/test/build-rule searches at `d77d1083`
found no callers of `CircularProgressButton`, `CircularAnimatedDrawable`, or
`CircularRevealAnimatedDrawable` outside their own implementations and the
button's unused styleable declaration. The two drawables were instantiated only
by the button; its package-private interfaces had no other implementations or
callers. All five custom attributes were used only by that button. No reflective
class lookup exists in app sources; the test-only dynamic resource lookup in
HomeMemoryTest selects unrelated home navigation IDs/strings. Documentation
mentions describe historical bridge inventories rather than runtime callers.

Remove the three obsolete Java files and their exclusive `values/attrs.xml` as
part of #27's all-app Kotlin migration. Activation and request screens already
own busy, result, retry and confirmation UI in Compose, driven by ActivationModel;
the backend acknowledgement and saved-state paths are unaffected. Shared helpers
and other owners' Java files are outside this deletion. Before/after searches,
Java inventory and validation logs are retained in `integrations/circular-retirement/`.
This is a private follow-up above the validated UI chain, not a rewrite of it or
an assertion that the combined #27 migration is complete.
Post-removal runtime/reference search has zero matches. Unit/debug/test APK/
release/both lint validation passed in 2m42s. Existing activation/request suites
passed 7/7 on API24 EN11.052s / HE11.219s and API37 EN19.478s / HE18.669s,
including token/acknowledgement, retry, confirmation and recreation coverage.
Optimized request/code entry launches succeeded on both APIs; installed bytes
match signed APK SHA256
`827df0192e5cde416c906a0913af50182b77df88d01970801355e6ee9e403d6b`.
The three remaining Java files on this private branch are the separately owned
Tools host, Privacy alias and ActionReceiver; none is changed by this slice.

## Activation request and dialog Compose layer above #106

Tested source `3278850e`: request form, disclaimer/confirmation and request
progress/result dialogs now Compose; activation-code progress/result dialogs
also use Compose. Native toolbar/menu and DetectionControls are retained.
Request fields save across recreation, preserve multiline text, validate email/
required fields, and trim only at the existing confirmation boundary. Confirmed
requests still use ActivationModel acknowledgement; no backend contract change.

Evidence: `~/.local/share/opencode/evidence/integrations/activation-request-compose-final/`.
Full unit/debug/test APK/release/both lint build passed in 5m4s. Seven activation
checks passed per run: API24 EN27.191s / HE21.072s; API37 EN43.114s / HE36.379s.
The preceding API24 Hebrew run failed because the fixture resolved `OK` from
an English context; stable action tags and explicitly localized assertions
correct that test. Prior artifacts/logs remain in `activation-request-compose/`.

Optimized APK SHA256:
`0e84f54010f67e079d0de8d4be6f3069c4b50fe40a7812c0e73884d470269464`.
Both pulled installations match exact signed bytes. Optimized disclaimer/form
launches and candidate EN/HE captures are retained. Actual background SIGKILL
restored request draft `fixture@example.org` on API24 (20866→21100) and API37
(6922→7603), with before/after UI XML and process logs. No submit/production
write occurred. Confirmation recreation is tested separately with fixture text.

Production visual parity is unaccepted; Compose default field/dialog styling
still differs from the native reference. Pending-write process-death recovery,
live backend/SDK acceptance, host/shared confirmation/team dialog/full form/chat
Compose remain open. This layer does not add ToolsWindow callers or change
SettingsPage/permission interfaces.

## Activation-code Compose slice above #105

Tested source `45848422`: activation's four masked numeric cells and submit
button now Compose. Digits use saveable state, retain leading zeroes, advance
focus on entry and move back on deletion. Validation, queued push entry,
backend acknowledgement and local-access grant remain in ActivationModel.
The native toolbar and progress/result dialogs remain pending; request screen
and its confirmation are not converted by this slice.

Evidence: `~/.local/share/opencode/evidence/integrations/activation-compose/`.
Full unit/debug/test APK/release/both lint build passed in 5m31s. Final fixture
APK adds keyboard dismissal before tapping the native request button; app
bytes are unchanged by that fixture-only follow-up. Seven activation checks
pass per run: API24 EN32.258s / HE38.568s, API37 EN94.287s / HE29.532s.
Tests cover masked digit restoration/leading-zero submission, no local grant
before acknowledgement, retained work across rotation, cold/malformed push
entry, token identity preservation and native request confirmation restoration.

Initial masked-value assertion inspected displayed bullets instead of InputText;
the corrected assertion inspects underlying semantics. An earlier Hebrew
request test attempted to tap behind the keyboard; explicit dismissal fixes
that fixture. A combined shell timeout interrupted the last API37 HE run;
the owned package was stopped, logcat retained, and that full seven-test run
passed independently. All failures remain beside final evidence.

Optimized APK SHA256:
`2d0e371d958474769fc9ef2040f9154cbff39ed2b12357f176d61d18e000d63b`.
Both installed APKs match exact signed bytes; optimized launches/UI XML/screenshots
captured on both APIs. Actual process-death restoration and production visual
parity for activation remain open, as do live backend/SDK and request/dialog
Compose acceptance. Tests use fixture acknowledgements, not production writes.

## Medical dialog Compose layer above #104

Tested app source `cab184b1` replaces the medical dialog's native search/list/
buttons with Compose, retaining the owning DialogFragment, argument catalog,
catalog-order results, nonselectable section headers, category colors, cardiac
alias, and saved query/selection. Parent form remains native. Adapter-specific
regression coverage remains alongside the actual Compose dialog test.

Evidence: `~/.local/share/opencode/evidence/integrations/medical-compose/`.
Full unit/debug/test APK/release/lintDebug/lintRelease build passed in 8m33s
after the first build hit its 300s shell limit (raw logs retained). Combined
dispatch/team/picker tests passed 6/6 each: API24 EN17.214s / HE19.828s,
API37 EN45.45s / HE49.14s. Tests cover search, selection, cardiac alias without
duplicate rows, recreation, parent payload and cleared billing-mode behavior.
The first fixture incorrectly used a launching Compose rule; its ComponentActivity
timed out before dialog testing. Switching to the existing empty-rule pattern
resolved that setup failure. The hung owned package was explicitly stopped.

Optimized APK SHA256:
`01814691cbbec1b1fdf41abb670fec87ccbbbd033b8887ac61fd9d064e4f3359`.
Pulled installed bytes match on both APIs. Actual background SIGKILL while the
medical dialog was open restored query `5000`, its checked selection and the
dialog itself: API24 PID16871→17165; API37 PID14038→14844. Before/after UI XML,
hashes and per-PID logs (no fatal exception) are retained. Initial automation
failed because the native form's medical field was below the viewport; the
successful attempt scrolled to the real field before opening the dialog.

Production EN/HE visual parity is still unaccepted; candidate screenshots are
keyboard-visible. Full-form, team-member/confirmation dialogs, host, activation
and chat Compose work and authorized live acceptance remain separate gates.
No SMS, backend write or permission-contract change is part of this layer.

## Team Compose layer above #103

Exact tested source: `4c5875dc87ae381dc9876bf59250628b2995dd22`, descending
from shared-helper #103 (`25a5aef2`). Team content, bounded two-column member
cards and edit/delete menus now use Compose. Member entry and SMS confirmation
remain native dialogs; the four-member cap, seven roles and provisional SMS
contract remain intact. This is partial #26 work, not whole-screen acceptance.

API24 launch originally timed out and test cleanup hung. Sampling evidence
showed repeated inset dispatch; consuming system/stable insets at the legacy
ViewPager boundary below API30 resolved the traversal loop. CoordinatorLayout
already handles system bars. Child-root consumption, focus, clock and layout
experiments did not resolve it and were removed. Failed runs remain under
`~/.local/share/opencode/evidence/integrations/team-compose-rows/`.

Final evidence: `~/.local/share/opencode/evidence/integrations/team-compose-above103/`.
Full unit/debug/test APK/release/lintDebug build passed in 1m38s; separate
lintRelease passed. Combined dispatch/team/picker fixtures passed 6/6 each:
API24 EN 8.862s / HE 8.884s; API37 EN 13.555s / HE 13.911s.
API37 reports 16384-byte pages. A prior combined run selected the wrong window
for the medical catalog; the fixture now explicitly selects the dialog root.
Earlier failures and pre-rebase artifacts remain in their original directories.

Locally debug-key-signed optimized APK SHA256:
`3a040f0649ebef25cb535d32d2e26162f252d12708c9a34e1b9cf641d644beb2`.
Both pulled installed APKs match those bytes. On both optimized installations,
a locally entered team member survived background SIGKILL and relaunch:
API24 PID 13606→13933; API37 PID 31545→31945. Before/after UI XML and per-PID
logs are retained; logs contain no fatal exception. The first runtime script
failed before producing restoration evidence; only the subsequent explicit
before/after checks support this claim. No SMS was sent.

Remaining gates: production EN/HE visual parity, member/confirmation dialog
Compose conversion and process-death coverage for open dialogs/full form,
approved SMS policy, full form/picker/host migration and authorized live SDK/
backend acceptance. Current screenshots are candidate evidence, not approved
production parity. Optimization milestone #78–86 remains deferred.

References: native source inherited by #91; RN `be72a163` FormDictation,
TeamReport, TeamMemberDialog, MedicalCodePicker, medicalCodes and DispatchService.

## Resolved team limit

**Both UIs cap the team at four.** Native hides Add at size4; RN TeamReport
renders Add only when `teamMembers.size <= 3`. The earlier claim that RN is
uncapped inspected the store but missed its rendering guard and is withdrawn.
Keep all seven roles and add/edit/delete, ordered insertion and four-member limit.

## Historical native/RN payload alternatives (decision now recorded above)

For ambulance1234, visa12, addressרחוב א, form56, bill78, amount90,
codes2000/5000, nameדן כהן, native emits:

```text
אמב' 1234
ויזה: 12
כתובת: רחוב א
חולה נפגע 56
התחייבות 78
סכום 90
קוד 2000, 5000
שם: דן כהן
```

RN emits:

```text
אמב: 1234
ויזה: 12
כתובת: רחוב א
חולה נפגע: 56
התחייבות: 78
סכום: 90
קוד: 2000, 5000
שם: דן כהן
```

RN trims optional/required body values except ambulance; native preserves them.
Native optional visa/address require length>1; RN includes any nonblank value.
Native's stale `yishuv` flag incorrectly retains bill/amount mode after clearing
both fields; this is a bug, not an approved payload requirement.
Code order differs too: selecting5000 then2000 yields native `2000, 5000`
(catalog order), versus RN `5000, 2000` (Set insertion order). Preserve native
ordering provisionally along with its wording until that policy is approved.

For Jerusalem ambulance1234 with driverדן כהן, native emits:

```text
צוות אמבולנס י-ם 1234:
נהג - דן כהן
```

RN Hebrew uses `צוות אמבולנס ירושלים 1234` without the colon; RN English uses
`צוות אמבולנס Jerusalem 1234`. Role text stays Hebrew in both. Please choose
the body contract; provisional native wording must not be represented as resolved.
Regional phone mapping/literal SMS extra/shared confirmation are already #89.

## Medical catalog and selection

Both sources contain the same149 numeric code IDs in source order and12 sections.
RN JavaScript numeric object enumeration sorts codes within sections at runtime;
native catalog order remains the published comparison baseline. Catalog literals
stay unchanged in this adapter/model slice. Search by code/text and native's
cardiac `לבבי` alias are retained; matching both predicates yields one row.
Kotlin replaces mCodeAdapt/mCodeKid, retaining native row layout, font and category
colors. Selection has one source of truth per item rather than the native parallel
boolean array; recycled binding detaches its listener before changing checked state.
Filtered headings now use their own item identity, not an unrelated full-list index.
Headers are not selectable. Parent form dialog/state migration remains a later layer.

### Picker row verification

Raw evidence lives under `~/.local/share/opencode/evidence/integrations/`.
Initial full build passed (`medical-picker-build.log`); the UI test first failed
because both hidden header and checkbox matched the text. The narrowed matcher
then exposed a genuine adapter crash: ListView queried a previous position while
filtering. Removed unnecessary stable IDs and retained ArrayAdapter's positional
ID behavior. Both failures remain in `medical-picker/` and `medical-picker-final/`.
Final row/search tests pass API24 **2/2,3.474s**, API37 **2/2,8.291s**, plus
API24 HE actual-dialog **1/1,4.324s**, in `medical-picker-position-fixed/`.
The real form opens all161 rows, selects2000, filters/selects5000, and returns
`2000, 5000`; isolated recycling assertions verify selection and header identity.
Opened actual production12822 and candidate EN modal captures: row font, tint,
height and published data agree. Inherited AppCompat title is heavier/taller and
the focused-search state differs; full-dialog parity is not claimed from row parity.
The owning form/picker-dialog migration will address that title and recreation.
Final application source `0bcd773a` full debug/release/unit/lint build passes in
2m51s (`medical-picker-position-build.log`). Optimized debug-key-signed release
SHA256 `94788f6cf3c72e1a3a403f1672f85a337d1f93a9bb8bc936d227da49ad6b484d`
matches installed bytes pulled from both APIs. Real optimized form-to-picker
launches captured on both, with API37 modal opened for inspection. Initial smoke
navigation landed on MokedPage's default team tab; the corrected run explicitly
selected Form first, with both hierarchy captures retained. No form/dispatch
approval was performed. The final documentation commit changes no APK inputs.

## Team state migration

Kotlin replaces MainActivity2, VolunteerAdapter and Volunteer. TeamModel uses
SavedStateHandle Bundle entries for names/roles; add/edit/delete update one list,
with four-member and role/name validation in the shared model as well as the UI.
MemberDialog is a child DialogFragment and saves draft name/role across recreation.
The adapter owns no Activity and uses current binding positions for edit/delete.
Deleting the last member restores the empty explanation (native previously left
it hidden); deleting below four restores Add. Native card layout, colors, seven
roles, mirrored grid geometry and temporary native SMS body are retained.
Standard button handling replaces the legacy touch interception, retaining click
actions and allowing platform accessibility/touch cancellation behavior.
The owning member dialog restores normal title weight and production right-side
action order. TeamContractTest locks exact provisional native payloads; runtime
tests cover model restoration/limits, real add/validation/edit, dialog recreation,
card retention, remove/add visibility and preview body without SMS approval.

### Private team verification (before Calls restack)

Application source `2bb813a0`; fixture-only update `3020cf0b` closes the IME before
card capture. Full debug/release/unit/lint build passes in 2m03s (`team-build.log`).
Final fixtures under `~/.local/share/opencode/evidence/integrations/team-final/`:
API24 EN 2/2 (3.009s), HE 2/2 (3.389s); API37 EN 2/2 (4.609s), HE 2/2 (4.520s).
Original keyboard-obscured screenshots remain in `team-2bb813a0/`.
Signed optimized APK SHA256
`09d7c635dcfaaef553d28ab6d033077dbe9d9035181052042ec4d579c5fb7a24`
matches pulled installed bytes on both APIs. Real release Add dialog captures
were opened; initial API37 Back navigation exited because no IME was present,
then corrected explicit launch/Add navigation succeeded. No Send approval occurred.
API37 reports actual page size16384. Owned AVD identities and outbound IPv4/IPv6
blocking were verified before runtime checks.

Opened production12822 EN Add/card and candidate captures: card geometry/name
font/overflow placement agree; role tint changes with the selected role as expected.
Dialog title weight is restored; full-modal geometry/focus still differs and is
not accepted as pixel-identical. Candidate HE labels are localized. Attempting a
production HE reference by changing its local `lng` preference produced English
strings with mirrored actions (`production-add-he.png`); this is a retained
failed locale setup, not a valid Hebrew reference. Production source remains the
user-supplied12822 APK (`b08c3d812f4781cfc667edf4240c917350c8083ea91cdb95bc971e0524c09179`).
SavedStateHandle rehydration and Activity recreation pass; an actual killed-process
restore is not established by those checks. Calls publication/restack and final
combined artifact verification remain pending; this team layer stays private.

### Team final append verification above #93

The preceding private results are superseded for publication by exact team-only
source `e9791abf` above Calls `02368337`. Full build2m11s passes. API24 EN/HE2/2
(3.095s/3.065s); API37 EN/HE2/2 (4.266s/4.269s). Raw artifacts/screenshots in
`team-restacked/`, build log `team-restacked-build.log` under the same evidence root.
Optimized release SHA256
`04fcf9560225eadee5f0a6ef52dc44f26d3b0e6b2fe8b4d21303954b43eee544`
matches pulled installed bytes on both APIs; exact optimized Add dialog launches
captured both APIs. No ongoing build/test remains. This documentation-only commit
does not alter APK inputs. Full-modal parity/Compose acceptance remains open.

### Owning form/picker follow-up

MedicalPickerDialog now owns search and selection in saved dialog state. Its
child-fragment result updates the form's saved selection; catalog ordering and
immediate selection semantics remain native. The Java form is a temporary caller
bridge, not a completed Kotlin/Compose form conversion. The fixture recreates the
Activity with a filtered selected row, checks restored query/checkbox, dismisses,
recreates again, then checks both billed and cleared-billing confirmation bodies.
The stale `yishuv` mode now derives from the current two billing fields on every
submission; clearing both returns to the native unbilled body. No payload policy
change is implied. All previews are cancelled without approving SMS.

Application source `8682e810`, final fixture `f35c467e`: full debug/release/unit/lint
retry passed1m53s. Initial command hit the tool's120s timeout during R8; its raw
log remains `form-dialog-build.log`, completed retry `form-dialog-build-retry.log`.
API37 initially selected the Activity root before the asynchronous dialog
transaction ran; the fixture now executes its pending child transaction before
Espresso root selection. Raw focus failure retained in `form-dialog-8682e810/`.
Combined picker/form+team tests in `form-dialog-final/`: API24 EN/HE4/4
(5.759s/5.575s), API37 EN/HE4/4 (8.629s/8.866s).
Optimized signed SHA256
`980480eda35500f98368c7e6dd0a069a7e8994c8f72e7a01990939868ff907ba`
matches both pulled installed artifacts; real release picker screenshots opened
on both APIs. API24 release is Hebrew, API37 English. Initial coordinate-based
navigation selected Custom in RTL; corrected navigation finds the localized tab
in the hierarchy. Initial hierarchy remains for provenance. Full-modal production
parity, killed-process restoration and Compose conversion remain acceptance gates.

### Owning picker final append verification above team/#93

Exact combined source `dbdf3376` (same `app/` tree as the final doc-only restack)
passes full build2m10s in `form-team-restacked-build.log`. Final combined picker/form
and team runtime: API24 EN/HE4/4 (5.881s/5.341s), API37 EN/HE4/4 (8.569s/9.084s).
`form-team-restacked/` contains exact APKs, source, tests, release hierarchies and
screenshots. Optimized signed SHA256
`ad78ee827c90bc3fa0645737fbb4a6f6eb7e40b450bf6acf92ae52f1c4ef6fde`
matches both pulled installations; exact release picker launches captured on both.
All active checks completed before publication; publication does not close the
explicit remaining UI/Compose/backend gates.

## Remaining Compose accounting

Private team-content follow-up replaces the RecyclerView/adapter with a Compose
grid, ambulance input, actions and edit/delete menus, using the existing saved
TeamModel. Four-member limit, seven roles, native card order/tints, literal
provisional header and child MemberDialog remain. The fixture now drives real
Compose add/menu/delete/send semantics, retaining draft/dialog recreation and
snapshot assertions. Member dialog and shared confirmation are still native;
this content conversion does not claim complete team-modal parity.

The next private slice converts the custom-message content to actual Compose
(BasicTextField, Surface action, saveable text/error state), retaining the existing
fragment identity and mirrored-pager counter-transform. The confirmation remains
the shared restorable DialogFragment. The existing literal-recipient/body and
blocked-approval fixture now drives Compose semantics, including restored draft
after confirmation recreation. This is one feature-content conversion, not the
remaining team/form/activation/chat/Moked host conversion or full UI acceptance.

Custom Compose exact source `1d7ea59c`: full build1m46s passes, including debug,
optimized release, unit tests and debug lint. Final combined runtime fixtures
under `custom-compose-final/`: API24 EN/HE6/6 (9.355s/9.314s); API37 EN/HE6/6
(13.796s/14.834s). All12 recipient mappings and literal multiline/whitespace body
remain tested; positive approval is intercepted by a blocking Activity monitor.
The initial API37 fixture raced pending confirmation dismissal/show transactions;
explicit transaction execution fixes the harness, with raw failure retained in
`custom-compose-36fd20b5/`. Initial IME-obscured capture also remains there.

Actual background-process kill (after Home saves state) restores the custom draft
on API24 and API37, with before/after strings and distinct PIDs recorded in
`process-verified-*.txt` and screenshots. Initial `am kill` did not kill API24's
process; initial input setup dropped its first character before saving. Those
failed setup records remain; the corrected check compares the actual pre-kill
editable value with the post-kill value and kills only the verified app PID.
This establishes custom-draft process restoration, not other screens' restoration.

Optimized signed release SHA256
`fc10e4bbcee3d966fadb2743c76b588641c21136238335c5e604b9b5360069bd`
matches both pulled installed artifacts. Exact release custom-screen launches,
hierarchies, screenshots and per-PID logs retained; no fatal exception in either.
Opened production12822 EN and final candidate EN/HE: field underline/action
placement aligned with the reference; typography/focus are not pixel-identical.
The historical production HE capture is keyboard-obscured/empty and cannot prove
HE parity. That acceptance gate remains open; XML confirmation and the other
feature/host conversions remain explicitly unfinished.

#26 owns its feature-screen Compose conversions as confirmed by the integrator;
#21 establishes the shared Compose/lifecycle/latest-target foundation. The present
chat, activation and dispatch behavior layers retain native XML. They do not count
as Compose screen conversions. MokedPage, form/team/custom layouts and their dialogs,
activation and contact remain to reconcile with the milestone's Compose acceptance.
SDK layers have no screen-conversion scope. Keep this distinction visible rather
than checking full UI completion from a Kotlin implementation or XML parity test.
