package atlow.chemi.mymada;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactActivityDelegate;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.i18nmanager.I18nUtil;

public class MainReactActivity extends ReactActivity implements DefaultHardwareBackBtnHandler {
    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "Root";
    }

    public static class MyActivityDelegate extends DefaultReactActivityDelegate {
        private static final String TEST = "test";
        private Bundle mInitialProps = null;

        private final ReactActivity mActivity;

        public MyActivityDelegate(ReactActivity activity, String mainComponentName, Boolean fabricEnabled) {
            super(activity, mainComponentName, fabricEnabled);
            this.mActivity = activity;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            if (mActivity == null) {
                return;
            }
            Bundle bundle = mActivity.getIntent().getExtras();
            if (bundle != null) {
                mInitialProps = bundle;
            }
            super.onCreate(savedInstanceState);
        }

        @Override
        public boolean onNewIntent(Intent intent) {
            if (intent != null) {
                Bundle bundle = intent.getExtras();
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

    /**
     * Returns the instance of the {@link ReactActivityDelegate}. Here we use a util class {@link
     * DefaultReactActivityDelegate} which allows you to easily enable Fabric and Concurrent React
     * (aka React 18) with two boolean flags.
     */
    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new MyActivityDelegate(
                this,
                getMainComponentName(),
                // If you opted-in for the New Architecture, we enable the Fabric Renderer.
                DefaultNewArchitectureEntryPoint.getFabricEnabled());
    }
}