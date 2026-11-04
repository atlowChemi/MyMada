# Production / final-candidate visual audit — batch 1

Refs #35, #19, #27. Visual auditing is agent-owned execution, not a blanket
user blocker. This batch does not close the full inventory or authorize merging.

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
