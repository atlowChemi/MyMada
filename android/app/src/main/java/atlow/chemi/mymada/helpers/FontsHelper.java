package atlow.chemi.mymada.helpers;
import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.Typeface;

public final class FontsHelper {
    private static final String[] fieldNames = new String[] {"DEFAULT", "MONOSPACE", "SERIF", "SANS_SERIF"};
    public static void setupFonts(Context context) {
        final var typeface = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans.ttf");
        for (var fieldName : fieldNames) {
            setupFont(typeface, fieldName);
        }
    }
    private static void setupFont(Typeface typeface, String staticTypefaceFieldName) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, typeface);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }
}
