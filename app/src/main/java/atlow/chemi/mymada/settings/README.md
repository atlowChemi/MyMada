# Persisted-state bridge (#22)

Application locale lifetime: API33+ must attach the unwrapped application context
so LocaleManager can update application resources/default locale. A configuration
override there pins startup language even when new Activities use the selection.
Before API33, `LocalHelper.setLocale` updates application resources and process
default alongside saved `lng`; Activity contexts still attach independently.
This keeps AppCompat's locale-directed button panels aligned after warm language
changes. `SettingsChoiceTest` checks the actual language-choice path, application
and default locales, and a fresh themed dialog without per-dialog direction fixes.

## API37 day/night instrumentation

The API37/16KB image can reject a tap immediately after a dialog closes:
InputDispatcher logs `Dropping untrusted touch event due to /1000`, identifying
an alpha=0 system `Dim Layer for - Task` as the obscurer. The failing night tap
never creates a ModeDialog; this is not lost FragmentManager saved state.
Keep the click→recreate regression unchanged and use the runner's standard
window-animation control (no app delays, retries or synchronization):

```sh
adb -s emulator-5580 shell am instrument --no-window-animation -w -r \
  -e rounds 20 -e class atlow.chemi.mymada.settings.DayNightSettingsTest \
  atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
```

2026-10-30: original animated run reproduced in round5/English; controlled
font1.5 run passed all20 bilingual rounds plus Advanced/Sound/navigation (6 tests,
207.999s). Raw input/lifecycle traces are in owned evidence `api37-owned-final/`.
This diagnoses the emulator input failure; it does not fix the system image's
animation bug or claim every animated device is unaffected.

Keep SharedPreferences for the existing installed data and Java consumers. Migration
runs in Application.attachBaseContext, before locale attachment, providers and services.
No separate database or global completed marker is needed: each file is independently
idempotent and a restart resumes remaining files. A file's original values and converted
values commit together; no original is removed before its recovery copy is recorded.

| File / keys | Installed formats / native defaults |
|---|---|
| Settings: Mapp, realM, privacy, oldU, pickedMoked, WasLangChanged | boolean or RN true/false string; false |
| cut, stat, statVib, ride, loop, killS, Napp / NappT1–4 | boolean/string; false |
| not, win, volumeApp, callT1–6, Nvib/Nsou and T1–4 variants | boolean/string; true |
| merhav | int/string, 0–11; 0 |
| nightStart / nightEnd | int/string, 0–23; 23 / 7 |
| sleepDay / sleepNight / sleep | int/string, 0–2 / 0–2 / 0–8; 0 |
| volumeAppNum | nonnegative int/string; existing device-volume default |
| contarctNum / contractLong | nonnegative int / long, also RN strings; 0 |
| Tools | native string set or RN `[metro, oxy]`; absent means all, `[]` means none |
| timeK | integer text seconds, bounded to prevent millisecond integer overflow; 6 |
| firebaseGenratedKey, fcm, UserName, NuRi / NuRiT1–4 | strings retained verbatim, including spelling and URI |
| contractList | raw native Date JSON or RN epoch JSON retained verbatim; parser migration still required |
| lng / first_time_migration | strings; RN application locale supersedes stale lng on first native startup |
| permissionStatus: android.permission.* | boolean/string; false; not an OS permission grant |
| rate_app: dontshowagain, launch_count, date_first_launch | boolean / nonnegative long / nonnegative long; false / 0 / 0 |
| notifications | no active native typed consumer; retained verbatim |

