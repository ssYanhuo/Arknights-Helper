package com.ssyanhuo.arknightshelper.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.overlay.BackendService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final String TAG = "MainActivity";
    Handler handler;
    SharedPreferences sharedPreferences;
    final int STATE_UP_TO_DATE = 0;
    final int STATE_NEED_UPDATE = 1;
    final int STATE_BETA = 2;
    final int STATE_ERROR = -1;
    public void startEngine(final View view, final boolean startGame){
        Snackbar.make(view, R.string.start_game, Snackbar.LENGTH_LONG).show();
        sharedPreferences = getSharedPreferences("Config", MODE_PRIVATE);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Looper.prepare();
                if((Build.BRAND.equals("Meizu") || Build.BRAND.equals("MEIZU")) && sharedPreferences.getBoolean("firstRun", true)){
                    Snackbar.make(view, "魅族用户请手动前往系统设置授予应用悬浮窗权限", Snackbar.LENGTH_INDEFINITE).show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("firstRun", false);
                    editor.apply();
                    return;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("firstRun", false);
                editor.apply();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(Settings.canDrawOverlays(getApplicationContext())){
                        Intent intent1 = new Intent(getApplicationContext(), BackendService.class);
                        try{
                            startService(intent1);
                        }catch (Exception e){
                            Log.e("Akrnights Helper", "Start service failed!", e);
                        }
                        if(startGame){
                            final Intent intent2 = new Intent(Intent.ACTION_MAIN);
                            intent2.addCategory(Intent.CATEGORY_LAUNCHER);
                            if(checkApplication("com.hypergryph.arknights") && checkApplication("com.hypergryph.arknights.bilibili")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_AlertDialog);
                                builder.setTitle(R.string.start_two_apps)
                                        .setPositiveButton(R.string.game_official, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ComponentName componentName = new ComponentName("com.hypergryph.arknights", "com.u8.sdk.U8UnityContext");
                                                intent2.setComponent(componentName);
                                                try{
                                                    startActivity(intent2);
                                                }catch (Exception e){
                                                    Log.e(TAG, "Start game failed!", e);
                                                }
                                            }
                                        })
                                        .setNeutralButton(R.string.game_bilibili, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ComponentName componentName = new ComponentName("com.hypergryph.arknights.bilibili", "com.u8.sdk.SplashActivity");
                                                intent2.setComponent(componentName);
                                                try{
                                                    startActivity(intent2);
                                                }catch (Exception e){
                                                    Log.e(TAG, "Start game failed!", e);
                                                }
                                            }
                                        }).show();
                            }else if(checkApplication("com.hypergryph.arknights")){
                                ComponentName componentName = new ComponentName("com.hypergryph.arknights", "com.u8.sdk.U8UnityContext");
                                intent2.setComponent(componentName);
                                try{
                                    startActivity(intent2);
                                }catch (Exception e){
                                    Log.e(TAG, "Start game failed!", e);
                                }
                            }else if(checkApplication("com.hypergryph.arknights.bilibili")){
                                ComponentName componentName = new ComponentName("com.hypergryph.arknights.bilibili", "com.u8.sdk.SplashActivity");
                                intent2.setComponent(componentName);
                                try{
                                    startActivity(intent2);
                                }catch (Exception e){
                                    Log.e(TAG, "Start game failed!", e);
                                }
                            }

                        }
                    }else {
                        Snackbar.make(view, R.string.no_overlay_permission_error, Snackbar.LENGTH_INDEFINITE).setAction(R.string.no_overlay_permission_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        }).show();
                    }
                }else {
                    Intent intent1 = new Intent(getApplicationContext(), BackendService.class);
                    try{
                        startService(intent1);
                    }catch (Exception e){
                        Log.e(TAG, "Start service failed!", e);
                    }
                    if(startGame){
                        final Intent intent2 = new Intent(Intent.ACTION_MAIN);
                        intent2.addCategory(Intent.CATEGORY_LAUNCHER);
                        if(checkApplication("com.hypergryph.arknights") && checkApplication("com.hypergryph.arknights.bilibili")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme);
                            builder.setTitle(R.string.start_two_apps)
                                    .setPositiveButton(R.string.game_official, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ComponentName componentName = new ComponentName("com.hypergryph.arknights", "com.u8.sdk.U8UnityContext");
                                            intent2.setComponent(componentName);
                                            try{
                                                startActivity(intent2);
                                            }catch (Exception e){
                                                Log.e(TAG, "Start game failed!", e);
                                            }
                                        }
                                    })
                                    .setNeutralButton(R.string.game_bilibili, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ComponentName componentName = new ComponentName("com.hypergryph.arknights.bilibili", "com.u8.sdk.SplashActivity");
                                            intent2.setComponent(componentName);
                                            try{
                                                startActivity(intent2);
                                            }catch (Exception e){
                                                Log.e(TAG, "Start game failed!", e);
                                            }
                                        }
                                    }).show();
                        }else if(checkApplication("com.hypergryph.arknights")){
                            ComponentName componentName = new ComponentName("com.hypergryph.arknights", "com.u8.sdk.U8UnityContext");
                            intent2.setComponent(componentName);
                            try{
                                startActivity(intent2);
                            }catch (Exception e){
                                Log.e(TAG, "Start game failed!", e);
                            }
                        }else if(checkApplication("com.hypergryph.arknights.bilibili")){
                            ComponentName componentName = new ComponentName("com.hypergryph.arknights.bilibili", "com.u8.sdk.SplashActivity");
                            intent2.setComponent(componentName);
                            try{
                                startActivity(intent2);
                            }catch (Exception e){
                                Log.e(TAG, "Start game failed!", e);
                            }
                        }

                    }
                }
                Looper.loop();
            }
        }, 500);

    }
    public boolean checkApplication(String packageName) {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        //View的更新并非线程安全，需要从子线程post一个Runnable，下面是这个Runnable的Handler
        handler = new Handler();
        checkApplicationUpdate();

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
            System.exit(0);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void checkApplicationUpdate(){
        UpdateRunnable updateRunnable = new UpdateRunnable();
        new Thread(updateRunnable).start();
    }
    public void changeUpdateState(int state, @Nullable final String versionName, @Nullable final String releaseNote){
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
            default:
                textView.setText(getResources().getString(R.string.update_state_error));
                textView.setTextColor(getResources().getColor(R.color.colorError));
                imageView.setBackground(getDrawable(R.color.colorError));
                imageView.setImageResource(R.drawable.ic_check_error);
                break;
        }
    }
    private class UpdateRunnable implements Runnable{
        PackageManager packageManager = getPackageManager();
        String prop;
        String versionCode;
        String versionName;
        String releaseNote;

        @Override
        public void run() {

            try{
                //取得当前版本号
                PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
                int versionCurrent = packageInfo.versionCode;
                //从Github获取最新版本号
                URL url = new URL("https://raw.githubusercontent.com/ssYanhuo/Arknights-Helper/master/versioninfo");
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
                prop = new String(data, StandardCharsets.UTF_8);
                //不在同一语句中截取，否则可能截取到前面的换行
                versionCode = prop.substring(prop.indexOf("versionCode") + 12);
                versionCode = versionCode.substring(0, versionCode.indexOf("\n") - 1);
                versionName = prop.substring(prop.indexOf("versionName") + 12);
                versionName= versionName.substring(0, versionName.indexOf("\n") - 1);
                releaseNote = prop.substring(prop.indexOf("releaseNote") + 12);
                Log.i(TAG, "Latest version: " + versionCode);
                int versionLatest = Integer.valueOf(versionCode);
                if(versionCurrent < versionLatest){
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
                Log.e(TAG, "Version check failed: " + e);
            }
        }
    }
}
