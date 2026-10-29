package atlow.chemi.mymada.helpers;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.textfield.TextInputEditText;

// Retain the XML class name; Material now handles the Meizu hint workaround itself.
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
}
