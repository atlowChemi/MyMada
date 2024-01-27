export default {
    toolbarTitle: 'Tools',
    metronome: {
        menuName: 'CPR Metronome',
        title: 'Click anywhere on the screen to start CPR',
        start: 'Start',
        stop: 'Stop',
        notificationTitle: 'Metronome running in Background',
        notificationTxt: 'This notification avoids the metronome shutting down for battery saving.',
        notificationBtn: 'stop',
    },
    contraction: {
        menuName: 'Contractions timer',
        start: 'Contraction start',
        end: 'Contraction end',
        lengthMsg: 'Duration: {{ length }}',
        minutes: 'minutes',
        seconds: 'seconds',
        and: 'and ',
        delta: 'Time between contractions: {{ delta }}',
        recoverDialogMsg: 'Recover previous data?',
        delete: 'Delete current contractions?',
    },
    pulse: {
        menuName: 'Pulse calculator',
        pulse: 'Pulses in 15 seconds',
        fullPulse: 'Beats per minute',
        pulseInputErr: "You didn't write the number of heart beats in 15 seconds!",
        seconds: 'Seconds',
        timerHint: 'Click the grey area to start pulse timer',
    },
    oxygen: {
        menuName: 'Oxygen calculator',
        sizes: {
            twoDot4: 'Small oxygen tank',
            twenty: 'Big oxygen tank',
        },
        atm: 'Atmospheric pressure',
        psi: 'psi pressure',
        minLeft: 'Minutes left',
        clarify: 'Assuming oxygen using 10-lpm.',
    },
    apgar: {
        menuName: 'APGAR calculator',
        score: 'Apgar is: {{ score }}',
        info: `If Apgar score is between 0 - 3, start CPR immediately.
If Apgar score is between 4 - 6, you should oxygenate baby.
Apgar score above 7 is considered normal.`,
        appearance: {
            title: 'Appearance',
            options: ['Blue or pale', 'Blue at extremities, pink body', 'Pink'],
        },
        pulse: {
            title: 'Pulse',
            options: ['Absent', 'Less than 100 bpm', 'More than 100 bpm'],
        },
        grimace: {
            title: 'Grimace',
            options: ['No response to stimulation', 'Slight grimace upon stimulation', 'Cry on stimulation'],
        },
        activity: {
            title: 'Activity (Muscle tone)',
            options: ['None', 'Some flexion', 'Flexed arms and legs'],
        },
        respiration: {
            title: 'Respiration',
            options: ['None', 'Slow, irregular, gasping', 'Strong, vigorous cry'],
        },
    },
    glazgow: {
        menuName: 'Glazgow calculator',
        score: 'Glazgow is: {{ score }} ({{ scoreDesc }})',
        scoreDesc: ['Deep coma', 'Moderate coma', 'Light coma'],
        info: `If Glazgow score is between 0 - 8, patient is in a deep coma.
If Glazgow score is between 9 - 12, patient is in a moderate coma.
If Glazgow score is above 12, patient is in a light coma.`,
        eyes: {
            title: 'Eye Opening',
            options: ['None', 'To pain', 'To speech', 'Spontaneous'],
        },
        verbal: {
            title: 'Verbal Response',
            options: ['None', 'Incomprehensible sounds', 'Inappropriate words', 'Confused', 'Orientated'],
        },
        motor: {
            title: 'Motor Response',
            options: ['None', 'Abnormal Extension', 'Abnormal Flexion', 'Flexion withdrawal from pain', 'Moves to localized pain', 'Obeying Commands'],
        },
    },
    dictionary: {
        menuName: 'Medical dictionary',
        create: {
            title: 'Create term',
            success: 'Added successfully, sent for our approval!',
            failure: 'Term creation failed, please try again.',
        },
        edit: {
            title: 'Edit term',
            btn: 'Edit Term',
            success: 'Edited successfully, sent for our approval!',
            failure: 'Editing failed, please try again.',
        },
        name: 'Medical term',
        definition: "Term's definition",
        searchHint: 'Search dictionary',
    },
    vitals: {
        menuName: 'Vital signs calculator',
        ageRange: {
            adult: 'Adult',
            child: 'Child',
            baby: 'Baby',
        },
        msg: `{{ ageRange }} vitals normal range:
pulse: {{ pulseLow }}-{{ pulseHigh }}
breathing: {{ breathLow }}-{{ breathHigh }}
blood pressure: {{ systolicLow }}-{{ systolicHigh }}/{{ diastolicLow }}-{{ diastolicHigh }}`,
        disclaimer: "The information brought here is based on MDA's books, and may vary between patients. This information is not medical advice.",
        pulse: 'Beats per minute',
        breathing: 'Breaths per minute',
        systolic: 'Systolic',
        diastolic: 'Diastolic',
    },
    protocols: {
        menuName: 'Protocols',
        als: 'ALS protocol',
        bls: 'BLS protocol',
        death: '"Handling the deceased" pamphlet',
    },
} as const;
