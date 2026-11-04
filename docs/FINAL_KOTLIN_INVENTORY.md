# Maintained Kotlin / RN inventory

Acceptance reconciliation at published #118 `d381e8357c390b4e060c9a1e3a5bbd8973c1654c`
(#113–118 above #112). This records implementation and exact evidence, not merged
or maintainer-accepted scope. GitHub issues/project remain the roadmap.

## Verified source inventory

- `app/src` contains **zero Java source files**, including application and tests.
  Generated build outputs and third-party SDK bytecode are excluded.
- `git ls-files '*.java'` returns no maintained tracked Java source.
- No tracked JS/JSX/TS/TSX application source, `package.json`, npm/yarn lockfile,
  Metro/Babel configuration or Node dependency directory remains.
- Source/build-rule scan finds no React Native runtime dependency or bridge
  (`com.facebook.react`, `ReactNativeHost`, `ReactApplication`, `node_modules`).
  Historical Markdown mentions describe reference behavior, not executable RN.
- The three Circular Java widgets and their exclusive attributes are removed;
  no application/source/resource/build-rule references remain.
- Installed listener aliases, widget/receiver names and the Privacy component
  remain intentionally: they preserve existing Android grants and entry points.
  They are Kotlin compatibility components, not obsolete RN runtime bridges.
- XML resources and native widget interoperability remain where still used.
  MokedPage's ViewPager is the tested swipe/Fragment-state bridge; retained XML
  is not evidence of remaining Java or an excuse to delete live resources.

## Exact remaining acceptance work

- #19/#35 feature-specific production parity and approved-retirement disposition
  remain required. Android font scaling is approved; this is not pixel equality.
- SMS policy is approved: RN form punctuation/trimming except ambulance, any
  nonblank optional visa/address, native catalog order, and full Hebrew team-region
  names independent of locale with no trailing colon. Four-person cap is settled.
- Authorized nonproduction live FCM/backend/SDK delivery, real test-ad delivery
  and console configuration remain distinct from offline fixtures/source audits.
- By explicit maintainer agreement, actual Play/production-signed native/RN upgrade
  acceptance is **deferred to release readiness (#11)** while the account is locked;
  it no longer blocks milestone2. Local migration verification remains in #27.
  Locally signed installed-byte checks do not establish signing continuity.
- OEM/physical-device audibility, prolonged idle/background reliability and
  unobserved/withdrawn notification gaps remain documented limitations. The
  inclusive45-second recovery policy is bounded recovery, not zero loss.
- Final integrated checks are complete for the scopes in the matrix below;
  their passing evidence does not establish every state or maintainer approval.
  Source inventory alone does not close #21–#27.
- CI #13 and historical crash triage #10 are separate subsequent/backlog work,
  not invented prerequisites for milestone2 (#27's current issue definition).

Raw tracked-file/runtime-reference audit and integration build/test evidence:
`~/.local/share/opencode/evidence/m2-integration/final-ui/`.

## Acceptance matrix — #19 feature inventory

Evidence roots below: **E** = `~/.local/share/opencode/evidence/`,
**C** = `~/.cache/mymada-m2/evidence/`. These are retained local artifacts,
not files automatically available to a fresh checkout. Repository docs describe
contracts and evidence provenance; raw images/logs require access to this archive.
Earlier private snapshots remain historical; published successors are listed here.

| Issue / feature | Completed implementation / local evidence | Remaining acceptance |
| --- | --- | --- |
| #15 local coverage | Final task set: debug/test/release, both lints, **19 JVM tests**, E/m2-integration/final-ui/build.log. Final ordered UI matrix **17/17 EN + HE on each API24/37**, E/integrations/combined-host-accessibility/fixed-sequence-*.log. Feature previews and focused checks are retained in source/owning PRs. | Existing broad preview/state and screenshot checkboxes are not satisfied by a 17-test subset; review per-feature evidence, not a coverage quota. CI #13 is subsequent. |
| #21 Home/About/Privacy, navigation/onboarding/locale | #39/#53/#107 shell; #118 installed Privacy alias. E/tools-shell/ production/candidate EN/HE font/orientation comparisons; E/m2-integration/shell-above106/ combined checks. Home offline ad-context OOM resolved in #38 (issue35 comment6510666880); not an open heap blocker. | Maintainer visual review of existing comparisons. Native settings→overlay→privacy onboarding order versus RN privacy-first still needs disposition. Broad process/adaptive claims must stay scoped to recorded cases. |
| #22 Settings / persisted data | #40–52, full Compose #112; C/history-upgrades/fixed/ actual local install-r, settings/history original backups. C/api37-owned-final/RESULTS.md resolves day/night input failure and external audio/picker-return checks. E/m2-integration/settings-combined/ verifies final Settings integration. | Review visual comparisons and retain local final-candidate migration scope. Production signing deferred to #11. Volume writes only on OK is approved, not pending. Picker-process death is not established by app-process death. |
| #23 calculators/timers/history | #46/49/51/54/58: APGAR, Pulse, Glasgow, Oxygen, contractions; #45 history compatibility. E/m2-integration/shell-above106/ final adapters and docs/OXYGEN.md; retained feature PR evidence. | Review feature-specific production comparisons; do not reopen approved native Oxygen truncation/bounds or contraction expiry after observed absence >20min. |
| #23 vitals/dictionary/documents | #55–57 retain all three families; validated vitals ranges approved. Native PdfRenderer replaces obsolete PDF runtime. E/documents/acceptance/FOLLOW-UP.md proves actual API24 Android Storage downloads of BLS/ALS/Kuntres (137/290/30 pages), hashes, corrected 4× rendering; API37 actual-file offline checks. E/dictionary/EVIDENCE.md records matched empty/add dialogs and fixture rows. | Authorized Vitals/Firestore dictionary reads/edit acknowledgments and populated production dictionary/cache fixture. Production document cover loaded, but full interactive production paging/zoom comparison remains unverified; do not repeat proven candidate PDF downloads as a missing SDK gate. |
| #24 calls/overlays/background | #59–75/#93/#102/#111/#118; target37, native listener aliases, shared permission UX, popup Compose and cleanup. E/metronome-call-compose/combined-c1e4213c/RESULTS.md and E/m2-integration/metro-above108/: **actual OS notification→automatic full popup with overlay granted**, source actions, restoration, 45s positive/negative recovery on both APIs. | Authorized physical/OEM audibility, screen-off/long idle/reboot and regrant reliability. Bounded ≤45,000ms recovery is approved; withdrawn/unobserved events and zero-loss recovery are not promised. Notification-tap fallback is not substituted for automatic-popup evidence. |
| #25 metronome/widget/tile/audio | #62/64/68/#110 Compose/#118 receiver: notification/widget controls, process restart, cleanup and volume restoration in same combined evidence. Target37 prerequisites have executed local evidence; target33 hold and private-Compose claims are obsolete. | Physical tempo/audibility and representative audio contention on authorized hardware; local no-audio emulator checks cannot establish these. |
| #26 activation/contact/dispatch | #87–96/#104–109/#113–116: all listed feature UI layers published. E/integrations/{rtl-accessibility-final,swipe-rootcause}/ records actual draft process restoration and release gestures; E/integrations/combined-host-accessibility/ proves final corrected test against frozen combined app bytes. Approved SMS follow-up above #119 is separately checked in E/m2-integration/approved-policy/. | Full-modal visual review and native/RN onboarding/button-direction differences require disposition. Pending backend-write process death and live identity/FCM delivery are not covered by saved unsent drafts. |
| #26 SDKs | #90/#91; docs/SDK_VALIDATION.md, E/integrations/{sdk-test-ads,sdk-events}/: demo resources, event payloads, local crash identity, Performance integration. | Authorized live test ads and server-side event/crash/Performance/FCM receipt; no new consent or security redesign is implied. |
| #27 final Kotlin / cleanup / platform | #117/#118: zero maintained Java/no RN runtime; retained aliases/XML are live compatibility, not debt to delete. At #118 app/test tree equals owner1070f877 excluding Markdown. That optimized installed-byte smoke both APIs: SHA256 `5d30d64f722fa64d10152191fff359a62e18145fabcb5bb3939d09ecab38ede3`; not the later payload-policy artifact. | Feature disposition, local final-candidate migration, live/physical and maintainer acceptance remain; production signing is deferred to #11. API24/37 are the agreed local matrix; no newly invented API33–36 release gate. |
| #28 documentation | README now describes actual target37, Kotlin/Compose architecture, signing/configuration, exact executed local task set and scoped instrumentation. Existing AGENTS.md remains concise and operational. | Maintainer review of this docs draft. No duplicate roadmap or CI setup added. |
| #35 published parity | Published native12822 supplied AAB SHA256 `9784d64714dcd284160390c242e153ee8bcd12c26c51e83906ec1f8ef16fec5b`; locally signed rendering APK `b08c3d812f4781cfc667edf4240c917350c8083ea91cdb95bc971e0524c09179`. Shell comparisons and C/settings-acceptance/, feature comparisons linked above. | Full inventory and maintainer per-screen review remain open. Matched screenshots are state-specific, not blanket acceptance; unavailable live content needs authorized fixtures. |

### Resolved differences that must not become new blockers

Android font scaling is approved, including API24; do not reproduce native font
suppression. Both rendered team UIs cap at four with seven Hebrew roles. #109 fixes
the inherited request-title locale omission. #77 restores supported fonts/status
bars. APGAR recreation retains its active feature title (existing test contract),
while production retained content but reset the generic title. Final API24 host
failure was a sequence-dependent **null active-window lookup**, not missing input
bounds: #116 queries the unique focused active application window, checks package,
then the unchanged visible/nonempty bounds and real swipe/recreation assertions.
No retry/tolerance/timeout relaxation. Original failures remain in final-ui/.

### Approved product decision: #26 SMS bodies

[DISPATCH_CONTRACTS.md — approved policy](DISPATCH_CONTRACTS.md#approved-sms-policy-follow-up-above-119)
records the implemented decision and caller trace. The table below retains the
historical alternatives: choose **RN form punctuation/trimming and optional-field
rules**, **native code order**, and **full Hebrew team header without a colon** in
both UI locales. The stale billing-mode bug remains fixed.

| Field | Historical native | Historical RN |
| --- | --- | --- |
| Ambulance / labels | `אמב' 1234`; `חולה נפגע 56`, `התחייבות 78`, `סכום 90`, `קוד 2000, 5000` | `אמב: 1234`; colon after each other label |
| Whitespace / optional visa,address | Preserve whitespace; optional fields included only at length >1 | Trim values except ambulance; include any nonblank optional value |
| Select 5000 then 2000 | `2000, 5000` (catalog order) | `5000, 2000` (selection insertion order) |
| Jerusalem team header | `צוות אמבולנס י-ם 1234:` | HE `צוות אמבולנס ירושלים 1234`; EN `צוות אמבולנס Jerusalem 1234`, no colon |

Roles stay Hebrew. Regional recipients, literal body extra, shared confirmation,
161 catalog rows/149 IDs/12 sections, four-person cap and cleared-billing behavior
remain unchanged. Exact tests now lock the approved formatting. No SMS is sent.

### Signing / upgrade: release deferral and retained local scope

The maintainer explicitly defers actual Play/production-signed upgrade verification
to release readiness (#11) because account access is locked. This is a scope
decision, **not a passing signing test**, and must not be reintroduced as a milestone2
blocker. Local migration correctness and final-candidate fixtures remain in #27.

C/ACCEPTANCE-2026-10-30.md records real install-r intervals without clearing data:
native UI-authored history and RN startup plus **seeded** 14 settings/epoch history.
RN fixture source `be72a163`, optimized Hermes with dummy Firebase configuration;
all fixtures use local signer `9c2b6863e7a135c9fe990e33a36e3b2fbc9a583e9ed5c4b04eb2825e9784ce8d`.
Candidate in that upgrade is `d0f60b40`, not final #118. These prove compatibility
for those values, not Play certificate lineage or every upgrade surface.

Maintainer must supply an authorized release/test distribution path with compatible
signing lineage and actual native/RN installed-version provenance (and confirm the
maximum published versionCode; 12828 remains development). On a designated disposable
test device/profile, upgrade without uninstall/clear and retain before/after settings,
activation, chat identity/unread, histories, widget placement/actions, listener grants
and entry-point results. Do not request private signing keys in chat or re-sign a
production install with the local debug key. Signing access is tracked in #11;
no new release requirement is added here.

### User-dependent execution, not more offline reruns

1. **Product / visual review:** SMS policy is approved; review matched
   production/candidate comparisons in #35, including full modals, localized action
   direction and onboarding order. Approve actual differences explicitly; candidate
   captures alone cannot close these items.
2. **Nonproduction backend authorization:** provide project configuration and scoped
   test data/accounts for codes/activation acknowledgement, chats/name/unread/token
   identity, cold/warm FCM, Vitals and dictionary. For remaining production document
   interaction comparison, authorize the necessary read-only viewer access; the
   prior scoped GET-only Storage download evidence is already complete. Authorize test
   writes only there; observe SDK event/crash/Performance receipt and demo-ad loading/
   dismissal in its console. Retain pending-write kill/restart outcomes separately
   from local draft restoration. Existing production configuration is not permission.
3. **Authorized hardware:** designate a non-personal test device and permitted
   notification/overlay/audio settings changes. Observe actual alert audibility,
   metronome tempo, concurrent alarm-volume restoration, screen-off/idle/reboot and
   listener regrant behavior. Preserve the known bounded recovery limitations.
4. **Release readiness, deferred (#11):** provide the compatible signed distribution/
   test path and installed baselines described above when account access returns.
   Local final-candidate migration checks remain distinct. Owned disposable emulator
   checks are authorized; physical-phone actions await guided coordination.

### This documentation-only reconciliation

This section describes #119. The subsequent approved payload-policy implementation
changes only FormContract and TeamContract in application source. Settings migration,
history, component identities, recipients and picker ordering are byte-for-byte
source unchanged from #119/#118. Existing migration evidence remains valid for its
original scope; there is no new persistence migration to invent or rerun for this
delta. The policy build passes 20 JVM tests and both lints; changed picker/team UI
assertions subsequently passed4/4 in EN/HE on each owned API24/37 emulator.
New final-candidate install-r checks exposed missing RN activity identities, now
addressed in the [local-upgrade follow-up](LOCAL_UPGRADE_VERIFICATION.md). Its exact
artifact/runtime scope is distinct from the older settings/history fixtures.
Native/RN→final optimized candidate now passes on both API24/37 (four install-r
intervals), including locally persisted activation/name/conversation identity,
all seeded settings, exact history/backups/reopen, actual retained widget controls
and OS listener grants. Actual retained RN notification targets pass on both APIs.
No locally executable upgrade blocker remains in this recorded scope; remote
chat receipts/authorization remain the explicit live-service gate, not fabricated
local unread data. Production signing remains deferred to release readiness.

README's final task names/options were checked against final-ui/build.log and the
retained invocation (JDK17, 6GiB, two workers, mapping upload excluded). Instrumentation
package/class/options match final-ui bridge logs and owner fixed-sequence evidence.
No build or runtime rerun is warranted: this layer changes Markdown only. Earlier
local checks and failures are retained, not relabeled as fresh final-artifact runs.
