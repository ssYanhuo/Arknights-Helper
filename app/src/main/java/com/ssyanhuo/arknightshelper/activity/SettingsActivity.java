package com.ssyanhuo.arknightshelper.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.utils.OCRUtils;
import com.ssyanhuo.arknightshelper.utils.ScreenUtils;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.utils.PythonUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;
import com.ssyanhuo.arknightshelper.utils.I18nUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
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
            final Activity activity = getActivity();
            setPreferencesFromResource(R.xml.preferences, rootKey);
            if(preferences.getBoolean("enable_dark_mode", false)){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            Preference long_press_back_to_game = findPreference("long_press_back_to_game");
            ListPreference game_version = findPreference("game_version");
            final Preference margin_fix = findPreference("margin_fix");
            final Preference update_site = findPreference("update_site");
            final Preference update_data = findPreference("update_data");
            final ListPreference theme = findPreference("theme");
            final SeekBarPreference floating_button_opacity = findPreference("floating_button_opacity");
            final ListPreference game_language = findPreference("game_language");
            final Preference button_img = findPreference("button_img");
            final SwitchPreference disable_planner = findPreference("disable_planner");
            final SwitchPreference enable_dark_mode = findPreference("enable_dark_mode");
            final SwitchPreference emulator_mode = findPreference("emulator_mode");
            final Preference add_shortcut = findPreference("add_shortcut");
            final SwitchPreference auto_catch_screen = findPreference("auto_catch_screen");
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
                    updateLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.dialog_data_updater, null);
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
                        restartService();
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
            button_img.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                        showButtonImagePicker(null);
                    return false;
                }
            });
            if (!PythonUtils.isSupported()){
                disable_planner.setVisible(false);
            }
            disable_planner.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    stopService();
                    return true;
                }
            });
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                enable_dark_mode.setVisible(false);
            }
            enable_dark_mode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(Boolean.valueOf(newValue.toString())){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    activity.recreate();
                    restartService();
                    return true;
                }
            });
            emulator_mode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    restartService();
                    return true;
                }
            });
            if (PackageUtils.getGameCount(getContext()) <= 0){
                add_shortcut.setVisible(false);
            }
            add_shortcut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        final ArrayList<String> packageList = PackageUtils.getGamePackageNameList(getContext());
                        final ArrayList<String> nameList = PackageUtils.getGameNameList(getContext());
                        builder.setSingleChoiceItems(nameList.toArray(new String[nameList.size()]), 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final int index = which;
                                int padding = activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
                                final EditText editText = new EditText(activity);
                                editText.setHint("明日方舟 " + nameList.get(index));
                                //editText.setPadding(padding, padding, padding, padding);
                                AlertDialog dialog1 = new AlertDialog.Builder(activity)
                                        .setTitle("设置快捷方式名")
                                        .setView(editText)
                                        .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (editText.getText().toString().equals("")){
                                                    PackageUtils.addShortCut(getContext(), packageList.get(index), "明日方舟 " + nameList.get(index));
                                                }else {
                                                    PackageUtils.addShortCut(getContext(), packageList.get(index), editText.getText().toString());
                                                }

                                            }
                                        })
                                        .create();

                                dialog1.show();
                                FrameLayout.MarginLayoutParams marginLayoutParams = (FrameLayout.MarginLayoutParams) editText.getLayoutParams();
                                marginLayoutParams.setMargins(padding,0, padding, 0);
                                editText.setLayoutParams(marginLayoutParams);

                                dialog1.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));

                                dialog.dismiss();
                            }
                        })
                                .setTitle("选择要启动的游戏")
                                .show();


                    return true;
                }
            });
            if (!OCRUtils.isAbiSupported() || !OCRUtils.isLanguageSupported(getActivity())){
                auto_catch_screen.setVisible(false);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            //Toast.makeText(getContext(), Uri.fromFile(new File(data.getData().getPath())).toString(), Toast.LENGTH_SHORT).show();
            if (requestCode == 0 && data != null){
                try{
                    Intent intent;
                    if (!data.getDataString().contains("com.miui.gallery.open")){
                        intent = new Intent("com.android.camera.action.CROP");
                        intent.setDataAndType(data.getData(), "image/*");
                        intent.putExtra("aspectX", 256);
                        intent.putExtra("aspectY", 256);
                        intent.putExtra("outputX", 256);
                        intent.putExtra("outputY", 256);
                        intent.putExtra("scale", true);
                        intent.putExtra("return-data", true);
                        File outputFile = new File(getContext().getCacheDir().getPath() + File.separator + "images" + File.separator + "button.png");
                        if (!outputFile.getParentFile().exists()){
                            outputFile.getParentFile().mkdirs();
                        }
//                    if (!outputFile.exists()){
//                        outputFile.createNewFile();
//                    }
                        Uri outputUri = FileProvider.getUriForFile(getContext(), "com.ssyanhuo.arknightshelper.fileprovider", outputFile);
                        //Log.e("" ,data.toString());
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                        List<ResolveInfo> resolveInfoList = getContext().getPackageManager()
                                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo info : resolveInfoList) {
                            getContext().grantUriPermission(info.activityInfo.packageName,
                                    outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                    }
                    else{
                        intent = new Intent("com.android.camera.action.CROP");
                        intent.setComponent(new ComponentName("com.miui.gallery","com.miui.gallery.editor.photo.app.CropperActivity"));
                        intent.setDataAndType(data.getData(), "image/*");
                        intent.putExtra("aspectX", 256);
                        intent.putExtra("aspectY", 256);
                        intent.putExtra("outputX", 256);
                        intent.putExtra("outputY", 256);
                        intent.putExtra("scale", true);
                        intent.putExtra("return-data", true);
                        File outputFile = new File(getContext().getCacheDir().getPath() + File.separator + "images" + File.separator + "button.png");
                        if (!outputFile.getParentFile().exists()){
                            outputFile.getParentFile().mkdirs();
                        }
                        Uri outputUri = FileProvider.getUriForFile(getContext(), "com.ssyanhuo.arknightshelper.fileprovider", outputFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                        List<ResolveInfo> resolveInfoList = getContext().getPackageManager()
                                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo info : resolveInfoList) {
                            Toast.makeText(getContext(), info.activityInfo.name, Toast.LENGTH_SHORT).show();
                            getContext().grantUriPermission(info.activityInfo.packageName,
                                    outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                    }

//                    if(!data.getDataString().contains("com.miui.gallery.open") || true){
//                        List<ResolveInfo> resolveInfoList = getContext().getPackageManager()
//                                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                        for (ResolveInfo info : resolveInfoList) {
//                            getContext().grantUriPermission(info.activityInfo.packageName,
//                                    outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                        }
//                    }else {
//                        List<ResolveInfo> resolveInfoList = getContext().getPackageManager()
//                                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                        for (ResolveInfo info : resolveInfoList) {
//                            if (info.activityInfo.packageName.contains("miui")){
//
////                                getContext().grantUriPermission(info.activityInfo.packageName,
////                                        outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
////                                Toast.makeText(getContext(), info.activityInfo.name, Toast.LENGTH_SHORT).show();
//                            }else {
//                                continue;
////                                getContext().grantUriPermission(info.activityInfo.packageName,
////                                        outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                            }
//
//                        }
//                    }
                    //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, 1);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), R.string.setting_button_image_error, Toast.LENGTH_SHORT).show();
                }

            }if (requestCode == 1 && data != null){
                showButtonImagePicker(data);
            }
        }

        private void showButtonImagePicker(@Nullable Intent data){
            LinearLayout linearLayout = new LinearLayout(getActivity());
            final ImageView imageView = new ImageView(getActivity());
            CheckBox checkBox = new CheckBox(getActivity());
            Bitmap bitmap = null;
            int padding = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
            checkBox.setText(R.string.setting_button_image_crop_to_circle);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        //TODO 裁切
                    }
                }
            });
            checkBox.setChecked(true);
            checkBox.setVisibility(View.GONE);
            //imageView.setPadding(padding, padding, padding, padding);
            imageView.setClickable(true);
            imageView.setFocusable(true);
            if (data == null){
                if (preferences.getBoolean("button_img", false)){
                    try{
                        ColorDrawable drawable1 = new ColorDrawable();
                        drawable1.setColor(Color.WHITE);
                        Drawable drawable2 = Drawable.createFromPath(getContext().getFilesDir().getPath() + File.separator + "button.png");
                        imageView.setBackground(new LayerDrawable(new Drawable[]{drawable1, drawable2}));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    imageView.setImageResource(R.mipmap.overlay_button);
                }
            }else {
                try {
                    ColorDrawable drawable1 = new ColorDrawable();
                    drawable1.setColor(Color.WHITE);
                    Drawable drawable2 = BitmapDrawable.createFromPath(getContext().getCacheDir().getPath() + File.separator + "images" + File.separator + "button.png");
                    imageView.setBackground(new LayerDrawable(new Drawable[]{drawable1, drawable2}));
                }catch (Exception e){
                    imageView.setImageResource(R.mipmap.overlay_button);
                }

            }
            final Bitmap finalBitmap = BitmapFactory.decodeFile(getContext().getCacheDir().getPath() + File.separator + "images" + File.separator + "button.png");;
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.setting_button_image_pick_image)
                    .setView(linearLayout)
                    .setPositiveButton(R.string.setting_button_image_pick_image_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(finalBitmap != null){
                                try {
                                    File outPutPic = new File(getContext().getFilesDir().getPath() + File.separator + "button.png");
                                    if (!outPutPic.exists()){
                                        outPutPic.createNewFile();
                                    }
                                    FileOutputStream outputStream = new FileOutputStream(outPutPic);
                                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                    outputStream.flush();
                                    outputStream.close();
                                    preferences.edit().putBoolean("button_img", true).apply();
                                    restartService();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.setting_button_image_pick_image_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNeutralButton(R.string.setting_button_image_resume, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preferences.edit().putBoolean("button_img", false).apply();
                            restartService();
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .create();


            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 0);
                    alertDialog.dismiss();
                }
            });
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(padding, padding, padding, padding);
            linearLayout.addView(imageView);
            linearLayout.addView(checkBox);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            lp.height = ScreenUtils.dip2px(getContext(), 48);
            lp.width = ScreenUtils.dip2px(getContext(), 48);
            lp.topMargin = padding; lp.bottomMargin = padding; lp.leftMargin = padding; lp.rightMargin = padding;
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0,0, ScreenUtils.dip2px(getContext(), 48), ScreenUtils.dip2px(getContext(), 48));
                }
            });
            imageView.setClipToOutline(true);
            imageView.setElevation(8f);
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        }

        private Bitmap clipToCircle(Bitmap bitmap){
            //Canvas canvas = new Canvas(bitmap);
            return bitmap;
        }

        private void restartService(){
            List<ActivityManager.RunningServiceInfo> runningServiceInfoList = ((ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE);
            if(runningServiceInfoList.size() > 0){
                for (int i = 0; i < runningServiceInfoList.size(); i++){
                    if(runningServiceInfoList.get(i).service.getClassName().equals("com.ssyanhuo.arknightshelper.service.OverlayService")){
                        Intent intent = new Intent(getContext(), OverlayService.class);
                        getContext().stopService(intent);
                        getContext().startService(intent);
                        return;
                    }
                }
            }
        }

        private void stopService(){
            List<ActivityManager.RunningServiceInfo> runningServiceInfoList = ((ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE);
            if(runningServiceInfoList.size() > 0){
                for (int i = 0; i < runningServiceInfoList.size(); i++){
                    if(runningServiceInfoList.get(i).service.getClassName().equals("com.ssyanhuo.arknightshelper.service.OverlayService")){
                        Intent intent = new Intent(getContext(), OverlayService.class);
                        getContext().stopService(intent);
                        return;
                    }
                }
            }
        }

        private class UpdateRunnable implements Runnable{
            String BASE_URL_GITEE = "http://ssyanhuo.gitee.io/arknights-helper-data/";
            String BASE_URL_GITHUB = "https://ssyanhuo.github.io/Arknights-Helper-Data/";
            @Override
            public void run() {
                String site = preferences.getString("update_site", "0");
                String BaseURL;
                if (site.equals(SITE_GITEE)){
                    BaseURL = BASE_URL_GITEE;
                }else {
                    BaseURL = BASE_URL_GITHUB;
                }
                Log.e(TAG, site);
                try {
                    String indexString = URLRequest(BaseURL + "/index.json");
                    JSONArray indexArray = JSONArray.parseArray(indexString);
                    JSONObject latestObject = indexArray.getJSONObject(0);
                    if (latestObject.getIntValue("versionMax") > BuildConfig.VERSION_CODE){
                        Snackbar.make(getView(), R.string.settings_data_update_success, Snackbar.LENGTH_SHORT).show();
                        throw new IllegalStateException("Please update application.");
                    }
                    String selectedVersion = latestObject.getString("dir");
                    //获取指定的目录
                    String selectedList = URLRequest(BaseURL + selectedVersion + "/datainfo.json");
                    //获取文件
                    JSONArray selectedArray = JSONArray.parseArray(selectedList);
                    for(int i = 0; i < selectedArray.size(); i++){
                        JSONObject selectedObj = selectedArray.getJSONObject(i);
                        String objURL = selectedObj.getString("name");
                        String result = URLRequest(BaseURL + selectedVersion + objURL);
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