# Private communications corrections

Base: `47cc4e16`. No publication or device execution is implied.

## Chat lone action

Native `contactChatActivity.ChatDialog.onStart` forced the button container LTR.
Compose's shared `Arrangement.End` lost that override in Hebrew. Only chat now
requests `Arrangement.Absolute.Right`; title, body and input retain their locale
direction, and all other callers keep the default End arrangement. Team is owned
separately and is not changed here.

`ChatDialogGeometryTest` checks disclaimer/rename in both layout directions and
the unchanged shared RTL default. Compile and run this focused test in the
integrator's serialized build/device queue. Also capture Hebrew standalone
disclaimer and rename, IME hidden, against retained
`integrations/production-chat-{disclaimer,name}-he.png`; preserve complete copy,
right-hand OK, validation and ready/disabled behavior. Existing
`ChatIntegrationTest` exercises those latter contracts with an offline backend.

Local verification: `assembleDebug`, `assembleDebugAndroidTest` and all20 JVM
tests passed in21s (2 workers,3GiB heap). `git diff --check` passed. The new device
tests compiled but have not run: no device lease was given. Lint/release and
corrected APK screenshot binding remain pending; no parity row is closed here.

Frozen APKs are under evidence `m2-integration/parallel-visual-audit/comms-private-10d93547/`:

- debug: `bbe586a88fbb160292852d14a08388c0fafc83d2b25a3d4437c5b0253ca9881e`
- androidTest: `7dbdc95b10fe4de2c32912dd959fcb54e5ff38f7829cb5ec0f037ce684494355`

Build log: sibling `comms-private-build-10d93547-retry.log`. The initial1s shell
timeout is retained separately; the retry completed successfully. This final
verification note changes documentation only from the built10d93547 source.
Build slot was released directly to the medical owner after completion.

## Overlay removal target

Native enlarged the target's layout and WRAP_CONTENT overlay window. Kotlin
scaled only the child, clipping the highlighted circle. The service now resizes
the child layout and updates its window, restoring base dimensions on leave,
UP/CANCEL and the next DOWN. Hit bounds still use the original dimensions and
600ms hold. Bubble placement retains the native entry calculation against the
unexpanded target bounds, held constant across repeated MOVE events so the
window's enlarged measurement cannot cause a second jump. No pixel offsets or
global theme changes are introduced; close/expand/collapse/open controls remain.

`OverlayLifecycleTest#removalResizesRestoresAndCancelsWithoutDismissing` injects
one local enter/leave/re-enter/CANCEL gesture and a subsequent UP removal. It
checks real accessibility bounds, including 1.5× target size, stable bubble
position and restored base size. Run on the integrator's explicitly granted
overlay fixture only; it grants no permission itself. Interactive-window and
not-important-node retrieval flags are enabled; if the platform still withholds
overlay nodes, retain that test limitation rather than substitute an empty pass.

Capture only corrected normal/highlighted removal at API37 EN/font1.0,
1080×1920/420dpi, against retained
`geometry/inventory-overlay-production12822-removal/removal-{normal,highlighted}.png`.
Use a silent local fixture with a valid local URI for native's unconditional
parse; hold >600ms, move into the target shown in the image, capture while held,
then release. Record installed APK hash and source commit. Existing collapsed,
expanded and notification pairs remain retained; no Team recapture is requested.
