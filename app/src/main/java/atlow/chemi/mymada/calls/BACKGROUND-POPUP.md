# Background popup fallback

Actual API37/target33 testing without overlay access showed BAL_BLOCK while the
app was backgrounded, even with the foreground monitor running. startActivity
returned normally and the native popup-only branch left no call notification.
Evidence: background-popup-logcat.txt and background-popup-activities.txt under
the persistent calls evidence directory.

When call notifications are enabled and OS permission is granted, popup-mode
delivery now posts a per-event tap-to-open fallback before attempting the native
popup. Opening the popup removes only that call's fallback. Dictation and source
details actions remain available, and replacement affects only Calls. Explicit
notification opt-out or OS denial still suppresses notification posting.

No background-launch exemption, full-screen intent or mediaPlayback classification
is manufactured. A blocked automatic popup requires user interaction; this does
not certify audible/visual equivalence to an unrestricted popup. The existing
channel's user-selected importance is retained. Common target37 and product/parity
acceptance remain separate gates.
