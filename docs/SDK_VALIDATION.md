# SDK integration follow-up (#26)

## Source contracts inspected

RN reference `be72a163` uses Google demo banner/interstitial IDs in development
(`AdsStore.ts`) and existing production IDs in release. The restored native
debug banner resource and both interstitial callers used production IDs.
This SDK slice selects IDs through Android build-type resources: demo units for
debug, unchanged production units for release. The offline instrumentation check
resolves both IDs in EN/HE; no network request is required for that check.
Callers: `HomeContent`, `HomeActions`, `InfoActivityKt`; no other native ad-unit
callers were found. Ad dismissal/failure navigation and weak references remain
part of their existing screen behavior.

Official demo IDs are documented at
https://developers.google.com/admob/android/test-ads (consulted 2026-10-31):
fixed banner `ca-app-pub-3940256099942544/6300978111`, interstitial
`ca-app-pub-3940256099942544/1033173712`.

Existing Kotlin integrations use Database (`Vitals`, `codes`, `chats`), Firestore
(`dictionary`), Storage (`DocumentStore`), FCM and Analytics. Crashlytics dependency
and Gradle plugin were already present. Performance was present in RN dependencies
but absent from the restored native baseline; RN source contains no custom traces.

RN `NavigationStore` emits screen_view with screen_name/screen_class equal to
the top-level route when that route changes. `RateUsStore` emits rate_us_prompt
with launchCount/firstLaunchDate at five launches, before its three-day guard.
The baseline rate helper logged only to Logcat. Existing native metronome
SELECT_CONTENT remains another event contract. RN MessagingStore sets the
Crashlytics user ID to the retrieved FCM token; before this follow-up the native
client preserved conversation/delivery identity but did not set that crash identity.

No explicit consent manager/UMP or collection override was found in either
source; adding a new consent product flow requires a decision rather than
inventing one during migration. SDK defaults alone do not establish deployed
console consent/configuration parity.

## Demo-ad layer verification

Tested code `a1bea4f6`: debug/release/unit/lint PASS (2m13s), raw log
`sdk-test-ads-build.log`. API24/API37 offline instrumentation **1/1 each**
resolves both demo unit IDs in both EN/HE contexts. `aapt2 dump resources` on
the exact debug/release APKs confirms demo IDs in debug and unchanged production
IDs in release. Artifacts, resource dumps and runtime logs: `sdk-test-ads/`
under `~/.local/share/opencode/evidence/integrations/`.
Optimized debug-key-signed release SHA256
`5272f59a91a802519ca3870ac38884bd8f81a42cff305f5df486b624441deeee`
matches installed bytes pulled from API24 and actual16KB API37. Both launch
the real About activity offline, exercising its interstitial configuration;
no live ad load or impression acceptance is inferred.

## Native SDK contracts follow-up

`SdkAnalytics` restores the RN top-level names Home/Tools/Lock/Dispatch/ContactUs/
RequestCode/ActivateCode/About/Privacy/Settings, both screen_name and screen_class.
Like NavigationStore, initial Home and repeated same-route resumes/recreation do
not emit another manual event; returning from another route does. Activity type
checks survive R8 renaming. Existing native automatic analytics and metronome
SELECT_CONTENT remain enabled. The small Java rate-helper call is a temporary
bridge to Kotlin's exact rate_us_prompt parameter builder, before the existing
three-day guard and at the existing five-launch threshold.

Shared FCM token persistence also sets Crashlytics user ID to the current delivery
token as RN does; it does not change pinned chatIdentity. Native Performance SDK
and its Gradle instrumentation plugin restore RN's existing automatic integration
(RN android/app/build.gradle applies the plugin). No custom traces or collection/
consent policy overrides are added. Plugin version2.0.2 was obtained from Google's
Maven metadata, not guessed.

Combined final-debug fixture runs pass API24 **9/9 (10.524s)** and API37
**9/9 (21.283s)**: exact event parameters and route deduplication, native Performance
initialization, demo-ad resources, all chat fixtures, custom dispatch, historical
identity/token rotation and cold/warm FCM entry. These use fake backend/analytics
recorders and a blocked SMS composer, with network firewalls retained.
Crashlytics' real local `user-data` file on both emulators contains
`{"userId":"second-delivery-token"}` after the token-rotation fixture; raw copies
are retained in `sdk-events/crash-identity-{5980,5982}.txt`. This verifies local
crash identity persistence while the fixture separately verifies pinned chat identity.
It does not prove uploaded attribution.

Tested source `6c1a3745`: existing full debug/release/unit/lint job passed in
3m27s (`sdk-events-build.log`), including Performance bytecode instrumentation
and R8. No duplicate build or suppression. Final optimized debug-key-signed APK
SHA256 `47359bb7cbc0731e5dc7b702098adf590c4fb64a59a762298f107c0c985c2e3a`
matches pulled installed bytes on both APIs (`sdk-events/sha256.txt`). Actual API37
page size16384. Real Home/About/chat launches passed on both with no AndroidRuntime
errors in the retained logs; captures include the expected offline chat-token error,
not an asserted successful send. `sdk-events-release-smoke.log` records each launch.

## Acceptance boundaries

All emulator execution remains offline. A demo ad-unit selection check is not a
successful test-ad load, impression, dismissal or mediation/consent verification.
Live SDK traffic needs an authorized non-production project/configuration;
production credentials are not used to claim that gate passed. Server receipt of
screen/rate events, crash attribution and Performance samples remains unverified
until authorized non-production integration runs; offline tests cannot close it.
