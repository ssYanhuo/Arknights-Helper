package com.ssyanhuo.arknightshelper.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.misc.StaticData;
import com.ssyanhuo.arknightshelper.fragment.BottomAboutFragment;
import com.ssyanhuo.arknightshelper.fragment.BottomFeedbackFragment;
import com.ssyanhuo.arknightshelper.fragment.BottomSettingsFragment;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {
    MaterialCardView startEngineCardView;
    MaterialCardView engineConfigCardView;
    MaterialCardView settingsCardView;
    MaterialCardView feedbackCardView;
    MaterialCardView aboutCardView;
    CoordinatorLayout rootView;
    ContextThemeWrapper contextThemeWrapper;
    static final String TAG = "MainActivity2";
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        preferences = getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", MODE_PRIVATE);
        contextThemeWrapper = this;
        rootView = findViewById(R.id.main_root_view);
        startEngineCardView = findViewById(R.id.main_start_engine_card_view);
        engineConfigCardView = findViewById(R.id.main_engine_config_card_view);
        settingsCardView = findViewById(R.id.main_settings_card_view);
        feedbackCardView = findViewById(R.id.main_feedback_card_view);
        aboutCardView = findViewById(R.id.main_about_card_view);
        settingsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BottomSettingsFragment().show(getSupportFragmentManager(), null);
            }
        });
        feedbackCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BottomFeedbackFragment().show(getSupportFragmentManager(), null);
            }
        });
        aboutCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BottomAboutFragment().show(getSupportFragmentManager(), null);
            }
        });
        startEngineCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEngine(true);
            }
        });
    }
    private void startEngine(final boolean startGame){
        //new DataUpdateDialog().showDialog(getApplicationContext());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setCancelable(false)
                        .setTitle(R.string.get_permission_storage_title)
                        .setMessage(R.string.get_permission_storage_content)
                        .setPositiveButton(R.string.get_permission_manually, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        });
                builder.show();
                return;
            }
        }


        Snackbar.make(rootView, R.string.start_game, Snackbar.LENGTH_LONG).show();
        Timer timer = new Timer();
        final Handler handler = new Handler();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final SharedPreferences.Editor editor = preferences.edit();
                Intent overlayServiceIntent = new Intent(getApplicationContext(), OverlayService.class);
                Looper.prepare();
                if (preferences.getInt("versionLast", -1) != BuildConfig.VERSION_CODE
                        || !FileUtils.checkFiles(getApplicationContext(), getFilesDir().getPath(), StaticData.Const.DATA_LIST)
                        || BuildConfig.BUILD_TYPE == "debug") {
                    FileUtils.copyFilesFromAssets(getApplicationContext(), StaticData.Const.DATA_LIST);
                }
                if((Build.BRAND.equals("Meizu") || Build.BRAND.equals("MEIZU") || Build.BRAND.equals("MeiZu") || Build.BRAND.equals("meizu")) && preferences.getBoolean("firstRun", true)){
                    Snackbar.make(rootView, R.string.meizu_floating_window_permission, Snackbar.LENGTH_INDEFINITE).show();
                    editor.putBoolean("firstRun", false);
                    editor.apply();
                    return;
                }
                editor.putBoolean("firstRun", false);
                editor.apply();

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(Settings.canDrawOverlays(getApplicationContext())){
                        try{
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //changeProgressbarMode(false);
                                }
                            });

                            startService(overlayServiceIntent);
                        }catch (Exception e){
                            Log.e(TAG, "Start service failed!", e);
                        }
                    }else {
                        Snackbar.make(rootView, R.string.no_overlay_permission_error, Snackbar.LENGTH_INDEFINITE).setAction(R.string.no_overlay_permission_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        }).show();
                        return;
                    }
                } else {
                    try{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //changeProgressbarMode(false);
                            }
                        });
                        startService(overlayServiceIntent);
                    }catch (Exception e){
                        Log.e(TAG, "Start service failed!", e);
                    }
                }
                if (startGame){
                    String gameSelected = preferences.getString("game_version", StaticData.Const.PACKAGE_MANUAL);
                    final ArrayList<String> list = PackageUtils.getGamePackageNameList(getApplicationContext());
                    if (!gameSelected.equals(StaticData.Const.PACKAGE_MANUAL) && !list.contains(gameSelected)){
                        preferences.edit().putString("game_version", StaticData.Const.PACKAGE_MANUAL).apply();
                        gameSelected = StaticData.Const.PACKAGE_MANUAL;
                    }
                    if (list.size() == 1){
                        PackageUtils.startApplication(list.get(0), getApplicationContext());
                    }else if (list.size() >= 2){
                        if(!gameSelected.equals(StaticData.Const.PACKAGE_MANUAL)){
                            PackageUtils.startApplication(gameSelected, getApplicationContext());
                        }else {
                            String[] nameArray = new String[list.size()];
                            for (String packageName :
                                    list) {
                                nameArray[list.indexOf(packageName)] = PackageUtils.getName(packageName, getApplicationContext());
                            }
                            final String[] game = {""};
                            String[] packageArray = new String[list.size()];
                            list.toArray(packageArray);
                            //Log.e("", Arrays.toString(arr));
                            new AlertDialog.Builder(contextThemeWrapper)
                                    .setTitle(R.string.start_multiple_apps)
                                    .setSingleChoiceItems(nameArray, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            game[0] = list.get(which);
                                        }
                                    })
                                    .setPositiveButton(R.string.start_game_remember_selection_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (game[0].equals("")){
                                                game[0] = list.get(0);
                                            }
                                            preferences.edit().putString("game_version", game[0]).apply();
                                            PackageUtils.startApplication(game[0], getApplicationContext());
                                        }
                                    })
                                    .setNegativeButton(R.string.start_game_remember_selection_no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (game[0].equals("")){
                                                game[0] = list.get(0);
                                            }
                                            PackageUtils.startApplication(game[0], getApplicationContext());
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    }
                }
                Looper.loop();
            }
        }, 200);
        //TODO 计数
//        versionLast = preferences.getInt("versionLast", -1);
//        if (versionLast == -1 && versionLast != BuildConfig.VERSION_CODE){
//            preferences.edit().putInt("versionLast", BuildConfig.VERSION_CODE).apply();
//            preferences.edit().putInt("up_count_from_last_update", 0).apply();
//        }
    }

}