package atlow.chemi.mymada.recieversAndServices;

import static atlow.chemi.mymada.Constants.METRONOME_STATE;
import static atlow.chemi.mymada.Constants.METRONOME_TOGGLED;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.util.HashMap;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import atlow.chemi.mymada.MetronomeManager;

@TargetApi(Build.VERSION_CODES.N)
public class MyTileService extends TileService {
    private final Consumer<HashMap<String, Object>> onToggleHandler =  val -> {
        var state = (boolean)val.getOrDefault(METRONOME_STATE, false);
        toggleState(state ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
    };
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        toggleState(Tile.STATE_INACTIVE);
    }
    @Override
    public void onTileRemoved() {
        toggleState(Tile.STATE_INACTIVE);
        super.onTileRemoved();
        MetronomeManager.getInstance().off(METRONOME_TOGGLED, onToggleHandler);
    }
    @Override
    public void onClick() {
        super.onClick();
        var metronomeManager = MetronomeManager.getInstance();
        metronomeManager.toggleMetronome(getApplicationContext());
    }
    @Override
    public void onStartListening() {
        super.onStartListening();
        var metronomeManager = MetronomeManager.getInstance();
        toggleState(metronomeManager.isActive() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        metronomeManager.on(METRONOME_TOGGLED, onToggleHandler);
    }
    @Override
    public void onStopListening() {
        super.onStopListening();
        MetronomeManager.getInstance().off(METRONOME_TOGGLED, onToggleHandler);
    }

    private int getOppositeState(int state) {
        return state == Tile.STATE_ACTIVE ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE;
    }
    private void toggleState(@Nullable Integer state) {
        Tile tile = getQsTile();
        if (tile == null || (state != null && state == tile.getState())) {
            return;
        }
        if (state == null) {
            state = getOppositeState(tile.getState());
        }
        try {
            tile.setState(state);
        } catch (Exception e)    {
            e.printStackTrace();
        }
        switch (state) {
            case Tile.STATE_ACTIVE:
                tile.setIcon(tile.getIcon().setTint(Color.WHITE));
                break;
            case Tile.STATE_INACTIVE:
            case Tile.STATE_UNAVAILABLE:
            default:
                tile.setIcon(tile.getIcon().setTint(Color.GRAY));
                break;

        }
        tile.updateTile();
    }
}