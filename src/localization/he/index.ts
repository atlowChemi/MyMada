import aboutUs from './aboutUs';
import common from './common';
import contact from './contact';
import errors from './errors';
import home from './home';
import permissions from './permissions';
import privacy from './privacy';
import testCall from './testCall';
import tools from './tools';

export default {
    aboutUs,
    common,
    contact,
    errors,
    home,
    permissions,
    privacy,
    testCall,
    tools,
    // smsMoni: 'הפעלת האפליקציה ברקע',
    // notiMoni: 'זיהוי התראות ברקע',
    // helloWorld: 'הכתבת טופס',
    // ambN: 'מספר אמבולנס',
    // visa: 'מספר ויזה',
    // address: 'כתובת אירוע',
    // HN: 'חולה נפגע',
    // HT: 'התחייבות',
    // titleActivityHomePage: 'מסך כניסה',
    // titleActivityMainMenu: 'מסך כניסה',
    // SUM: 'סכום',
    // Fna: 'שם פרטי',
    // Lna: 'שם משפחה',
    // code: 'קוד רפואי',
    // succ: 'שלח',
    // ret: 'ביטול',
    // action_settings: 'הגדרות',
    // send: 'שלח',
    // Mrec: 'אשר קבלה',
    // Mrec2: 'חזור',
    // Minf: 'פרטי מקרה',
    // Mhac: 'עבור להכתבה',
    // test_beep: 'דמה קבלת מקרה',
    // title_activity_msgaprove: 'אישור סופי',
    // title_activity_sms_res: 'הודעה חדשה',
    // title_activity_main2: 'דיווח אנשי צוות',
    // enjoy: 'משמרת מהנה!',
    // Nteam: 'הוספת איש צוות',
    // teamN: 'שם איש צוות',
    // teamT: 'ת.ז:',
    // teamE: `ניתן לדווח על 3 מתנדבים ונהג.\nהדיווח הוא של שם פרטי ומשפחה יחד עם ת.ז.
    // \nכדי לשלוח פרטי מתנדב יש להפעיל תחילה את השורה שלו באמצעות לחיצה על התיבה מימינו.
    // \nמשמרת מהנה!`,
    // title_activity_dialog_parent: 'אישור קריאת התראות',
    // dialog_help: 'כדי לאפשר לאפליקציה לזהות מקרים באפליקציית מדא, יש לאפשר לה גישה להתראות.\nכדי לאשר גישה, יש ללחוץ אישור ובעמוד שיפתח לבחור ב"דיווחי מגן דוד אדום", ולאפשר את האפשרות הזו.',
    // dialog_yes: 'איפשור גישה להתראות',
    // dialog_rate_yes: 'דרג כעת',
    // dialog_never: 'לא תודה',
    // title_activity_code: 'בקשת קוד הפעלה',
    // title_code: 'הכנסת קוד הפעלה',
    // codeNote: 'כתוב את פרטיך והוסף בהערות את פרטי ההתנדבות שלך (מרחב ותפקיד) ואנו נבחן את הפרטים ונשלח קוד הפעלה בהקדם.',
    // driverQ: 'נהג',
    // aQ: 'מתנדב 1',
    // bQ: 'מתנדב 2',
    // cQ: 'מתנדב 3',
    // tQ: 'מספר ת.ז.',
    // notMada:
    //     'נראה לנו שאתה לא מתנדב מד"א.\nבכדי שהאפליקציה תפעל כראוי צריכה להיות מותקנת אצלך אפליקציית צוותי מגן דוד אדום.\nבאם האפליקציה מותקנת אצלך ובכל זאת אתה רואה מסך זה, התנתק מאפליקציית הצוותים והתחבר שנית.\nשימוש מהנה ומועיל!',
    // notM: 'מתנדב פעיל ונתקל בחלונית זו?\nצור עימנו קשר ונסייע לך.',
    // passH: 'הזן כאן את הקוד',
    // title_activity_code_c: 'התחברות בקוד',
    // title_activity_help_page: 'עמוד הסברים',
    // pref_description_enable_on: 'זיהוי מקרים בסמס פועל.',
    // pickC: 'לחץ לבחירת קוד',
    // pref_description_vibrate_on: 'רטט בעת קבלת מקרים פועל.',
} as const;

