package com.ssyanhuo.arknightshelper.activity;

import android.Manifest;
import android.content.*;
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

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.utils.CompatUtils;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
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
                final SharedPreferences.Editor editor = preferences.edit();
                Looper.prepare();
                if (preferences.getInt("versionLast", BuildConfig.VERSION_CODE) != BuildConfig.VERSION_CODE || !FileUtils.checkFiles(getApplicationContext(), getFilesDir().getPath(), StaticData.Const.DATA_LIST) || !FileUtils.checkFiles(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", new String[]{"formula.json", "matrix.json"})){
                    FileUtils.copyFilesFromAssets(getApplicationContext(), StaticData.Const.DATA_LIST);
                    FileUtils.copyFileFromAssets(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", "formula.json");
                    FileUtils.copyFileFromAssets(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", "matrix.json");
                }
                if((Build.BRAND.equals("Meizu") || Build.BRAND.equals("MEIZU") || Build.BRAND.equals("MeiZu") || Build.BRAND.equals("meizu")) && preferences.getBoolean("firstRun", true)){
                    Snackbar.make(view, R.string.meizu_floating_window_permission, Snackbar.LENGTH_INDEFINITE).show();
                    editor.putBoolean("firstRun", false);
                    editor.apply();
                    return;
                }
                editor.putBoolean("firstRun", false);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), OverlayService.class);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(Settings.canDrawOverlays(getApplicationContext())){
                        try{
                            startService(intent);
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
                    try{
                        startService(intent);
                    }catch (Exception e){
                        Log.e(TAG, "Start service failed!", e);
                    }
                }
                if (startGame){
                        String gameSelected = preferences.getString("game_version", StaticData.Const.PACKAGE_MANUAL);
                        final ArrayList<String> list = PackageUtils.getGameList(getApplicationContext());
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
                                                preferences.edit().putString("game_version", game[0]).apply();
                                                PackageUtils.startApplication(game[0], getApplicationContext());
                                            }
                                        })
                                        .setNegativeButton(R.string.start_game_remember_selection_no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
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
        }, 500);
        versionLast = preferences.getInt("versionLast", -1);
        if (versionLast == -1 && versionLast != BuildConfig.VERSION_CODE){
            preferences.edit().putInt("versionLast", BuildConfig.VERSION_CODE).apply();
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
        contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, R.style.AppTheme_Default);
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
                default:
                    break;
        }
        CompatUtils.check(getApplicationContext());
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
                if(ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_NEW_YEAR){
                    textView.setTextColor(getResources().getColor(R.color.colorAccent));
                    imageView.setBackground(getDrawable(R.color.colorAccent));
                    if (System.currentTimeMillis() >= 1579881600000.0 && System.currentTimeMillis() <= 1581177600000.0){
                        boolean inChina = false;
                            if (Locale.getDefault().getLanguage().contains("zh")){
                                inChina = true;
                            }
                        if (inChina){
                            textView.setText("祝各位刀客塔新年快乐！");
                        }
                    }
                }else {
                    textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    imageView.setBackground(getDrawable(R.color.colorPrimaryDark));
                }
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
        JSONObject versionInfo;
        int versionCode;
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
                    spec = "http://ssyanhuo.gitee.io/arknights-helper-data/latest/versioninfo.json";
                }else {
                    spec ="https://ssyanhuo.github.io/Arknights-Helper-Data/latest/versioninfo.json";
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
                versionInfo = JSONObject.parseObject(new String(data, StandardCharsets.UTF_8));
                versionCode = versionInfo.getIntValue("versionCode");
                versionName = versionInfo.getString("versionName");
                releaseNote = versionInfo.getString("releaseNote");
                Log.i(TAG, "Latest version: " + versionCode +' ' + versionName);
                int versionLatest = versionCode;
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
