import { now } from 'mobx-utils';
import { injectable } from 'inversify';
import { makeAutoObservable, runInAction } from 'mobx';
import { Dimensions, Platform, NativeModules, NativeEventEmitter } from 'react-native';
import i18n from '../localization';
import { colors } from '../consts';
import { notificationsService, Importance, Priority, NOTIFICATION_MAP } from './NotificationService';

const { MetronomeModule } = NativeModules;

const MINUTE = 60_000;
const TWO_MINUTES = 2 * MINUTE;
const HOUR = 60 * MINUTE;
const CLOCK_COMMON_PROPS = {
    fill: colors.black,
    stroke: colors.black,
    fontSize: 80,
    textAnchor: 'middle',
    x: '50%',
} as const;

@injectable()
export class MetronomeStore {
    public startTime = 0;
    private readonly beat = 30;
    public readonly circle = Math.PI * 2;
    public readonly strokeWidth = 17;
    public readonly diameter = Math.min(Dimensions.get('screen').width - 40, 390);
    public readonly progressBarDiameter = this.diameter * 0.43 - this.strokeWidth / 2;
    private readonly eventEmitter = new NativeEventEmitter(MetronomeModule);

    constructor() {
        makeAutoObservable(this, { circle: false, strokeWidth: false, diameter: false, progressBarDiameter: false }, { autoBind: true });
        this.updateStartTimeFromModule();

        this.eventEmitter.addListener('MetronomeToggled', ({ METRONOME_START_TIME = 0 } = {}) => {
            console.warn('MetronomeManager: MetronomeToggled', METRONOME_START_TIME);
            runInAction(() => (this.startTime = METRONOME_START_TIME));
        });
    }

    public get isActive() {
        return !!this.startTime;
    }

    public get circumference() {
        return this.progressBarDiameter * this.circle;
    }

    private get timeDiff() {
        return now('frame') - this.startTime;
    }

    private get twoMinutePartition() {
        if (!this.startTime) {
            return 0;
        }
        return this.timeDiff / TWO_MINUTES;
    }

    public get progress() {
        const progressOfRound = this.twoMinutePartition % 1;
        const roundCount = Math.floor(this.twoMinutePartition);
        const isOddRound = roundCount % 2 === 1;
        const isOddOddRound = (roundCount / 2) % 2 === 1;
        if (isOddRound && !isOddOddRound) {
            return progressOfRound;
        }
        const factor = isOddRound ? -1 : 1;
        return factor - progressOfRound;
    }

    public get clock() {
        if (!this.startTime) {
            return { time: { ...CLOCK_COMMON_PROPS, y: this.diameter / 2 + 25, children: '00:00' } };
        }
        const diff = this.timeDiff;
        const hours = Math.floor(diff / HOUR);
        const minutes = Math.floor((diff - hours * HOUR) / MINUTE);
        const seconds = Math.floor((diff - minutes * MINUTE) / 1000);
        const hoursString = hours ? `${hours.toString().padStart(2, '0')}` : '';
        const hoursProps = hours ? { ...CLOCK_COMMON_PROPS, y: this.diameter / 2 - 5, children: hoursString } : undefined;
        const timeProps = {
            ...CLOCK_COMMON_PROPS,
            ...(hours && { fontSize: 48 }),
            y: this.diameter / 2 + (hours ? 45 : 25),
            children: `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`,
        };
        return { hours: hoursProps, time: timeProps };
    }

    private async startMetronome() {
        try {
            if (Platform.OS === 'android' && Platform.Version >= 26) {
                await notificationsService.createNotificationChannelGroup({ id: NOTIFICATION_MAP.GROUPS.OTHERS, name: 'התראות שונות', description: 'התראות מהאפליקציה שאינן קשורות לאירועי רפואה' });
                await notificationsService.createNotificationChannel({
                    name: 'פעילות ברקע',
                    id: NOTIFICATION_MAP.CHANNELS.BACKGROUND,
                    group: NOTIFICATION_MAP.GROUPS.OTHERS,
                    importance: Importance.MIN,
                });
            }
            const startTime = await MetronomeModule.startMetronome({
                id: 13,
                color: true,
                icon: 'ic_metronome_tile',
                priority: Priority.PRIORITY_MIN,
                channelId: NOTIFICATION_MAP.CHANNELS.BACKGROUND,
                text: i18n.t('tools:metronome:notificationTxt'),
                button: i18n.t('tools:metronome:notificationBtn'),
                title: i18n.t('tools:metronome:notificationTitle'),
                startTime: Date.now(),
            });
            runInAction(() => (this.startTime = startTime));
        } catch (e) {
            console.warn('MetronomeManager startMetronome failed', e);
            // Crashlytics
        }
    }

    private async stopMetronome() {
        try {
            const startTime = await MetronomeModule.stopMetronome();
            runInAction(() => (this.startTime = startTime));
        } catch (e) {
            console.warn('MetronomeManager stopMetronome failed', e);
            // Crashlytics
        }
    }

    public async updateStartTimeFromModule() {
        const startTime = await MetronomeModule.getStartTime();
        runInAction(() => (this.startTime = startTime));
    }

    public async toggleMetronome() {
        if (this.startTime) {
            return this.stopMetronome();
        }
        await this.startMetronome();
    }
}