// <resources>
//     <string-array name="languages_entry">
//         <item>en</item>
//         <item>iw</item>
//     </string-array>
//     <string-array name="languages">
//         <item>English</item>
//         <item>עברית</item>
//     </string-array>
//     <string-array name="sleep_entry">
//         <item>0</item>
//         <item>1</item>
//         <item>2</item>
//         <item>3</item>
//         <item>4</item>
//     </string-array>
//     <string-array name="sleep">
//         <item>יום - צליל ורטט. לילה - צליל ורטט.</item>
//         <item>יום - צליל ורטט. לילה - רטט.</item>
//         <item>יום - צליל ורטט. לילה - שקט.</item>
//         <item>יום - רטט. לילה - צליל ורטט.</item>
//         <item>יום - שקט. לילה - צליל ורטט.</item>
//     </string-array>
//     <string-array name="ampm_entry">
//         <item>0</item>
//         <item>1</item>
//         <item>2</item>
//     </string-array>
//     <string-array name="ampm">
//         <item>צליל ורטט</item>
//         <item>רטט</item>
//         <item>שקט</item>
//     </string-array>
//     <string-array name="region_entry">
//         <item>0</item>
//         <item>1</item>
//         <item>2</item>
//         <item>3</item>
//         <item>4</item>
//         <item>5</item>
//         <item>6</item>
//         <item>7</item>
//         <item>8</item>
//         <item>9</item>
//         <item>10</item>
//         <item>11</item>
//     </string-array>
//     <string-array name="region">
//         <item>ירושלים</item>
//         <item>איילון</item>
//         <item>אשר</item>
//         <item>גלבוע</item>
//         <item>דן</item>
//         <item>ירדן</item>
//         <item>ירקון</item>
//         <item>כרמל</item>
//         <item>לכיש</item>
//         <item>נגב</item>
//         <item>שרון</item>
//         <item>מוקד 4</item>
//     </string-array>
//     </string-array>
//     <string name="Mcode">קודים רפואיים</string>
//     <string name="langSwitched">השינוי יכנס לתוקף בכניסה הבאה לאפליקציה.</string>
//     <string name="apply">החל</string>
//     <string name="codeGood">קוד הפעלה נקלט בהצלחה!\nהאפליקציה תופעל כעת!</string>
//     <string name="codeBad">קוד הפעלה\nשגוי או לא קיים. נסה שנית!</string>
//     <string name="codeNotExist">נראה כי מעולם לא שלחת לנו בקשת קוד.\nבמידה ושלחת כבר פעם, אנא נסה לשלוח בקשת קוד שוב, ופרט על התקלה.</string>
//     <string name="codeUsed">הקוד כבר נוצל בעבר.\nלא ניתן להשתמש בקוד יותר מפעם אחת!\nייתכן והייתה תקלה. נסו להזין ידנית את הקוד: %s</string>
//     <string name="codeNotAllowed">קוד לא אושר לשימוש!\nייתכן ואירעה תקלה. נסה להזין קוד ידנית: %s</string>
//     <string name="emailGood">האימייל הגיע אלינו\nאנו נעשה מיטב המאמצים להגיב במהירות.</string>
//     <string name="emailSubjNone">לא רשמת נושא להודעה!</string>
//     <string name="load">טוען. אנא המתן…</string>
//     <string name="pickMokTtl">מוקד ברירת מחדל</string>
//     <string name="pickMokTxt">בשלב זה, טרם בחרת מרחב ברירת מחדל בהגדרות. אנא עבור להגדרות והגדר מרחב ברירת מחדל</string>
//     <string name="goToSet">עבור להגדרות</string>
//     <string name="skip">דלג</string>
//     <string name="SendNoNames">לא ניתן לשלוח ללא אנשי צוות!</string>
//     <string name="protocoles">בחר פרוטוקולים לצפייה</string>
//     <string name="title_activity_main3">הודעה מותאמת אישית</string>
//     <string name="toMoked">כתוב הודעתך למוקד.</string>
//     <string name="customMsgErr">לא כתבת שום הודעה!</string>
//     <string name="msgSent">ההודעה נשלחה בהצלחה!</string>
//     <string name="msgNotSent">שליחה נכשלה, נסה שוב!</string>
//     <string name="smsCountChar">תווים בהודעה.</string>
//     <string name="smsCountMsg">סמסים.</string>
//     <string name="title_activity_moked_page">דיווחים למוקד</string>
//     <string name="button_a">הכתבת טפסים</string>
//     <string name="button_b">דיווח צוות</string>
//     <string name="button_i">הודעה כללית</string>
//     <string name="button_h">כלי עזר</string>
//     <string name="button_j">דיווחים למוקד</string>
//     <string name="newSettings">נראה כי זוהי כניסתך הראשונה לאפליקציה.\nמומלץ לבדוק את כל ההגדרות לפני תחילת השימוש</string>
//     <string name="privacyMsg">נראה כי זוהי כניסתך הראשונה לאפליקציה. יש לקרוא את מדיניות הפרטיות כעת.</string>
//     <string name="importanceMsg">זיהינו כי התראה על זיהוי הודעות ברקע מופיעה במסך.\nלידיעתך, ניתן להעלים אותו על ידי העברה לדחיפות נמוכה.</string>
//     <string name="importance">הורד דחיפות</string>
//     <string name="search">חיפוש</string>
//     <string name="newCall">אירוע חדש!</string>
//     <string name="edit">ערוך</string>
//     <string name="delete">הסר</string>

