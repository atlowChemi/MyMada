package atlow.chemi.mymada;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

final class FontsOverride {
    private String TAG = FontsOverride.class.getSimpleName();
    private static FontsOverride instance;
    private static Map<Integer, Typeface> fontMap = new HashMap<>();

    public static FontsOverride getInstance(Context context) {
        if (instance == null)
            instance = new FontsOverride(context);
        return instance;
    }
    private FontsOverride(Context context) {
        AssetManager manager = context.getResources().getAssets();
        fontMap.put(Typeface.NORMAL, Typeface.createFromAsset(manager, "fonts/opensanshebrew.ttf"));
        fontMap.put(Typeface.BOLD, Typeface.createFromAsset(manager, "fonts/opensanshebrewbold.ttf"));
        fontMap.put(Typeface.ITALIC, Typeface.createFromAsset(manager, "fonts/opensanshebrewitalic.ttf"));
        fontMap.put(Typeface.BOLD_ITALIC, Typeface.createFromAsset(manager, "fonts/opensanshebrewBoldItalic.ttf"));
    }
    static void setDefaultFont(Context context, String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(), fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    private static void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    void replaceFonts(ViewGroup viewTree) {
        View child;
        for (int i = 0; i < viewTree.getChildCount(); i++) {
            child = viewTree.getChildAt(i);
            if (child instanceof ViewGroup)
                replaceFonts((ViewGroup)child);
            else if (child instanceof TextView) {
                TextView tv = (TextView)child;
                int style = tv.getTypeface().getStyle();
                if (style == Typeface.NORMAL)
                    tv.setTypeface(fontMap.get(Typeface.NORMAL));
                if (style == Typeface.BOLD)
                    tv.setTypeface(fontMap.get(Typeface.BOLD));
                if (style == Typeface.ITALIC)
                    tv.setTypeface(fontMap.get(Typeface.ITALIC));
                if (style == Typeface.BOLD_ITALIC)
                    tv.setTypeface(fontMap.get(Typeface.BOLD_ITALIC));
            }
        }
    }
}