package atlow.chemi.mymada

import android.widget.EditText
import android.widget.TextView
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** Run only on an offline disposable device: normal startup initializes production SDKs. */
@RunWith(AndroidJUnit4::class)
class PlatformSmokeTest {
    @Test
    fun homeSettingsNavigationAndCalculator() {
        ActivityScenario.launch(HomePage::class.java).use { screen ->
            screen.onActivity { assertNotNull(it.findViewById<android.view.View>(R.id.rv)) }
        }
        ActivityScenario.launch(SettingsPage::class.java).use { screen ->
            screen.onActivity {
                val menu = it.findViewById<NavigationView>(R.id.navigation).menu
                assertTrue(it.onNavigationItemSelected(menu.findItem(R.id.tools)))
                it.supportFragmentManager.executePendingTransactions()
                assertNotNull(it.findViewById<android.view.View>(R.id.metro_pref))
            }
        }
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { screen ->
            screen.onActivity {
                val menu = it.findViewById<NavigationView>(R.id.navigation).menu
                assertTrue(it.onNavigationItemSelected(menu.findItem(R.id.oxygenCalc)))
                it.supportFragmentManager.executePendingTransactions()
                val pressure = it.findViewById<EditText>(R.id.ATM)
                pressure.requestFocus()
                pressure.setText("100")
                assertEquals(it.getString(R.string.oxyCalc, 24), it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                assertEquals(it.getString(R.string.oxyCalc, 1500), it.findViewById<EditText>(R.id.PSI).text.toString())
            }
        }
    }

    @Test
    fun dataStoreNativeCounterRoundTrip() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "native-smoke-${System.nanoTime()}").apply { mkdirs() }
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        try {
            val store = MultiProcessDataStoreFactory.create(
                serializer = object : Serializer<Int> {
                    override val defaultValue = 0
                    override suspend fun readFrom(input: InputStream) = input.read()
                    override suspend fun writeTo(t: Int, output: OutputStream) = output.write(t)
                },
                scope = scope,
                produceFile = { File(directory, "counter") }
            )
            assertEquals(0, store.data.first())
            assertEquals(1, store.updateData { it + 1 })
            assertEquals(1, store.data.first())
            // The native library can map directly from base.apk without its .so name.
            // Its version-counter file must be mmap'ed by the JNI SharedCounter.
            assertTrue(File("/proc/self/maps").readText().contains("${directory.name}/counter.version"))
        } finally {
            scope.coroutineContext.job.cancelAndJoin()
            directory.deleteRecursively()
        }
    }
}
