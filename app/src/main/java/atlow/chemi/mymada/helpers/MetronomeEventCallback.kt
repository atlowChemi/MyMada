package atlow.chemi.mymada.helpers
@FunctionalInterface
fun interface MetronomeEventCallback {
    fun metronomeEventHandler(timestamp : Long)
}
