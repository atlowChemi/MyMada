package atlow.chemi.mymada.classes

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.sin

/** Each caller owns its player; releasing one never stops another caller's track. */
class AudioGenerator(private val sampleRate: Int) {
    private var audioTrack: AudioTrack? = null

    init { require(sampleRate > 0) }

    fun getSineWave(samples: Int, sampleRate: Int, frequencyOfTone: Double): DoubleArray {
        require(samples >= 0 && sampleRate > 0 && frequencyOfTone.isFinite())
        return DoubleArray(samples) { sin(2 * Math.PI * it * frequencyOfTone / sampleRate) }
    }

    @Synchronized
    fun createPlayer() {
        check(audioTrack == null) { "Player already created" }
        val minimum = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT)
        check(minimum > 0) { "Unsupported sample rate: $sampleRate" }
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
            .setBufferSizeInBytes(maxOf(minimum, sampleRate))
            .setTransferMode(AudioTrack.MODE_STREAM).build()
        try {
            check(track.state == AudioTrack.STATE_INITIALIZED) { "AudioTrack initialization failed" }
            track.play()
            audioTrack = track
        } catch (error: Exception) {
            track.release()
            throw error
        }
    }

    fun writeSound(samples: DoubleArray) {
        val pcm = ShortArray(samples.size) { (samples[it].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort() }
        val track = synchronized(this) { checkNotNull(audioTrack) { "Player not created" } }
        var offset = 0
        while (offset < pcm.size) {
            val written = synchronized(this) {
                // Stop/recreate cannot redirect an old write to the new player.
                if (audioTrack !== track) return
                track.write(pcm, offset, pcm.size - offset, AudioTrack.WRITE_NON_BLOCKING)
            }
            check(written >= 0) { "AudioTrack write failed: $written" }
            offset += written
            // Wait outside the lock so stop can immediately flush/release a full buffer.
            if (written == 0) Thread.sleep(2)
        }
    }

    @Synchronized
    fun destroyAudioTrack() {
        val track = audioTrack ?: return
        audioTrack = null
        try {
            track.stop()
        } finally {
            track.release()
        }
    }
}
