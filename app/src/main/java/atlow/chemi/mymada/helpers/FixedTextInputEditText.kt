package atlow.chemi.mymada.helpers

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

// Retain the XML class name; Material now handles the Meizu hint workaround itself.
open class FixedTextInputEditText : TextInputEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
