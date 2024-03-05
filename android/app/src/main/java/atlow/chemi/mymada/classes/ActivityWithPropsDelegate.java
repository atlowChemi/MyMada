package atlow.chemi.mymada.classes;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.facebook.react.defaults.DefaultReactActivityDelegate;

public class ActivityWithPropsDelegate extends DefaultReactActivityDelegate {
    private Bundle mInitialProps = null;

    private final ReactActivity mActivity;

    public ActivityWithPropsDelegate(ReactActivity activity, String mainComponentName, Boolean fabricEnabled) {
        super(activity, mainComponentName, fabricEnabled);
        this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mActivity == null) {
            return;
        }
        var bundle = mActivity.getIntent().getExtras();
        if (bundle != null) {
            mInitialProps = bundle;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        if (intent != null) {
            var bundle = intent.getExtras();
            if (bundle != null) {
                mInitialProps = bundle;
            }
        }
        return super.onNewIntent(intent);
    }

    @Override
    protected Bundle getLaunchOptions() {
        return mInitialProps;
    }
}