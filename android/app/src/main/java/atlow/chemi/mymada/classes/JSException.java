package atlow.chemi.mymada.classes;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;

public class JSException extends Exception {
    private final String errorType;
    private final String msg;

    public JSException(String errorType, String msg) {
        super(errorType);
        this.errorType = errorType;
        this.msg = msg;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getMsg() {
        return msg;
    }

    public void rejectPromiseForException(@Nullable Promise promise) throws JSException {
        if (promise == null) {
            throw this;
        }
        promise.reject(errorType, msg);
    }
}
