package atlow.chemi.mymada.helpers;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;


public class EventListener<T> implements java.util.EventListener {
    private final Map<String, Set<Consumer<T>>> eventListenersMap = new HashMap<>();

    public void on(String eventName, Consumer<T> listener) {
        eventListenersMap
                .computeIfAbsent(eventName, k -> new HashSet<>())
                .add(listener);
    }

    public void off(String eventName) {
        eventListenersMap.remove(eventName);
    }

    public void off(String eventName, Consumer<T> listener) {
        Objects.requireNonNull(eventListenersMap.getOrDefault(eventName, new HashSet<>()))
                .remove(listener);
    }

    public void emit(String eventName, T eventData) {
        var listeners = eventListenersMap.getOrDefault(eventName, new HashSet<>());
        if (listeners == null) {
            return;
        }
        for (var listener : listeners) {
            listener.accept(eventData);
        }
    }
}
