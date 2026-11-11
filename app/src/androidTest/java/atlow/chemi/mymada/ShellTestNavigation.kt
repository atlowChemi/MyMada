package atlow.chemi.mymada

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry

/** Preserve the failing window/task state before ActivityScenario teardown. */
fun ComposeTestRule.waitForShellCondition(label: String, condition: () -> Boolean) {
    try {
        waitUntil(5_000, condition)
    } catch (failure: Throwable) {
        runCatching {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val directory = java.io.File(instrumentation.targetContext.filesDir, "shell-failures").apply { mkdirs() }
            val report = buildString {
                appendLine(label)
                instrumentation.runOnMainSync {
                    val monitor = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                    for (stage in androidx.test.runner.lifecycle.Stage.values()) {
                        for (activity in monitor.getActivitiesInStage(stage)) {
                            appendLine("$stage ${activity.javaClass.name} task=${activity.taskId} root=${activity.isTaskRoot} focused=${activity.hasWindowFocus()} finishing=${activity.isFinishing}")
                            val decor = activity.window.decorView
                            val viewport = android.graphics.Rect().also(decor::getWindowVisibleDisplayFrame)
                            appendLine("decor=${decor.width}x${decor.height} viewport=$viewport focus=${activity.currentFocus}")
                        }
                    }
                }
                for (command in listOf("dumpsys activity activities", "dumpsys window windows")) {
                    appendLine(command)
                    android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
                        .bufferedReader().use { appendLine(it.readText()) }
                }
            }
            java.io.File(directory, "$label.txt").writeText(report)
            report.chunked(3000).forEach { android.util.Log.e("ShellFailure", it) }
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                java.io.File(directory, "$label.png").outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
                bitmap.recycle()
            }
        }.exceptionOrNull()?.let(failure::addSuppressed)
        throw failure
    }
}

/** Back dispatch must see the settled drawer, not a partially visible opening/closing frame. */
fun ComposeTestRule.waitForShellDrawer(open: Boolean) {
    val home = onNodeWithTag("shell-destination-${R.id.homeDrawer}")
    val width = InstrumentationRegistry.getInstrumentation().targetContext.resources
        .getDimension(R.dimen.navigation_drawer_width)
    // Material exposes Dismiss only when DrawerState.isOpen, the same state the
    // shell BackHandler consumes. Nearly-full animated bounds alone are too early.
    val dismissibleDrawer = onAllNodes(
        SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsActions.Dismiss) and
            hasAnyDescendant(hasTestTag("shell-destination-${R.id.homeDrawer}")),
        useUnmergedTree = true,
    )
    var lastBounds = ""
    try {
        waitForShellCondition("drawer-$open") {
            val node = home.fetchSemanticsNode()
            val dismissible = dismissibleDrawer.fetchSemanticsNodes().isNotEmpty()
            lastBounds = "window=${node.boundsInWindow} root=${node.boundsInRoot} size=${node.size} expectedWidth=$width displayed=${home.isDisplayed()} dismissible=$dismissible"
            if (open) dismissible && home.isDisplayed() && node.boundsInWindow.width >= width - 1f
            else !dismissible && !home.isDisplayed()
        }
    } catch (failure: Throwable) {
        android.util.Log.e("ShellFailure", "drawer open=$open $lastBounds")
        throw failure
    }
    waitForIdle()
    if (open) home.assertIsDisplayed() else home.assertIsNotDisplayed()
}

/** Route through the rendered shell; feature tests keep their own content assertions. */
fun ComposeTestRule.selectShellDestination(id: Int) {
    // RESUMED can precede the restored Compose shell after a background rotation.
    waitForShellCondition("navigation-ready-$id") {
        onNodeWithTag("shell-navigation").isDisplayed() ||
            onNodeWithTag("shell-destination-${R.id.homeDrawer}").isDisplayed()
    }
    if (!onNodeWithTag("shell-destination-${R.id.homeDrawer}").isDisplayed()) {
        onNodeWithTag("shell-navigation").performClick()
    }
    onNodeWithTag("shell-destination-$id").performScrollTo().performClick()
}
