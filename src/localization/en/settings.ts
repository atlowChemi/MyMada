export default {
    detection: {
        menuName: 'Notification settings',
        splitNotificationByCallType: {
            title: 'Separate alert settings',
            subTitleEnabled: "Each call will alert by it's specific type.",
            subTitleDisabled: 'All calls will alert the same way.',
        },
        notifySoundByDeviceStatus: {
            title: 'Alert by phone status',
            subTitleEnabled: 'Emergency call will make sound only if phone is not on silent/vibrate mode.',
            subTitleDisabled: 'Emergency call will make sound even if phone is on silent mode.',
        },
        notifyVibrationByDeviceStatus: {
            title: 'Vibrate by phone status',
            subTitleEnabled: 'Emergency call will vibrate only if phone is not on silent mode.',
            subTitleDisabled: 'Emergency call will vibrate even if phone is on silent mode.',
        },
        lowCallsCategory: 'Non emergency calls',
        detectServiceCalls: {
            title: 'Recognize service calls',
            subTitleEnabled: 'Service calls recognition off',
            subTitleDisabled: 'Service calls recognition on',
        },
        detectAdministrativeCalls: {
            title: 'Recognize Administrative messages',
            subTitleEnabled: 'Administrative messages recognition on',
            subTitleDisabled: 'Administrative messages recognition off',
        },
    },
    notification: {
        menuName: 'Sound & vibration settings',
    },
    advanced: {
        menuName: 'Advanced settings',
        notificationTitle: 'Notification',
        moreTitle: 'More',
        sendNotificationOnCall: {
            title: 'Notification at new call',
            subTitleEnabled: 'When new call arrives, a notification will be fired.',
            subTitleDisabled: "When new call arrives, a notification won't be fired.",
        },
        openPopUpOnCall: {
            title: 'Pop-up window at new call',
            subTitleEnabled: 'When new call arrives, a pop-up will be fired.',
            subTitleDisabled: "When new call arrives, a pop-up won't be fired.",
        },
        overridePreviousCalls: {
            title: 'Over-ride old notifications',
            subTitleEnabled: 'A new call will over-ride old notifications.',
            subTitleDisabled: 'A new call will join the old notifications.',
        },
        infiniteAlert: {
            title: 'Snooze (night mode)',
            subTitleEnabled: 'Notification will loop till stopped.',
            subTitleDisabled: 'Notification will play once.',
        },
        autoStopAlert: {
            title: 'Auto-stop',
            subTitleEnabled: 'Notification will auto-stop.',
            subTitleDisabled: 'Notification will require manually being closed.',
            duration: 'Delay before auto-stop (seconds)',
            durationEmpty: 'Please provide a value!',
            durationInvalid: 'Value must be a positive whole number!',
        },
        alertVolume: {
            link: 'Notification volume',
            title: 'Change volume for notifications',
            subTitleEnabled: "The volume will be set by the app's settings",
            subTitleDisabled: "The volume will be set by the phone's settings",
            pickerTitle: 'Pick notification volume',
        },
    },
    ampm: {
        menuName: 'Day & night settings',
        dayNotificationLevel: 'Notifications during the day',
        nightNotificationLevel: 'Notifications during the night',
        nightStart: 'Night shift start',
        nightEnd: 'Night shift end',
        nightEndDesc: 'Pick time when calls will alert by the day pattern',
        nightStartDesc: 'Pick time when calls will alert by the night pattern',
        notificationLevel: {
            silent: 'Silent',
            vibrate: 'Vibrate',
            soundVibrate: 'Sound & Vibrate',
        },
    },
    tools: {
        menuName: 'Utilities & tools settings',
        pageTitle: 'Pick tools to show on the tools page',
        minimumMsg: 'Pick at least one tool!',
    },
    generalSettings: {
        menuName: 'General settings',
        detectionEnabled: 'MADA app recognition on',
        detectionDisabled: 'MADA app recognition off',
        language: 'Language settings',
        selectLanguage: 'Pick language',
        region: 'Pick MDA region',
    },
} as const;
