package atlow.chemi.mymada;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.EditText;

class IntPre extends EditTextPreference {
    private int mTime;
    private EditText mEditText;

    public IntPre(Context context) {
        super(context);
    }

    public IntPre(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntPre(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected String getPersistedString(String defaultReturnValue) {
        try {
            return String.valueOf(getPersistedInt(-1));
        }catch (Exception e)    {
            return "";
        }
    }

    @Override
    protected boolean persistString(String value) {
        try {
            return persistInt(Integer.valueOf(value));
        }catch (Exception e)    {
            return false;
        }
    }
}