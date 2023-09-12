export default {
    toolbarTitle: 'Tools',
    pulse: {
        pulse: 'Pulses in 15 seconds',
        fullPulse: 'Beats per minute',
        pulseInputErr: "You didn't write the number of heart beats in 15 seconds!",
        seconds: 'Seconds',
        timerHint: 'Click the grey area to start pulse timer',
    },
    oxygen: {
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
    },
} as const;
