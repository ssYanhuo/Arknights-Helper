package com.ssyanhuo.arknightshelper.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.service.PythonService;
import com.ssyanhuo.arknightshelper.utils.CompatUtils;
import com.ssyanhuo.arknightshelper.utils.ScreenUtils;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.utils.PythonUtils;
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

public class MainActivity extends AppCompatActivity {

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
    private LinearLayout startWithoutGame;
    CoordinatorLayout snackbarContainer;
    Activity activity;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fab;
    private ProgressBar bottomProgressBar;

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

        if ((!preferences.getBoolean("python_finished",false)) && PythonUtils.isSupported() && !preferences.getBoolean("disable_planner", false)){
            PythonUtils.setupEnvironment(getApplicationContext(), MainActivity.this, snackbarContainer);
            return;
        }
        if((preferences.getBoolean("python_finished",false)) && PythonUtils.isSupported() && !preferences.getBoolean("disable_planner", false) && PythonUtils.getPluginVersion(getApplicationContext()) < StaticData.Const.PLANNER_PLUGIN_MIN_VERSION){
            PythonUtils.setupEnvironment(getApplicationContext(), MainActivity.this, snackbarContainer);
            return;
        }


        Snackbar.make(snackbarContainer, R.string.start_game, Snackbar.LENGTH_LONG).show();
        Timer timer = new Timer();
        final Handler handler = new Handler();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final SharedPreferences.Editor editor = preferences.edit();
                Intent overlayServiceIntent = new Intent(getApplicationContext(), OverlayService.class);
                Intent pythonServiceIntent = new Intent(getApplicationContext(), PythonService.class);
                Looper.prepare();
                if (preferences.getInt("versionLast", -1) != BuildConfig.VERSION_CODE || !FileUtils.checkFiles(getApplicationContext(), getFilesDir().getPath(), StaticData.Const.DATA_LIST) || ((!FileUtils.checkFiles(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", new String[]{"formula.json", "matrix.json"})) && preferences.getBoolean("disable_planner", false))){
                    FileUtils.copyFilesFromAssets(getApplicationContext(), StaticData.Const.DATA_LIST);
                    FileUtils.copyFileFromAssets(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", "formula.json");
                    FileUtils.copyFileFromAssets(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", "matrix.json");
                }
                if((Build.BRAND.equals("Meizu") || Build.BRAND.equals("MEIZU") || Build.BRAND.equals("MeiZu") || Build.BRAND.equals("meizu")) && preferences.getBoolean("firstRun", true)){
                    Snackbar.make(snackbarContainer, R.string.meizu_floating_window_permission, Snackbar.LENGTH_INDEFINITE).show();
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
                            if(PythonUtils.isSupported() && !preferences.getBoolean("disable_planner", false)){
                                startService(pythonServiceIntent);
                            }
                        }catch (Exception e){
                            Log.e(TAG, "Start service failed!", e);
                        }
                    }else {
                        Snackbar.make(snackbarContainer, R.string.no_overlay_permission_error, Snackbar.LENGTH_INDEFINITE).setAction(R.string.no_overlay_permission_action, new View.OnClickListener() {
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
                        if(PythonUtils.isSupported() && !preferences.getBoolean("disable_planner", false)){
                            startService(pythonServiceIntent);
                        }
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
        versionLast = preferences.getInt("versionLast", -1);
        if (versionLast == -1 && versionLast != BuildConfig.VERSION_CODE){
            preferences.edit().putInt("versionLast", BuildConfig.VERSION_CODE).apply();
            preferences.edit().putInt("up_count_from_last_update", 0).apply();
        }
    }

    @Deprecated
    private void startEngine(final String game){
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

        if ((!preferences.getBoolean("python_finished",false)) && PythonUtils.isSupported() && !preferences.getBoolean("disable_planner", false)){
            PythonUtils.setupEnvironment(getApplicationContext(), MainActivity.this, snackbarContainer);
            return;
        }


        Snackbar.make(snackbarContainer, R.string.start_game, Snackbar.LENGTH_LONG).show();
        Timer timer = new Timer();
        final Handler handler = new Handler();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final SharedPreferences.Editor editor = preferences.edit();
                Intent overlayServiceIntent = new Intent(getApplicationContext(), OverlayService.class);
                Intent pythonServiceIntent = new Intent(getApplicationContext(), PythonService.class);
                Looper.prepare();
                if (preferences.getInt("versionLast", -1) != BuildConfig.VERSION_CODE || !FileUtils.checkFiles(getApplicationContext(), getFilesDir().getPath(), StaticData.Const.DATA_LIST) || ((!FileUtils.checkFiles(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", new String[]{"formula.json", "matrix.json"})) && preferences.getBoolean("disable_planner", false))){
                    FileUtils.copyFilesFromAssets(getApplicationContext(), StaticData.Const.DATA_LIST);
                    FileUtils.copyFileFromAssets(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", "formula.json");
                    FileUtils.copyFileFromAssets(getApplicationContext(), getFilesDir().getPath() + File.separator + "python" + File.separator + "data", "matrix.json");
                }
                if((Build.BRAND.equals("Meizu") || Build.BRAND.equals("MEIZU") || Build.BRAND.equals("MeiZu") || Build.BRAND.equals("meizu")) && preferences.getBoolean("firstRun", true)){
                    Snackbar.make(snackbarContainer, R.string.meizu_floating_window_permission, Snackbar.LENGTH_INDEFINITE).show();
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
                            if(PythonUtils.isSupported() && !preferences.getBoolean("disable_planner", false)){
                                startService(pythonServiceIntent);
                            }
                        }catch (Exception e){
                            Log.e(TAG, "Start service failed!", e);
                        }
                    }else {
                        Snackbar.make(snackbarContainer, R.string.no_overlay_permission_error, Snackbar.LENGTH_INDEFINITE).setAction(R.string.no_overlay_permission_action, new View.OnClickListener() {
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
                        if(PythonUtils.isSupported() && !preferences.getBoolean("disable_planner", false)){
                            startService(pythonServiceIntent);
                        }
                    }catch (Exception e){
                        Log.e(TAG, "Start service failed!", e);
                    }
                }
                PackageUtils.startApplication(game, activity);
                Looper.loop();
            }
        }, 200);
        versionLast = preferences.getInt("versionLast", -1);
        if (versionLast == -1 && versionLast != BuildConfig.VERSION_CODE){
            preferences.edit().putInt("versionLast", BuildConfig.VERSION_CODE).apply();
        }
    }


    private void preNotifyThemeChanged(){
        setTheme(ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_MAIN, getApplicationContext()));
        themeNow = String.valueOf(ThemeUtils.getThemeMode(getApplicationContext()));
    }

    private void notifyThemeChanged(){
        if (ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_LIGHT){
            bottomAppBar.replaceMenu(R.menu.activity_main_appbar_light);
        }
//        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES && ThemeUtils.getThemeMode(getApplicationContext()) != ThemeUtils.THEME_LIGHT){
//            bottomAppBar.replaceMenu(R.menu.activity_main_appbar);
//        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preNotifyThemeChanged();
        setContentView(R.layout.activity_main);
//        Intent intent = new Intent(this, IntroActivity.class);
//        startActivity(intent);
//        finish();
//        Intent intent = new Intent(this, MainActivity2.class);
//        startActivity(intent);
//        finish();
        activity = this;
        bottomAppBar = findViewById(R.id.bottomAppBar);
        bottomProgressBar = findViewById(R.id.bottom_progressBar);
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

        contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, R.style.AppTheme_Default);
        if(preferences.getBoolean("enable_dark_mode", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEngine(true);
            }
        });
        snackbarContainer = findViewById(R.id.snackbar_container);
        startWithoutGame = findViewById(R.id.main_start_without_game);
        startWithoutGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEngine(false);
            }
        });
//        fab.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                ArrayList<String> gameList = PackageUtils.getGamePackageNameList(activity);
//                if (gameList.size() > 0){
//                    PopupMenu popupMenu = new PopupMenu(activity, v);
//                    popupMenu.getMenuInflater().inflate(R.menu.activity_main_game_selector, popupMenu.getMenu());
//                    Menu menu = popupMenu.getMenu();
//                    for (int i = 0; i < menu.size(); i++) {
//                        menu.getItem(i).setVisible(false);
//                    }
//                    for (String game :
//                            gameList) {
//                            if (StaticData.Const.PACKAGE_OFFICIAL.equals(game)) {
//                                menu.findItem(R.id.game_selector_official).setVisible(true);
//                            } else if (StaticData.Const.PACKAGE_BILIBILI.equals(game)) {
//                                menu.findItem(R.id.game_selector_bilibili).setVisible(true);
//                            } else if (StaticData.Const.PACKAGE_TAIWANESE.equals(game)) {
//                                menu.findItem(R.id.game_selector_taiwanese).setVisible(true);
//                            } else if (StaticData.Const.PACKAGE_ENGLISH.equals(game)) {
//                                menu.findItem(R.id.game_selector_english).setVisible(true);
//                            } else if (StaticData.Const.PACKAGE_JAPANESE.equals(game)) {
//                                menu.findItem(R.id.game_selector_japanese).setVisible(true);
//                            } else if (StaticData.Const.PACKAGE_KOREAN.equals(game)) {
//                                menu.findItem(R.id.game_selector_korean).setVisible(true);
//                            }
//                        }
//                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem item) {
//                            switch (item.getItemId()){
//                                case R.id.game_selector_official:
//                                    startEngine(StaticData.Const.PACKAGE_OFFICIAL);
//                                    break;
//                                case R.id.game_selector_bilibili:
//                                    startEngine(StaticData.Const.PACKAGE_BILIBILI);
//                                    break;
//                                case R.id.game_selector_taiwanese:
//                                    startEngine(StaticData.Const.PACKAGE_TAIWANESE);
//                                    break;
//                                case R.id.game_selector_english:
//                                    startEngine(StaticData.Const.PACKAGE_ENGLISH);
//                                    break;
//                                case R.id.game_selector_japanese:
//                                    startEngine(StaticData.Const.PACKAGE_JAPANESE);
//                                    break;
//                                case R.id.game_selector_korean:
//                                    startEngine(StaticData.Const.PACKAGE_KOREAN);
//                                    break;
//                                default:
//                                    break;
//                            }
//                            return true;
//                        }
//                    });
//                    popupMenu.show();
//                }else {
//                    Toast.makeText(activity, R.string.no_game_selectable, Toast.LENGTH_SHORT).show();
//                }
//
//                return true;
//            }
//        });


        notifyThemeChanged();
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_settings:
                        startActivity(new Intent(activity, SettingsActivity.class));
                        break;
                    case R.id.nav_about:
                        startActivity(new Intent(activity, AboutActivity.class));
                        break;
                    case R.id.nav_exit:
                        finish();
                        break;
                    default:
                        break;
                }


                return true;
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
//        MaterialShowcaseSequence materialShowcaseSequence = new MaterialShowcaseSequence(this);
//
//        materialShowcaseSequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
//                .setTarget(fab)
//                .setContentText(R.string.showcase_start)
//                .setShapePadding((int) getResources().getDimension(R.dimen.activity_horizontal_margin))
//                .setDelay(500)
//                .setMaskColour(Color.parseColor("#DD1A1A1A"))
//                .setDismissOnTouch(true)
//                .renderOverNavigationBar()
//                .build());
//        materialShowcaseSequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
//                .setTarget(toolbar.getChildAt(1))
//                .setContentText(R.string.showcase_menu)
//                .setShapePadding((int) getResources().getDimension(R.dimen.activity_horizontal_margin))
//                .setDelay(500)
//                .setMaskColour(Color.parseColor("#DD1A1A1A"))
//                .setDismissOnTouch(true)
//                .renderOverNavigationBar()
//                .build());
//        materialShowcaseSequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
//                .setTarget(toolbar)
//                .setContentText(R.string.showcase_tile)
//                .setShapePadding((int) getResources().getDimension(R.dimen.activity_horizontal_margin))
//                .setDelay(500)
//                .setMaskColour(Color.parseColor("#DD1A1A1A"))
//                .setDismissOnTouch(true)
//                .renderOverNavigationBar()
//                .build());
//
//        materialShowcaseSequence.singleUse("FIRST_RUN");
//        materialShowcaseSequence.start();
        int upCount = preferences.getInt("up_count", 0);

        int upCountFromLastUpdate = preferences.getInt("up_count_from_last_update", 0);
        //TODO 用两个变量保存上次和这次的版本号
        switch (upCount){
            case 1:
                if (PackageUtils.getGameCount(this) > 0){
                    Snackbar.make(snackbarContainer, R.string.tip_create_shortcut, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.tip_create_shortcut_confirm, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(activity, SettingsActivity.class));

                                }
                            })
                            .show();
                }
                break;
            default:
                break;
        }

        CompatUtils.check(getApplicationContext());

        bottomAppBar.post(new Runnable() {
            @Override
            public void run() {
                int bottomBarHeight = bottomAppBar.getHeight();
                if (bottomBarHeight <= 0){
                    bottomBarHeight = 184;
                }
                bottomProgressBar.setPadding(0, 0 ,0, bottomBarHeight - ScreenUtils.dip2px(activity, 8.0f));
                ((ViewGroup.MarginLayoutParams) snackbarContainer.getLayoutParams()).setMargins(0,0,0, bottomBarHeight);
            }
        });
    }

    private void checkApplicationUpdate(){
//        ImageView imageView = findViewById(R.id.main_state_img);
//        if(imageView.getDrawable() instanceof AnimatedVectorDrawable){
//            ((AnimatedVectorDrawable) imageView.getDrawable()).start();
//        }
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
    private void changeProgressbarMode(boolean canStart){
        if (canStart){
            fab.setClickable(true);
            bottomProgressBar.setVisibility(View.INVISIBLE);
        }else {
            fab.setClickable(false);
            bottomProgressBar.setVisibility(View.VISIBLE);
        }
    }

}