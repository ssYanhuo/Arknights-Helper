package com.ssyanhuo.arknightshelper.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;
import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;
import com.ssyanhuo.arknightshelper.utils.I18nUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SettingsActivity extends AppCompatActivity {
    final String GAME_OFFICIAL = "0";
    final String GAME_BILIBILI = "1";
    final String GAME_MANUAL = "-1";
    static final String PACKAGE_OFFICIAL = "com.hypergryph.arknights";
    static final String PACKAGE_BILIBILI = "com.hypergryph.arknights.bilibili";
    static final String SITE_GITEE = "0";
    static final String SITE_GITHUB = "1";
    static SharedPreferences preferences;
    static Handler handler;
    static LinearLayout updateLayout;
    static AlertDialog updateDialog;
    static final String TAG = "SettingsActivity";
    static boolean updateSucceed = false;

    private void preNotifyThemeChanged(){
        setTheme(ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_MAIN, getApplicationContext()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preNotifyThemeChanged();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        preferences = getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", MODE_PRIVATE);
        handler = new Handler();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            Preference long_press_back_to_game = findPreference("long_press_back_to_game");
            ListPreference game_version = findPreference("game_version");
            Preference margin_fix = findPreference("margin_fix");
            final Preference update_site = findPreference("update_site");
            final Preference update_data = findPreference("update_data");
            final ListPreference theme = findPreference("theme");
            final SeekBarPreference floating_button_opacity = findPreference("floating_button_opacity");
            final ListPreference game_language = findPreference("game_language");
            margin_fix.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @SuppressLint("SourceLockedOrientationActivity")
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Activity activity = getActivity();
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                    return false;
                }
            });
            if (PackageUtils.getGameCount(getContext()) < 2){
                game_version.setVisible(false);
            }else {
                ArrayList<Integer> index = new ArrayList<>();
                CharSequence[] entryValues = game_version.getEntryValues();
                for (int i = 0; i < entryValues.length; i++) {
                    CharSequence packageName = entryValues[i];
                    if (packageName == StaticData.Const.PACKAGE_MANUAL){continue;}
                    if (checkApplication((String) packageName)) {
                        index.add(i);
                    }
                }
                CharSequence[] e = new CharSequence[index.size() + 1];
                CharSequence[] v = new CharSequence[index.size() + 1];
                for (int i = 0; i < index.size(); i++) {
                    e[i] = game_version.getEntries()[index.get(i)];
                    v[i] = game_version.getEntryValues()[index.get(i)];
                }
                e[index.size()] = getString(R.string.game_manual);
                v[index.size()] = StaticData.Const.PACKAGE_MANUAL;
                game_version.setEntries(e);
                game_version.setEntryValues(v);

                game_version.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        final String oldLang = preferences.getString("game_language", I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE);
                        if (newValue.equals(StaticData.Const.PACKAGE_ENGLISH)){
                            preferences.edit().putString("game_language", I18nUtils.LANGUAGE_ENGLISH).putBoolean("need_reload",true).apply();
                            game_language.setValue(I18nUtils.LANGUAGE_ENGLISH);
                            Snackbar.make(getView(), "游戏语言已自动设置为英语", Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            preferences.edit().putString("game_language", oldLang).putBoolean("need_reload",true).apply();
                                            game_language.setValue(oldLang);
                                        }
                                    })
                                    .show();
                        }else if (newValue.equals(StaticData.Const.PACKAGE_JAPANESE)){
                            preferences.edit().putString("game_language", I18nUtils.LANGUAGE_JAPANESE).putBoolean("need_reload",true).apply();
                            game_language.setValue(I18nUtils.LANGUAGE_JAPANESE);
                            Snackbar.make(getView(), "游戏语言已自动设置为日文", Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            preferences.edit().putString("game_language", oldLang).putBoolean("need_reload",true).apply();
                                            game_language.setValue(oldLang);
                                        }
                                    })
                                    .show();
                        }else if (newValue.equals(StaticData.Const.PACKAGE_KOREAN)){
                            //TODO Unfinished
                        }

                        return true;
                    }
                });
            }

            update_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    updateLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.content_data_updater, null);
                    updateDialog = new AlertDialog.Builder(getContext())
                            .setView(updateLayout)
                            .setTitle(R.string.settings_data_update_title)
                            .create();
                    updateDialog.show();
                    Thread thread = new Thread(new UpdateRunnable());
                    thread.start();
                    return false;
                }
            });
            theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preferences.edit().putBoolean("allowAutoTheme", false).apply();
                    try {
                        preferences.edit().putBoolean("need_reload",true).apply();
                        getActivity().recreate();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return true;
                }
            });
            game_language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (!newValue.equals(I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE)){
                        preferences.edit().putBoolean("need_reload",true).apply();
                        Snackbar.make(getView(), R.string.ocr_not_available_in_other_languages, Snackbar.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
        private class UpdateRunnable implements Runnable{
            String BASE_URL_GITEE = "http://ssyanhuo.gitee.io/arknights-helper-data/";
            String BASE_URL_GITHUB = "https://ssyanhuo.github.io/Arknights-Helper-Data/";
            @Override
            public void run() {
                String site = preferences.getString("update_site", "0");
                String spec;
                if (site.equals(SITE_GITEE)){
                    spec = BASE_URL_GITEE;
                }else {
                    spec = BASE_URL_GITHUB;
                }
                Log.e(TAG, site);
                try {
                    String indexString = URLRequest(spec + "/index.json");
                    JSONArray indexArray = JSONArray.parseArray(indexString);
                    JSONObject latestObject = indexArray.getJSONObject(0);
                    String selectedVersion = latestObject.getString("dir");
                    //获取指定的目录
                    String selectedList = URLRequest(spec + selectedVersion + "/datainfo.json");
                    //获取文件
                    JSONArray selectedArray = JSONArray.parseArray(selectedList);
                    for(int i = 0; i < selectedArray.size(); i++){
                        JSONObject selectedObj = selectedArray.getJSONObject(i);
                        String objURL = selectedObj.getString("name");
                        String result = URLRequest(spec + selectedVersion + objURL);
                        FileUtils.writeFile(result, objURL.substring(1), getContext());
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateSucceed = true;
                            try{
                                updateDialog.dismiss();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            Snackbar.make(getView(), R.string.settings_data_update_success, Snackbar.LENGTH_SHORT).show();

                        }
                    });
                    Log.i(TAG, "Update finished!");
                }catch (final Exception e){
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateLayout.removeAllViews();
                            TextView textView = new TextView(getContext());
                            textView.setText(e.toString());
                            int padding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) * 2;
                            textView.setPadding(padding, padding, padding, padding);
                            updateDialog.setContentView(textView);
                        }
                    });
                }
            }
            private String URLRequest(String site) throws Exception {
                URL url = new URL(site);
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
                return new String(data, StandardCharsets.UTF_8);
            }
        }
        public boolean checkApplication(String packageName) {
            if (packageName == null || "".equals(packageName)){
                return false;
            }
            try {
                ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
    }

}