package atlow.chemi.mymada.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

public class MyDrawerLayout extends DrawerLayout {
    private boolean m_disallowIntercept;

    public MyDrawerLayout (Context context) {
        super(context);
    }
    public MyDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public MyDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        // as the drawer intercepts all touches when it is opened
        // we need this to let the content beneath the drawer to be touchable
        return !m_disallowIntercept && super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setDrawerLockMode(int lockMode) {
        super.setDrawerLockMode(lockMode);
        // if the drawer is locked, then disallow interception
        m_disallowIntercept = (lockMode == LOCK_MODE_LOCKED_OPEN);
    }
}