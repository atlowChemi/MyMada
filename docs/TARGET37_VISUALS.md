# Target37 font and system-bar follow-up

This layer is above the individually restacked Calls PRs and target37-only #75.
The c198cf28 baseline is tree-identical to validated55021bcb; existing runtime
artifacts remain immutable. This follow-up changes presentation and needs its own
artifact/test evidence.

## Traced causes and supported replacements

At target37, setting public static-final Typeface.DEFAULT/MONOSPACE/SERIF/SANS_SERIF
by reflection throws IllegalAccessException. AppTheme/PreTheme/AlertDialogStyle
requested monospace and depended on that mutation. Oxygen's ArrayAdapter uses the
theme, so its selector exposed the actual monospace fallback. Use AppCompat/native
fontFamily resources instead and remove the unsupported global mutation. The
resource is byte-identical to the existing Open Sans Hebrew asset (SHA256
34f795fc4fecb05884ba42cda7f59446d95a9eb2ea8dc908f53895e7f7e1ad6a); the asset remains
for existing explicitly styled views. No new font/dependency is introduced.

Tools and Settings share activity_utillities_and_tools: its RelativeLayout fits
system insets, leaving the top padded area showing the gray window background.
Target37 enforces transparent edge-to-edge status bars, revealing that area.
The installed AndroidX Core ProtectionLayout/ColorProtection paints only the top
system-bar inset with the existing primary color; existing content inset handling
remains responsible for layout. White status icons retain contrast. API24 retains
the existing window layout path.

Verify native selector/dropdown glyph metrics against the original asset, readable
range errors, portrait/landscape/rotation, both locales, status-area pixels and
toolbar bounds on API24 and actual16KB API37. Inspect matched-state screenshots;
functional passes do not independently grant visual/product acceptance. Calls
kill/rebind/regrant loss windows are unrelated and remain unresolved.

## Verification and remaining comparison differences

Debug, instrumentation and optimized release builds and both lints passed.
Oxygen's three existing checks plus the font/status test passed 4/4 on each API24
and API37. The final screenshot-readiness test also passed independently on both:
it waits for the result TextView to be laid out before capturing, after an initial
API24 capture caught the panel before drawing. Original captures are retained.
Both locales/orientations verify selector/dropdown metrics against the bundled
font, toolbar below the status inset, and primary-color status-area pixels.

Actual published12822 screenshots from `evidence/oxygen/production-*.png` were
compared with these device captures. Their source APK SHA256 is
`b08c3d812f4781cfc667edf4240c917350c8083ea91cdb95bc971e0524c09179`
(published AAB universal APK with temporary bundletool signing). Selector glyphs
and dark status background now match the production appearance. This is NOT full
pixel parity: production has a heavier toolbar title, initial caret/focus differs,
and API37 gesture navigation changes available height/landscape width versus the
production three-button captures. These differences remain visible for review;
no maintainer visual acceptance is inferred from passing tests.

Evidence: `evidence/m2-integration/target37-visuals/`, including initial and corrected
captures, immutable APKs, logs and source/artifact hashes. No Calls logic changes;
theme typography can affect Calls presentation and requires review there too.

Build deviation: the initial successful release build inadvertently executed
`uploadCrashlyticsMappingFileRelease`; host backend-write-free status cannot be
claimed. Subsequent builds exclude that task. Owned emulator IPv4/IPv6 external
traffic stayed blocked. No phones, merges or global ADB resets were used.

## Owning-layer typography/inset correction

The generic AppTheme font override also reached toolbar titles. Clear both inherited
fontFamily attributes in AppBarOverlay so the platform title text appearance retains
its own medium face. Explicitly setting the family name was insufficient on API24;
the final inherited-style path passes actual title glyph-width checks on both APIs.
AlertDialogStyle uses the system sans-serif face; the request disclaimer now has the
published four-line Hebrew wrapping. Explicit per-view asset fonts (including chat
and request-confirmation buttons) continue to override the theme.

Gate/activation CoordinatorLayout roots lacked fitsSystemWindows under target37;
request already fit but had no status background. Use the existing CoordinatorLayout
inset handling and primary-color statusBarBackground for these three shared layouts.
Tests host the layouts without invoking backend actions and verify toolbar bounds
and status pixels; the owning activation layer must also verify its actual routes.

Both focused tests pass on API24 and API37, including EN/HE portrait/landscape title,
selector and dialog font checks. Captures were reopened against published12822.
Earlier font assertion failures and too-early dialog captures are retained under
`evidence/m2-integration/theme-parity/`; final captures wait for dialog window focus.

The lone-action button-side difference is independent: compiled production and
candidate button bars both specify layoutDirection=locale with the same order.
Old API24 localization did not update the process default locale; the migration
does. No global locale rollback or button-role swap is made here. Request button
direction remains an acceptance decision; the chat owner handles its scoped policy.
