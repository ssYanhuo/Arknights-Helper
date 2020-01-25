package com.ssyanhuo.arknightshelper.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
            Preference game_version = findPreference("game_version");
            Preference margin_fix = findPreference("margin_fix");
            final SwitchPreference use_builtin_data = findPreference("use_builtin_data");
            final Preference update_site = findPreference("update_site");
            final Preference update_data = findPreference("update_data");
            final ListPreference theme = findPreference("theme");
            margin_fix.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
            if (!checkApplication(PACKAGE_OFFICIAL) || !checkApplication(PACKAGE_BILIBILI)){
                game_version.setEnabled(false);
            }
            if (use_builtin_data.isChecked()){
                update_data.setEnabled(false);
            }
            use_builtin_data.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals(true)){
                        update_data.setEnabled(false);
                    }else {
                        try{
                            FileUtils.readData("akhr.json", getContext(), false);
                            FileUtils.readData("aklevel.json", getContext(), false);
                            FileUtils.readData("charMaterials.json", getContext(), false);
                            FileUtils.readData("datainfo.json", getContext(), false);
                            FileUtils.readData("items.json", getContext(), false);
                            FileUtils.readData("material.json", getContext(), false);
                            FileUtils.readData("matrix.json", getContext(), false);
                            FileUtils.readData("stages.json", getContext(), false);
                        } catch (IOException e) {
                            e.printStackTrace();
                            updateLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.content_data_updater, null);
                            updateDialog = new AlertDialog.Builder(getContext())
                                    .setView(updateLayout)
                                    .setTitle(R.string.settings_data_update_title)
                                    .create();
                            updateDialog.show();
                            Thread thread = new Thread(new UpdateRunnable());
                            thread.start();
                            final Handler handler = new Handler();
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (updateSucceed){
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                use_builtin_data.setChecked(false);
                                                update_data.setEnabled(true);
                                            }
                                        });
                                        timer.cancel();
                                    }
                                }
                            }, 100, 100);
                            return false;
                        }
                        update_data.setEnabled(true);
                    }
                    return true;
                }
            });
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
                        getActivity().recreate();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return true;
                }
            });
        }
        private class UpdateRunnable implements Runnable{
            String BASE_URL_GITEE = "https://gitee.com/ssYanhuo/Arknights-Helper-Data/raw/master";
            String BASE_URL_GITHUB = "https://raw.githubusercontent.com/ssYanhuo/Arknights-Helper-Data/master";
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
                            Toast.makeText(getContext(), R.string.settings_data_update_success, Toast.LENGTH_SHORT).show();

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