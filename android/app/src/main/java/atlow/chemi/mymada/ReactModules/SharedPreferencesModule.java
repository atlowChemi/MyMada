package atlow.chemi.mymada.ReactModules;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ReactModule(name = SharedPreferencesModule.NAME)
public class SharedPreferencesModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    private final Map<String, SharedPreferences> sharedPreferencesMap = new HashMap<>(4);
    public static final String NAME = "SharedPreferencesModule";

    public SharedPreferencesModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    private SharedPreferences initSharedHandler(String name) {
        return sharedPreferencesMap.computeIfAbsent(name, s -> reactContext.getSharedPreferences(s, Context.MODE_PRIVATE));
    }

    @ReactMethod
    public void setItem(String name, String key, String value, Promise promise) {
        var sharedPref = initSharedHandler(name);
        var editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
        promise.resolve(null);
    }

    @ReactMethod
    public void getItem(String name, String key, Promise promise){
        var sharedPref = initSharedHandler(name);
        var value = sharedPref.getString(key, null);
        promise.resolve(value);

    }

    @ReactMethod
    public void getAll(String name, Promise promise){
        var sharedPref = initSharedHandler(name);
        var data = sharedPref.getAll();
        var keys = new ArrayList<>(data.keySet());

        var response = new WritableNativeArray();
        for (int i = 0; i < keys.size(); i++) {
            WritableArray arr = new WritableNativeArray();
            arr.pushString(keys.get(i));
            arr.pushString(String.valueOf(data.get(keys.get(i))));
            response.pushArray(arr);
        }
        promise.resolve(response);
    }
}
