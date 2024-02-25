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
