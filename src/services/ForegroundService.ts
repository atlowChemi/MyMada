import { injectable } from 'inversify';
import { NativeModules, NativeEventEmitter } from 'react-native';
import type { NotificationConfig } from './NotificationService';

const { ForegroundServiceModule } = NativeModules;

type Handler = (...args: any[]) => void;
type Event = 'ForegroundServiceButtonPressed' | 'TilePressed';
type EventPayloadType = 'FOREGROUND_SERVICE_BUTTON_PRESSED' | 'TILE_PRESSED';
type EventPayload = { event: EventPayloadType } & {
    event: 'TILE_PRESSED';
    state: number;
};

@injectable()
export class ForegroundService {
    private listeners = new Map<Event, Set<Handler>>();
    private readonly eventEmitter = new NativeEventEmitter(ForegroundServiceModule);

    constructor() {
        this.eventEmitter.addListener('ForegroundServiceButtonPressed', this.foregroundServiceButtonPressed);
        this.eventEmitter.addListener('TilePressed', this.tilePressed);
    }

    /** Start foreground service */
    async startService(notificationConfig: NotificationConfig): Promise<void> {
        return await ForegroundServiceModule.startService(notificationConfig);
    }

    /** Stop foreground service */
    async stopService(): Promise<void> {
        return await ForegroundServiceModule.stopService();
    }

    /**
     * Adds a handler to be invoked when button on notification will be pressed.
     * The data arguments emitted will be passed to the handler function.
     */
    on(event: Event, handler: Handler) {
        if (typeof handler !== 'function') {
            console.warn('ForegroundService: on: handler is not a Function');
            return;
        }
        if (!this.listeners.has(event)) {
            this.listeners.set(event, new Set());
        }
        this.listeners.get(event)?.add(handler);
    }

    /**
     * Removes the registered `handler` for the specified event.
     *
     * If `handler` is not provided, this function will remove all registered handlers.
     */
    off(event: Event, handler?: Handler) {
        if (!this.listeners.has(event)) {
            return;
        }
        if (typeof handler !== 'undefined') {
            this.listeners.get(event)?.delete(handler);
            return;
        }
        this.listeners.set(event, new Set());
    }

    private emit(event: Event, ...args: any[]) {
        const handlers = this.listeners.get(event);
        handlers?.forEach(handler => handler(...args));
    }

    private foregroundServiceButtonPressed = (event: EventPayload) => this.emit('ForegroundServiceButtonPressed', event);
    private tilePressed = (event: EventPayload) => this.emit('TilePressed', event);
}
