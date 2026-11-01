package atlow.chemi.mymada.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import atlow.chemi.mymada.SettingsPage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SettingsTextGeometryTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun legacyLineBoxesAndSingleTextSemanticsRespectSystemScaling() {
        val samples = listOf(Triple("Notification", 14, 1), Triple("Notification at new call", 21, 1),
            Triple("Notification will play once.", 16, 2), Triple("First line\nSecond line", 16, 2),
            Triple("הגדרות יום ולילה", 21, 1),
            Triple("When new call arrives, a notification will be fired.", 16, 1),
            Triple("Notifications during the day", 18, 1))
        // Native12822, 1080px/420dpi, font1.0: retain fallback spacing for wrapped/RTL text.
        val normal = listOf(42, 61, 98, if (Build.VERSION.SDK_INT >= 28) 102 else 98,
            if (Build.VERSION.SDK_INT >= 28) 67 else 61,
            if (Build.VERSION.SDK_INT >= 28) 102 else 98, 53)
        var scale = 1f
        var density = 0f
        ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
            scenario.onActivity { activity ->
                scale = activity.resources.configuration.fontScale
                density = activity.resources.displayMetrics.density
                activity.setContentView(ComposeView(activity).apply {
                    setContent {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            samples.forEachIndexed { index, (text, size, lines) ->
                                if (index == samples.lastIndex) Row(Modifier.horizontalScroll(rememberScrollState())) {
                                    SettingsText(text, size, Modifier.testTag("sample-$index"), minLines = lines)
                                } else SettingsText(text, size, Modifier.width(300.dp).testTag("sample-$index"), minLines = lines)
                            }
                        }
                    }
                })
            }
            compose.waitForIdle()
            samples.forEachIndexed { index, (text, _, _) ->
                compose.onAllNodesWithText(text).assertCountEquals(1)
                val height = compose.onNodeWithTag("sample-$index").fetchSemanticsNode().size.height
                if (density == 2.625f && scale == 1f) assertEquals(text, normal[index], height)
                if (density == 2.625f && scale >= 1.5f) assertTrue("$text must scale: $height", height > normal[index])
            }
        }
    }
}
