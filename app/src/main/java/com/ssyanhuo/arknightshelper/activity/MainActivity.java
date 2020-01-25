package com.ssyanhuo.arknightshelper.activity;

import android.Manifest;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final String TAG = "MainActivity";
    Handler handler;
    final int STATE_UP_TO_DATE = 0;
    final int STATE_NEED_UPDATE = 1;
    final int STATE_BETA = 2;
    final int STATE_BETA_FINISHED = 3;
    final int STATE_ERROR = -1;
    final int CODE_STORAGE = 1;
    final String GAME_OFFICIAL = "0";
    final String GAME_BILIBILI = "1";
    final String GAME_MANUAL = "-1";
    final String PACKAGE_OFFICIAL = "com.hypergryph.arknights";
    final String PACKAGE_BILIBILI = "com.hypergryph.arknights.bilibili";
    final String SITE_GITEE = "0";
    final String SITE_GITHUB = "1";
    private SharedPreferences preferences;
    private ContextThemeWrapper contextThemeWrapper;
    String themeNow = "0";
    int versionLast = -1;

    private void startEngine(final View view, final boolean startGame){
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


        Snackbar.make(view, R.string.start_game, Snackbar.LENGTH_LONG).show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final SharedPreferences.Editor appPreferenceEditor = preferences.edit();
                final SharedPreferences.Editor settingPreferenceEditor = preferences.edit();
                Looper.prepare();
                if((Build.BRAND.equals("Meizu") || Build.BRAND.equals("MEIZU")) && preferences.getBoolean("firstRun", true)){
                    Snackbar.make(view, "魅族用户请手动前往系统设置授予应用悬浮窗权限", Snackbar.LENGTH_INDEFINITE).show();
                    appPreferenceEditor.putBoolean("firstRun", false);
                    appPreferenceEditor.apply();
                    return;
                }
                appPreferenceEditor.putBoolean("firstRun", false);
                appPreferenceEditor.apply();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(Settings.canDrawOverlays(getApplicationContext())){
                        Intent intent1 = new Intent(getApplicationContext(), OverlayService.class);
                        try{
                            startService(intent1);
                        }catch (Exception e){
                            Log.e(TAG, "Start service failed!", e);
                        }
                    }else {
                        Snackbar.make(view, R.string.no_overlay_permission_error, Snackbar.LENGTH_INDEFINITE).setAction(R.string.no_overlay_permission_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        }).show();
                        return;
                    }
                } else {
                    Intent intent1 = new Intent(getApplicationContext(), OverlayService.class);
                    try{
                        startService(intent1);
                    }catch (Exception e){
                        Log.e(TAG, "Start service failed!", e);
                    }
                }
                    if(startGame){
                        final Intent intent2 = new Intent(Intent.ACTION_MAIN);
                        intent2.addCategory(Intent.CATEGORY_LAUNCHER);
                        if(checkApplication(PACKAGE_OFFICIAL) && checkApplication(PACKAGE_BILIBILI)){
                            Log.e(TAG, preferences.getString("game_version", GAME_MANUAL));
                            if (preferences.getString("game_version", GAME_MANUAL).equals(GAME_OFFICIAL) || preferences.getString("game_version", GAME_MANUAL).equals(GAME_BILIBILI)){

                                try{
                                    switch (preferences.getString("game_version", GAME_MANUAL)) {
                                        case GAME_OFFICIAL:
                                            startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_OFFICIAL));
                                            break;
                                        case GAME_BILIBILI:
                                            startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_BILIBILI));
                                            break;
                                    }
                                }catch (Exception e){
                                    Log.e(TAG, "Start game failed!", e);
                                }
                                return;
                            }
                            settingPreferenceEditor.putString("game_version", GAME_MANUAL);
                            settingPreferenceEditor.apply();
                            contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, R.style.AppTheme_Default);
                            AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                            final CheckBox checkBox = new CheckBox(contextThemeWrapper);
                            checkBox.setText(R.string.start_game_remember_selection);
                            int padding = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
                            LinearLayout linearLayout = new LinearLayout(contextThemeWrapper);
                            linearLayout.setPadding(padding * 2, padding ,padding * 2, 0);
                            linearLayout.addView(checkBox);
                            builder.setTitle(R.string.start_two_apps)
                                    .setView(linearLayout)
                                    .setPositiveButton(R.string.game_official, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            try{
                                                if (checkBox.isChecked()){
                                                    settingPreferenceEditor.putString("game_version", GAME_OFFICIAL);
                                                    settingPreferenceEditor.apply();
                                                }
                                                startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_OFFICIAL));
                                            }catch (Exception e){
                                                Log.e(TAG, "Start game failed!", e);
                                            }
                                        }
                                    })
                                    .setNeutralButton(R.string.game_bilibili, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            try{
                                                if (checkBox.isChecked()){
                                                    settingPreferenceEditor.putString("game_version", GAME_BILIBILI);
                                                    settingPreferenceEditor.apply();
                                                }
                                                startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_BILIBILI));
                                            }catch (Exception e){
                                                Log.e(TAG, "Start game failed!", e);
                                            }
                                        }
                                    }).show();
                        }else if(checkApplication(PACKAGE_OFFICIAL)){
                            try{
                                settingPreferenceEditor.putString("game_version", GAME_MANUAL);
                                settingPreferenceEditor.apply();
                                startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_OFFICIAL));
                            }catch (Exception e){
                                Log.e(TAG, "Start game failed!", e);
                            }
                        }else if(checkApplication(PACKAGE_BILIBILI)){
                            try{
                                settingPreferenceEditor.putString("game_version", GAME_MANUAL);
                                settingPreferenceEditor.apply();
                                startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_BILIBILI));
                            }catch (Exception e){
                                Log.e(TAG, "Start game failed!", e);
                            }
                        }
                    }
                Looper.loop();
            }
        }, 500);
        versionLast = preferences.getInt("versionLast", -1);
        if (versionLast == -1 && versionLast != BuildConfig.VERSION_CODE){
            //TODO do-something
            preferences.edit().putInt("versionLast", BuildConfig.VERSION_CODE).apply();
        }
    }
    private boolean checkApplication(String packageName) {
        if (packageName == null || "".equals(packageName)){
            return false;
        }
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void preNotifyThemeChanged(){
        setTheme(ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_NO_ACTION_BAR, getApplicationContext()));
        themeNow = String.valueOf(ThemeUtils.getThemeMode(getApplicationContext()));
    }

    private void notifyThemeChanged(Toolbar toolbar){
        if (ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_LIGHT){
            AppCompatImageButton navBtn = (AppCompatImageButton) toolbar.getChildAt(1);
            Drawable drawable = navBtn.getDrawable();
            drawable.setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.DARKEN);
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));
            navBtn.setImageDrawable(drawable);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", MODE_PRIVATE);
        if (System.currentTimeMillis() >= 1579881600000.0 && System.currentTimeMillis() <= 1581177600000.0 && preferences.getBoolean("allowAutoTheme", true)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("theme", "1");
            editor.apply();
        }else if(preferences.getBoolean("allowAutoTheme", true)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("theme", "0");
            editor.apply();
        }
        preNotifyThemeChanged();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEngine(view, true);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        LinearLayout linearLayout = findViewById(R.id.main_start_without_game);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEngine(view, false);
            }
        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_STORAGE);
            }
        }
        //View的更新并非线程安全，需要从子线程post一个Runnable，下面是这个Runnable的Handler
        handler = new Handler();
        checkApplicationUpdate();
        //修改主题
        notifyThemeChanged(toolbar);
        MaterialShowcaseSequence materialShowcaseSequence = new MaterialShowcaseSequence(this);

        materialShowcaseSequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(fab)
                .setContentText(R.string.showcase_start)
                .setShapePadding((int) getResources().getDimension(R.dimen.activity_horizontal_margin))
                .setDelay(500)
                .setMaskColour(Color.parseColor("#DD1A1A1A"))
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build());
        materialShowcaseSequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(toolbar.getChildAt(1))
                .setContentText(R.string.showcase_menu)
                .setShapePadding((int) getResources().getDimension(R.dimen.activity_horizontal_margin))
                .setDelay(500)
                .setMaskColour(Color.parseColor("#DD1A1A1A"))
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build());
        materialShowcaseSequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(toolbar)
                .setContentText(R.string.showcase_tile)
                .setShapePadding((int) getResources().getDimension(R.dimen.activity_horizontal_margin))
                .setDelay(500)
                .setMaskColour(Color.parseColor("#DD1A1A1A"))
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build());

        materialShowcaseSequence.singleUse("FIRST_RUN");
        materialShowcaseSequence.start();
        int upCount = preferences.getInt("up_count", 0);
        switch (upCount){
            case 2:
                Snackbar.make(fab, R.string.tip_use_remote, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.tip_use_remote_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();
                break;
                default:
                    break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_exit) {
            finish();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkApplicationUpdate(){
        UpdateRunnable updateRunnable = new UpdateRunnable();
        new Thread(updateRunnable).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!preferences.getString("theme", "0").equals(themeNow)) {
            recreate();
        }
    }

    private void changeUpdateState(int state, @Nullable final String versionName, @Nullable final String releaseNote){
        TextView textView = findViewById(R.id.main_state_text);
        ImageView imageView = findViewById(R.id.main_state_img);
        LinearLayout linearLayout = findViewById(R.id.main_state);
        switch (state){
            case STATE_UP_TO_DATE:
                textView.setText(getResources().getString(R.string.update_state_correct));
                textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                imageView.setBackground(getDrawable(R.color.colorPrimaryDark));
                imageView.setImageResource(R.drawable.ic_check_correct);
                break;
            case STATE_NEED_UPDATE:
                textView.setText(getResources().getString(R.string.update_state_need_update));
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                imageView.setBackground(getDrawable(R.color.colorAccent));
                imageView.setImageResource(R.drawable.ic_check_attention);


                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getResources().getString(R.string.update_dialog_title) + " - " + versionName)
                                .setMessage(releaseNote)
                                .setPositiveButton(R.string.update_dialog_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try{
                                            Uri uri = Uri.parse("market://details?id=" + getPackageName());
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            intent.setPackage("com.coolapk.market");
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            startActivity(intent);
                                        }catch (Exception e){
                                            Log.e(TAG, "Call Coolapk failed:" + e);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("http://www.coolapk.com/apk/com.ssyanhuo.arknightshelper"));
                                            startActivity(intent);
                                        }
                                    }
                                })
                                .setNeutralButton(R.string.update_dialog_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .show();
                    }
                });
                linearLayout.setFocusable(true);
                linearLayout.setClickable(true);
                break;
            case STATE_BETA:
                PackageManager packageManager = getPackageManager();
                String string;
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
                    string = getResources().getString(R.string.update_state_beta) + "(" + packageInfo.versionName + ")";
                } catch (PackageManager.NameNotFoundException e) {
                    string = getResources().getString(R.string.update_state_beta);
                    e.printStackTrace();
                }

                textView.setText(string);
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                imageView.setBackground(getDrawable(R.color.colorAccent));
                imageView.setImageResource(R.mipmap.ic_check_beta);
                break;
            case STATE_BETA_FINISHED:
                textView.setText(getResources().getString(R.string.update_state_beta_finished));
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                imageView.setBackground(getDrawable(R.color.colorAccent));
                imageView.setImageResource(R.drawable.ic_check_attention);
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getResources().getString(R.string.update_dialog_title) + " - " + versionName)
                                .setMessage(releaseNote)
                                .setPositiveButton(R.string.update_dialog_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try{
                                            Uri uri = Uri.parse("market://details?id=" + getPackageName());
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            intent.setPackage("com.coolapk.market");
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            startActivity(intent);
                                        }catch (Exception e){
                                            Log.e(TAG, "Call Coolapk failed:" + e);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("http://www.coolapk.com/apk/com.ssyanhuo.arknightshelper"));
                                            startActivity(intent);
                                        }
                                    }
                                })
                                .setNeutralButton(R.string.update_dialog_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .show();
                    }
                });
                linearLayout.setFocusable(true);
                linearLayout.setClickable(true);
                break;
            default:
                textView.setText(getResources().getString(R.string.update_state_error));
                textView.setTextColor(getResources().getColor(R.color.colorError));
                imageView.setBackground(getDrawable(R.color.colorError));
                imageView.setImageResource(R.drawable.ic_check_error);
                break;
        }
    }
    private void checkDataUpdate(){
        boolean local = preferences.getBoolean("use_builtin_data", false);
        if (local){
            return;
        }else {
            //TODO 补全
        }
    }
    private class UpdateRunnable implements Runnable{
        PackageManager packageManager = getPackageManager();
        String versionInfo;
        String versionCode;
        String versionName;
        String releaseNote;

        @Override
        public void run() {

            try{
                //取得当前版本号
                PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
                int versionCurrent = packageInfo.versionCode;
                //从指定地址获取最新版本号
                String site = preferences.getString("update_site", SITE_GITEE);
                String spec;
                if (site.equals(SITE_GITEE)){
                    spec = "https://gitee.com/ssYanhuo/Arknights-Helper-Data/raw/master/latest/versioninfo";
                }else {
                    spec ="https://raw.githubusercontent.com/ssYanhuo/Arknights-Helper-Data/master/latest/versioninfo";
                }
                URL url = new URL(spec);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.setReadTimeout(10000);
                InputStream inputStream = httpURLConnection.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0,len);
                }
                inputStream.close();
                byte[] data = outputStream.toByteArray();
                versionInfo = new String(data, StandardCharsets.UTF_8);
                //不在同一语句中截取，否则可能截取到前面的换行
                versionCode = versionInfo.substring(versionInfo.indexOf("versionCode") + 12);
                versionCode = versionCode.substring(0, versionCode.indexOf("\n"));
                versionName = versionInfo.substring(versionInfo.indexOf("versionName") + 12);
                versionName= versionName.substring(0, versionName.indexOf("\n"));
                releaseNote = versionInfo.substring(versionInfo.indexOf("releaseNote") + 12);
                Log.i(TAG, "Latest version: " + versionCode +' ' + versionName);
                int versionLatest = Integer.parseInt(versionCode);
                if (versionCurrent <= versionLatest && (BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("DEBUG"))){
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Runnable runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    changeUpdateState(STATE_BETA_FINISHED, versionName, releaseNote);
                                }
                            };
                            handler.post(runnable1);
                        }
                    };
                    runnable.run();
                }else if(versionCurrent < versionLatest){
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Runnable runnable1 = new Runnable() {
                                    @Override
                                    public void run() {
                                        changeUpdateState(STATE_NEED_UPDATE, versionName, releaseNote);
                                    }
                                };
                                handler.post(runnable1);
                            }
                        };
                        runnable.run();
                }else if(versionCurrent == versionLatest){
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Runnable runnable1 = new Runnable() {
                                @Override
                                public void run() {
                                    changeUpdateState(STATE_UP_TO_DATE, null, null);
                                }
                            };
                            handler.post(runnable1);
                        }
                    };
                    runnable.run();
                }else {
                    Runnable runnable1 = new Runnable() {
                        @Override
                        public void run() {
                            changeUpdateState(STATE_BETA, null, null);
                        }
                    };
                    handler.post(runnable1);
                }
            }catch (Exception e){
                //获取当前版本号失败，我也不知道啥情况下会失败orz
                Runnable runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        changeUpdateState(STATE_ERROR, null, null);
                    }
                };
                handler.post(runnable1);
                Log.e(TAG, "Version check failed: ");
                e.printStackTrace();
            }
        }
    }
}
