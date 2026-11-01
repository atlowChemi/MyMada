# Request title localization follow-up (#26)

Classification: inherited Kotlin migration omission, not a Compose/shell regression
or a product-policy question. The manifest labels `codeRequest` with
`R.string.title_activity_code` (EN: `Contact us`; HE: `בקשת קוד הפעלה`).
On API24 the framework's initial activity label can use the base locale, while
`LocalHelper.onAttach` supplies localized activity resources for the body.
Installing the toolbar does not reload that initial title.

The native Java activity called `resetTitle()` before setting its content view:
it read the manifest label resource and called `setTitle(labelRes)`. The Kotlin
migration dropped this step. Restore the same behavior with the explicit,
manifest-matching `setTitle(R.string.title_activity_code)` before toolbar setup.

The request confirmation/recreation fixture now asserts both `Activity.title`
and the actual Toolbar title against the localized resource, before and after
recreation. It failed on API24 HE before the fix. Focused confirmation/capture
checks then passed 2/2 on API24 EN6.875s / HE6.635s and API37 EN8.443s / HE8.587s.
The API24 HE request capture was opened and shows the correct Hebrew title.
Evidence: `~/.local/share/opencode/evidence/integrations/request-localized-title/`.

This independent private follow-up leaves the frozen request commits untouched.
Only debug/test APKs and focused tests were built/run here; the coordinator owns
combined optimized/lint validation and serialized publication.
