package atlow.chemi.mymada.helpers

class Event<T> {
    private val namedListeners = HashMap<String?, T>()
    private val anonymousListeners = ArrayList<T>()

    fun addListener(methodName: String?, namedEventHandlerMethod: T) {
        if (!namedListeners.containsKey(methodName)) namedListeners[methodName] = namedEventHandlerMethod
    }

    fun removeListener(methodName: String?) {
        namedListeners.remove(methodName)
    }

    fun addListener(unnamedEventHandlerMethod: T) {
        anonymousListeners.add(unnamedEventHandlerMethod)
    }

    fun listeners(): MutableList<T> = ArrayList<T>().apply {
        addAll(namedListeners.values)
        addAll(anonymousListeners)
    }
}
