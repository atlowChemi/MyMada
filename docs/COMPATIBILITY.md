# Compatibility contracts

These are application and upgrade contracts, not clinical certification or a record
of test runs. Resolve native/RN disagreements through the
[assessment](https://github.com/atlowChemi/MyMada/issues/19) and existing product
decisions. [Testing](TESTING.md) distinguishes local behavior from live delivery.

## Installed components and navigation

- Preserve Activity, Fragment, service, listener, receiver, widget and tile names,
  preference-key spellings and Intent extras. XML/native interop is intentional.
- The native `MyTileService` is the sole Quick Settings tile. The maintainer
  confirmed the RN `MetronomeTileService` never shipped and approved its removal;
  a previously installed development RN tile may disappear on upgrade and require
  adding the native tile. Widgets and other RN component compatibility remain.
- `HomePage` remains the launcher. RN `MainReactActivity` and `MadaCallActivity`
  aliases retain historical export behavior without a duplicate launcher icon or
  RN runtime. Home routes retained Dispatch/Form, ContactUs, ActivateCode
  (`firebaseKey`) and MetronomeActivity notification targets through existing gates.
- Popup native `callT` takes priority over RN numeric `callType`, including JS
  doubles. RN's process-local source-action UUID map cannot survive an upgrade;
  no reconstruction or zero-loss guarantee is implied.
- `atlow.chemi.mymada.PrivacyActivity` remains the public no-argument subclass of
  the privacy implementation, with its manifest metadata and existing callers.
- `recieversAndServices.ActionReceiver` reads the **`action` extra**, not
  `Intent.action`: `widget` toggles Metro, `stopM` stops it, `runInBG` calls detection
  Stop; missing/unknown extras do nothing. Widget request code 0, Metro stop 9,
  detection stop 13, notification IDs 13/78, channel `runInBg` and legacy widget
  `ON_CLICK_TAG` forwarding remain. Widget-start failures log `MetroWidget` /
  `Control unavailable`; stop exceptions retain their existing behavior.
- `ToolsWindow` owns shell insets, toolbar, modal drawer and locked landscape drawer;
  feature Fragments and `R.id.frame` remain stable. Restore the selected destination
  and existing Fragment; legacy state without the destination key infers it from
  Fragment identity. The active feature title survives recreation.
- Keep `displayFragment(Fragment, toCheck)`, `isDeviceTablet()` and Java-callable
  `SettingsPage.isRTL()`. BLS/ALS/deceased routes share `BlsProtocoles`. Dictionary
  search/add and contraction delete callbacks stay feature-owned; SearchView query,
  collapse and Back behavior remain native interop.
- Tools uses `Settings/Tools`; metronome `running`/`start` intents override hiding,
  and an empty tool set falls back to metronome. Back closes an unlocked drawer
  before leaving; locked landscape drawers do not intercept ordinary Back.
- `MokedPage` keeps ViewPager for real swipes and Fragment saved state. RTL reverses
  physical page order, not content transforms or accessibility bounds.

## Preferences, locale and settings

- `PreferenceMigration` runs in `Application.attachBaseContext` before locale,
  providers and services; migration is independent and idempotent per file.
  Native typed and RN string/scalar/string-set values normalize before consumers.
  Exact originals use `migration.original.<key>` and are never overwritten;
  originals and conversions commit together. Unknown values survive; malformed
  recognized values use consumer defaults.
- Preserve `firebaseGenratedKey` spelling, URI strings and installed preference
  keys. `permissionStatus` is history, not an OS grant. `timeK` seconds must not
  overflow the existing milliseconds conversion; zero volume and delay are valid.
- Tools absent means all; `[]` means none. The UI prevents disabling the last
  visible tool and preserves unknown keys.
- RN application locale has priority over stale `lng`. API33+ application context
  remains unwrapped for LocaleManager; earlier versions update application resources
  and process default with `lng`. Activities attach independently; resumed back-stack
  screens refresh locale. Both languages are bundled offline; toolbar titles resolve
  the app-selected locale rather than retaining the framework's initial label.
- Volume/delay drafts commit only on OK. Cancel/denial affects only the originating
  audio group. OS grants govern storage on API24–32 and audio on API33+; null picker
  results preserve the old URI. `sleep = sleepDay * 3 + sleepNight` commits with the
  changed selection; stable time-dialog tags retain the destination on recreation.
- Native hosted controls retain activity-theme colors where required (including
  the discrete volume slider). Error fields keep native announcements, wrapping,
  hint/underline state and validation.

## Presentation, Home and Privacy

- Android font scaling is approved, including API24. Verify readability, reachable
  controls, clipping and RTL rather than reproducing old font suppression.
  [Approved visual policy](https://github.com/atlowChemi/MyMada/issues/35#issuecomment-6571785809)
  accepts incidental 1px/rounding differences when content and interaction remain
  intact; it does not approve missing controls, clipping or substantial layout changes.
- Bundled fonts use supported resources/native text interop, not mutation of
  static-final Typeface fields. BoringLayout and wrapped/RTL StaticLayout fallback
  spacing differ; editable text must not inherit static-label fitting. Scoped
  dialog sizing resolves legacy usable-height qualifiers and both minimum heights.
  Shell and screen owners consume system-bar insets once under target37.
- Labeled integration fields use native TextInputLayout/EditText measurement;
  activation's unlabeled cells retain their renderer. Notes expand beyond their
  11-line minimum. Read-only code fields reject edits but open the picker. Native
  NEXT follows relative focus; view updates must not replay stale text during typing.
- Home retains tile artwork, destinations, volunteer gate, channels and native
  settings → overlay → privacy onboarding. RN privacy-first remains a product
  decision, not an inferred replacement. Restorable prompts retain destinations
  and dismissal state; fresh launch keeps the existing order.
- Home observes detection preference changes while resumed, including asynchronous
  permission completion; its icon must update without leaving the screen.
- The reserved 50dp empty-ad slot is **transparent when no ad loads** on every API.
  Pending ad callbacks use weak owners/application context and skip destroyed
  Activities. About retains localized credits/disclaimer/privacy artwork and
  native English wrapping without automatic hyphenation.
- Privacy retains real hosted EN/HE policy WebView content, links and the local
  offline error page. Policy language follows the selected app locale. Page
  completion sets boolean `Settings.privacy=true`, **including the error page**;
  there is no new consent button. RN string `"true"` migrates to the native type.
  WebView recreation state is not a persistent remote-content cache. Keep the
  current loading indicator; historical absence of it is not established.

## Calls, permission and recovery

- Native `.NotificationService` and RN `.recieversAndServices.NotificationService`
  share implementation. Grant checks use full component identities and select
  native when both are granted. Never toggle component enablement to request rebind.
- Detection opt-in requires actual notification and listener grants. Revoking the
  last listener grant clears intent/status; transient disconnection with grants
  retained requests rebind. Stop clears `Mapp`, recovery state and status78, not
  listener grants. A grant alone does not opt back in.
- Detection is listener-only. `SMSMonitorService` is a retiring non-sticky identity,
  not an idle foreground keep-alive; `runInBg` status78 proves neither OS binding
  nor delivery. No zero-loss guarantee exists during rebinding or force-stop.
- Each live callback is a separate event, including identical text/key. Preserve
  each delivery's source PendingIntent. Replacement clears only Calls; IDs advance
  beyond active Calls after restart. Filter only contentless GROUP_SUMMARY events:
  ticker/actions/custom views/other content remain parsable; contentIntent alone
  is not content. Flagless malformed events retain fallback handling.
- Parse nonempty notification `EXTRA_TEXT` first, then `EXTRA_BIG_TEXT`, then the
  original short value. Expanded-only updates must not discard a valid call body;
  this does not suppress repeated events or change title-only error fallback.
- After a valid parsed delivery, a missing/blank-body update for the same source
  notification key does not replace the retained alert. Different keys and valid
  repeats remain independent. Protection is in-process, bounded to 1024 active
  keys, and cleared on removal/reconnect/disconnect; eviction or process restart
  returns to fresh-event behavior. No notification borrows another's content.
- Classification retains substring families `mdaemergency`/`mda.health`, exact
  `atlow.mymadaadmin`, native service/admin/on-call/group/shift/legacy precedence and
  admin-title legacy override. Legacy=T3, on-call=T1, group=T2, shift=T3; service T6
  enablement uses T4 audio. Unsplit calls use general audio. Inclusive night bounds
  and combined sleep remain; popup/overlay `statVib` predicates are not identical.
- Post notification fallback before attempted background popup launch when allowed;
  automatic popup opening, closing, recreation and timeout retain it. Explicit
  shade/source-details taps clear only the matching Calls notification; user swipe
  and existing Calls-only replacement remain available. A shade tap opens the original source once
  through the existing Activity without restarting alert playback/vibration.
  Missing/canceled source actions leave readable, silent call details; automatic
  incoming alerts retain their normal alert behavior. This grants no automatic-launch
  exemption. Explicit source actions use API34 sender BAL opt-in and API36
  visible-only behavior. Replacement/close/error release audio, overridden volume,
  vibration, timers and windows without releasing a newer player.
- Popup preserves address scrolling, code spans, admin blink/color, timeout and
  physical drag-cancellation thresholds. Overlay removal retains the 600ms hold,
  original hit bounds, real window enlargement and restoration on leave/UP/CANCEL/
  next DOWN. Its directionless removal container is LTR; bubble placement remains
  stable across repeated MOVE events. Other overlay controls remain available.
- `NotificationPermission` registers lifecycle FragmentResult before requesting;
  return from Settings rechecks permission and app blocking, including below API33.
  Duplicate requests share a prompt. Services never request UI permission and a
  cached grant is not authorization. Notification denial does not stop metronome
  playback; screen/widget/tile Stop remain available.

### Bounded reconnect recovery

Scan only the selected OS-bound listener. Both source `when` and `postTime` must
be positive, nonfuture and **≤45,000ms old at scan and dispatch**, with
`when <= postTime`. A persisted wall/elapsed anchor prevents rollback freshness.
First connection, upgrade, reboot, Stop, observed revocation or grant-identity
change establishes a baseline without replay; unavailable BOOT_COUNT disables
recovery. Suppress same-process pending copies and acknowledge after receiver
processing. Persist revision SHA256 + postTime, not message content. The ledger
caps at 1024 with a monotonic exclusion watermark: overflow reduces recovery,
never live delivery. Only latest active revisions are available; withdrawn or
unobserved events may be lost. A crash after effect but before acknowledgment may
repeat it. This is neither exactly-once nor force-stop recovery.

## Medical tools and stored history

- **Oxygen:** 2.4/20L tanks, 10L/min, approximate 15 PSI/ATM. Whole ASCII digits
  only, inclusive 0–999 ATM / 0–14985 PSI. Reject invalid/out-of-range edits as a
  whole with visible error and retain accepted values. Use native truncation:
  1499 PSI with 20L gives 198 minutes, not 200; duration truncates ATM × liters / 10.
  Defaults 100 ATM / 1500 PSI / 24 minutes. Clearing either pressure clears both
  and shows zero. Rotation retains values/tank/empty state; portrait and landscape
  share bounds, with the 64%/36% landscape split. No cross-tool session promise.
- **Contractions:** new intervals are previous-start → start; imported `previous`
  and `oldEnd` remain exact. `Settings/contractions.session.v1` immediately persists
  active-only rows. The unfinished row stays visible for fresh, native and RN
  sessions. Expire only after **more than 20 minutes observed absence**
  (pause/background/navigation), not contraction time. Configuration change clears
  transient pause. Same-boot elapsed time measures absence; cross-boot wall clock
  is rollback-clamped. Foreground death without departure and first import recover
  without invented expiry. No background timer/heartbeat is required.
  Before expiry or confirmed discard/delete, atomically archive exact payload and
  reason at `contractions.session.v1.archive.N` with live replacement. Cancel keeps
  it; unreadable state/failed commit fails closed. Legacy parser and raw backups
  remain; native Date second precision and RN epoch milliseconds are compatibility
  inputs, not new-session policy. Gson fields `start`, `end`, `oldEnd`, `number`,
  `previous`, `length`, constructors, nullable fields and JVM accessors stay stable.
- **Pulse:** 0–99 counts over 15 seconds ×4; invalid edit retains last valid output.
  Native floor-seconds can display zero before beep. Application-context monotonic
  deadline/beep continues over tool changes, background and rotation; an expired
  saved deadline shows zero without replay. No process-death/reboot beep guarantee.
  Restore original alarm volume on failure/completion.
- **Vitals:** inclusive approved bounds; baby pulse fallback 125–130. Paired blood
  pressure additionally requires systolic−diastolic 30–60 inclusive; diastolic-only
  is neutral. Input allows at most three ASCII digits; reject malformed paste,
  not strip signs/decimals/length. Remote paths are `Pulse`, `Breathing`,
  `BloodPressure/Systolic`, `BloodPressure/Diastolic`, with `Old/Child/Baby_low/high`.
  Accept finite integral nonnegative Int endpoints and low≤high atomically per
  pair; missing/malformed pairs fall back without an invented maximum. Rebind
  immediately, ignore stale callbacks, and do not persist medical inputs.
  Keep empty-caption reservation after valid pulse/clear and calculated tint.
- **APGAR/Glasgow:** retain scoring/bands and selectable state. APGAR English
  “above7” differs from calculation/Hebrew including7; pulse wording leaves exactly
  100 unstated. Do not silently rewrite clinical wording from migration tests.
- **Metronome:** deliberate historical aliased wave: 1000 frames at 8kHz,
  9440/9350Hz + 3800 silent frames, nominal 100BPM, native 30-beat accent (RN31).
  Independently owned tracks serialize partial writes/release; worker monotonic
  origin and volume survive same-boot sticky recreation. Ignore duplicate starts;
  cleanup must not overwrite later external volume changes. Service owns playback
  and timestamps; UI refresh is 500ms, two-minute ring and 650/350ms pulse run only
  while started. Notification/widget/tile controls share state. Physical sound,
  tempo and overlapping feature volume leases need separate evidence.
- Shared `Event` keeps first named registration (including null), anonymous
  duplicates/order and independent mutable snapshots. Glasgow `AnimatedColor`
  retains endpoint alpha, opaque interior HSV interpolation and float arithmetic.

## Dictionary and documents

- Dictionary document ID is authoritative; query `approved == true`. Add writes
  only `key/name/definition/approved=false`. Edit writes only changed
  `newName/newDefinition`; never overwrite approved fields or recreate removed
  documents. Search names and definitions, case-insensitive lexical order, trimmed
  validation, comparison against the opened entry and retained submitted filter.
- Skip invalid remote fields. Pending disables editing/resubmission and retains
  draft ID through recreation. Success follows Task success, not enqueue; read
  errors retain cache with Retry and snapshots replace the whole list. There is
  no process-death completion receipt or offline-download guarantee.
- Documents use OS PdfRenderer, not a packaged native PDF engine. Validated
  downloads atomically replace private `files/documents`; interruption/corruption
  never replaces a good copy. Cache-first is labeled; Refresh requests latest and
  failure preserves saved content. No TTL or backend write. Raster zoom max4× uses
  serialized bounded visible-region rendering: 720×1024 at ≤64MiB heap, otherwise
  1440×2048. Raster output supplies no text selection/search/full text accessibility.

### Deferred viewer presentation — issue #145

Refresh/page/zoom rows lower the cover and clip its bottom. Viewer chrome/viewport
work remains deferred under [#145](https://github.com/atlowChemi/MyMada/issues/145).

## Activation, chat and dispatch

- Activation `codes/{pushKey}` retains `email/name/reason/code/token/allowed/used/time`:
  four ASCII code digits including leading zeros and native **string epoch seconds**
  (RN used milliseconds). Request identity changes only after token, confirmation
  and acknowledged write. `realM` becomes true only after `used=true` acknowledgment;
  malformed flags fail closed. The protocol is not atomic authorization.
  Preserve cold/warm native/RN push variants and key/code-distinct PendingIntents.
- Code cells mask digits, advance on entry, move back on deletion and save state.
  Request fields preserve multiline drafts and validate email/required values;
  trimming occurs at the confirmation boundary. Manual incomplete input uses its
  native Hebrew error; backend-invalid/malformed intent retains invalid-code copy.
  Local alerts retain localized Error title and negative OK action.
- Token refresh pins the prior conversation in `chatIdentity` before changing
  `fcm`; this does not migrate backend delivery mapping. Chat is `chats/{token}`
  with `Name/LastSent/SeenByAdmin` and message `sender/msg/numeric millisecond time/`
  optional `WhatsApp`. Never use an empty sentinel or invent unread-count schema.
- Multi-location send acknowledgment clears the draft only on success; name
  preference follows write acknowledgment. Rejected send retains the draft.
  Lifecycle listeners process add/change/remove, reject malformed events and
  remove stale callbacks. Encode WhatsApp query values. Pending work survives
  recreation without duplicate writes; this is not queued-write process recovery.
- First blank-name launch queues disclaimer then noncancelable name above it;
  restored fragments own recreation. Chat's lone action is physically right in
  both locales, while text/input remain locale-directed. Resize for IME so header,
  name and the whole Send control remain visible.
- The native chat-name editor fills the dialog width with a minimum 48dp touch
  height; empty-field taps and draft editing remain usable across recreation.
- Dispatch retains 12 region recipient mappings and zero-based `merhav`; invalid
  indices fall back to Jerusalem. Confirmation snapshots region/body across
  recreation. ACTION_VIEW `sms:` recipient and literal `sms_body` are separate;
  no SEND_SMS or direct-send API. Cancel retains draft, approval opens a composer
  without claiming sent, and missing composer shows localized feedback. Retain
  approximate `1 + length/70` count. Preview fills width with content-directed
  paragraphs: Hebrew right, English left, including mixed role/name lines.
- **Approved form policy:** RN punctuation, trim body fields except ambulance,
  optional visa/address whenever nonblank (even one character), trimmed/nonblank
  required/paired billing validation. Clearing both billing fields removes billing
  mode. Native medical catalog order wins over selection order. Labels include
  `אמב:`, `חולה נפגע:`, `התחייבות:`, `סכום:`, `קוד:`.
- Medical catalog retains 149 IDs/12 nonselectable sections (161 rows), category
  colors, code/text search and `לבבי` cardiac alias without duplicate matches.
  Dialog query/selection restore; results follow catalog order.
- **Team:** insertion order, four-member cap and seven Hebrew roles. Header always
  uses the full Hebrew region independent of UI locale, without colon:
  `צוות אמבולנס ירושלים 1234`. Keep member-name handling, edit/delete and empty-team
  feedback. Member editor uses native name/role arrangement/spinner and stacks for
  large text; role/draft survive recreation. Add returns below four members.
- Custom messages reject whitespace-only input but preserve valid input byte-for-byte,
  including whitespace/newlines. Shared confirmation adds no trimming.

## SDK event contracts

- Debug uses Google demo banner/interstitial IDs; release retains production IDs.
  Ad failure/dismissal navigation stays intact. Offline ID/layout checks do not
  establish loading, impressions or mediation/consent behavior.
- `SdkAnalytics` uses Home/Tools/Lock/Dispatch/ContactUs/RequestCode/ActivateCode/
  About/Privacy/Settings for both `screen_name` and `screen_class`. Initial Home
  and repeated same-route resumes/recreation do not emit another manual event;
  returning from another route does. Native automatic events and metronome
  SELECT_CONTENT remain.
- `rate_us_prompt` retains `launchCount/firstLaunchDate` at five launches before
  the three-day guard; rating preference keys, opt-out, actions and market URI stay.
- Crashlytics user ID follows current delivery token, independently of pinned
  chat identity. Performance retains automatic integration without custom traces.
  No new consent manager/collection override is inferred from SDK defaults;
  deployed configuration and server receipt require authorized service evidence.
