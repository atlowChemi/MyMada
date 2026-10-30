package atlow.chemi.mymada

import androidx.test.ext.junit.runners.AndroidJUnit4
import atlow.chemi.mymada.classes.AudioGenerator
import atlow.chemi.mymada.classes.Metronome
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class MetronomeAudioTest {
    @Test fun nativeWaveformAndThirtyBeatCycle() {
        assertEquals(600, Metronome.BEAT_FRAMES * 1000 / Metronome.SAMPLE_RATE)
        var beat = 0
        val accents = mutableListOf<Int>()
        repeat(90) {
            if (beat == 0) accents.add(it)
            beat = Metronome.nextBeat(beat)
        }
        assertEquals(listOf(0, 30, 60), accents)
        val audio = AudioGenerator(8000)
        assertTrue(audio.getSineWave(3000, 8000, 0.0).all { it == 0.0 })
        val wave = audio.getSineWave(1000, 8000, 9440.0)
        wave.forEachIndexed { i, value ->
            assertEquals(Math.sin(2 * Math.PI * i / (8000 / 9440.0)), value, 1e-10)
        }
    }

    @Test fun stopUnblocksWriterAndOtherPlayerSurvives() {
        val metro = AudioGenerator(8000)
        val pulse = AudioGenerator(8000)
        val writing = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()
        metro.createPlayer()
        pulse.createPlayer()
        val writer = Thread {
            try {
                writing.countDown()
                metro.writeSound(DoubleArray(8000 * 10))
            } catch (error: Throwable) { failure.set(error) }
        }
        try {
            writer.start()
            assertTrue(writing.await(2, TimeUnit.SECONDS))
            Thread.sleep(100)
            metro.destroyAudioTrack()
            metro.destroyAudioTrack()
            writer.join(2000)
            assertFalse("Stopped writer must exit", writer.isAlive)
            assertNull(failure.get())
            // Pulse's existing tone+silence sequence still has an independent live player.
            pulse.writeSound(pulse.getSineWave(3000, 8000, 9000.25))
            pulse.writeSound(DoubleArray(3000))
            metro.createPlayer()
            metro.writeSound(DoubleArray(100))
        } finally {
            metro.destroyAudioTrack()
            pulse.destroyAudioTrack()
        }
    }

    @Test fun stopBeforeWorkerStartsDoesNotCreateAudio() {
        val metro = Metronome()
        metro.stop()
        metro.play()
        metro.stop()
    }
}
