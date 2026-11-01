# Maintained Kotlin / RN inventory

Integration audit after the remaining #26 layers and installed receiver/activity
bridges, based on published #112. This records source inventory, not milestone
acceptance or permission to merge.

## Verified source inventory

- `app/src` contains **zero Java source files**, including application and tests.
  Generated build outputs and third-party SDK bytecode are excluded.
- `git ls-files '*.java'` returns no maintained tracked Java source.
- No tracked JS/JSX/TS/TSX application source, `package.json`, npm/yarn lockfile,
  Metro/Babel configuration or Node dependency directory remains.
- Source/build-rule scan finds no React Native runtime dependency or bridge
  (`com.facebook.react`, `ReactNativeHost`, `ReactApplication`, `node_modules`).
  Historical Markdown mentions describe reference behavior, not executable RN.
- The three Circular Java widgets and their exclusive attributes are removed;
  no application/source/resource/build-rule references remain.
- Installed listener aliases, widget/receiver names and the Privacy component
  remain intentionally: they preserve existing Android grants and entry points.
  They are Kotlin compatibility components, not obsolete RN runtime bridges.
- XML resources and native widget interoperability remain where still used.
  MokedPage's ViewPager is the tested swipe/Fragment-state bridge; retained XML
  is not evidence of remaining Java or an excuse to delete live resources.

## Exact remaining acceptance work

- #19/#35 feature-specific production parity and approved-retirement disposition
  remain required. Android font scaling is approved; this is not pixel equality.
- Native SMS punctuation/whitespace, optional-field thresholds, code ordering and
  team header are preserved provisionally. Final product payload policy still
  needs disposition; the four-person cap is settled and is not a blocker.
- Authorized nonproduction live FCM/backend/SDK delivery, real test-ad delivery
  and console configuration remain distinct from offline fixtures/source audits.
- Production-signed in-place native/RN upgrades across all settings, activation,
  chat identities, histories, widgets/grants and entry points remain a final #27
  gate. Locally signed installed-byte checks do not establish signing continuity.
- OEM/physical-device audibility, prolonged idle/background reliability and
  unobserved/withdrawn notification gaps remain documented limitations. The
  inclusive45-second recovery policy is bounded recovery, not zero loss.
- Final integrated state/process/accessibility/adaptive coverage and maintainer
  review remain necessary. Source inventory alone does not close #21–#27.
- CI #13 and historical crash triage #10 are separate subsequent/backlog work,
  not invented prerequisites for milestone2 (#27's current issue definition).

Raw tracked-file/runtime-reference audit and integration build/test evidence:
`~/.local/share/opencode/evidence/m2-integration/final-ui/`.
