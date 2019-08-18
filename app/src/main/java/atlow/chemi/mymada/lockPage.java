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
import android.widget.Button;

import com.crashlytics.android.Crashlytics;

import atlow.chemi.mymada.helpers.LocalHelper;

public class lockPage extends AppCompatActivity {
    private Button btn5, btn6, btn7;
    private boolean isMove5 = false, isMove6 = false, isMove7 = false;
    private float oldX5, oldY5, oldX6, oldY6, oldX7, oldY7;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_not_mada);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btn5 = findViewById(R.id.b9);
        btn6 = findViewById(R.id.b6);
        btn7 = findViewById(R.id.b4);
        btn5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX5 - event.getX() > 30 || event.getX() - oldX5 > 30;
                        boolean y = oldY5 - event.getY() > 30 || event.getY() - oldY5 > 30;
                        if (x || y)
                            isMove5 = true;
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldX5 = event.getX();
                        oldY5 = event.getY();
                        isMove5 = false;
                        btn5.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btn5.setAlpha(1f);
                        if (!isMove5)
                            btn5.performClick();
                        return true;
                }
                return false;
            }
        });
        btn6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX6 - event.getX() > 30 || event.getX() - oldX6 > 30;
                        boolean y = oldY6 - event.getY() > 30 || event.getY() - oldY6 > 30;
                        if (x || y)
                            isMove6 = true;
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldX6 = event.getX();
                        oldY6 = event.getY();
                        isMove6 = false;
                        btn6.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btn6.setAlpha(1f);
                        if (!isMove6)
                            btn6.performClick();
                        return true;
                }
                return false;
            }
        });
        btn7.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX7 - event.getX() > 30 || event.getX() - oldX7 > 30;
                        boolean y = oldY7 - event.getY() > 30 || event.getY() - oldY7 > 30;
                        if (x || y)
                            isMove7 = true;
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldX7 = event.getX();
                        oldY7 = event.getY();
                        isMove7 = false;
                        btn7.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btn7.setAlpha(1f);
                        if (!isMove7)
                            btn7.performClick();
                        return true;
                }
                return false;
            }
        });
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
    }
    private void resetTitle() {
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA).labelRes;
            if (label != 0)
                setTitle(label);
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.log(e.getMessage());
        }
    }
    public void cod(View view) {
        Intent intent = new Intent(getApplicationContext(), codeC.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }
    public void cond(View view) {
        Intent intent = new Intent(getApplicationContext(), codeRequest.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void TZapp(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.groboot.mdaemergency"));
        try {
            startActivity(intent);
        } catch (Exception e) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.groboot.mdaemergency"));
        }
    }
}
