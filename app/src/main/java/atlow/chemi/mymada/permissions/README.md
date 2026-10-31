# Notification permission UI

`NotificationPermission.request(FragmentActivity, resultKey)` presents the shared
foreground explanation/request/denial/settings flow. Register a FragmentResult
listener for `resultKey` with a lifecycle owner before requesting; result bundle
boolean `granted` reports the current OS state. Use a recreated Fragment's
`onCreate` listener for workflows that must survive configuration/process state.
Duplicate requests share one visible prompt. No Activity or grant cache is global.

API33+ requests POST_NOTIFICATIONS through Activity Result. Denial and dismissal
offer system app-notification settings or cancel; settings return rechecks both
permission and app notification enablement. Below33 the same API checks app-level
notification blocking and links system settings when blocked. Callers must recheck
`granted(context)` when they need it, including after revocation; a prior result is
not permanent authorization. Services must never open the permission prompt.

This ports RN `be72a163` NotificationService/PermissionsStore behavior while fixing
their stale granted/blocked caches. Copy explains call alerts/background controls
rather than the RN chat-specific explanation. DetectionControls uses a restorable
fragment continuation. Home, Settings, Privacy, About, Tools, Dispatch, Contact and
code-request detection controls all route through it; old duplicated toggles are
removed rather than allowing a second path to bypass the permission request.

NotificationPermissionTest exercises the real app dialog and Android permission
controller with fresh installs and **no adb grant**. Run separately with runner
`permissionRoute=grant|deny|dismiss|settings`; each starts with notifications
ungranted. These tests are separate from CallRouteTest, which explicitly pregrants
notifications to test routing. Listener access/overlay/monitor startup and grant
upgrades belong to the dependent calls lifecycle layer, not this shared UI API.

#25 may reuse the permission API commit independently; it owns metronome callers.
#24 owns this implementation and the POST_NOTIFICATIONS manifest declaration.
Compile37/target33 remain until the coordinated #21/#25 target prerequisites pass.
