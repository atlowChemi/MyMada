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

public class lockPage extends AppCompatActivity implements View.OnTouchListener {
    private boolean isMove = false;
    private float oldX, oldY;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_not_mada);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.b9).setOnTouchListener(this);
        findViewById(R.id.b6).setOnTouchListener(this);
        findViewById(R.id.b4).setOnTouchListener(this);
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