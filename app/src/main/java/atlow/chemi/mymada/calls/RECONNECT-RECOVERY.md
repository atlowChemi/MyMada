# Fresh retained notification recovery

Only the selected OS-bound listener reconciles `activeNotifications` on reconnect.
The same parser, metadata-only summary filter and GrobootRec route handle both live
and recovered events, including source PendingIntent and existing popup/BAL policy.
Live callbacks remain asynchronous, repeated and unfiltered by age or ledger state.
The popup's explicit Call info click sends the original source PendingIntent with
the API34+ sender BAL opt-in (API36+ visible-only). Target37 otherwise blocks even
this foreground user action. This opt-in is not used for automatic popup launches.

## Eligibility (user-selected inclusive 45 seconds)

- Both OS StatusBarNotification.postTime and source Notification.when must be
  positive, non-future and no older than45,000ms at scan AND actual receiver dispatch.
- Source when must not exceed the OS postTime: a timestamp future when posted does
  not become valid merely by waiting. Missing/zero/invalid source time fails closed.
- API24/37 offline OS probes verify updates change postTime, while reconnection
  preserves it. Notification.when is independent/source-controlled and can remain
  stale on updates. A fresh OS update cannot refresh the age of stale source content.
- A same-boot persisted wall/elapsed-realtime anchor prevents clock rollback from
  making old records fresh. Recovery never resets the source timestamp on reconnect.

## Epoch and duplicate policy

First connection/upgrade, reboot (BOOT_COUNT), explicit Stop, observed complete
listener revocation and changed grant identities establish a baseline without
replaying old active notifications. Explicit enable while already bound starts a
new baseline immediately; requestRebind alone need not cause another connection.
An unavailable boot counter disables recovery. A POST_NOTIFICATIONS return may
recover fresh unseen records only if detection/boot/listener-grant epoch continued;
there is no recovery dispatch while that notification permission is denied.

Revision identity combines user/package/UID/ID/tag key, OS postTime, source when,
title and body. Only SHA256 plus OS postTime is persisted, not raw call content.
Reused IDs and identical text with a new OS postTime are distinct revisions. Content
alone is never deduplicated. Same-process pending broadcasts suppress a simultaneous
snapshot copy. The receiver acknowledges synchronously to disk after processing,
not when the listener merely queues a broadcast. Disabled alert types are also
acknowledged rather than replayed later when preferences change.

The ledger retains at most1024 revisions. Entries beyond the45-second horizon are
pruned behind a persisted, never-decreasing exclusion timestamp. Capacity overflow
advances that timestamp past evicted records, avoiding historical replay; it can
reduce recovery coverage during extreme bursts, but never suppresses live callbacks.

## Hard limits

- Only the latest still-active source revision is available. Withdrawn notifications
  and overwritten intermediate updates cannot be reconstructed. No force-stop or
  zero-loss guarantee; listener reconnect is still controlled by Android/OEM policy.
- A snapshot is not atomic with subsequent source removal. Eligibility describes
  the notification present at scan; source cancellation can race later dispatch.
- A crash after an alert effect but before its durable acknowledgement may repeat
  that fresh revision on reconnect. Android alert effects and app storage cannot
  commit atomically. Persistence failures are logged; durability is not claimed
  when storage writes fail. No speculative exactly-once promise.
- An identical update with the same key, postTime millisecond, source timestamp and
  content is indistinguishable in a snapshot. Live callbacks still all alert.
- API37 can demote an orphan app group summary into a flagless child. An observed
  subsequent live callback then followed the existing malformed-event “תקלה” policy;
  it was not a recovered duplicate. Public summary provenance is no longer present.
  The filter still ignores actual metadata-only GROUP_SUMMARY records; it does not
  silently discard flagless malformed notifications or meaningful grouped children.
- A complete revoke/regrant occurring while the process is dead may be unobserved.
  Current grant state cannot reconstruct that history; the freshness cap still
  applies. Actual observed Stop/revocation invalidates queued recovery via epoch.

`RecoveryWindowTest` checks exact0/44999/45000/45001ms boundaries, invalid/future
times, rollback, handled revisions, update identity, pruning and capacity.
`CallRecoveryTest` checks Android persistence, pending/live-repeat behavior, Stop
and reboot baselines. The offline source fixture's optional SnapshotProbe records
timestamp provenance; owned-device external tests kill the real listener process
and post through NotificationManager before it rebinds.
