package atlow.chemi.mymada.classes

/** Audio frames are the monotonic beat clock: 4,800 frames / 8 kHz = 600 ms (100 BPM). */
class Metronome {
    private val audio = AudioGenerator(SAMPLE_RATE)
    private val tick = audio.getSineWave(TONE_FRAMES, SAMPLE_RATE, 9440.0)
    private val tock = audio.getSineWave(TONE_FRAMES, SAMPLE_RATE, 9350.0)
    private val silence = DoubleArray(BEAT_FRAMES - TONE_FRAMES)
    @Volatile private var stopped = false

    fun play() {
        synchronized(this) {
            if (stopped) return
            audio.createPlayer()
        }
        try {
            var beat = 0
            while (!stopped) {
                audio.writeSound(if (beat == 0) tock else tick)
                if (stopped) break
                audio.writeSound(silence)
                beat = nextBeat(beat)
            }
        } catch (error: Exception) {
            if (!stopped) throw error
        } finally {
            audio.destroyAudioTrack()
        }
    }

    @Synchronized
    fun stop() {
        stopped = true
        audio.destroyAudioTrack()
    }

    companion object {
        internal const val SAMPLE_RATE = 8000
        internal const val TONE_FRAMES = 1000
        internal const val BEAT_FRAMES = 4800
        internal fun nextBeat(beat: Int): Int = (beat + 1) % 30
    }
}
