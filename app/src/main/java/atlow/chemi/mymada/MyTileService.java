package atlow.chemi.mymada;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

@TargetApi(Build.VERSION_CODES.N)
public class MyTileService extends TileService {

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        setCurrentState(Tile.STATE_INACTIVE);
    }

    @Override
    public void onTileRemoved() {
        setCurrentState(Tile.STATE_INACTIVE);
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
                ContextCompat.startForegroundService(MyTileService.this, serviceIntent);
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