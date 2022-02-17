package com.ssyanhuo.arknightshelper.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.fragment.BottomAboutFragment;
import com.ssyanhuo.arknightshelper.fragment.BottomPermissionFragment;
import com.ssyanhuo.arknightshelper.fragment.BottomSettingsFragment;
import com.ssyanhuo.arknightshelper.misc.StaticData;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.utils.PermissionUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;
import com.ssyanhuo.arknightshelper.widget.GameSelectorItem;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import me.kaelaela.verticalviewpager.VerticalViewPager;
import me.kaelaela.verticalviewpager.transforms.ZoomOutTransformer;

public class MainActivity extends AppCompatActivity {
    final int STATE_UP_TO_DATE = 0;
    final int STATE_NEED_UPDATE = 1;
    final int STATE_BETA = 2;
    final int STATE_BETA_FINISHED = 3;
    final int STATE_ERROR = -1;
    final int CODE_STORAGE = 1;
    final String SITE_GITEE = "0";
    final String SITE_GITHUB = "1";
    MaterialCardView startEngineCardView;
    MaterialCardView engineConfigCardView;
    MaterialCardView settingsCardView;
    MaterialCardView aboutCardView;
    VerticalViewPager gameSelector;
    ImageView updateButton;
    CoordinatorLayout rootView;
    ContextThemeWrapper contextThemeWrapper;
    static final String TAG = "MainActivity";
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", MODE_PRIVATE);
        contextThemeWrapper = this;
        rootView = findViewById(R.id.main_root_view);
        startEngineCardView = findViewById(R.id.main_start_engine_card_view);
        engineConfigCardView = findViewById(R.id.main_engine_config_card_view);
        settingsCardView = findViewById(R.id.main_settings_card_view);
        aboutCardView = findViewById(R.id.main_about_card_view);
        gameSelector = findViewById(R.id.main_game_selector);
        updateButton = findViewById(R.id.main_update_button);
        settingsCardView.setOnClickListener(v -> {
            BottomSettingsFragment bottomSettingsFragment = new BottomSettingsFragment();
            bottomSettingsFragment.show(getSupportFragmentManager(), null);
        });
        aboutCardView.setOnClickListener(v -> new BottomAboutFragment().show(getSupportFragmentManager(), null));
        ArrayList<String> gameList = PackageUtils.getGamePackageNameList(this);
        int selectedGame = 0;
        GameListAdapter adapter = new GameListAdapter();
        for (int i = 0, gameListSize = gameList.size(); i < gameListSize; i++) {
            String game = gameList.get(i);
            adapter.addData(new GameSelectorItemData(PackageUtils.getName(game, getApplicationContext()), game, true));
            if(game.equals(preferences.getString("game_version", "default"))){
                selectedGame = i;
            }
        }
        adapter.addData(new GameSelectorItemData(null, StaticData.Const.PACKAGE_NONE, false));
        if (preferences.getString("game_version", "default").equals(StaticData.Const.PACKAGE_NONE)){
            selectedGame = gameList.size();
        }
        gameSelector.setHorizontalScrollBarEnabled(false);
        gameSelector.setAdapter(adapter);
        gameSelector.setPageMargin(contextThemeWrapper.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
        gameSelector.setPageTransformer(false, new ZoomOutTransformer());
        gameSelector.setCurrentItem(selectedGame);
        startEngineCardView.setOnClickListener(v -> {
            ArrayList<String> requiredPermissions = PermissionUtils.checkPermissions(new String[]{PermissionUtils.PERMISSION_STORAGE, PermissionUtils.PERMISSION_OVERLAY}, getApplicationContext());
            if (requiredPermissions.size() == 0){
                GameSelectorItemData data = adapter.getData(gameSelector.getCurrentItem());
                startEngine(data.isLaunchGame(), data.getGamePackage());
            }else {
                new BottomPermissionFragment().show(getSupportFragmentManager(), null);
            }
        });
        checkApplicationUpdate();
        updateButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.coolapk.com/apk/com.ssyanhuo.arknightshelper"));
            startActivity(intent);
        });
    }

    private void checkApplicationUpdate(){
        UpdateRunnable updateRunnable = new UpdateRunnable();
        new Thread(updateRunnable).start();
    }

    private void startEngine(final boolean startGame, String gamePackage){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!PermissionUtils.checkPermission(PermissionUtils.PERMISSION_STORAGE, getApplicationContext())){
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setCancelable(false)
                        .setTitle(R.string.get_permission_storage_title)
                        .setMessage(R.string.get_permission_storage_content)
                        .setPositiveButton(R.string.get_permission_manually, (dialog, which) -> {
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);
                        });
                builder.show();
                return;
            }
        }
        preferences.edit().putString("game_version", gamePackage).apply();
        Snackbar.make(rootView, R.string.start_game, Snackbar.LENGTH_LONG).show();
        Timer timer = new Timer();
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
                    if(PermissionUtils.checkPermission(PermissionUtils.PERMISSION_OVERLAY, getApplicationContext())){
                        try{
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
                        startService(overlayServiceIntent);
                    }catch (Exception e){
                        Log.e(TAG, "Start service failed!", e);
                    }
                }
                if (startGame){
                    PackageUtils.startApplication(gamePackage, getApplicationContext());
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
                    runOnUiThread(() -> changeUpdateState(STATE_BETA_FINISHED, versionName, releaseNote));
                }else if(versionCurrent < versionLatest){
                    runOnUiThread(() -> changeUpdateState(STATE_NEED_UPDATE, versionName, releaseNote));
                }else if(versionCurrent == versionLatest){
                    runOnUiThread(() -> changeUpdateState(STATE_UP_TO_DATE, null, null));
                }else {
                    runOnUiThread(() -> changeUpdateState(STATE_BETA, null, null));
                }
            }catch (Exception e){
                runOnUiThread(() -> changeUpdateState(STATE_ERROR, null, null));
                Log.e(TAG, "Version check failed: ");
                e.printStackTrace();
            }
        }
    }
    private void changeUpdateState(int state, @Nullable final String versionName, @Nullable final String releaseNote){

        TextView textView = findViewById(R.id.main_state_text);

        switch (state){
            case STATE_UP_TO_DATE:
                textView.setText(getResources().getString(R.string.update_state_correct));
                if(ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_NEW_YEAR){
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
                }
                break;
            case STATE_NEED_UPDATE:
                textView.setText(String.format("%s (%s)", getResources().getString(R.string.update_state_need_update), versionName));
                updateButton.setVisibility(View.VISIBLE);
                break;
            case STATE_BETA:
                PackageManager packageManager = getPackageManager();
                String string;
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
                    string = String.format("%s (%s)", getResources().getString(R.string.update_state_beta), packageInfo.versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    string = getResources().getString(R.string.update_state_beta);
                    e.printStackTrace();
                }
                textView.setText(string);
                break;
            case STATE_BETA_FINISHED:
                textView.setText(getResources().getString(R.string.update_state_beta_finished));
                updateButton.setVisibility(View.VISIBLE);
                break;
            default:
                textView.setText(getResources().getString(R.string.update_state_error));
                break;
        }
    }
    private class GameListAdapter extends PagerAdapter{
        ArrayList<GameSelectorItemData> dataList = new ArrayList<>();

        public void addData(GameSelectorItemData data) {
            dataList.add(data);
        }

        public void removeData(int pos) {
            dataList.remove(pos);
        }

        public GameSelectorItemData getData(int index){
            return dataList.get(index % dataList.size());
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull @NotNull View view, @NonNull @NotNull Object object) {
            return view == object;
        }

        @NonNull
        @NotNull
        @Override
        public Object instantiateItem(@NonNull @NotNull ViewGroup container, int position) {
            GameSelectorItem gameSelectorItem = new GameSelectorItem(contextThemeWrapper);
            GameSelectorItemData data = dataList.get(position % dataList.size());
            if(data.isLaunchGame()){
                gameSelectorItem.setGameName(data.getGameName());
                gameSelectorItem.setGamePackage(data.getGamePackage());
            } else {
                gameSelectorItem.setLaunchGame(false);
            }
            gameSelectorItem.setClickable(true);
            gameSelectorItem.setFocusable(true);
            gameSelectorItem.setOnClickListener(v -> {
                gameSelector.setCurrentItem((gameSelector.getCurrentItem() + 1) % dataList.size());
            });
            container.addView(gameSelectorItem);
            return gameSelectorItem;
        }

        @Override
        public void destroyItem(@NonNull @NotNull ViewGroup container, int position, @NonNull @NotNull Object object) {
            container.removeView((View)object);
        }
    }

    private static class GameSelectorItemData{

        String gameName;
        String gamePackage;
        boolean launchGame;

        GameSelectorItemData(String gameName, String gamePackage, boolean launchGame){
            setGameName(gameName);
            setGamePackage(gamePackage);
            setLaunchGame(launchGame);
        }

        public boolean isLaunchGame() {
            return launchGame;
        }

        public void setLaunchGame(boolean launchGame) {
            this.launchGame = launchGame;
        }

        public String getGamePackage() {
            return gamePackage;
        }

        public void setGamePackage(String gamePackage) {
            this.gamePackage = gamePackage;
        }

        public String getGameName() {
            return gameName;
        }

        public void setGameName(String gameName) {
            this.gameName = gameName;
        }
    }
}