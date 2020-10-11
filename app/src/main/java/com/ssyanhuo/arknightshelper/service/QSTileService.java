package com.ssyanhuo.arknightshelper.service;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.MainActivity;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.utils.PythonUtils;

import java.lang.reflect.Method;
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
                        if(runningServiceInfoList.get(i).service.getClassName().equals("com.ssyanhuo.arknightshelper.service.OverlayService")){
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
        Intent intent = new Intent(getApplicationContext(), OverlayService.class);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        List<ActivityManager.RunningServiceInfo> runningServiceInfoList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if(runningServiceInfoList.size() > 0){
            for (int i = 0; i < runningServiceInfoList.size(); i++){
                if(runningServiceInfoList.get(i).service.getClassName().equals("com.ssyanhuo.arknightshelper.service.OverlayService")){
                    try{
                        tile.setState(Tile.STATE_INACTIVE);
                        tile.updateTile();
                        collapseStatusBar();
                        stopService(intent);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
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
            if ((getApplicationContext().getSharedPreferences(StaticData.Const.PREFERENCE_PATH, MODE_PRIVATE).getBoolean("python_finished", false) || !getApplicationContext().getSharedPreferences(StaticData.Const.PREFERENCE_PATH, MODE_PRIVATE).getBoolean("disable_planner", false) || !PythonUtils.isSupported() && getApplicationContext().getSharedPreferences(StaticData.Const.PREFERENCE_PATH, MODE_PRIVATE).getInt("up_count_from_last_update", 0) >= 1)){
                tile.setState(Tile.STATE_ACTIVE);
                tile.updateTile();
                collapseStatusBar();
                startService(intent);
            }else {
                Toast.makeText(this, R.string.tile_init_note, Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(this, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                collapseStatusBar();
                startActivity(intent1);
            }
        }catch (Exception e){
            Log.e(TAG, "Start service failed!", e);
        }
    }
    private void collapseStatusBar(){
        try {
            @SuppressLint("WrongConstant") Object statusBarManager = getApplicationContext().getSystemService("statusbar");
            Method collapse;
            collapse = statusBarManager.getClass().getMethod("collapsePanels");
            collapse.invoke(statusBarManager);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
