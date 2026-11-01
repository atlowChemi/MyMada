package atlow.chemi.mymada

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry

/** Back dispatch must see the settled drawer, not a partially visible opening/closing frame. */
fun ComposeTestRule.waitForShellDrawer(open: Boolean) {
    val home = onNodeWithTag("shell-destination-${R.id.homeDrawer}")
    val width = InstrumentationRegistry.getInstrumentation().targetContext.resources
        .getDimension(R.dimen.navigation_drawer_width)
    waitUntil(5_000) {
        if (open) home.isDisplayed() && home.fetchSemanticsNode().boundsInWindow.width >= width - 1f
        else !home.isDisplayed()
    }
    waitForIdle()
    if (open) home.assertIsDisplayed() else home.assertIsNotDisplayed()
}

/** Route through the rendered shell; feature tests keep their own content assertions. */
fun SemanticsNodeInteractionsProvider.selectShellDestination(id: Int) {
    if (!onNodeWithTag("shell-destination-${R.id.homeDrawer}").isDisplayed()) {
        onNodeWithTag("shell-navigation").performClick()
    }
    onNodeWithTag("shell-destination-$id").performScrollTo().performClick()
}
