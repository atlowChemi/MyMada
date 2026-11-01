# Production / final-candidate visual audit — batch 1

Refs #35, #19, #27. Visual auditing is agent-owned execution, not a blanket
user blocker. This batch does not close the full inventory or authorize merging.

## Approved Home empty-ad policy — 2026-11-01

The maintainer explicitly approved retaining reserved ad space **transparent
when no ad loads**, consistently across Android versions. The resulting API24
difference from production12822 is intentional and accepted. This supersedes
the earlier pending-policy status. Completed verification is recorded below.

Private `5bec8950` and diagnostic `950bf568` remain retained evidence. Inspection
shows that the private test conditionally accepts navy based on candidate ad
height; that condition does not establish the newly approved policy. The final
offline check must assert reserved space and transparency independently of ad
height, with matched EN/HE API24/API37 images documenting the intentional
production difference. Geometry work continues independently, with one owner
serializing access to emulators5960/5962. No phone actions or merges authorized.

Approved-policy candidate evidence is under `../geometry/home-approved/`
relative to the original audit archive. Build/debug/release lints passed in
1m45s, with 20 JVM tests. API37's full Home class passed 7/7. The initial API24
class run exhausted its heap in Chrome_IOThread; raw records are
`5962-class-process-failed.txt` and `5962-crash-full.txt`. An isolated retry
passed English navigation before the emulator host crashed with SIGSEGV and
graphics ColorBuffer errors. These failures are retained, not counted as
passing tests. Recovery uses the same owned AVD under an outbound-network-denying
host sandbox, followed by restoring both guest firewalls; no global ADB reset.

Recovery completed: API24 passed all seven Home tests in isolated processes;
API37's completed seven-test run was reused, giving **14/14 completed checks**.
Both include unconditional 50dp reserved-slot/artwork assertions (EN normal,
HE 1.5 font scale), navigation/recreation and onboarding/dialog coverage.
All four optimized production/candidate pairs were opened:
`geometry/home-approved/pair-5960-en-home.png`,
`geometry/home-approved/pair-5960-he-home.png`,
`geometry/home-approved/pair-5962-en-home.png`, and
`geometry/home-approved/pair-5962-he-home.png`, relative to the milestone evidence
root. They show preserved tile geometry and transparent empty reserved space.
API37 matches the production empty band; API24 intentionally replaces the
historical navy band with artwork under the approved policy.
Installed optimized SHA256:
`0c1eb79ee6b14f5277911bf122211630a274c904acbd554ab228bc4467c5a04e`.
The private patch was inspected rather than cherry-picked: its wrapper-color
removal is retained, but its conditional navy assertion and explicit navy ad
background are not. No API-specific branch is needed.

## Evidence and provenance

Local archive: `~/.local/share/opencode/evidence/m2-integration/visual-audit-121/`.
Open `index.html` for full-size side-by-side images (production left); `pairs/`
contains directly openable PNGs. `raw/` retains screenshots, UI XML and activity
configuration. `capture.rb` records seeded offline state and component routes.

- Supplied production AAB: native 12822; locally signed universal APK SHA256
  `b08c3d812f4781cfc667edf4240c917350c8083ea91cdb95bc971e0524c09179`.
- Final optimized #121 `73054ad9af60b809c31af40c4a941e91cdac7d7b` APK SHA256
  `b09d8d706cfd2a75f257636e363469b454c573082509529ee8af129b85e0afa0`.
- Same owned emulator5960/API37, 1080×1920, 420dpi, portrait, font1.0, EN/HE
  application locales. Both installed APKs are pulled and hashed against sources.
  IPv4/IPv6 external OUTPUT rejected; no SMS or authorized live data implied.
- Fresh visual fixtures are separate from completed install-r evidence. Resets
  here do not invalidate the immutable four upgrade intervals or prove migration.

## Final #121 pairs actually opened: 22/22

Each row covers both `pairs/en-<state>.png` and `pairs/he-<state>.png`.
These are findings, not a passing screenshot count. Normal font1.0 differences
below cannot be dismissed as the approved large-font behavior.

