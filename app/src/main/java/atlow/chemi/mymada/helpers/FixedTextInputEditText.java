package atlow.chemi.mymada.helpers;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.lang.reflect.Field;

public class FixedTextInputEditText extends TextInputEditText {
    public FixedTextInputEditText(Context context) {
        super(context);
    }
    public FixedTextInputEditText(Context context, AttributeSet set) {
        super(context, set);
    }
    public FixedTextInputEditText(Context context, AttributeSet set, int defStyleAttr) {
        super(context, set, defStyleAttr);
    }
    @Override
    public CharSequence getHint() {
        String manufacturer = Build.MANUFACTURER.toUpperCase();
        if (!manufacturer.contains("MEIZU") || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return super.getHint();
        else
            try {
                return getSuperHintHack();
            } catch (Exception e) {
                return super.getHint();
            }
    }
    private CharSequence getSuperHintHack() throws NoSuchFieldException, IllegalAccessException {
        Field hintField = TextView.class.getDeclaredField("mHint");
        hintField.setAccessible(true);
        return (CharSequence) hintField.get(this);
    }
}