import { NativeModules } from 'react-native';
import type { RingerMode } from '../consts';

const { AudioGeneratorModule } = NativeModules;
interface AudioGenerator {
    createPlayer(): Promise<string>;
    destroyWave(id: string): void;
    maximizeVolume(): Promise<number>;
    setVolume(volume: number): Promise<void>;
    destroyAudioTrack(id: string): Promise<void>;
    getEmptySineWave(samples: number): Promise<string>;
    writeSound(playerId: string, waveId: string): Promise<void>;
    getSineWave(samples: number, frequencyOfTone: number): Promise<string>;
    getMaxVolumeForAlarmStream(): Promise<number>;
    getRingerMode(): Promise<RingerMode>;
}

export const audioGenerator = AudioGeneratorModule as AudioGenerator;
