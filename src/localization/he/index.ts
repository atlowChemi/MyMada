import aboutUs from './aboutUs';
import activationCode from './activationCode';
import common from './common';
import contact from './contact';
import dispatch from './dispatch';
import errors from './errors';
import home from './home';
import lock from './lock';
import permissions from './permissions';
import privacy from './privacy';
import rateUs from './rateUs';
import settings from './settings';
import testCall from './testCall';
import tools from './tools';

export default {
    aboutUs,
    activationCode,
    common,
    contact,
    dispatch,
    errors,
    home,
    lock,
    permissions,
    privacy,
    rateUs,
    settings,
    testCall,
    tools,
    // smsMoni: 'הפעלת האפליקציה ברקע',
    // notiMoni: 'זיהוי התראות ברקע',
    // action_settings: 'הגדרות',
    // Mrec: 'אשר קבלה',
    // Mrec2: 'חזור',
    // Minf: 'פרטי מקרה',
    // Mhac: 'עבור להכתבה',
    // title_activity_msgaprove: 'אישור סופי',
    // title_activity_sms_res: 'הודעה חדשה',
    // title_activity_main2: 'דיווח אנשי צוות',
    // enjoy: 'משמרת מהנה!',
    // title_activity_dialog_parent: 'אישור קריאת התראות',
    // dialog_help: 'כדי לאפשר לאפליקציה לזהות מקרים באפליקציית מדא, יש לאפשר לה גישה להתראות.\nכדי לאשר גישה, יש ללחוץ אישור ובעמוד שיפתח לבחור ב"דיווחי מגן דוד אדום", ולאפשר את האפשרות הזו.',
    // dialog_yes: 'איפשור גישה להתראות',
    // codeNote: 'כתוב את פרטיך והוסף בהערות את פרטי ההתנדבות שלך (מרחב ותפקיד) ואנו נבחן את הפרטים ונשלח קוד הפעלה בהקדם.',
    // driverQ: 'נהג',
    // aQ: 'מתנדב 1',
    // bQ: 'מתנדב 2',
    // cQ: 'מתנדב 3',
    // tQ: 'מספר ת.ז.',
    // passH: 'הזן כאן את הקוד',
    // title_activity_code_c: 'התחברות בקוד',
    // title_activity_help_page: 'עמוד הסברים',
    // pref_description_enable_on: 'זיהוי מקרים בסמס פועל.',
    // pref_description_vibrate_on: 'רטט בעת קבלת מקרים פועל.',
} as const;

// <resources>
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
//     <string name="Mcode">קודים רפואיים</string>
//     <string name="langSwitched">השינוי יכנס לתוקף בכניסה הבאה לאפליקציה.</string>
//     <string name="apply">החל</string>
//     <string name="emailGood">האימייל הגיע אלינו\nאנו נעשה מיטב המאמצים להגיב במהירות.</string>
//     <string name="emailSubjNone">לא רשמת נושא להודעה!</string>
//     <string name="load">טוען. אנא המתן…</string>
//     <string name="protocoles">בחר פרוטוקולים לצפייה</string>
//     <string name="title_activity_main3">הודעה מותאמת אישית</string>
//     <string name="msgSent">ההודעה נשלחה בהצלחה!</string>
//     <string name="msgNotSent">שליחה נכשלה, נסה שוב!</string>
//     <string name="button_j">דיווחים למוקד</string>
//     <string name="newCall">אירוע חדש!</string>

//     <string name="explain">בכדי להוסיף איש צוות לחצו על כפתור ה+ בצד ימין בתחתית המסך.\nבכדי לשלוח את ההודעה למוקד לחצו על הכפתור בצד שמאל.</string>

//     <string name="kodimT">\nקוד הזנקה: %1$s\nקוד רפואי: %2$s</string>
//     <string name="callT">כל האירועים</string>
//     <string name="callTS">הודעות שירות</string>

//     <string name="pref_title_sleep">מצב יום-לילה</string>
//     <string name="pref_description_sleep_off">פיצול סגנון התראה בהתאם לשעה (לילה מ23:00 עד 7:00).\\\\nשימו לב!\\\\nהגדרה זו לא מפעילה צליל ורטט, אלא מאפשרת אותם אם הם פעילים.</string>
//     <string name="time">התקבל בשעה: %1$s</string>

//     <string name="service_exp">בכדי שנוכל לזהות את המקרים המגיעים למכשיר, אפשרות זו צריכה להיות דולקת.\nבמידה ואפשרות זו לא דולקת, לא יזוהו מקרים כלל.</string>
//     <string name="seconds">שניות</string>
//     <string name="subject">"נושא: "</string>
//     <string name="note">הערות</string>

//     <string name="navigation_drawer_open">פתח מגירה</string>
//     <string name="navigation_drawer_close">סגור מגירה</string>

//     <string name="BGServiceTtl">מאזין ברקע להודעות</string>
//     <string name="BGServiceTicker">מזהה הודעות חדשות ממד\"א</string>
//     <string name="BGServiceTxt">מזהה הודעות חדשות שנשלחות ממוקד מד\"א ברקע</string>
//     <string name="title_notif">התראה</string>
//     <string name="pick_sound">בחר צליל</string>
//     <string name="numberNone">לא רשמת מספר!</string>
// </resources>
