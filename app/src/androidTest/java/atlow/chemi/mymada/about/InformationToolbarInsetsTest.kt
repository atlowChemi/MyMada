package atlow.chemi.mymada.about

import android.app.Activity
import android.view.View
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.PrivacyActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Run on the owned offline emulator: both users of the shared information toolbar. */
class InformationToolbarInsetsTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun informationActionsStayBelowStatusBar() {
        for (activity in listOf<Class<out Activity>>(InfoActivityKt::class.java, PrivacyActivity::class.java)) {
            ActivityScenario.launch(activity).use { scenario ->
                val bounds = compose.onNodeWithTag("information-toolbar").fetchSemanticsNode().boundsInRoot
                scenario.onActivity {
                    val content = it.findViewById<View>(android.R.id.content)
                    val origin = IntArray(2).also(content::getLocationOnScreen)
                    val statusBar = ViewCompat.getRootWindowInsets(content)!!
                        .getInsets(WindowInsetsCompat.Type.statusBars()).top
                    assertTrue("${activity.simpleName}: toolbar $bounds + origin ${origin[1]} overlaps status bar $statusBar",
                        bounds.top + origin[1] >= statusBar)
                    assertEquals(56 * it.resources.displayMetrics.density, bounds.height, 1f)
                }
            }
        }
    }
}
