package atlow.chemi.mymada.ReactModules;

import static atlow.chemi.mymada.Constants.ERROR_SERVICE_ERROR;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class TimePickerModule extends ReactContextBaseJavaModule implements TimePickerDialog.OnTimeSetListener {
    public static final String NAME = "TimePickerModule";
    private Promise promise;

    public TimePickerModule(ReactApplicationContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void openHourPicker(int hour, String title, Promise promise) {
        var pickerDialog = TimePickerDialog.newInstance(this, hour, 25, 0, true);
        pickerDialog.enableMinutes(false);
        pickerDialog.setTitle(title);
        var activity = getCurrentActivity();
        if (activity == null) {
            promise.reject(ERROR_SERVICE_ERROR, "TimePickerModule: Failed getting activity");
            return;
        }
        this.promise = promise;
        pickerDialog.setOnCancelListener((v) -> promise.resolve(null));
        pickerDialog.show(((AppCompatActivity)activity).getSupportFragmentManager(), "tAg");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        promise.resolve(hourOfDay);
        promise = null;
    }
}
