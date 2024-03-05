package atlow.chemi.mymada;

import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import atlow.chemi.mymada.classes.ActivityWithPropsDelegate;

public class MainReactActivity extends ReactActivity implements DefaultHardwareBackBtnHandler {
    @Override
    protected String getMainComponentName() {
        return "Root";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ActivityWithPropsDelegate(this, getMainComponentName(), DefaultNewArchitectureEntryPoint.getFabricEnabled());
    }
}