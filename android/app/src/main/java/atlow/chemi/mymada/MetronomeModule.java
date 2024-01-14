package atlow.chemi.mymada;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import static atlow.chemi.mymada.Constants.METRONOME_START_TIME;
import static atlow.chemi.mymada.Constants.METRONOME_TOGGLED;
import static atlow.chemi.mymada.Constants.METRONOME_TOGGLED_EVENT_NAME;

import android.util.Log;

import atlow.chemi.mymada.classes.JSException;


@ReactModule(name = MetronomeModule.NAME)
public class MetronomeModule extends ReactContextBaseJavaModule {
    private final MetronomeManager metronomeManager = MetronomeManager.getInstance();
    private final ReactApplicationContext reactContext;
    public static final String NAME = "MetronomeModule";

    public MetronomeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        metronomeManager.on(
                METRONOME_TOGGLED,
                eventData -> sendEvent(METRONOME_TOGGLED_EVENT_NAME, Arguments.makeNativeMap(eventData))
        );
    }

    @ReactMethod
    public void getStartTime(Promise promise) {
        promise.resolve(metronomeManager.getStartTime());
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void addListener(String eventName) {}
    @ReactMethod
    public void removeListeners(Integer count) {}

    @ReactMethod
    public void startMetronome(ReadableMap notificationConfig, Promise promise) {
        try {
            metronomeManager.startMetronome(reactContext, notificationConfig, promise);
        } catch (JSException e) {
            promise.reject(e.getErrorType(), e.getMsg());
        }
    }

    @ReactMethod
    public void stopMetronome(Promise promise) {
        try {
            metronomeManager.stopMetronome(reactContext, promise);
        } catch (JSException e) {
            promise.reject(e.getErrorType(), e.getMsg());
        }
    }

    private void sendEvent(String eventName, @Nullable ReadableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}
