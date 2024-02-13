export default {
    form: {
        title: 'הכתבת טפסים',
    },
    team: {
        title: 'דיווח צוות',
    },
    general: {
        title: 'הודעה כללית',
        msgHint: 'כתוב הודעתך למוקד.',
        msgErr: 'לא כתבת שום הודעה!',
    },
    smsReviewTitle: 'יש לאשר לפני שליחה',
    smsDispatchTarget: 'ישלח למוקד: {{ region }}',
    smsCounter: '{{ length }} תווים. {{ msgCount }} הודעות סמס.',
    regions: {
        Jerusalem: 'ירושלים',
        Ayalon: 'איילון',
        Asher: 'אשר',
        Gilboa: 'גלבוע',
        Dan: 'דן',
        Yarden: 'ירדן',
        Yarkon: 'ירקון',
        Carmel: 'כרמל',
        Lachish: 'לכיש',
        Negev: 'נגב',
        Sharon: 'שרון',
        'Moked 4': 'מוקד 4',
    },
} as const;
