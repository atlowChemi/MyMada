package atlow.chemi.mymada.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build

open class UtilsJava {
    companion object {
        @JvmStatic fun getDrawable(context: Context, id: Int): Drawable? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) context.getDrawable(id)
            else context.resources.getDrawable(id)

        @JvmStatic fun getColorWrapper(context: Context, id: Int): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.getColor(id)
            else context.resources.getColor(id)
    }
}
