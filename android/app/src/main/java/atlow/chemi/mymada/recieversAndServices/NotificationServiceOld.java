package atlow.chemi.mymada.recieversAndServices;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * This service class catches Toast or Notification of applications
 *
 * @author pankaj
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class NotificationServiceOld extends AccessibilityService {
    private final AccessibilityServiceInfo info = new AccessibilityServiceInfo();
    private static final String TAG = "MADA_APP";
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            final String sourcePackageName = (String)event.getPackageName();
            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification) {
                final Notification not = (Notification) event.getParcelableData();
                final CharSequence tick = not.tickerText;
                final PendingIntent pi = not.contentIntent;
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                RemoteViews remoteView = not.bigContentView;
                ViewGroup localView = (ViewGroup) inflater.inflate(remoteView.getLayoutId(), null);
                remoteView.reapply(getApplicationContext(), localView);
                Resources resources = null;
                PackageManager pkm = getPackageManager();
                try {
                    resources = pkm.getResourcesForApplication("com.groboot.mdaemergency");
                }   catch (Exception e) {
                    e.printStackTrace();
                }
                if (resources == null)
                    return;
                int TITLE = resources.getIdentifier("android:id/title", null, null);
                int INBOX = resources.getIdentifier("android:id/big_text", null, null);
                //int TEXT = resources.getIdentifier("android:id/text", null, null);
                String title = ((TextView) localView.findViewById(TITLE)).getText().toString();
                String msg = ((TextView) localView.findViewById(INBOX)).getText().toString();
                //text = (TextView) localView.findViewById(TEXT);
                SharedPreferences sp = getSharedPreferences("Settings", 0);
                Boolean Mapp = sp.getBoolean("Mapp", false);
                if (Mapp) {
                    try {
                        Intent mIntent = new Intent("MADA_APP");
                        mIntent.putExtra("pack", sourcePackageName);
                        mIntent.putExtra("text", msg);
                        mIntent.putExtra("tick", tick);
                        if (title.contains("אירוע")) {
                            if (msg.contains("אירוע חדש"))
                                mIntent.putExtra("callT", 1);
                            else if (msg.contains("הודעת קבוצת"))
                                mIntent.putExtra("callT", 2);
                            else if (msg.contains("הודעת צוות"))
                                mIntent.putExtra("callT", 3);
                            else
                                mIntent.putExtra("callT", 1);
                        }
                        GrobootRec.madasApp(pi);
                        NotificationServiceOld.this.getApplicationContext().sendBroadcast(mIntent);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                } else {
                    Log.e(TAG, "not reco is off!");
                }
            }
        } else {
            Log.v(TAG, "Got un-handled Event");
        }
    }
    @Override
    public void onInterrupt() {
    }
    @Override
    public void onServiceConnected() {
        // Set the type of events that this service wants to listen to.
        //Others won't be passed to this service.
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.packageNames = new String[]{"com.groboot.mdaemergency"};
        // If you only want this service to work with specific applications, set their
        // package names here.  Otherwise, when the service is activated, it will listen
        // to events from all applications.
        //info.packageNames = new String[]
        //{"com.appone.totest.accessibility", "com.apptwo.totest.accessibility"};

        // Set the type of feedback your service will provide.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        } else {
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        }
        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated.  This service *is*
        // application-specific, so the flag isn't necessary.  If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }

    /**
     * Check if Accessibility Service is enabled.
     *
     * @param mContext
     * @return <code>true</code> if Accessibility Service is ON, otherwise <code>false</code>
     */
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + NotificationServiceOld.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessabilityService = mStringColonSplitter.next();
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}