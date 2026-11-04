# Popup Kotlin migration

`smsRes` retains its installed activity name, XML layout, type labels, source
PendingIntent action, keep-screen-on/lock-screen flags, touch feedback, warm-intent
recreation and per-type native audio decisions. Blinking administrative alerts
now cancel both animations on destruction. Payload remains intent-owned.

CallText extracts display fields separately from classification. Hebrew and English
markers, legacy text and missing delimiters have a focused JVM check. Missing
markers no longer produce arbitrary substrings from index -1; they use the
existing fallback text/time. A malformed auto-stop preference defaults to six
seconds instead of throwing. This does not change source classification policy.

Failed/completed audio now releases and restores overridden alarm volume promptly.
The previous Java implementation restored it only on close/recreation, despite
earlier summary wording implying immediate failure cleanup. CallRouteTest now
explicitly checks restoration before close and across recreation.

This remains native-layout migration, not latest-target UI or production visual
parity acceptance. Target33 stays until the coordinated common target candidate.