Each changed key keeps its exact original typed value at `migration.original.<key>`.
Malformed recognized values use the existing consumer default; unknown keys remain
untouched. Backups are never overwritten. Migration does not invent a day/night
precedence rule or rewrite the combined sleep value (#24). Contract JSON/unfinished
session recovery and settings UI remain #22 work; this scalar bridge alone is not
complete RN upgrade compatibility. Real production-signed upgrades remain #11/#27.

Language and region choice dialogs now live in Kotlin `SettingsChoiceDialog`, using
the published native AlertDialog style and restoring via FragmentManager. Both
languages are bundled (language splits disabled), so changing language works offline
without a Play download or an Activity-retaining split listener. `LocaleRefresh`
refreshes existing back-stack Activities when they resume in an outdated locale;
it retains no Activity reference. The remaining Settings sections still use native
Java/XML and are migrated separately. `SettingsChoiceTest` changes both languages,
checks Home return, restores region selection and supports `capture=true` screenshots.

The Tools settings section is now Kotlin/Compose. Its saved Fragment identity remains
`fragments.SettingsTools`; Compose owns scroll/toggle state and switch accessibility.
Native text metrics and SwitchCompat artwork preserve the published list appearance.
Rows grow for larger text rather than clipping fixed-height labels. The last visible
tool cannot be disabled, matching native behavior; unknown saved tool keys are retained.
`ToolSettingsTest` covers both languages, all selections, the last-tool guard, stored
unknown keys and recreation/scroll state. Set `capture=true` for comparison PNGs.

Notification switches now use Kotlin view-scoped listeners with the published XML
layout. No retained Fragment holds obsolete Activity views. Rendering reads defaults
without writing them; user changes persist the existing `cut`, `stat`, `statVib`,
`callT6` and `callT4` keys. `NotificationSettingsTest` exercises both locales, defaults,
all toggles/descriptions, recreation, and the Sound/Vibration navigation route.

## Saved contraction compatibility

`ContractionHistory` backs up the original list/counter/timestamp before parsing or
expiration. Malformed lists remain verbatim. Native Gson Date strings and RN epoch
milliseconds are read separately; mixed/ambiguous records are not guessed. Numbering
comes from entries, never stale `contarctNum`. An RN active tail restores the End
button and its start time; completed entries supply the actual previous timestamp.

The compatibility bridge retains each known writer's behavior: native interval
end→start and `contractLong` = last completion. Native JSON dates lose milliseconds;
the matching last completion second is restored from `contractLong` before saving
or using it as the next interval anchor. A different-second timestamp is not used
to alter history. RN interval start→start and
`contractLong` = disposal time. Both retain their existing 20-minute eligibility
comparison. RN writes remain numeric (including an unfinished tail), native writes
remain Gson Date strings. This is provenance compatibility, **not** resolution of
#23's choice of one future interval/expiration policy. Newly created native sessions
still use native behavior. Full timer Kotlin/UI/lifecycle migration remains #23.

`ContractionHistoryTest` covers recent RN active + multiple-completed records, absent
and stale counters, complete/reopen without renumbering, native dates, exact expiry
boundary, malformed data and original-backup retention.
`ContractionRecoveryTest` also covers an active-only RN session: cancel deletion
keeps the running state, confirmed deletion clears it across recreation while
retaining the original raw backup.

Day/night controls use Kotlin with the published XML and hour-only dial. Choice
dialogs are child DialogFragments; restored time pickers rebind their callback and
use stable `nightStart`/`nightEnd` tags rather than a transient boolean destination.
The receiver's existing nine-state `sleep` code is `sleepDay * 3 + sleepNight`,
persisted atomically with the changed selection. `DayNightSettingsTest` checks all
nine combinations in both edit orders and bilingual mode/hour dialog recreation.

`PreferenceMigrationTest` covers typed/RN/malformed/missing values, overflow, empty
tool selection, preservation and repeated/interrupted-between-file migration.
`StartupUpgradeTest` is opt-in: run with `startupFixture=seed`, force-stop the app,
then run with `startupFixture=verify` in a new process. It checks the actual Application
startup path and RN AppCompat locale storage on API24/platform locales on API33+.
Use only a disposable emulator with IPv4/IPv6 egress blocked before either invocation.
