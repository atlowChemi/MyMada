export default {
    form: {
        title: 'Form dictation',
    },

    team: {
        title: 'Team report',
    },

    general: {
        title: 'Custom message',
        msgHint: 'Type your message to the dispatch here',
        msgErr: 'You did not type a message!',
    },
    smsReviewTitle: 'Please review before sending',
    smsDispatchTarget: 'Send to: "{{ region }}" dispatch',
    smsCounter: '{{ length }} characters. {{ msgCount }} SMS messages.',
    regions: {
        Jerusalem: 'Jerusalem',
        Ayalon: 'Ayalon',
        Asher: 'Asher',
        Gilboa: 'Gilboa',
        Dan: 'Dan',
        Yarden: 'Yarden',
        Yarkon: 'Yarkon',
        Carmel: 'Carmel',
        Lachish: 'Lachish',
        Negev: 'Negev',
        Sharon: 'Sharon',
        'Moked 4': 'Moked 4',
    },
} as const;
