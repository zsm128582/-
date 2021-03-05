package com.shimmerz.mediaoptputslice;

import android.service.quicksettings.TileService;

public class QuickSetting extends TileService {
    private final static int STATE_ON = 1;
    private final static int STATE_OFF = 0;
    private  int tileState = STATE_ON;

    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onClick() {
        if(tileState == STATE_ON){

        }else if(tileState == STATE_OFF){

        }
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }
}
