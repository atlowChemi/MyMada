package atlow.chemi.mymada.recieversAndServices;

import static atlow.chemi.mymada.Constants.NOTIFICATION_HELPER_SOURCE;
import static atlow.chemi.mymada.Constants.VIBRATION_PATTERN;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import atlow.chemi.mymada.MainReactActivity;
import atlow.chemi.mymada.R;
import io.invertase.firebase.crashlytics.ReactNativeFirebaseCrashlyticsNativeHelper;

public class FloatingWidgetService extends Service {
    private WindowManager mWindowManager;
    private View mFloatingWidgetView, collapsedView, expandedView, removeFloatingWidgetView;
    private ImageView remove_image_view;
    private final Point szWindow = new Point();
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin, currentVolume, overrideVolumeValue;
    private boolean vibrate, sound, infiniteAlert, autoStopAlert, overrideVolume;
    private Uri url;
    private String autoStopAlertDuration;
    private Vibrator vibrator;
    private AudioManager audioManager;
    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent , int flags , int startId) {
        super.onStartCommand(intent, flags , startId);
        cleanup();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mMediaPlayer = new MediaPlayer();

        var extras = intent.getExtras();
        if(extras != null) {
            vibrate = extras.getBoolean("vibrate", true);
            sound = extras.getBoolean("sound", true);
            infiniteAlert = extras.getBoolean("infiniteAlert", false);
            url = Uri.parse(extras.getString("uri", "android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.alert));
            autoStopAlert = extras.getBoolean("autoStopAlert", false);
            autoStopAlertDuration = extras.getString("autoStopAlertDuration");
            overrideVolume = extras.getBoolean("overrideVolume", true);
            overrideVolumeValue = extras.getInt("volume", audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        }
        //init WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        getWindowManagerDefaultDisplay();
        //Init LayoutInflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            addRemoveView(inflater);
            addFloatingWidgetView(inflater);
            implementClickListeners();
            implementTouchListenerToFloatingWidgetView();
            doNotification();
        }

        return START_REDELIVER_INTENT;

    }

    private void doNotification() {
        startVibrations();
        playSound();
        if (!autoStopAlert)    {
            return;
        }
        final var finishTime = Integer.parseInt(autoStopAlertDuration);
        final var handler = new Handler();
        handler.postDelayed(() -> {
            mediaCleanup();
            stopSelf();
        }, finishTime * 1000L);
    }
    private void startVibrations() {
        if (!vibrate) {
            return;
        }
        var repeat = infiniteAlert ? 0 : -1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            var effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, repeat);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(VIBRATION_PATTERN, repeat);
        }
    }
    private void playSound() {
        if (!sound) {
            return;
        }
        ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: attempt playing sound");
        try {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            if (overrideVolume) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, overrideVolumeValue, 0);
                mMediaPlayer.setVolume(overrideVolumeValue, overrideVolumeValue);
            }
            mMediaPlayer.setDataSource(getApplicationContext(), url);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            if (infiniteAlert) {
                mMediaPlayer.setLooping(true);
            }
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (IOException | RuntimeException e) {
            ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: an exception while playing sound");
            ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
        }
    }
    /*  Add Remove View to Window Manager  */
    @SuppressLint({"InflateParams"})
    private void addRemoveView(@NonNull LayoutInflater inflater) {
        //Inflate the removing view layout we created
        removeFloatingWidgetView = inflater.inflate(R.layout.remove_floating_widget_layout, null);
        //Add the view to the window.
        final var paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        //Specify the view position
        paramRemove.gravity = Gravity.TOP | Gravity.END;
        //Initially the Removing widget view is not visible, so set visibility to GONE
        removeFloatingWidgetView.setVisibility(View.GONE);
        remove_image_view = removeFloatingWidgetView.findViewById(R.id.remove_img);

        //Add the view to the window
        mWindowManager.addView(removeFloatingWidgetView, paramRemove);
    }

    /*  Add Floating Widget View to Window Manager  */
    @SuppressLint({"InflateParams"})
    private void addFloatingWidgetView(@NonNull LayoutInflater inflater) {
        //Inflate the floating view layout we created
        mFloatingWidgetView = inflater.inflate(R.layout.floating_widget_layout, null);
        //Add the view to the window.
        final var params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.END;
        //Initially view will be added to top-left corner.
        params.x = 0;
        params.y = 100;
        //Add the view to the window
        mWindowManager.addView(mFloatingWidgetView, params);
        //find id of collapsed view layout
        collapsedView = mFloatingWidgetView.findViewById(R.id.collapse_view);
        //find id of the expanded view layout
        expandedView = mFloatingWidgetView.findViewById(R.id.expanded_container);
    }

    private void getWindowManagerDefaultDisplay() {
        mWindowManager.getDefaultDisplay().getSize(szWindow);
    }

    /*  Implement Touch Listener to Floating Widget Root View  */
    private void implementTouchListenerToFloatingWidgetView() {
        //Drag and move floating view using user's touch action.
        mFloatingWidgetView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            long time_start = 0, time_end = 0;
            boolean isLongClick = false;//variable to judge if user click long press
            boolean inBounded = false;//variable to judge if floating view is bounded to remove view
            int remove_img_width = 0, remove_img_height = 0;

            final Handler handler_longClick = new Handler();
            final Runnable runnable_longClick = () -> {
                    //Set isLongClick as true
                    isLongClick = true;
                    //Set remove widget view visibility to VISIBLE
                    removeFloatingWidgetView.setVisibility(View.VISIBLE);
                    onFloatingWidgetLongClick();
            };

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                var removeParams = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();
                //get x and y coordinates of remove view
                int remove_x_cord = (szWindow.x - remove_image_view.getWidth()) / 2;
                int remove_y_cord = szWindow.y - (removeFloatingWidgetView.getHeight() + getStatusBarHeight());
                removeParams.x = remove_x_cord;
                removeParams.y = remove_y_cord;
                //Update Remove view params
                mWindowManager.updateViewLayout(removeFloatingWidgetView, removeParams);
                //Get Floating widget view params
                var layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

                //get the touch location coordinates
                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();

                int x_cord_Destination, y_cord_Destination;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();

                        handler_longClick.postDelayed(runnable_longClick, 600);

                        remove_img_width = remove_image_view.getLayoutParams().width;
                        remove_img_height = remove_image_view.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        //remember the initial position.
                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;

                        return true;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        removeFloatingWidgetView.setVisibility(View.GONE);
                        remove_image_view.getLayoutParams().height = remove_img_height;
                        remove_image_view.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        //If user drag and drop the floating widget view into remove view then stop the service
                        if (inBounded) {
                            stopSelf();
                            inBounded = false;
                            break;
                        }

                        //Get the difference between initial coordinate and current coordinate
                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        //The check for x_diff <20 && y_diff< 20 because sometime elements moves a little while clicking, So that is click event.
                        if (Math.abs(x_diff) < 20 && Math.abs(y_diff) < 20) {
                            time_end = System.currentTimeMillis();

                            //Also check the difference between start time and end time should be less than 300ms
                            if ((time_end - time_start) < 300 && isViewCollapsed()) toggleCollapsedView(false);
                        }

                        y_cord_Destination = y_init_margin + y_diff;
                        int barHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (mFloatingWidgetView.getHeight() + barHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (mFloatingWidgetView.getHeight() + barHeight);
                        }

                        layoutParams.y = y_cord_Destination;
                        inBounded = false;
                        //reset position if user drags the floating view
                        resetPosition(x_cord);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        //If user long click the floating view, update remove view
                        if (isLongClick) {
                            int x_bound_left = szWindow.x / 2 - (int) (remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2 + (int) (remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int) (remove_img_height * 1.5);

                            //If Floating view comes under Remove View update Window Manager
                            if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight()));

                                if (remove_image_view.getLayoutParams().height == remove_img_height) {
                                    remove_image_view.getLayoutParams().height = (int) (remove_img_height * 1.5);
                                    remove_image_view.getLayoutParams().width = (int) (remove_img_width * 1.5);

                                    WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();
                                    param_remove.x = x_cord_remove;
                                    param_remove.y = y_cord_remove;

                                    mWindowManager.updateViewLayout(removeFloatingWidgetView, param_remove);
                                }

                                layoutParams.x = x_cord_remove + (Math.abs(removeFloatingWidgetView.getWidth() - mFloatingWidgetView.getWidth())) / 2;
                                layoutParams.y = y_cord_remove + (Math.abs(removeFloatingWidgetView.getHeight() - mFloatingWidgetView.getHeight())) / 2;

                                //Update the layout with new X & Y coordinate
                                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                                break;
                            } else {
                                //If Floating window gets out of the Remove view update Remove view again
                                inBounded = false;
                                remove_image_view.getLayoutParams().height = remove_img_height;
                                remove_image_view.getLayoutParams().width = remove_img_width;
//                                if (isViewCollapsed()) {
//                                    toggleCollapsedView(false);
//                                }
                            }

                        }


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                        return true;
                }
                return false;
            }
        });
    }
    private void implementClickListeners() {
        mFloatingWidgetView.findViewById(R.id.close_floating_view).setOnClickListener((v) -> stopSelf());
        mFloatingWidgetView.findViewById(R.id.close_expanded_view).setOnClickListener((v) -> toggleCollapsedView(true));
        mFloatingWidgetView.findViewById(R.id.open_activity_button).setOnClickListener((v) -> {
            var intent = new Intent(FloatingWidgetService.this, MainReactActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("source", NOTIFICATION_HELPER_SOURCE);
            intent.putExtra("target", "Dispatch");
            intent.putExtra("page", "Form");
            startActivity(intent);
            stopSelf();
        });
    }
    private void toggleCollapsedView(boolean showCollapsedView) {
        collapsedView.setVisibility(showCollapsedView ? View.VISIBLE : View.GONE);
        expandedView.setVisibility(showCollapsedView ? View.GONE : View.VISIBLE);
    }
    /*  on Floating Widget Long Click, increase the size of remove view as it look like taking focus */
    private void onFloatingWidgetLongClick() {
        //Get remove Floating view params
        var removeParams = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();
        //get x and y coordinates of remove view
        int x_cord = (szWindow.x - remove_image_view.getWidth()) / 2;
        int y_cord = szWindow.y - (removeFloatingWidgetView.getHeight() + getStatusBarHeight());
        removeParams.x = x_cord;
        removeParams.y = y_cord;
        //Update Remove view params
        mWindowManager.updateViewLayout(removeFloatingWidgetView, removeParams);
    }
    /*  Reset position of Floating Widget view on dragging  */
    private void resetPosition(int x_cord_now) {
        //Variable to check if the Floating widget view is on left side or in right side
        // initially we are displaying Floating widget view to Left side so set it to true
        if (x_cord_now <= szWindow.x / 2) {
            moveToLeft(x_cord_now);
        } else {
            moveToRight(x_cord_now);
        }
    }
    /*  Method to move the Floating widget view to Left  */
    private void moveToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;
        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            final WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
            public void onTick(long t) {
                long step = (500 - t) / 5;
                //mParams.x = 0 - (int) (current_x_cord * current_x_cord * step);
                //If you want bounce effect uncomment below line and comment above line
                mParams.x = -(int) (double) bounceValue(step, x);
                //Update window manager for Floating Widget
                if (mWindowManager == null) {
                    return;
                }
                try {
                    mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
                }catch (Exception e) {
                    ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: couldn't update view layout");
                    ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
                }
            }
            public void onFinish() {
                mParams.x = 0;
                //Update window manager for Floating Widget
                if (mWindowManager == null) {
                    return;
                }
                try {
                    mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
                }
                catch (Exception e) {
                    ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: couldn't update view layout");
                    ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
                 }
            }
        }.start();
    }
    /*  Method to move the Floating widget view to Right  */
    private void moveToRight(final int current_x_cord) {
        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            final WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
            public void onTick(long t) {
                long step = (500 - t) / 5;
                  mParams.x = szWindow.x + (int) (double) bounceValue(step, current_x_cord) - mFloatingWidgetView.getWidth();
                //Update window manager for Floating Widget
                try {
                    mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
                }catch (Exception e) {
                    ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: couldn't update view layout");
                    ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
                }
            }
            public void onFinish() {
                mParams.x = szWindow.x - mFloatingWidgetView.getWidth();
                //Update window manager for Floating Widget
                try {
                    mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
                }catch (Exception e) {
                    ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: couldn't update view layout");
                    ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
                }
            }
        }.start();
    }
    /*  Get Bounce value if you want to make bounce effect to your Floating Widget */
    private double bounceValue(long step, long scale) {
        return scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step);
    }
    /*  Detect if the floating view is collapsed or expanded */
    private boolean isViewCollapsed() {
        return mFloatingWidgetView == null || mFloatingWidgetView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }
    /*  return status bar height on basis of device display metrics  */
    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }
    /*  Update Floating Widget view coordinates on Configuration change  */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindowManagerDefaultDisplay();
        var layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (layoutParams.y + (mFloatingWidgetView.getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (mFloatingWidgetView.getHeight() + getStatusBarHeight());
                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
            }
            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && layoutParams.x > szWindow.x) {
            resetPosition(szWindow.x);
        }
    }

    @Override
    public void onDestroy() {
        cleanup();
    }

    private void cleanup() {
        ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: Starting cleanup");
        // Remove views
        if (mFloatingWidgetView != null) mWindowManager.removeView(mFloatingWidgetView);
        if (removeFloatingWidgetView != null) mWindowManager.removeView(removeFloatingWidgetView);
        // Stop vibration & media player
        mediaCleanup();
    }

    private void mediaCleanup() {
        if (vibrator != null) vibrator.cancel();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                mMediaPlayer.setVolume(currentVolume, currentVolume);
                mMediaPlayer.release();
            } catch (IllegalStateException e) {
                ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: Failed to stop media player for");
                ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
            }
        }
    }
}