# דיווחי מד"א — MyMada

Android emergency-notification and field-tools app for MDA volunteers. Maintained
application and test code is Kotlin; Compose owns feature content, with native
widgets, WebView, Fragments and Android components retained where needed.

## Development

Use JDK **17**, Android SDK platform **37** (`platforms;android-37.0`), build-tools
**37.0.0** and platform-tools. Install packages and accept licenses with Android
Studio or `sdkmanager`. Set `JAVA_HOME` and `ANDROID_HOME`, or put `sdk.dir` in an
untracked `local.properties`.

The checked-in wrapper uses Gradle **9.6.0**, AGP **9.5.0**, built-in Kotlin and
Compose compiler **2.4.21**. Minimum Android is **API 24**; compile/target are **37**.
Java bytecode targets 17 and Kotlin inherits that target. No separate Kotlin
Android plugin is needed. Dependency versions remain in the Gradle files.

```sh
JAVA_HOME="<JDK17>" ANDROID_HOME="<SDK37>" ./gradlew \
  :app:assembleDebug :app:assembleDebugAndroidTest \
  --max-workers=2 '-Dorg.gradle.jvmargs=-Xmx3g'
```

Before installing or launching, follow [device isolation and focused testing](docs/TESTING.md).
The checked-in Firebase configuration is not authorization for production traffic.
Debug builds use an automatically generated local debug key and demo ad IDs.
Different signing keys cannot update an existing installation in place; use an
owned disposable emulator for development.

## Maintainer references

- [Compatibility contracts](docs/COMPATIBILITY.md): installed components, stored
  data, approved behavior, backend protocols and platform boundaries.
- [Testing guide](docs/TESTING.md): build/lint, isolated fixtures, mocked-client
  limits, packaging, upgrades and visual verification.
- [GitHub project](https://github.com/users/atlowChemi/projects/1): execution order
  and prerequisites; [assessment #19](https://github.com/atlowChemi/MyMada/issues/19)
  and [parity #35](https://github.com/atlowChemi/MyMada/issues/35) own feature decisions
  and acceptance. Historical run receipts belong in owning issues/PRs and external
  evidence archives, not a second roadmap in these docs.

The app has one module. Activities/Fragments own lifecycle and installed entry
points; ViewModels, SavedStateHandle and saveable state own UI drafts. Platform
permissions and SDK work stay outside Compose content. The RN runtime/build
workflow is removed; retained RN component identities preserve upgrades.

Release outputs are optimized but unsigned until signing continuity is established.
Version code `12828` is a development value: verify the published maximum before
release. Production-signed native/RN upgrades are deferred to
[release readiness #11](https://github.com/atlowChemi/MyMada/issues/11), not proved
by local debug signing. CI follows the working local baseline under
[#13](https://github.com/atlowChemi/MyMada/issues/13).
