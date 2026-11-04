# Glasgow (#23)

`GlazgoCalc` retains the installed Tools Fragment identity. Compose owns layout,
three saveable dropdown positions and scrolling. Native text/spinner interop keeps
the shipped Open Sans Hebrew metrics, underlines and dropdown menus. Resources,
3–15 scoring, 9/13 band boundaries, one-second HSV transition and navigation
follow native `b36f4a9c`/published 3.5.7; clinical wording is unchanged.

Previews: English, Hebrew/RTL, Hebrew 1.5× text and landscape. Portrait retains
bottom result/info cards; large text scrolls the complete content. Landscape uses
the native 64/36 split and a scrollable explanation.

On a dedicated firewalled emulator (see root README), run:

```sh
adb -s "$SERIAL" shell am instrument -w \
  -e class atlow.chemi.mymada.tools.glasgow.GlasgowScoreTest,atlow.chemi.mymada.tools.glasgow.GlasgowScreenTest \
  atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
```

The pure scoring test exhausts all 120 combinations, bands/colors and invalid
inputs. UI checks select every localized popup option, restore distinct positions
and score after Activity recreation, then exercise drawer/system Back. Optional
`-e capture true` saves screenshots to the debug app's files directory; optional
`-e language en` or `he` permits matching the system locale for visual comparisons.
Repeat at font scale 1.5. Activity recreation is not an OS process-death test.

Evidence and validation details: `~/.local/share/opencode/evidence/glasgow/EVIDENCE.md`.
This is a Glasgow-only slice; #23 remains open and #22 integration/maintainer
visual approval are separate acceptance gates.
