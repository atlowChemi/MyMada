package atlow.chemi.mymada;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;

@TargetApi(Build.VERSION_CODES.N)
public class MyTileService extends TileService {

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        setCurrentState(Tile.STATE_INACTIVE);
    }
    @Override
    public void onTileRemoved() {
        try {
            setCurrentState(Tile.STATE_INACTIVE);
        } catch (NullPointerException e) {
            Crashlytics.log("removing tile error");
            Crashlytics.logException(e);
        }
        super.onTileRemoved();
    }
    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        // switch from active to passive based on tiles current state
        switch (tile.getState()) {
            case Tile.STATE_ACTIVE:
                Intent btn = new Intent(MyTileService.this, ActionReciver.class);
                getApplicationContext().sendBroadcast(btn);
                /*****/
                setCurrentState(Tile.STATE_INACTIVE);
                break;
            case Tile.STATE_INACTIVE:
                Intent serviceIntent = new Intent(MyTileService.this, MetroService.class);
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&manager != null && manager.getNotificationChannel("runInBg") == null) {
                    NotificationChannelGroup group2 = new NotificationChannelGroup("grpOthers", "התראות שונות");
                    NotificationChannel serviceChannel = new NotificationChannel("runInBg", "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
                    serviceChannel.enableVibration(false);
                    serviceChannel.enableLights(false);
                    serviceChannel.setShowBadge(false);
                    serviceChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
                    serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
                    serviceChannel.setGroup("grpOthers");
                    manager.createNotificationChannelGroup(group2);
                    manager.createNotificationChannel(serviceChannel);
                }
                try {
                    ContextCompat.startForegroundService(MyTileService.this, serviceIntent);
                } catch (Exception e) {
                    Crashlytics.log("failed starting metronome tile");
                    Crashlytics.logException(e);
                }
                /*****/
                setCurrentState(Tile.STATE_ACTIVE);
                break;
            case Tile.STATE_UNAVAILABLE:
                break;
        }
    }
    private void setCurrentState(int state){
        Tile tile = getQsTile();
        try {
            tile.setState(state);
        }catch (Exception e)    {
            e.printStackTrace();
        }
        try {
            switch (state) {
                case Tile.STATE_ACTIVE:
                    tile.setIcon(tile.getIcon().setTint(Color.WHITE));
                    break;
                case Tile.STATE_INACTIVE:
                    tile.setIcon(tile.getIcon().setTint(Color.GRAY));
                    break;
                case Tile.STATE_UNAVAILABLE:
                    tile.setIcon(tile.getIcon().setTint(Color.GRAY));
                    break;
                default:
                    tile.setIcon(tile.getIcon().setTint(Color.GRAY));
                    break;

            }
        } catch (NullPointerException e) {
            Crashlytics.log("Cant get tile icon!");
            Crashlytics.logException(e);
        }
        if (tile != null)
            tile.updateTile();
    }
    @Override
    public void onStartListening() {
        super.onStartListening();
    }
    @Override
    public void onStopListening() {
        super.onStopListening();
    }
}