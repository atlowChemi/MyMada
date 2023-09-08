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
} as const;
