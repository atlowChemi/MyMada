export default {
    detection: {
        menuName: 'Notification settings',
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
