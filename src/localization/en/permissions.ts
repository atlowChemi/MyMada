export default {
    alertTitle: 'We need permission!',
    generalDescription: 'We need you to approve some permission so the app can work properly.',
    memoryDescription: 'We need permission to read from the phone memory in order to play the alarm.',
    overlayDescription: 'We need permission to show the app on top of other apps, in order to allow vibration &amp; sound even when phone is on silent mode.',
    approve: 'Approve permission',
    deny: 'Deny',
    blocked: 'This action has been blocked by a required permission which is missing.',
    notificationExplain: 'In order for us to update you when we see your message, we need permission to send notifications.',
    allowNotificationListener: 'Allow notification reading',
    notificationListenerDescription: `In order for the app to recognize emergency calls from the official MADA app, it needs permission to read the notifications.
To do that, you need to click "ok" and in the window that will open, find the "mada reports" and enable it.`,
} as const;
