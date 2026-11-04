package atlow.chemi.mymada.tools

import android.os.Build
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ColorProtection
import androidx.core.view.insets.ProtectionLayout
import atlow.chemi.mymada.R

/** The shared Tools/Settings content already fits insets; paint the transparent status area. */
object ToolsWindow {
    @JvmStatic fun setContentView(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT < 35) {
            activity.setContentView(R.layout.activity_utillities_and_tools)
            return
        }
        WindowCompat.enableEdgeToEdge(activity.window)
        val protection = ColorProtection(WindowInsetsCompat.Side.TOP, activity.getColor(R.color.ColorPrimary))
        val container = ProtectionLayout(activity, listOf(protection))
        container.addView(activity.layoutInflater.inflate(R.layout.activity_utillities_and_tools, container, false),
            MATCH_PARENT, MATCH_PARENT)
        activity.setContentView(container)
        WindowCompat.getInsetsController(activity.window, container).isAppearanceLightStatusBars = false
    }
}
