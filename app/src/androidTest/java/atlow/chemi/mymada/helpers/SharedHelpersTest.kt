package atlow.chemi.mymada.helpers

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.appcompat.view.ContextThemeWrapper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.classes.AnimatedColor
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Modifier

class SharedHelpersTest {
    @Test fun ratingLaunchCounterRetainsStoredKeysAndOptOutShortCircuit() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = target.getSharedPreferences("shared-helpers-rating-test", 0)
        val context = object : ContextWrapper(target) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
                assertEquals("rate_app", name)
                return prefs
            }
        }
        prefs.edit().clear().commit()
        try {
            val before = System.currentTimeMillis()
            rateUs.app_launched(context)
            val first = prefs.getLong("date_first_launch", 0)
            assertTrue(first in before..System.currentTimeMillis())
            assertEquals(1L, prefs.getLong("launch_count", 0))
            rateUs.app_launched(context)
            assertEquals(first, prefs.getLong("date_first_launch", 0))
            assertEquals(2L, prefs.getLong("launch_count", 0))
            prefs.edit().putBoolean("dontshowagain", true).commit()
            rateUs.app_launched(context)
            assertEquals(2L, prefs.getLong("launch_count", 0))
        } finally { prefs.edit().clear().commit() }
    }

    @Test fun xmlConstructorsStaticBridgesAndLockedDrawerRetainBehavior() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = ContextThemeWrapper(instrumentation.targetContext, R.style.AppTheme)
            for (type in listOf(FixedTextInputEditText::class.java, MyDrawerLayout::class.java)) {
                type.getConstructor(Context::class.java).newInstance(context)
                type.getConstructor(Context::class.java, AttributeSet::class.java).newInstance(context, null)
                type.getConstructor(Context::class.java, AttributeSet::class.java, Int::class.javaPrimitiveType)
                    .newInstance(context, null, 0)
            }
            val input = LayoutInflater.from(context).inflate(R.layout.new_word_dialog, null)
            assertTrue(input.findViewById<android.view.View>(R.id.wordName) is FixedTextInputEditText)
            val tools = LayoutInflater.from(context).inflate(R.layout.activity_utillities_and_tools, null)
            val drawer = tools.findViewById<MyDrawerLayout>(R.id.activity_Utils)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
            val event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
            try { assertFalse(drawer.onInterceptTouchEvent(event)) } finally { event.recycle() }
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            assertEquals(DrawerLayout.LOCK_MODE_UNLOCKED, drawer.getDrawerLockMode(android.view.Gravity.LEFT))
            val color = UtilsJava::class.java.getMethod("getColorWrapper", Context::class.java, Int::class.javaPrimitiveType)
            assertTrue(Modifier.isStatic(color.modifiers))
            assertEquals(context.getColor(android.R.color.black), color.invoke(null, context, android.R.color.black))
            assertNotNull(UtilsJava.getDrawable(context, R.drawable.round_button))
            assertTrue(Modifier.isStatic(rateUs::class.java.getMethod("app_launched", Context::class.java).modifiers))
        }
    }

    @Test fun hsvInterpolationKeepsEndpointAlphaAndInteriorArithmetic() {
        val start = 0x40ff0000
        val end = 0x8000ff00.toInt()
        val transition = AnimatedColor(start, end)
        assertEquals(start, transition.with(-1f))
        assertEquals(start, transition.with(0f))
        assertEquals(end, transition.with(1f))
        assertEquals(end, transition.with(2f))
        assertEquals(Color.YELLOW, transition.with(.5f))
        assertEquals(Color.HSVToColor(floatArrayOf(30f, 1f, 1f)), transition.with(.25f))
    }
}
