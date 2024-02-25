export default {
    detection: {
        menuName: 'הגדרות התראות',
        splitNotificationByCallType: {
            title: 'הפרדת הגדרות התראה',
            subTitleEnabled: 'ההתראות עבור מקרי כונן, קבוצה ומשמרת יוגדרו בנפרד',
            subTitleDisabled: 'ההתראות עבור מקרי כונן, קבוצה ומשמרת יוגדרו בלי הבדלים',
        },
        notifySoundByDeviceStatus: {
            title: 'התראה לפי סטטוס מכשיר',
            subTitleEnabled: 'התראה תצפצף רק כאשר המכשיר לא במצב רטט/שקט.',
            subTitleDisabled: 'התראה תצפצף גם כשהמכשיר במצב שקט.',
        },
        notifyVibrationByDeviceStatus: {
            title: 'רטט לפי סטטוס מכשיר',
            subTitleEnabled: 'התראה תרטוט רק כאשר המכשיר לא במצב שקט',
            subTitleDisabled: 'התראה תרטוט גם כשהמכשיר במצב שקט.',
        },
        lowCallsCategory: 'הודעות מנהלתיות',
        detectServiceCalls: {
            title: 'זהה הודעות שירות',
            subTitleEnabled: 'זיהוי הודעות שירות פועל',
            subTitleDisabled: 'זיהוי הודעות שירות כבוי',
        },
        detectAdministrativeCalls: {
            title: 'זהה הודעות מנהלתיות',
            subTitleEnabled: 'זיהוי הודעות מנהלתיות פועל',
            subTitleDisabled: 'זיהוי הודעות מנהלתיות כבוי',
        },
    },
    notification: {
        menuName: 'הגדרות צליל ורטט',
    },
    advanced: {
        menuName: 'הגדרות מתקדמות',
    },
    ampm: {
        menuName: 'הגדרות יום ולילה',
        dayNotificationLevel: 'התראות בשעות יום',
        nightNotificationLevel: 'התראות בשעות לילה',
        nightStart: 'תחילת משמרת לילה',
        nightEnd: 'סוף משמרת לילה',
        nightEndDesc: 'בחר מאיזו שעה יחזרו מקרים להתריע לפי הגדרת יום',
        nightStartDesc: 'בחר מאיזו שעה יתריעו מקרים לפי הגדרת הלילה',
        notificationLevel: {
            silent: 'שקט',
            vibrate: 'רטט',
            soundVibrate: 'צליל ורטט',
        },
    },
    tools: {
        menuName: 'הגדרות כלי עזר',
        pageTitle: 'בחרו איזה כלים יוצגו בעמוד כלי עזר',
        minimumMsg: 'יש לסמן לכל הפחות כלי עזר אחד!',
    },
    generalSettings: {
        menuName: 'הגדרות כלליות',
        detectionEnabled: 'זיהוי מקרים באפליקצייה פועל',
        detectionDisabled: 'זיהוי מקרים באפליקצייה מושבת',
        language: 'הגדרות שפה',
        selectLanguage: 'בחירת שפה',
        region: 'בחירת מרחב ברירת מחדל',
    },
} as const;
