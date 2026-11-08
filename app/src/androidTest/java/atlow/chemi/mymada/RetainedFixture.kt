package atlow.chemi.mymada

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/** CI packages hash-verified host downloads in the test APK; local files remain supported. */
internal fun retainedFixture(name: String, directory: String? = null): File {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val file = File(instrumentation.targetContext.getExternalFilesDir(directory), name)
    if (!file.isFile) {
        file.parentFile!!.mkdirs()
        instrumentation.context.assets.open("ci-fixtures/$name").use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }
    return file
}
