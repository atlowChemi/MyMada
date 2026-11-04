package atlow.chemi.mymada

import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.helpers.LocalHelper
import org.junit.Assert.assertTrue

/** Exercise the actual Compose accessibility action, including while its pulse is animating. */
internal fun clickMetronome(running: Boolean = false) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val label = LocalHelper.onAttach(instrumentation.targetContext)
        .getString(if (running) R.string.MetroStop else R.string.startT)
    fun find(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        // Nested Compose hosts can reuse virtual IDs across screens; don't act on cached nodes.
        if (!node.refresh()) return null
        if (node.contentDescription?.toString() == label) {
            // Compose exposes the label as a virtual child when native text leaves are present.
            var action: AccessibilityNodeInfo? = node
            while (action != null && !action.isClickable) action = action.parent
            if (action != null) return action
        }
        repeat(node.childCount) { find(node.getChild(it))?.let { found -> return found } }
        return null
    }
    val deadline = SystemClock.elapsedRealtime() + 8000
    while (SystemClock.elapsedRealtime() < deadline) {
        val button = find(instrumentation.uiAutomation.rootInActiveWindow)
        if (button != null) {
            assertTrue("Metronome is keyboard/accessibility focusable", button.isFocusable)
            assertTrue(button.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            return
        }
        Thread.sleep(50)
    }
    fun tree(node: AccessibilityNodeInfo?): String = if (node == null) "null" else
        "${node.className}: ${node.contentDescription} / ${node.text} clickable=${node.isClickable}\n" +
            (0 until node.childCount).joinToString("") { tree(node.getChild(it)) }
    throw AssertionError("Missing accessible metronome action: $label\n${tree(instrumentation.uiAutomation.rootInActiveWindow)}")
}
