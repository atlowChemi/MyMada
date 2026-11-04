# Activation / push migration — partial #26

First behavior slice replaces `lockPage`, `codeRequest`, `codeC` and
`MyFirebaseMessagingService` Java with Kotlin. Installed component names and the
published XML layouts remain; Compose conversion is still outstanding. Request
toolbar actions reuse `DetectionControls`, including permission orchestration.

## Preserved contracts

- `codes/{pushKey}` fields: email, name, reason, four ASCII decimal code characters,
  token, allowed, used, time. Preserve leading zeroes. Native time remains string
  epoch **seconds**; RN used numeric milliseconds. No backend evidence authorizes
  silently changing this schema. `firebaseGenratedKey` spelling is intentional.
- Request waits for FCM token, confirmation and acknowledged write before replacing
  the stored request identity. Failed requests remain retryable. A ViewModel owns
  in-flight work across recreation; dialogs contain no SDK callbacks.
- Activation validates code/key and the existing `code`, `used`, `allowed` record;
  `realM` changes only after `used=true` is acknowledged. Missing/malformed flags
  fail closed. The existing read/write protocol is **not atomic authorization**;
  backend rules/transactions remain #8, not claimed fixed here.
- Native `newChat`, `newC`, code-bearing fallback, generic title/msg and RN
  `codeApproval` routes parse null-safely. Service cold starts create `Push`;
  launcher notification+data extras and warm activity intents reach native screens.
  Immutable pending intents distinguish approvals by key/code, not extras alone.
- Token refresh preserves the prior conversation key locally as `chatIdentity`
  before updating `fcm`; Kotlin chat uses that identity. No backend
  conversation move, schema change, or authentication policy change is made.

## Checks / acceptance

`ActivationContractTest` covers exact payloads, leading zeroes, malformed keys,
missing/typed flags and native/RN push variants. `ActivationIntegrationTest` uses
deferred SDK Task fixtures for token/write failures, acknowledgement ordering,
identity retention, recreation and real activity/launcher entry. Run only on an
offline disposable emulator; the capture test records real screen images.

Persistent execution evidence: `~/.local/share/opencode/evidence/integrations/`.
Build, runtime and production-comparison results are recorded with the PR after
execution; test existence is not evidence of passing tests.

## Chat/name/read receipts

`contactChatActivity` and `ChatAdapter` now use Kotlin with the published XML
layouts. Source comparison: native `b36f4a9c`, RN `be72a163` (`ContactUs.tsx`,
`MessagingStore.ts`), and entry callers `HomeActions` / `MyFirebaseMessagingService`.
Both historical clients use `chats/{token}`, `Name`, `LastSent`, `SeenByAdmin`, and
message fields `sender`, `msg`, numeric millisecond `time`, optional `WhatsApp`.
Neither implements a separate unread-count schema; this slice handles the actual
seen/not-seen receipt and chat notification routes rather than inventing a field.

- Existing conversation keys are retained; fresh installs await a valid token.
  No reads/writes/listeners target the old `empty` sentinel. Token failure retries.
- A lifecycle ViewModel owns one listener and pending writes across recreation,
  handles add/change/remove events (including live `SeenByAdmin`), and removes the
  listener on disposal. Malformed messages are ignored, not rendered/crashed.
- Sending uses one multi-location acknowledgement for the unchanged metadata and
  new message shape. It clears the draft only on success; rejected sends retain
  the draft. Name validation rejects blank/numeric names; preferences update only
  after the `Name` write acknowledgement. Name dialog draft survives recreation.
- Native admin/user cards, name action, date format and WhatsApp handoff remain.
  Recycled cards reset all visible state; WhatsApp text/phone query values are URI
  encoded. No WhatsApp activity is invoked by fixtures.
- `ChatContractTest` asserts literal backend payloads and malformed-input behavior.
  `ChatIntegrationTest` exercises pending/failing tokens and writes, stable identity,
  live receipts, listener cleanup, name validation, recreation, and cold/warm chat
  PendingIntent entry with fixture acknowledgements. Screenshots are real app UI.

## Outstanding (do not close #26)

- Compose activation/gate/request UI and full production-state acceptance (#35).
- Chat Compose conversion and token-rotation delivery mapping with the backend.
- Dispatch forms/medical codes/team/custom messages, approved team-size decision,
  exact SMS punctuation/recipient fixtures and encoded special characters.
- Broader native SDK/analytics/performance/crash/ad integration and consent/config
  verification. Existing SDK configuration is preserved, not accepted as verified.
- **Live integration gate:** sanitized sender fixtures and accepted time type,
  backend security/authorization, real FCM delivery, acknowledged Firebase writes,
  process death during an offline queued write, signed native/RN upgrades and
  real integrations remain unverified. No production writes, test messages or SMS
  dispatch are authorized/performed by this slice.
