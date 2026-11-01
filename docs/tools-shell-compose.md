# Tools and Settings shell (#21)

`UtillitiesAndTools` keeps its installed Activity name and now uses Kotlin.
`ToolsWindow` renders the toolbar, actions, modal drawer, and locked landscape
drawer in Compose. Menu resources remain the localized route/icon catalog;
there is no runtime NavigationView, platform Toolbar, or MyDrawerLayout shell.

## Interoperability and restoration

- Existing feature Fragment classes, preferences, and `R.id.frame` remain stable.
  A FragmentContainerView inflated through FragmentActivity's factory attaches
  restored fragments when Compose creates the content container. It does not
  replace a restored fragment with a new default destination.
- Both activities save the checked destination. Tools reuses the restored active
  fragment and retains its existing per-activity fragment reuse behavior. BLS,
  ALS, and deceased routes share the same `BlsProtocoles` identity.
  Legacy shell saved state without a destination key is inferred from its restored
  Fragment class, so an APGAR fragment cannot be cached under the metronome route.
- Settings child navigation still calls `displayFragment(Fragment, toCheck)`.
  `isDeviceTablet()` and Java-callable `SettingsPage.isRTL()` remain available.
- Tools visibility uses the installed `Settings/Tools` keys. Metronome `running`
  and `start` intents continue to override a hidden metronome. An empty tool set
  uses the metronome rather than indexing an empty menu.
- Dictionary search/add and contraction delete stay owned by their fragments.
  The shell renders their menu actions in Compose and dispatches the existing
  fragment callbacks. Expanded dictionary SearchView remains a platform feature
  interop, including saved query restoration and collapse/back behavior.
- Detection still calls the existing `DetectionControls` permission workflow.
  A registered preference listener updates drawer and toolbar icons after async
  completion, without retaining a stale MenuItem checked flag.
- Insets belong to the shell, including target-37 edge-to-edge system bars.
  The existing window artwork remains visible behind transparent feature views.
  Locale direction controls drawer placement and relative padding.
  Status-bar icons remain light over the navy toolbar. Resuming after a locale
  change recreates the shell while preserving its selected destination.
- Toolbar typography resolves AppCompat's actual title dimension (20dp portrait,
  14dp landscape), rather than substituting a scalable 20sp Material3 title.
  Drawer labels retain the bundled font, production colors, and 48dp row spacing.

## Checks

`ToolsBackNavigationTest` exercises actual drawer swipes, toolbar navigation,
system back, open drawer recreation, APGAR saved input, locked landscape,
settings child restoration, async detection updates, and child toolbar actions.
On API29+ it also exercises injected system-edge gestures with gestural navigation.
It covers real notification-listener permission completion, legacy saved state
without a destination key, and locale changes while the shell is paused.
`SettingsNavigationTest` opens each settings destination through the Compose drawer
and checks the selected route and restored fragment tag in English and Hebrew.

`docs/checks/tools-shell-parity.rb` captures production12822 and candidate shells
in both locales, font scales 1.0/1.5, and portrait/landscape. It requires an owned,
network-isolated emulator; `PACKAGES` can select either installed package.
Evidence is kept outside the worktree in
`~/.local/share/opencode/evidence/tools-shell/`.

The focused suites passed 11/11 tests on each isolated API24 and API37 emulator
(`tests-api24.log`, `tests-api37.log`). The only later application change replaces
drawer-label ellipsis with native single-line word clipping; final screenshots
exercise that change without repeating the full behavioral suites.

### Production comparison findings

- Reference: user-supplied production12822, locally re-signed universal APK,
  SHA256 `b08c3d812f4781cfc667edf4240c917350c8083ea91cdb95bc971e0524c09179`.
- API37: all 16 shell captures (Tools/Settings × EN/HE × font1.0/1.5 ×
  portrait/landscape) have matching toolbar/drawer bounds within one pixel.
  Reviewed labels, ordering, selected backgrounds, icons, RTL placement and
  system bars against the corresponding production images. Capture after
  rotation must allow Activity recreation before tapping the toolbar.
- API24 production requires changing the **system** locale in Android Settings;
  changing `Settings/lng` alone is insufficient. The capture script rejects a
  mismatched system locale. Use `CASE=-en-` / `CASE=-he-` for separate passes.
  The corrected 16-image API24 matrix also matches toolbar/drawer bounds within
  one pixel after allowing the transient native scrollbar to fade.
- API24 production keeps app text at font1.0 under system font1.5, while the
  candidate honors scaling. The user approved honoring Android font scaling
  across migrated screens. This is no longer a product blocker; readability,
  reachability, clipping and RTL still require verification, not pixel equality
  to production's suppressed scaling.
