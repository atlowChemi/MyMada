package atlow.chemi.mymada.ReactModules;

import static atlow.chemi.mymada.Constants.ERROR_SERVICE_ERROR;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class TimePickerModule extends ReactContextBaseJavaModule {
    public static final String NAME = "TimePickerModule";
    private Promise promise;
    private final TimePickerDialog.OnTimeSetListener onTimeSet = (view, hourOfDay, minute, second) -> {
        if (promise == null) {
            return;
        }
        promise.resolve(hourOfDay);
        promise = null;
    };

    public TimePickerModule(ReactApplicationContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void openHourPicker(int hour, String title, String color, Promise promise) {
        var pickerDialog = TimePickerDialog.newInstance(onTimeSet, hour, 25, 0, true);
        pickerDialog.enableMinutes(false);
        pickerDialog.setTitle(title);
        var activity = getCurrentActivity();
        if (activity == null) {
            promise.reject(ERROR_SERVICE_ERROR, "TimePickerModule: Failed getting activity");
            return;
        }
        this.promise = promise;
        pickerDialog.setAccentColor(color);
        pickerDialog.setOnDismissListener((v) -> promise.resolve(null));
        pickerDialog.setOnCancelListener((v) -> promise.resolve(null));
        pickerDialog.show(((AppCompatActivity)activity).getSupportFragmentManager(), "tAg");
    }
}
