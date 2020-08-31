package com.ssyanhuo.arknightshelper.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AboutActivity extends AppCompatActivity {

    private static String TAG = "AboutActivity";
    int count;
    int line;
    boolean cancelEasterTimer;
    boolean showedEaster;
    Handler easterHandler;
    ScrollView scrollView;

    private void preNotifyThemeChanged(){
        setTheme(ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_NO_ACTION_BAR, getApplicationContext()));
    }

    private void notifyThemeChanged(Toolbar toolbar){
        toolbar.setBackgroundColor(getResources().getColor(ThemeUtils.getColorId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY, getApplicationContext())));
        if (ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_LIGHT){
            AppCompatImageButton navBtn = (AppCompatImageButton) toolbar.getChildAt(1);
            Drawable drawable = navBtn.getDrawable();
            drawable.setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));
            navBtn.setImageDrawable(drawable);
        }else {
            AppCompatImageButton navBtn = (AppCompatImageButton) toolbar.getChildAt(1);
            Drawable drawable = navBtn.getDrawable();
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            toolbar.setTitleTextColor(Color.WHITE);
            navBtn.setImageDrawable(drawable);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preNotifyThemeChanged();
        count = 0;
        line = 0;
        cancelEasterTimer = false;
        easterHandler = new Handler();
        showedEaster = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        scrollView = findViewById(R.id.about_scroll);
        Toolbar toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        SharedPreferences preferences = getSharedPreferences(StaticData.Const.PREFERENCE_PATH, MODE_PRIVATE);
        if(preferences.getBoolean("enable_dark_mode", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            TextView versionText = findViewById(R.id.about_version_text);
            versionText.setText(packageInfo.versionName);
        }catch (Exception e){
            Log.e(TAG, String.valueOf(e));
        }
        LinearLayout versionLayout = findViewById(R.id.about_version);
        versionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showedEaster){return;}
                count++;
                if(count >= 12){
                    count = 0;
                    showedEaster = true;
                    LinearLayout easterLinearLayout = findViewById(R.id.about_easter);
                    final TextView easterTextView = findViewById(R.id.about_easter_text);
                    easterLinearLayout.setVisibility(View.VISIBLE);
                    easterLinearLayout.setLongClickable(true);
                    easterLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            goLab(view);
                            return true;
                        }
                    });
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            final Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    int stringId = getEasterString();
                                    if (stringId != -1 && stringId != 0){
                                        easterTextView.append(getString(stringId));
                                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    }else {
                                        cancelEasterTimer = true;
                                    }
                                }
                            };
                            new  Thread(){
                                public void run(){
                                    easterHandler.post(runnable);
                                }
                            }.start();
                            if(cancelEasterTimer){
                                cancelEasterTimer = false;
                                this.cancel();
                            }
                        }
                    };
                    timer.schedule(timerTask, 0,1500);
                }
            }
        });
        notifyThemeChanged(toolbar);
    }

    private int getEasterString(){
        line++;
        switch (line){
            case 1:
                return R.string.easter_text_1;
            case 2:
                return R.string.easter_text_2;
            case 3:
                return R.string.easter_text_3;
            case 4:
                return R.string.easter_text_4;
            case 5:
                return R.string.easter_text_5;
            case 6:
                return R.string.easter_text_6;
            case 7:
                return R.string.easter_text_7;
            case 8:
                return R.string.easter_text_8;
            case 9:
                return R.string.easter_text_9;
            case 10:
                return R.string.easter_text_10;
            default:
                return -1;
        }
    }

    public void goGithub(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://github.com/ssYanhuo"));
        startActivity(intent);
    }
    public void goProject(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://github.com/ssYanhuo/Arknights-Helper"));
        startActivity(intent);
    }
    public void goRelease(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://github.com/ssYanhuo/Arknights-Helper/releases"));
        startActivity(intent);
    }
    public void goQQ(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://jq.qq.com/?_wv=1027&k=5bPf1xW"));
        startActivity(intent);
    }
    public void goLab(View view){
        Intent intent = new Intent(this, LabActivity.class);
        startActivity(intent);
    }
}
