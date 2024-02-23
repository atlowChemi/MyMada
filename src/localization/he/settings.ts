export default {
    detection: {
        menuName: 'הגדרות התראות',
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
