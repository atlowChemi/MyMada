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
and Gradle plugin are present. Performance is present in RN dependencies but not
the restored native Gradle dependencies; RN source contains no custom traces.

RN `NavigationStore` emits screen_view with screen_name/screen_class equal to
the top-level route when that route changes. `RateUsStore` emits rate_us_prompt
with launchCount/firstLaunchDate at five launches, before its three-day guard.
The restored rate helper logs only to Logcat. Existing native metronome
SELECT_CONTENT remains another event contract. RN MessagingStore sets the
Crashlytics user ID to the retrieved FCM token; native currently preserves
conversation/delivery identity separately but does not set that crash identity.

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

## Acceptance boundaries

All emulator execution remains offline. A demo ad-unit selection check is not a
successful test-ad load, impression, dismissal or mediation/consent verification.
Live SDK traffic needs an authorized non-production project/configuration;
production credentials are not used to claim that gate passed. Screen events,
rate event, Crashlytics identity and Performance still require implementation or
explicit disposition plus appropriate tests before the broader SDK checkbox closes.
