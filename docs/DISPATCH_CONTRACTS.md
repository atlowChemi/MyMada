# Remaining dispatch contracts (#19 / #26)

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

## Exact payload decisions still needed

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
