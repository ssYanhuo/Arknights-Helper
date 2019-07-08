package com.ssyanhuo.arknightshelper.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.android.material.navigation.NavigationView;
import com.ssyanhuo.arknightshelper.overlay.BackendService;
import com.ssyanhuo.arknightshelper.R;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
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
        final String TAG = "MainActivity";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.start_game, Snackbar.LENGTH_LONG).show();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    if(Settings.canDrawOverlays(getApplicationContext())){
                        Intent intent1 = new Intent(getApplicationContext(), BackendService.class);
                        try{
                            startService(intent1);
                        }catch (Exception e){
                            Log.e("Akrnights Helper", "Start service failed!", e);
                        }
                        final Intent intent2 = new Intent(Intent.ACTION_MAIN);
                        intent2.addCategory(Intent.CATEGORY_LAUNCHER);
                        if(checkApplication("com.hypergryph.arknights") && checkApplication("com.hypergryph.arknights.bilibili")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
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
                        Log.e("Akrnights Helper", "Start service failed!", e);
                    }
                    Intent intent2 = new Intent(Intent.ACTION_MAIN);
                    intent2.addCategory(Intent.CATEGORY_LAUNCHER);
                    ComponentName componentName = new ComponentName("com.hypergryph.arknights", "com.u8.sdk.U8UnityContext");
                    intent2.setComponent(componentName);
                    try{
                        startActivity(intent2);
                    }catch (Exception e){
                        Log.e(TAG, "Start game failed!", e);
                    }
                }

            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
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
    @SuppressWarnings("StatementWithEmptyBody")
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
}
