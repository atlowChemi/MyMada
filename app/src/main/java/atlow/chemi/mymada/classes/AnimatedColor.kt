package atlow.chemi.mymada.classes

import android.graphics.Color

open class AnimatedColor(private val mStartColor: Int, private val mEndColor: Int) {
    private val mStartHsv = toHsv(mStartColor)
    private val mEndHsv = toHsv(mEndColor)
    private val mMove = FloatArray(3)

    open fun with(delta: Float): Int {
        if (delta <= 0) return mStartColor
        if (delta >= 1) return mEndColor
        for (index in 0..2) mMove[index] = (mEndHsv[index] - mStartHsv[index]) * delta + mStartHsv[index]
        return Color.HSVToColor(mMove)
    }

    private fun toHsv(color: Int): FloatArray = FloatArray(3).also { Color.colorToHSV(color, it) }
}
