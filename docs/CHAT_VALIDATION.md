# Chat/name/read-receipt Kotlin slice (#26)

Private implementation starts above published #87 `142c83f4`, not a replacement
for that PR. Runtime/source evidence lives in
`~/.local/share/opencode/evidence/integrations/`. No production writes, messages,
SMS or WhatsApp launches are used. Both IP families reject non-loopback OUTPUT
on owned emulators5980/5982 before app launch. No phone/global ADB operations.

## Contract and behavior

Compared full native activity/adapter/model and RN `be72a163` ContactUs and
MessagingStore, plus current HomeActions, FCM service and launcher callers.
`integrations/README.md` records exact fields and intentional fixes. Authentication
and server rules are unchanged. Pinned historical chat key is not a substitute
for server delivery-token mapping; the live mapping still needs backend evidence.

Native and RN both miss changes to `SeenByAdmin`; the Kotlin listener processes
add/change/remove and detaches on disposal. Fresh installs await valid identity.
One atomic multi-location update acknowledges message and metadata together while
retaining exact existing names/types. No schema fields or conversation copies.
No local name/draft success is reported before acknowledgement. Recreation keeps
pending operations and dialog draft state without issuing duplicate writes.

## Local validation

JDK17/SDK from `~/.cache/mymada-m2`; Gradle command:

```
sh ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
  :app:assembleRelease :app:lintDebug -x :app:uploadCrashlyticsMappingFileRelease \
  '-Dorg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8' --no-daemon --max-workers=2
```

The initial two API24 fixture tests passed (1.309s) before adding token rejection,
name-dialog recreation and cold/warm PendingIntent coverage. That earlier run is
retained as `chat-api24-tests-initial.log`, not represented as final evidence.
Final revision results and APK hashes are recorded after execution.

## Published-production UI comparison

Same locally signed, unmodified production12822 universal APK as #87; hash in
`chat-reference-sha256.txt`. Actual API24 EN/HE screenshots:
`production-chat-{disclaimer,empty,name}-{en,he}.png`. Reference `UserName` was
seeded locally to `Fixture Name` via offline emulator preferences; the production
name-submit/send controls were never used. No backend fixture was written.

The first Hebrew entry capture shows the old simultaneous name/disclaimer dialogs;
the new flow presents disclaimer then mandatory name, retaining both requirements.
Matched dialog comparisons use the pre-seeded name and individually opened dialogs.
The asset-font chat disclaimer body/line wrapping and name-field geometry match
the inspected reference. Chat dialog action alignment is explicitly kept at the
published right edge in both locales. Initial candidate captures taken mid-window
animation were identified; final captures wait for accessibility idle before saving.
Message-card content uses fake backend events, not real production conversation
screenshots; it cannot establish live conversation acceptance.

Inherited toolbar weight/status-area and request-disclaimer font differences have
been referred to the integrator's owning #77 correction. Parent #77 request Java
and #87 Kotlin use the same builder style/title/message/positive OK: the earlier
claim that #87 switched button polarity or removed an explicit font was withdrawn
after checking the exact parent source. Do not mask those differences by changing
unrelated chat logic or mark them permanently accepted.

## Remaining acceptance

Live authenticated Database writes/listens, real token rotation/delivery mapping,
signed native/RN upgrades and process death during an offline queued write need
separate authorized integration evidence. XML preservation does not complete
Compose conversion. Dispatch team-size/SMS wording decisions and SDK/events/consent
work remain separate #26 layers; #78–86 optimization remains deferred after #27.
