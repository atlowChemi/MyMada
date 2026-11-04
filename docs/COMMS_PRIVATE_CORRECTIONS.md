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

Local status: source inspection and diff checks only; compilation/runtime and
new corrected APK screenshot binding remain pending the integrator's queue.
