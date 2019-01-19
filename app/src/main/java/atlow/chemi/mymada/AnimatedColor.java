package atlow.chemi.mymada;

import android.graphics.Color;
import android.util.Log;

public class AnimatedColor {
    private final int mStartColor, mEndColor;
    private final float[] mStartHsv, mEndHsv;
    private float[] mMove = new float[3];

    AnimatedColor(int start, int end) {
        mStartColor = start;
        mEndColor = end;
        mStartHsv = toHsv(start);
        mEndHsv = toHsv(end);
    }
    public int with(float delta) {
        if (delta <= 0)
            return mStartColor;
        if (delta >= 1)
            return mEndColor;
        return Color.HSVToColor(move(delta));
    }
    private float[] move(float delta) {
        mMove[0] = (mEndHsv[0] - mStartHsv[0]) * delta + mStartHsv[0];
        mMove[1] = (mEndHsv[1] - mStartHsv[1]) * delta + mStartHsv[1];
        mMove[2] = (mEndHsv[2] - mStartHsv[2]) * delta + mStartHsv[2];
        return mMove;
    }
    private float[] toHsv(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv;
    }
}