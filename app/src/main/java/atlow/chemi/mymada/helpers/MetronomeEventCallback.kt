package atlow.chemi.mymada.helpers
@FunctionalInterface
interface MetronomeEventCallback {
    fun metronomeEventHandler(timestamp : Long)
}