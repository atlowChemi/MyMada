# Chat keyboard visibility

Activity-local `SOFT_INPUT_ADJUST_RESIZE` keeps the header, name and entire Send
control above the keyboard. Source handoff `65fd9234`; no send/rename behavior
changes. Frozen425 fails the same whole-Send assertion that passes on the fix.

Evidence: `parallel-visual-audit/chat-ime-above144-runtime/HANDOFF.md`.
Final APK SHA256: `793929dd5dc7228b705b09a568e5de39b50f3b183e14111d9cf6d2f627bfc80f`.
Four EN/HE runs at actual system font1/1.3 pass keyboard, draft, recreation and
zero-write checks. Independent review opened eight visible/hidden pairs. Nominal
settings-only unscaled1.3/1.5 captures are excluded. Native Hebrew remained
effectivefont1 during the system1.3 comparison; this establishes usability, not
equal-effective-font pixel identity. API24 device APK/data/config/isolation restored.