- Precise restored-title evidence: launch Tools in English, select APGAR, rotate
  portrait → landscape → portrait. On both API24 and API37, production retains
  APGAR content but changes the toolbar from `APGAR calculator` to `Tools`;
  candidate retains both APGAR content and `APGAR calculator`. Expected behavior
  is the active feature title, already asserted by `ApgarScreenTest` before this
  shell migration. Evidence: `behavior24/` and `behavior37/`, respectively
  `production12822-apgar-selected.png`, `production12822-apgar-recreated.png`,
  `candidate-apgar-selected.png`, `candidate-apgar-recreated.png`. These are
  configuration-recreation comparisons, not process-death or signed-upgrade
  claims. This observation is not a broad title/parity acceptance gate.
- Paired API24/37 `behavior24/` / `behavior37/` captures show drawer swipe-close,
  Back closing the drawer, APGAR destination retained after rotation, and API37
  system-edge Back finishing Tools. The previous task differs by launch history.
  Existing child-content differences remain with their feature owners.

Legacy feature tests that access NavigationView or Toolbar directly must use
Compose shell semantics or their existing fragment entry points. Shell test tags
are `shell-navigation`, `shell-detection`, `shell-destination-<resource ID>`, and
`shell-action-<resource ID>`. The obsolete XML layout remains only for existing
shared-helper compatibility tests; production shell installation never inflates it.

## Local reproduction and integration

With the repository's JDK17 and Android SDK configured:

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest --max-workers=2
adb -s "$OWNED_SERIAL" shell am instrument -w \
  -e class atlow.chemi.mymada.ToolsBackNavigationTest,atlow.chemi.mymada.settings.SettingsNavigationTest \
  atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
./gradlew :app:assembleRelease :app:lintDebug -x uploadCrashlyticsMappingFileRelease --max-workers=2
PACKAGES=atlow.chemi.mymada.debug ruby docs/checks/tools-shell-parity.rb "$OWNED_SERIAL" "$EVIDENCE_DIRECTORY"
```

Install the matching debug and test APKs before instrumentation. Release smoke
uses an externally debug-signed copy of the optimized APK on the isolated
emulators; it does not establish production signing continuity.

The private Metro/popup layers (initially `118ae7eb`/`eafe5838`, latest owner-reported
source `e0ef94ee`/`d352562a`) change the Metro fragment's
content and popup Activity, not this shell or its two test suites. Inspection
confirms the Metro fragment identity, title callback and intent extras remain
compatible. Popup red-bar painting belongs to its own Activity; ToolsWindow's
light status icons apply only to Tools and Settings.

### Optimized artifact

Final `assembleRelease` + `lintDebug` completed successfully; the release build
also ran release vital lint. Debug lint reports no errors (292 warnings and two
hints across the inherited app). Mapping upload was explicitly excluded locally.

- Unsigned optimized APK SHA256:
  `65a05f90d520190d24fbc9d2294719238da879e8a3ecf83fc7c07d5d4b5d12e1`
- Externally debug-signed optimized APK SHA256:
  `3f608ae20954b46d1ce6a0de75fd80605d95a0ca145d2a152aa93cf92eb94dfc`
- Final debug visual-check APK SHA256:
  `4e6eec5d77735224c894d0c4dd22e2e6531961a4c4e67962fed22a7d4a117faa`

Artifacts, build logs and release smoke screenshots are retained in the evidence
directory. Production/reference screenshots were retained before installing the
optimized candidate over the locally re-signed reference package.
Both installed release APK hashes match the signed hash above. API24 and API37
release smoke passed: Tools drawer open/swipe-close/Back, APGAR route and rotation
restoration, all five Settings destinations; API37 system-edge Back also finished
Tools. Captures are in `release24/` and `release37/`.

### Acceptance and publication status

The two dedicated shell suites pass. The authorized follow-up adapts PlatformSmoke,
Target37Visual, Documents, Oxygen, Home, Pulse, APGAR, Glasgow and Dictionary tests
to shell semantics, preserving their feature assertions. Only tests and this
document change; the coordinator owns the combined test-APK build and affected
runtime checks, whose results are not claimed by this private adapter commit.
The font-scaling policy is approved; the precise title comparison above documents
an existing test expectation. Integration acceptance belongs to the coordinator.
Append only in the
integrator's exclusive window above the then-current single-stack tip, not above
the private #103 base or an assumed #104 tip. No merge is authorized.
