# Persisted-state bridge (#22)

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

`PreferenceMigrationTest` covers typed/RN/malformed/missing values, overflow, empty
tool selection, preservation and repeated/interrupted-between-file migration.
`StartupUpgradeTest` is opt-in: run with `startupFixture=seed`, force-stop the app,
then run with `startupFixture=verify` in a new process. It checks the actual Application
startup path and RN AppCompat locale storage on API24/platform locales on API33+.
Use only a disposable emulator with IPv4/IPv6 egress blocked before either invocation.