//     <string name="err_amb">לא הכנסת מספר אמבולנס!</string>
//     <string name="err_hn">לא הכנסת מספר חולה נפגע!</string>
//     <string name="err_ht">לא הכנסת מספר התחייבות!</string>
//     <string name="err_fn">לא הכנסת שם פרטי!</string>
//     <string name="err_ln">לא הכנסת שם משפחה!</string>
//     <string name="err_sum">לא הכנסת סכום!</string>
//     <string name="err_mc">לא הכנסת קוד רפואי!</string>

//     <string name="explain">בכדי להוסיף איש צוות לחצו על כפתור ה+ בצד ימין בתחתית המסך.\nבכדי לשלוח את ההודעה למוקד לחצו על הכפתור בצד שמאל.</string>

//     <string name="kodimT">\nקוד הזנקה: %1$s\nקוד רפואי: %2$s</string>
//     <string name="callT">כל האירועים</string>
//     <string name="callTS">הודעות שירות</string>

//     <string name="pref_title_state">התראה לפי סטטוס מכשיר</string>
//     <string name="pref_description_state_off">התראה תצפצף גם כשהמכשיר במצב שקט.</string>
//     <string name="pref_description_state_on">התראה תצפצף רק כאשר המכשיר לא במצב רטט/שקט.</string>
//     <string name="pref_title_stateVib">רטט לפי סטטוס מכשיר</string>
//     <string name="pref_description_stateVib_off">התראה תרטוט גם כשהמכשיר במצב שקט.</string>
//     <string name="pref_description_stateVib_on">התראה תרטוט רק כאשר המכשיר לא במצב שקט</string>
//     <string name="pref_title_sleep">מצב יום-לילה</string>
//     <string name="pref_description_sleep_off">פיצול סגנון התראה בהתאם לשעה (לילה מ23:00 עד 7:00).\\\\nשימו לב!\\\\nהגדרה זו לא מפעילה צליל ורטט, אלא מאפשרת אותם אם הם פעילים.</string>
//     <string name="time">התקבל בשעה: %1$s</string>

//     <string name="banner_ad_unit_id" translatable="false">ca-app-pub-7600269766434003/5732229370</string>
//     <string name="service_exp">בכדי שנוכל לזהות את המקרים המגיעים למכשיר, אפשרות זו צריכה להיות דולקת.\nבמידה ואפשרות זו לא דולקת, לא יזוהו מקרים כלל.</string>
//     <string name="seconds">שניות</string>
//     <string name="notifDay">התראות בשעות יום</string>
//     <string name="notifNight">התראות בשעות לילה</string>
//     <string name="nightEnd">סוף משמרת לילה</string>
//     <string name="nightStart">תחילת משמרת לילה</string>
//     <string name="nightStartDesc">בחר מאיזו שעה יתריעו מקרים לפי הגדרת הלילה</string>
//     <string name="nightEndDesc">בחר מאיזו שעה יחזרו מקרים להתריע לפי הגדרת יום</string>
//     <string name="toDispatch">"ישלח למוקד: "</string>
//     <string name="subject">"נושא: "</string>
//     <string name="note">הערות</string>

//     <string name="navigation_drawer_open">פתח מגירה</string>
//     <string name="navigation_drawer_close">סגור מגירה</string>

//     <string name="title_mda">הורדת יישומון מד\"א</string>
//     <string name="BGServiceTtl">מאזין ברקע להודעות</string>
//     <string name="BGServiceTicker">מזהה הודעות חדשות ממד\"א</string>
//     <string name="BGServiceTxt">מזהה הודעות חדשות שנשלחות ממוקד מד\"א ברקע</string>
//     <string name="NoPicked">יש לסמן לכל הפחות כלי עזר אחד!</string>
//     <string name="title_low_calls">הודעות לא קריטיות</string>
//     <string name="title_notif">התראה</string>
//     <string name="pick_sound">בחר צליל</string>
//     <string name="numberNone">לא רשמת מספר!</string>
//     <string name="general_settings">הגדרות כלליות</string>
//     <string name="langBad">החלפת שפה הופסקה בשל תקלה. ודא שיש חיבור לרשת ונסה שוב!</string>
//     <string name="langLoad">מוריד את השפה המבוקשת %s</string>
// </resources>
