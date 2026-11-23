package atlow.chemi.mymada

import android.widget.EditText
import android.widget.TextView
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import org.junit.Rule
import org.junit.runner.RunWith
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** Run only on an offline disposable device: normal startup initializes production SDKs. */
@RunWith(AndroidJUnit4::class)
class PlatformSmokeTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test
    fun homeSettingsNavigationAndCalculator() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.getSharedPreferences("Settings", 0).edit()
            .putStringSet("Tools", setOf("oxy"))
            .putBoolean("Mapp", false).putBoolean("privacy", true).putBoolean("oldU", true).commit()
        context.getSharedPreferences("rate_app", 0).edit().putBoolean("dontshowagain", true).commit()
        android.os.ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")
        ).use { it.readBytes() }
        assertTrue(android.provider.Settings.canDrawOverlays(context))
        ActivityScenario.launch(HomePage::class.java).use { screen ->
            var toolsLabel = ""
            var settingsLabel = ""
            screen.onActivity {
                assertTrue(it.findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0)
                    is androidx.compose.ui.platform.ComposeView)
                toolsLabel = it.getString(R.string.title_activity_utills_and_tools)
                settingsLabel = it.getString(R.string.action_settings)
            }
            for (destination in listOf(UtillitiesAndTools::class.java, SettingsPage::class.java)) {
                val monitor = instrumentation.addMonitor(destination.name, null, false)
                try {
                    if (destination == UtillitiesAndTools::class.java) {
                        compose.onNodeWithText(toolsLabel).performScrollTo().performClick()
                    } else {
                        compose.onNodeWithContentDescription(settingsLabel).performClick()
                    }
                    assertNotNull(instrumentation.waitForMonitorWithTimeout(monitor, 5_000))
                    compose.selectShellDestination(R.id.homeDrawer)
                    waitForHome()
                } finally {
                    instrumentation.removeMonitor(monitor)
                }
            }
        }
        ActivityScenario.launch(SettingsPage::class.java).use { screen ->
            compose.selectShellDestination(R.id.tools)
            compose.waitForIdle()
            screen.onActivity {
                it.supportFragmentManager.executePendingTransactions()
                assertTrue(it.supportFragmentManager.findFragmentById(R.id.frame)?.view
                    is androidx.compose.ui.platform.ComposeView)
            }
            var metroLabel = ""
            screen.onActivity {
                metroLabel = it.getString(atlow.chemi.mymada.settings.toolOptions.first { option -> option.first == "metro" }.second)
            }
            compose.onNodeWithText(metroLabel).performScrollTo().performClick().assertIsOn()
            screen.recreate()
            compose.onNodeWithText(metroLabel).assertIsOn()
            assertEquals(setOf("oxy", "metro"), context.getSharedPreferences("Settings", 0).getStringSet("Tools", null))
        }
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { screen ->
            compose.selectShellDestination(R.id.oxygenCalc)
            compose.waitForIdle()
            screen.onActivity {
                val pressure = it.findViewById<EditText>(R.id.ATM)
                pressure.requestFocus()
                pressure.setText("100")
            }
            compose.waitForIdle()
            screen.onActivity {
                assertEquals(it.getString(R.string.oxyCalc, 24), it.findViewById<TextView>(R.id.txtOxyMin).text.toString())
                assertEquals(it.getString(R.string.oxyCalc, 1500), it.findViewById<EditText>(R.id.PSI).text.toString())
            }
        }
    }

    private fun waitForHome() = compose.waitForShellCondition("smoke-return-home") {
        var home = false
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            home = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED)
                .any { it is HomePage && it.hasWindowFocus() }
        }
        home
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
