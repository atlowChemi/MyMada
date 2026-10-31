package atlow.chemi.mymada.helpers

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.drawerlayout.widget.DrawerLayout

open class MyDrawerLayout : DrawerLayout {
    private var m_disallowIntercept = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    // Let the content beneath a locked-open drawer receive touches.
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        !m_disallowIntercept && super.onInterceptTouchEvent(ev)

    override fun setDrawerLockMode(lockMode: Int) {
        super.setDrawerLockMode(lockMode)
        m_disallowIntercept = lockMode == LOCK_MODE_LOCKED_OPEN
    }
}
