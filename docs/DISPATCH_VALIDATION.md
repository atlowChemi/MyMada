# Custom dispatch and shared SMS confirmation (#26)

This layer replaces `MainActivity3.java` with Kotlin and moves the three existing
SMS confirmation/composer paths into `DispatchConfirmation`. Form dictation,
medical-code selection and team editing remain separate migration work.

## Contracts traced before changes

Read all three native fragments, MokedPage and their SMS callers; compared RN
`be72a163` DispatchService, CustomMsg and SmsApproval. All 12 phone numbers and
their order agree. `merhav` retains its zero-based index; invalid indices fall
back to Jerusalem like RN. Team composition's existing region-array access uses
the same bound check to avoid failing before confirmation.

Existing form/team builders feed the same body into the shared Kotlin dialog.
The custom-message field rejects whitespace-only input but preserves every byte
of valid input (leading/trailing spaces, Hebrew, ampersand, hash and newlines).
The ACTION_VIEW `sms:` recipient is separate from the literal `sms_body` extra;
the RN raw body-in-URI bug is not copied. No SEND_SMS permission or direct-send API.
Confirmation freezes recipient and body in fragment arguments across rotation.
Cancel retains the draft; approval opens the user's composer, never claims sent.
Missing composer is reported with a localized message rather than silently ignored.
The published approximate `1 + length/70` count is retained and labeled in code.

The user was asked about the native four-person team cap versus uncapped RN;
this slice makes no team-size or form/team wording change. Those decisions remain
explicit prerequisites for the later team/form migration, not this shared handoff.

## Offline verification and evidence

Persistent directory: `~/.local/share/opencode/evidence/integrations/`.
Owned emulators5980/5982 have non-loopback IPv4/IPv6 OUTPUT rejected. Production
reference12822 preferences were seeded locally with realM/pickedMoked/region11 to
open the existing page; this is not live authorization or backend evidence.
No phone, SMS transmission, external composer or global ADB operation is used.

`DispatchContractTest` locks all 12 recipients and index bounds.
`DispatchIntegrationTest` checks literal Intent payloads for every region and uses
a **blocking Instrumentation monitor** for ACTION_VIEW/sms. Its UI flow exercises
blank validation, frozen recipient/body, rotation, cancel/draft retention and one
intercepted approval. Tests never open an SMS app. Final artifact/runtime results
are recorded after execution, not inferred from these test definitions.

Initial API37 payload test passed. UI test reached restored confirmation but an
unnecessary Espresso closeKeyboard call behind the modal timed out; the failing
log `dispatch-api37-initial.log` is retained. The test must use the dialog's actual
state rather than require an IME acknowledgement from its unfocused activity.

Actual published-production API24 EN/HE custom screens and confirmation dialogs
were captured and opened: `production-dispatch-custom-{en,full-he}.png` and
`production-dispatch-confirmation-{en,he}.png`. Only the first Send (preview) was
pressed; the production approval was never used. Reference body is a local fixture.
Keep comparison content/focus states explicit; a focused collapsed hint differs
from an unfocused expanded hint even with identical XML. Candidate API37 initial
capture showed the inherited toolbar/tab typography and gray status-area issues;
the owning #77 font correction and this layer's status protection address them.

## Remaining #26 scope

Kotlin form dictation/medical codes/team management, team-size and exact wording
decision, Compose conversion, SDK/events/consent and authenticated live Firebase/
FCM delivery remain outstanding. No carrier delivery/signed-upgrade claim; #26
stays open. #78–86 optimization is deferred until after #27.
