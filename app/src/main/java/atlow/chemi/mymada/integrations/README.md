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
  before updating `fcm`; the existing Java chat uses that identity. No backend
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

## Outstanding (do not close #26)

- Compose activation/gate/request UI and full production-state acceptance (#35).
- Kotlin chat/name/unread migration, awaited fresh-install chat initialization,
  live SeenByAdmin changes, token-rotation delivery mapping with the backend.
- Dispatch forms/medical codes/team/custom messages, approved team-size decision,
  exact SMS punctuation/recipient fixtures and encoded special characters.
- Broader native SDK/analytics/performance/crash/ad integration and consent/config
  verification. Existing SDK configuration is preserved, not accepted as verified.
- **Live integration gate:** sanitized sender fixtures and accepted time type,
  backend security/authorization, real FCM delivery, acknowledged Firebase writes,
  process death during an offline queued write, signed native/RN upgrades and
  real integrations remain unverified. No production writes, test messages or SMS
  dispatch are authorized/performed by this slice.
