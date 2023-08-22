package atlow.chemi.mymada.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.appcompat.content.res.AppCompatResources;

public class UtilsJava {
    public static Drawable getDrawable(Context context, int id) {
        return AppCompatResources.getDrawable(context, id);
    }
    public static int getColorWrapper(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return context.getColor(id);
        else
            return context.getResources().getColor(id);
    }
}
