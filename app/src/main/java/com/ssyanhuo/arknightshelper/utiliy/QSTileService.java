package com.ssyanhuo.arknightshelper.utiliy;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.google.android.material.tabs.TabItem;
import com.ssyanhuo.arknightshelper.overlay.BackendService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(android.os.Build.VERSION_CODES.N)
public class QSTileService extends TileService {

    private final String TAG = "QSTileService";
    private Tile tile;

    @Override
    public void onCreate() {
        super.onCreate();
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        tile = getQsTile();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                assert activityManager != null;
                List<ActivityManager.RunningServiceInfo> runningServiceInfoList = activityManager.getRunningServices(Integer.MAX_VALUE);
                if(runningServiceInfoList.size() > 0 && tile != null){
                    for (int i = 0; i < runningServiceInfoList.size(); i++){
                        if(runningServiceInfoList.get(i).service.getClassName().equals("com.ssyanhuo.arknightshelper.overlay.BackendService")){
                            tile.setState(Tile.STATE_ACTIVE);
                            tile.updateTile();
                            return;
                        }
                    }
                }
                if (tile != null){
                    tile.setState(Tile.STATE_INACTIVE);
                    tile.updateTile();
                }
            }
        },1000,1000);
    }

    @Override
    public void onTileAdded(){
        super.onTileAdded();
        tile = getQsTile();
        Log.i(TAG, "Tile added!");
    }

    @Override
    public void onClick(){
        super.onClick();
        tile = getQsTile();
        if (tile == null){return;}
        Intent intent = new Intent(getApplicationContext(), BackendService.class);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        List<ActivityManager.RunningServiceInfo> runningServiceInfoList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if(runningServiceInfoList.size() > 0){
            for (int i = 0; i < runningServiceInfoList.size(); i++){
                if(runningServiceInfoList.get(i).service.getClassName().equals("com.ssyanhuo.arknightshelper.overlay.BackendService")){
                    tile.setState(Tile.STATE_INACTIVE);
                    tile.updateTile();
                    stopService(intent);
                    return;
                }
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(!Settings.canDrawOverlays(getApplicationContext())){
                Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }
        }
        try{
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
            startService(intent);
        }catch (Exception e){
            Log.e(TAG, "Start service failed!", e);
        }
    }
}
