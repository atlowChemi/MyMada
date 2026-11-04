# Contentless group headers are not source events

The shared native NotificationService (also inherited by the installed RN identity)
filters metadata-only GROUP_SUMMARY notifications before CallParser. Android's
auto-group header carries appInfo/originating-user/reduced-images/show-when metadata,
but no event. API24's listener wrapper also adds the legacy small-icon extra; this
was verified in the actual callback (it is absent from the server notification dump).
Previously null title/body became a new type1 “תקלה” popup and replaced the real call.

The filter is deliberately narrow: any other extra, ticker, action, fullscreen
intent or custom view preserves existing parsing. Even explicitly supplied empty
content fields defer to that policy. A navigation contentIntent alone is not event
content; generated headers can use it to open the source app. Meaningful summaries
and grouped children still route normally; nongroup malformed notifications still
use the existing fallback. No text/time deduplication or replay was added.

CallParser has one production caller, this listener. Both listener identities share
it. Application's GrobootRec and Home's explicit test-call broadcast continue to
receive events as before; filtering there would also affect legitimate test calls.

## Runnable offline OS regression

Build the separate source app with the repository Gradle wrapper:

```
./gradlew -p app/src/androidTest/fixtures/call-source assembleDebug
```

Install its APK on an isolated emulator (package atlow.mymadaadmin; no INTERNET
permission), plus app debug/test APKs. Grant source POST_NOTIFICATIONS on API33+;
grant target overlay access. Clear target listener grants before instrumentation
and pass `-e listeners <full-component[:full-component]>`. Run
`atlow.chemi.mymada.calls.SummaryNotificationTest` separately for native-only,
RN-only, and both grants on API24/API37 target37. This checks actual OS child/header
delivery and popup instance/text preservation, meaningful and title-only/expanded
summaries, malformed nongroup policy, repeated child updates, and eight independent
source notifications triggering real Android auto-grouping. Expanded content still
uses the prior parser policy; this change does not add new content extraction.

Instrumentation force-stops its target. The test grants access after startup and
revokes it before teardown, asserting the actual full-component live binding.
Otherwise killing a pending API24 bind can leave system_server's registration stuck;
repeated grant toggles or longer timeouts do not repair that stale setup. This is
not force-stop recovery. The known
kill/rebind loss window is separate and is not fixed by the summary filter.
