# Call overlay

The installed FloatingWidgetService identity now executes Kotlin. The same native
layouts retain collapsed/expanded, close, details, drag/edge-bounce and long-press
drag-to-remove actions. Details still opens MokedPage, as the native overlay did.
Alert decisions continue to come from CallAlerts through GrobootRec; the disputed
native overlay vibration predicate is unchanged.

Each replacement first cancels the prior auto-stop, long-press and animation,
releases audio, restores any overridden alarm volume, cancels vibration and removes
both windows. Completion/error/destruction share cleanup; delayed callbacks cannot
release a replacement player. Malformed timeout falls back to six seconds, missing
payload or overlay grant stops safely, and a background start rejected by Android
leaves the already-posted call notification available.

OverlayLifecycleTest covers replacement timer isolation, expand/collapse/close,
failed playback volume restoration and denied special access on disposable
emulators. Grant SYSTEM_ALERT_WINDOW for the first two tests; reset to default for
deniedOverlayStopsWithoutWindows. Notification permission tests
remain separate. Drag geometry/published visual parity and background-start
behavior at target37 still require acceptance; this layer retains target33.
