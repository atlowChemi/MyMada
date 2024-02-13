package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;

import atlow.chemi.mymada.helpers.LocalHelper;

public class lockPage extends AppCompatActivity implements View.OnTouchListener {
    private boolean isMove = false;
    private float oldX, oldY;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_mada);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
    }
    public void cod(View view) {
    }
    public void cond(View view) {
    }
    public void TZ_app(View view) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldX = event.getX();
                oldY = event.getY();
                isMove = false;
                v.setAlpha(0.4f);
                return true;
            case MotionEvent.ACTION_MOVE:
                boolean x = oldX - event.getX() > 30 || event.getX() - oldX > 30;
                boolean y = oldY - event.getY() > 30 || event.getY() - oldY > 30;
                if (x || y)
                    isMove = true;
                return true;
            case MotionEvent.ACTION_UP:
                v.setAlpha(1f);
                if (!isMove)
                    v.performClick();
        }
        return  false;
    }
}