| State filename | Actual inspected result | Disposition / owner |
| --- | --- | --- |
| `home` | Overlay permission modal, not unobscured Home. Artwork/order and localized actions retained. Candidate has a navy bottom strip absent from production; modal slightly taller. | Home #21: investigate background extent; full Home still needs unobscured pair. |
| `onboarding` | Same first-open settings prompt and EN/HE action order; candidate modal slightly taller. | Native first step matches; full sequence and RN privacy-first decision remain distinct. |
| `about` | Same text/artwork, except expected development version string. Candidate toolbar is 24dp too high and overlaps status icons; all content shifts upward. EN hyphenation/wrapping also differs. | Defect in shared information toolbar inset (#21); fix separately. Hyphenation remains a concrete typography discrepancy. |
| `privacy-offline` | Same localized offline text/ECG artwork; animation phase excluded. Same 24dp toolbar inset defect shifts error content upward. | #21 shared inset correction; loaded policy is not captured. |
| `request-disclaimer` | Same copy and localized positive action direction. Candidate dialog narrower/taller, larger line spacing; underlying input geometry differs. | #26 activation/dialog owner: unresolved visual delta, not approved redesign. |
| `activation` | Four empty digits and send remain. Candidate underlines are lower/wider and turquoise; send label smaller (EN sentence case). | #26 activation owner: unresolved geometry/typography delta. |
| `chat-name` | Production stacks name editor over disclaimer; candidate shows disclaimer first. Name banner is rounded/elevated in production, rectangular in candidate; send background also removed. | **Unmatched foreground state**, excluded from name-dialog parity. #26 chat owner must compare each modal and classify sequencing; live data not needed. |
| `team-empty` | Same empty instructions and mirrored add/send order. Candidate ambulance label floats smaller; underline color/thickness differs. EN custom tab wraps; candidate tab indicator turquoise versus white. | #26 team/host owners: visual deltas; no action-direction reversal in this state. |
| `form-empty` | Same field order; candidate spacing/underlines differ and send icon moves from edge to label. Code picker is visible in candidate viewport but not production initial viewport. EN tabs sentence-case and wrap. | #26 form/host owners: unresolved visual deltas, independent of approved SMS output. |
| `settings-notifications` | Overlay permission prompt obscures middle. Visible setting ordering/switches retained; EN rows taller, HE closer. Modal geometry matches Home discrepancy. | Partial comparison only; dismiss/grant fixture to compare full settings content. |
| `metro-idle` | Same navy toolbar, localized instruction, ring size/position and 00:00 hierarchy in EN/HE. | No unintended difference observed in this matched state. Active state remains separate. |

## Retained comparisons actually opened

Paths below are relative to `~/.local/share/opencode/evidence/`.

| Evidence | Inspection result | Scope |
| --- | --- | --- |
| `metronome-call-compose/comparisons-d352562a/api37-call-he-1.0.png` | Same red header, address/number hierarchy, logo, background and two bottom actions. Subtitle differs: production app name versus candidate test-app name. | Historical fixture branding; popup renderer has no diff from d352562a to #121. Not a new final-byte execution. |
| `metronome-call-compose/comparisons-d352562a/api37-metro-en-false-1.0.png` | Same navy toolbar, start instruction, ring proportions and centered 00:00. No unintended difference observed in idle content. | Retained renderer comparison; metronome directory unchanged through #121. |
| `oxygen/comparison-he-normal.png` | Same cylinder selection, values100/1500, 24-minute result, typography and background. Focused underline versus unfocused underline and menu ripple differ. | Focus/pressed state is unmatched; not an application regression or exact-state acceptance. |
| `contractions/visual-final/pair-37-normal-history-he.png` | Same two history cards, order, durations, turquoise number blocks and circular start action. | Historical populated local fixture; not live content or a final-byte recapture. |

## Remaining state inventory (agent-owned unless explicitly live)

| Family | Remaining comparisons after this batch |
| --- | --- |
| Home/About/Privacy/onboarding | Unobscured Home, scrolled About links, Privacy loaded/offline distinction, complete settings→overlay→privacy sequence; RN privacy-first remains a specific behavior decision. |
| Settings | All tabs, region/language/tool selectors, day/night, volume OK/cancel, ringtone picker and large-font/landscape modal review; reuse retained settings acceptance images first. |
| Calculators/timers | APGAR selections/result, Glasgow lists/result, Pulse active/result, Oxygen dropdown/large text, contractions active/history/delete confirmation. Existing images need explicit inspection and source applicability, not blanket recapture. |
| Vitals/dictionary/documents | Local empty/error/add/edit states; populated authorized backend fixture and production full PDF paging/zoom remain live-content gaps. Candidate actual PDF downloads already passed. |
| Activation/chat | Volunteer gate, request fields/validation, activation error, name editor, empty chat/composer/keyboard; real message/receipt content requires authorized fixtures. |
| Dispatch | Team rows/roles/cap, form picker/confirmation/errors, custom message/confirmation, billing states, full modals in both locales; approved SMS formatting is not a regression. |
| Popup/metronome | Retained EN/HE normal/large, source-action popup, active/pause/reset, overlay/widget/tile surfaces; inspect retained matched images before additional capture. |

Approved differences remain approved: Android font scaling, native Oxygen bounds/
truncation, contraction observed-absence expiry, vitals ranges, volume writes on OK,
and #120 SMS policy. They must not be reopened as generic visual blockers.

## Focused correction: information-screen status-bar inset

Ownership traced to shared `ui/ScreenToolbar.kt` (About/Privacy parity #36).
Both callers use it as a custom Scaffold top bar. Unlike a Material top app bar,
the custom Row did not consume status-bar insets; target37 edge-to-edge exposes
that omission. The correction applies `statusBarsPadding()` once in the shared
toolbar, outside its existing 56dp content/tag, retaining navy background above
the controls. API24 receives only its remaining insets, avoiding fixed pixel offsets.

Focused `InformationToolbarInsetsTest` checks both installed information screens:
toolbar screen coordinates must be below the actual OS status-bar inset and its
content must remain 56dp high. Evidence is retained separately under
`~/.local/share/opencode/evidence/m2-integration/information-insets/`, including
original-#121 failure and corrected optimized production comparisons. This fix
does not resolve or approve the other discrepancies in the table.

Executed results: debug/test/optimized release and both lints passed (2m13s).
New check fails against frozen #121 on API37 with toolbar top0 overlapping the
63px status bar; unchanged assertion passes corrected API37 (1.477s) and API24
(1.287s), exercising both screens each time. No JVM business logic changed;
the previous 20-test result is reused, not reported as rerun.

All four `information-insets/pair-{en,he}-{about,privacy-offline}.png` images were
opened and inspected. Corrected toolbar/body vertical alignment now matches
production. About's English hyphenation remains visible; version text is expected.
Privacy error layout matches, excluding animated ECG phase and OS clock/icons.
Corrected local-signed optimized installed SHA256:
`2b7220417ad6624e94d7060f97c96af6796c9b390c39676d29d6a418998e33f6`.

## Batch 2: About line breaking; Home attempt excluded

Evidence root: `~/.local/share/opencode/evidence/m2-integration/visual-audit-batch2/`.
About explicitly enabled `Hyphens.Auto`; disabling it restores production's
English wrapping without changing font scaling or the shared toolbar inset.
The About-only optimized APK installed on both owned emulators has SHA256
`c3a58f316a4d8e6eaff9046c115ece038f032c0090515111946418f81ad842b5`.
Debug/release builds and both lints passed in 1m44s (`about-final/build.log`).
Previous business-logic and toolbar-inset checks are reused, not reported as rerun.

Final-byte pairs actually opened:

| Path relative to evidence root | Disposition |
| --- | --- |
| `about-final/pair-5960-en-about.png` | API37: English word wrapping and vertical placement match; expected development version string differs. |
| `about-final/pair-5960-he-about.png` | API37: Hebrew content, placement and privacy artwork match; expected development version differs. |
| `about-final/pair-5962-en-about.png` | API24: English wrapping and placement match; expected development version differs. |

The production API24 captures named `he-*` actually rendered English. Those
locale-mismatched pairs are excluded; matched API24 Hebrew comparison remains
agent-owned fixture work. Raw screenshots/XML, installed APKs and hashes remain
under `production12822/` and `about-final-candidate/`. Both external-traffic
families remained rejected; captures are disposable visual fixtures, not upgrades.

Home's two attempted corrections are retained under `home-about/` and
`home-about-v2/`, and private commit `5bec8950` on
`integration/home-about-parity`; neither is included in this published slice.
All eight first-attempt pairs and six second-attempt matched-locale pairs were
opened. API37's navy placeholder disappears, but API24 loses production's navy
band. Production API24 XML has an ad view at `[0,1663][1080,1794]`; API37 has no
visible ad node. Moving background ownership to the ad view alone does not
resolve that difference. Its regression test fails against frozen #123 and
passes both attempted implementations on API24/37 (EN normal/HE large), but
those passing checks **do not establish visual parity**. Home remains unresolved.

Other concrete agent-owned work remains: activation digit/send sizing,
request/input/dialog geometry, dispatch tabs/fields/send positioning, and chat
modal sequencing/banner/send styling. Historical chat opens name entry over the
disclaimer for a blank name; current Kotlin defers it to disclaimer confirmation.
No live-content authorization is needed to resolve these local states. The live
backend, physical-device, onboarding-policy and deferred signing gaps above
remain separate.

## Batch 3: Chat first-open sequence; Home runtime diagnosis

Chat first creation omitted the historical blank-name prompt above the
disclaimer. It now queues the disclaimer then the noncancelable name dialog;
fragment restoration owns subsequent recreation. Existing rename/send handling
is reused. This is sequence acceptance only, not full chat visual acceptance.

Evidence: `~/.local/share/opencode/evidence/m2-integration/chat-first-open/`.
Debug/test/release builds and both lints passed (`build.log`, 1m27s).
`verify.rb` reproduced the new check's failure on frozen #124
(`emulator-5960-en-original124.txt`), then passed 8/8 checks across API24/37
and EN/HE. Checks cover focused noncancelable name-over-disclaimer order,
draft/restoration, fake-backend rename revealing the disclaimer, dismissal
without reopening, zero sends, and existing pending-send recreation.
Installed optimized candidate SHA256:
`604d1ac13e5653a2b4ce3dce0d56e1b6aa1d8e60f66702f705eb21b35884287d`.

Opened comparisons under `../home-ad-parity/pairs/` relative to that root:
`chat-5960-en.png`, `chat-5960-he.png`, `chat-5962-en.png`, and
`chat-5962-he.png`. Production and candidate both show name above disclaimer.
Narrower/taller dialog geometry, field/button placement, banner/send styling,
and API24 candidate keyboard visibility remain unresolved. API24 Hebrew now
uses actual Hebrew system locale; earlier mislabeled English images remain
excluded. The initial candidate capture stopped during locale switching;
the separate `chat-fixed-he24/` retry supplies the Hebrew API24 pair.

All eight `home-{parent,private}-{5960,5962}-{en,he}.png` comparisons were
also opened. Public Home retains its API37 extra band; private `5bec8950`
removes API24's production band, so neither establishes cross-API parity.
Retained production runtime dumps and ad bytecode show API24's old remote
ads module measuring an invisible 131px/50dp child, even after ad-load failure.
Production API37 and the current SDK instead measure a zero-height child.
Current `BaseAdView.onMeasure` measures a non-GONE child before considering
the AdSize fallback. This is an ad-runtime child-sizing difference, not a
navigation inset; no arbitrary API threshold or redesign approval is inferred.
Home remains open, with the private attempt and all failed evidence preserved.
