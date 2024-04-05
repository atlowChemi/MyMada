package atlow.chemi.mymada.classes;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.facebook.react.defaults.DefaultReactActivityDelegate;

import java.util.Objects;

import io.invertase.firebase.crashlytics.ReactNativeFirebaseCrashlyticsNativeHelper;

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
        ReactNativeFirebaseCrashlyticsNativeHelper.log("ActivityWithPropsDelegate: Got bundle in onCreate: " + (bundle == null ? "null" : "non-null"));
        mInitialProps = Objects.requireNonNullElseGet(bundle, Bundle::new);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        if (intent != null) {
            var bundle = intent.getExtras();
            ReactNativeFirebaseCrashlyticsNativeHelper.log("ActivityWithPropsDelegate: Got extras in onNewIntent: " + (bundle == null ? "null" : "non-null"));
            mInitialProps = Objects.requireNonNullElseGet(bundle, Bundle::new);
        }
        return super.onNewIntent(intent);
    }

    @Override
    protected Bundle getLaunchOptions() {
        ReactNativeFirebaseCrashlyticsNativeHelper.log("ActivityWithPropsDelegate: Returning launch options");
        return mInitialProps;
    }
}