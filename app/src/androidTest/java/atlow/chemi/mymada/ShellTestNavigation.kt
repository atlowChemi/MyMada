package atlow.chemi.mymada

import androidx.compose.ui.test.*

/** Route through the rendered shell; feature tests keep their own content assertions. */
fun SemanticsNodeInteractionsProvider.selectShellDestination(id: Int) {
    if (!onNodeWithTag("shell-destination-${R.id.homeDrawer}").isDisplayed()) {
        onNodeWithTag("shell-navigation").performClick()
    }
    onNodeWithTag("shell-destination-$id").performScrollTo().performClick()
}
