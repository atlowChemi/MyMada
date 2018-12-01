package atlow.chemi.mymada;
import java.lang.reflect.Field;
import android.content.Context;
import android.graphics.Typeface;

final class FontsOverride {

    static void setDefaultFont(Context context, String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(), fontAssetName);
        try {
            replaceFont(staticTypefaceFieldName, regular);
        }catch (Exception e)    {
            e.printStackTrace();
        }
    }

    private static void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}