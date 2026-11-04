# Remaining dispatch contracts (#19 / #26)

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

## Compose accounting

#26 owns its feature-screen Compose conversions as confirmed by the integrator;
#21 establishes the shared Compose/lifecycle/latest-target foundation. The present
chat, activation and dispatch behavior layers retain native XML. They do not count
as Compose screen conversions. MokedPage, form/team/custom layouts and their dialogs,
activation and contact remain to reconcile with the milestone's Compose acceptance.
SDK layers have no screen-conversion scope. Keep this distinction visible rather
than checking full UI completion from a Kotlin implementation or XML parity test.
