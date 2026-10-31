package atlow.chemi.mymada.recieversAndServices

/** RN v4 grant identity; execution is native Kotlin, with no headless-JS dependency. */
class NotificationService : atlow.chemi.mymada.NotificationService() {
    override val listenerClass get() = NotificationService::class.java
}
