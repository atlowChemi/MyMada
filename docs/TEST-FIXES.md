# Instrumentation test corrections

These fixes can run with the existing local Android test setup. Emulator CI and
host-downloaded fixture provisioning remain separate work.

## Navigation and screen tests (#173)

- Establish overlay/notification and native-dialog prerequisites explicitly.
- Target dialog roots and wait for the exact Activity lifecycle or window focus.
- Use lazy-grid scrolling for Home destinations; retain visibility assertions.
- Finish animated document/Privacy screens before idle-dependent scenario cleanup;
  decode WebView JavaScript results as JSON and retain artwork/viewport assertions.
- Track ad destination creations within the test rather than earlier activities.
- Finish Home directly and require destruction before closing the prompt scenario.

Run the affected packages with `:app:connectedDebugAndroidTest` and the documented
device isolation/fixture prerequisites in `TESTING.md`. These test corrections do
not establish complete instrumentation coverage or release acceptance by themselves.
