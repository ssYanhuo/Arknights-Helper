package com.ssyanhuo.arknightshelper.service;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.tabs.TabLayout;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.MainActivity;
import com.ssyanhuo.arknightshelper.misc.ServiceNotification;
import com.ssyanhuo.arknightshelper.misc.StaticData;
import com.ssyanhuo.arknightshelper.module.Drop;
import com.ssyanhuo.arknightshelper.module.Hr;
import com.ssyanhuo.arknightshelper.module.Material;
import com.ssyanhuo.arknightshelper.module.More;
import com.ssyanhuo.arknightshelper.module.Planner;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.utils.ScreenUtils;
import com.ssyanhuo.arknightshelper.utils.OCRUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class OverlayService extends Service {
    WindowManager windowManager;
    WindowManager.LayoutParams buttonLayoutParams;
    WindowManager.LayoutParams mainLayoutParams;
    WindowManager.LayoutParams backgroundLayoutParams;
    WindowManager.LayoutParams placeHolderLayoutParams;
    WindowManager.LayoutParams pinnedWindowLayoutParams;
    RelativeLayout mainLayout;
    LinearLayout linearLayout_hr;
    LinearLayout linearLayout_material;
    LinearLayout linearLayout_drop;
    LinearLayout linearLayout_planner;
    LinearLayout linearLayout_more;
    RelativeLayout relativeLayout_hr;
    RelativeLayout relativeLayout_material;
    RelativeLayout relativeLayout_drop;
    RelativeLayout relativeLayout_planner;
    RelativeLayout relativeLayout_more;
    ImageButton button;
    final int HR = 0;
    final int MATERIAL = 1;
    final int DROP = 2;
    final int PLANNER = 3;
    final int MORE = 4;
    final String TAG = "OverlayService";
    LinearLayout backgroundLayout;
    LinearLayout placeHolder;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    int orientation = -1;
    boolean isFloatingWindowShowing = false;
    Hr hr = new Hr();
    Material material = new Material();
    Drop drop = new Drop();
    More more = new More();
    Planner planner = new Planner();
    final String GAME_OFFICIAL = "0";
    final String GAME_BILIBILI = "1";
    final String GAME_MANUAL = "-1";
    final String PACKAGE_OFFICIAL = "com.hypergryph.arknights";
    final String PACKAGE_BILIBILI = "com.hypergryph.arknights.bilibili";
    ContextThemeWrapper contextThemeWrapper;
    private int backgroundColor;
    String themeNow = "0";
    int floatingButtonOpacity = 0;
    boolean attachToEdge = true;
    private LinearLayout pinnedWindow;
    private TabLayout tabLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, BroadcastReceiver.class).setAction("com.ssyanhuo.arknightshelper.stopservice");
        notificationIntent.putExtra("action", "StopService");
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Notification.Builder builder = new Notification.Builder(this);
        preferences = getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", MODE_PRIVATE);
        editor = preferences.edit();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("notification", getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
            builder
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent)
                    .setChannelId("notification");
        }else {
            builder
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent);
        }

        Notification notification = builder.build();
        startForeground(1, ServiceNotification.build(getApplicationContext(), 1));
        if(preferences.getBoolean("enable_dark_mode", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        //启动悬浮窗
        contextThemeWrapper = new ContextThemeWrapper(getApplicationContext(), ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, getApplicationContext()));
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        buttonLayoutParams = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            buttonLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            buttonLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        backgroundLayoutParams = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            backgroundLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            backgroundLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        mainLayoutParams = new WindowManager.LayoutParams();
        backgroundLayoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        placeHolderLayoutParams = new WindowManager.LayoutParams();

        int upCount = preferences.getInt("up_count", 0) + 1;
        editor.putInt("up_count", upCount);
        editor.apply();
        int upCountFromLastUpdate = preferences.getInt("up_count_from_last_update", 0) + 1;
        editor.putInt("up_count_from_last_update", upCountFromLastUpdate);
        editor.apply();
        startFloatingButton();
        //预处理
        floatingButtonOpacity = preferences.getInt("floating_button_opacity", 0);
        attachToEdge = preferences.getBoolean("attach_to_edge", true);
        floatingWindowPreProcess();
        OCRUtils.init(getApplicationContext());
        final Handler handler = new Handler();
        Timer orientationListener = new Timer();
        orientationListener.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isFloatingWindowShowing && getApplicationContext().getResources().getConfiguration().orientation != orientation){
                    try{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                hideFloatingWindow();
                            }
                        });

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }, 1000, 1000);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void startFloatingButton(){
        buttonLayoutParams.format = PixelFormat.RGBA_8888;
        buttonLayoutParams.width = ScreenUtils.dip2px(getApplicationContext(), 48);
        buttonLayoutParams.height = ScreenUtils.dip2px(getApplicationContext(), 48);
        buttonLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        buttonLayoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        buttonLayoutParams.x = preferences.getInt("lastX", 0);
        buttonLayoutParams.y = preferences.getInt("lastY", 200);
        button = new ImageButton(this);
        if (!preferences.getBoolean("button_img", false) || !new File(getApplicationContext().getFilesDir().getPath() + File.separator + "button.png").exists()){
            button.setBackground(getResources().getDrawable(R.mipmap.overlay_button));
        }else {
            ColorDrawable drawable1 = new ColorDrawable();
            drawable1.setColor(Color.WHITE);
            Drawable drawable2 = Drawable.createFromPath(getApplicationContext().getFilesDir().getPath() + File.separator + "button.png");
            button.setBackground(new LayerDrawable(new Drawable[]{drawable1, drawable2}));
        }
        button.setScaleType(ImageView.ScaleType.CENTER_CROP);
        button.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0,0, ScreenUtils.dip2px(getApplicationContext(), 48), ScreenUtils.dip2px(getApplicationContext(), 48));
            }
        });
        button.setClipToOutline(true);
        button.setElevation(8f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setForeground(new RippleDrawable(ColorStateList.valueOf(Color.GRAY), null, null));
        }
        button.setAlpha((255 - ((float)preferences.getInt("floating_button_opacity", 0))) / 255);
        button.setOnTouchListener(new View.OnTouchListener() {
            int downX;
            int downY;
            long downTime;
            int upX;
            int upY;
            long upTime;
            int x;
            int y;
            int lastX;
            int lastY;
            boolean touching;
            Timer timer;
            TimerTask timerTask;
            Handler handler;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final SharedPreferences sharedPreferences;
                sharedPreferences = getSharedPreferences(StaticData.Const.PREFERENCE_PATH, MODE_PRIVATE);
                handler = new Handler();
                if (sharedPreferences.getBoolean("long_press_back_to_game", true)){
                    timer = new Timer();
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if ((Math.abs(downX - lastX) <= 10 && Math.abs(downY - lastY) <= 10) && touching){
                                handler.post(() -> {
                                    touching = false;
                                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                    assert vibrator != null;
                                    if (vibrator.hasVibrator()){
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vibrator.vibrate(VibrationEffect.createOneShot(32, 128));
                                        }else {
                                            vibrator.vibrate(32);
                                        }
                                    }
                                    if(PackageUtils.getGameCount(getApplicationContext()) == 1){
                                        startActivity(getPackageManager().getLaunchIntentForPackage(PackageUtils.getGamePackageNameList(getApplicationContext()).get(0)));
                                    }else if (!sharedPreferences.getString("game_version", StaticData.Const.PACKAGE_MANUAL).equals(StaticData.Const.PACKAGE_MANUAL) && PackageUtils.getGameCount(getApplicationContext()) > 1){
                                        String game = sharedPreferences.getString("game_version", GAME_MANUAL);
                                        Toast.makeText(getApplicationContext(), R.string.resume_game, Toast.LENGTH_SHORT).show();
                                        startActivity(getPackageManager().getLaunchIntentForPackage(game));
                                    }else if (sharedPreferences.getString("game_version", StaticData.Const.PACKAGE_MANUAL).equals(StaticData.Const.PACKAGE_MANUAL) && PackageUtils.getGameCount(getApplicationContext()) > 1){
                                        Toast.makeText(getApplicationContext(), R.string.start_multiple_apps, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                });

                            }
                        }
                    };
                }

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touching = true;
                        if (sharedPreferences.getBoolean("long_press_back_to_game", true)){
                            timer.schedule(timerTask, 350);
                        }
                        x = (int) motionEvent.getRawX();
                        y = (int) motionEvent.getRawY();
                        downX = lastX = buttonLayoutParams.x;
                        downY = lastY = buttonLayoutParams.y;
                        //对旋转屏幕后超出边界的情况做修正
                        int screenWidth = ScreenUtils.getScreenWidth(contextThemeWrapper);
                        int screenHeight = ScreenUtils.getScreenHeight(contextThemeWrapper);
                        if(buttonLayoutParams.x > screenWidth - view.getWidth()){
                            buttonLayoutParams.x = screenWidth- view.getWidth();
                        }
                        if(buttonLayoutParams.x < 0){
                            buttonLayoutParams.x = 0;
                        }
                        if(buttonLayoutParams.y > screenHeight - view.getHeight()){
                            buttonLayoutParams.y = screenHeight - view.getHeight();
                        }
                        if(buttonLayoutParams.y < 0){
                            buttonLayoutParams.y = 0;
                        }
                        windowManager.updateViewLayout(view, buttonLayoutParams);
                        downTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) motionEvent.getRawX();
                        int nowY = (int) motionEvent.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;
                        x = nowX;
                        y = nowY;
                        buttonLayoutParams.x = lastX = buttonLayoutParams.x + movedX;
                        buttonLayoutParams.y = lastY = buttonLayoutParams.y + movedY;
                        // 更新悬浮窗控件布局
                        windowManager.updateViewLayout(view, buttonLayoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        touching = false;
                        try{
                            timer.cancel();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        upX = buttonLayoutParams.x;
                        upY = buttonLayoutParams.y;
                        upTime = System.currentTimeMillis();
                        editor.putInt("lastX", upX);
                        editor.putInt("lastY", upY);
                        editor.apply();
                        attachToEdge = sharedPreferences.getBoolean("attach_to_edge", true);
                        if (attachToEdge){
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                            int rotation = windowManager.getDefaultDisplay().getRotation();
                            if ((rotation == 1 || rotation == 3)) {
                                if (upX >= displayMetrics.widthPixels * 0.75){
                                    upX = displayMetrics.widthPixels - view.getWidth();
                                }else if (upX <= displayMetrics.widthPixels * 0.25){
                                    upX = 0;
                                }else if (upY >= displayMetrics.heightPixels / 2){
                                    upY = displayMetrics.heightPixels - view.getHeight();
                                }else {
                                    upY = 0;
                                }
                            }else {

                                if (upY >= displayMetrics.heightPixels * 0.75){
                                    upY = displayMetrics.heightPixels - view.getHeight();
                                }else if (upY <= displayMetrics.heightPixels * 0.25){
                                    upY = 0;
                                }else if (upX >= displayMetrics.widthPixels / 2){
                                    upX = displayMetrics.widthPixels - view.getWidth();
                                }else {
                                    upX = 0;
                                }
                            }
                            buttonLayoutParams.x = upX;
                            buttonLayoutParams.y = upY;
                            editor.putInt("lastX", upX);
                            editor.putInt("lastY", upY);
                            editor.apply();
                            windowManager.updateViewLayout(view, buttonLayoutParams);
                        }
                        if (Math.abs(downX - upX) <= 10 && Math.abs(downY - upY) <= 10){
                            if (upTime - downTime <= 350){
                                if (sharedPreferences.getBoolean("need_reload", false)){
                                    floatingWindowPreProcess();
                                }
                                if (!isFloatingWindowShowing){
                                    showFloatingWindow();
                                }
                            }
                        }
                    default:
                        break;
                }
                return false;
            }
        });
        windowManager.addView(button, buttonLayoutParams);

    }

    @SuppressLint("ClickableViewAccessibility")
    public void showPinnedWindow(ArrayList<View> contents){
        pinnedWindow = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.overlay_pinned_window, null);
        pinnedWindowLayoutParams = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            pinnedWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            pinnedWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        pinnedWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        pinnedWindowLayoutParams.format = PixelFormat.RGBA_8888;
        pinnedWindowLayoutParams.height = ScreenUtils.dip2px(getApplicationContext(), 320);
        pinnedWindowLayoutParams.width = ScreenUtils.dip2px(getApplicationContext(), 256);
        pinnedWindowLayoutParams.windowAnimations = R.style.AppTheme_Default_FloatingButtonAnimation;
        pinnedWindow.setBackgroundColor(ThemeUtils.getBackgroundColor(getApplicationContext(), contextThemeWrapper));
        LinearLayout pinnedContentView = pinnedWindow.findViewById(R.id.pinned_window_content);
        if (contents.size() > 0){
            for (View v :
                    contents) {
                pinnedContentView.addView(v);
            }
        }
        ImageButton closeButton = pinnedWindow.findViewById(R.id.pinned_window_close);
        closeButton.setOnClickListener(v -> windowManager.removeViewImmediate(v.getRootView()));
        LinearLayout bar = pinnedWindow.findViewById(R.id.pinned_window_bar);
        bar.setOnTouchListener(new View.OnTouchListener() {
            int x;
            int y;
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = (int) motionEvent.getRawX();
                        y = (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) motionEvent.getRawX();
                        int nowY = (int) motionEvent.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;
                        x = nowX;
                        y = nowY;
                        pinnedWindowLayoutParams.x = pinnedWindowLayoutParams.x + movedX;
                        pinnedWindowLayoutParams.y = pinnedWindowLayoutParams.y + movedY;
                        // 更新悬浮窗控件布局
                        windowManager.updateViewLayout(pinnedWindow, pinnedWindowLayoutParams);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        windowManager.addView(pinnedWindow, pinnedWindowLayoutParams);
    }


    public void floatingWindowPreProcess(){
        themeNow = String.valueOf(ThemeUtils.getThemeMode(contextThemeWrapper));
        backgroundColor = ThemeUtils.getBackgroundColor(getApplicationContext(), contextThemeWrapper);
        contextThemeWrapper = new ContextThemeWrapper(getApplicationContext(), ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, getApplicationContext()));
        backgroundLayout = new LinearLayout(contextThemeWrapper);
        placeHolder = new LinearLayout(contextThemeWrapper);
        mainLayout = (RelativeLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.overlay_main, null);
        tabLayout = mainLayout.findViewById(R.id.tab_main);
        if (ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_LIGHT){
            ((ImageButton) mainLayout.findViewById(R.id.overlay_close)).setColorFilter(contextThemeWrapper.getResources().getColor(R.color.colorPrimary));
        }
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, Color.TRANSPARENT});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mainLayout.setBackground(gradientDrawable);
        //实例化view
        relativeLayout_hr = mainLayout.findViewById(R.id.relative_hr);
        relativeLayout_material = mainLayout.findViewById(R.id.relative_material);
        relativeLayout_drop = mainLayout.findViewById(R.id.relative_drop);
        relativeLayout_planner = mainLayout.findViewById(R.id.relative_planner);
        relativeLayout_more = mainLayout.findViewById(R.id.relative_more);
        linearLayout_hr = mainLayout.findViewById(R.id.hr_content);
        linearLayout_material = mainLayout.findViewById(R.id.material_content);
        linearLayout_drop = mainLayout.findViewById(R.id.drop_content);
        linearLayout_planner = mainLayout.findViewById(R.id.planner_content);
        linearLayout_more = mainLayout.findViewById(R.id.more_content);
        relativeLayout_hr.setVisibility(VISIBLE);
        relativeLayout_material.setVisibility(GONE);
        relativeLayout_drop.setVisibility(GONE);
        relativeLayout_planner.setVisibility(GONE);
        relativeLayout_more.setVisibility(GONE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case HR:
                        changeFloatingWindowContent(HR);
                        break;
                    case MATERIAL:
                        changeFloatingWindowContent(MATERIAL);
                        break;
                    case DROP:
                        changeFloatingWindowContent(DROP);
                        break;
                    case PLANNER:
                        changeFloatingWindowContent(PLANNER);
                        break;
                    case MORE:
                        changeFloatingWindowContent(MORE);
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        ImageButton imageButton = mainLayout.findViewById(R.id.overlay_close);
        imageButton.setOnClickListener(v -> hideFloatingWindow());
        imageButton.setOnLongClickListener(v -> {
            hideFloatingWindow();
            stopSelf();
            return true;
        });
        //初始化
        hr.init(contextThemeWrapper, linearLayout_hr, relativeLayout_hr, backgroundLayout, this);
        material.init(contextThemeWrapper, linearLayout_material, backgroundLayout, this);
        drop.init(contextThemeWrapper, linearLayout_drop, this);
        more.init(contextThemeWrapper, linearLayout_more, this, getApplicationContext());
        planner.init(contextThemeWrapper, linearLayout_planner, relativeLayout_planner, backgroundLayout, this);
        buttonLayoutParams.windowAnimations = R.style.AppTheme_Default_FloatingButtonAnimation;
    }
    public void hideAllComponentsTemporarily(){
        try {
            windowManager.removeViewImmediate(backgroundLayout);
        } catch (Exception ignored) {
        }
        try {
            windowManager.removeViewImmediate(button);
        } catch (Exception ignored) {
        }
    }

    public void resumeFloatingWindow(){
        try {
            windowManager.addView(backgroundLayout, backgroundLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showFloatingWindow(){
        orientation = getApplicationContext().getResources().getConfiguration().orientation;
        isFloatingWindowShowing = true;
        try{
            windowManager.removeViewImmediate(button);
        }catch (Exception ignored){
        }

        int width;
        int height;
        int rotation = ScreenUtils.getScreenRotation(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect rect = windowManager.getCurrentWindowMetrics().getBounds();
            width = rect.width();
            height = rect.height();
            if(windowManager.getCurrentWindowMetrics().getWindowInsets().hasInsets()){
                Insets systemBarsInsets = windowManager.getCurrentWindowMetrics().getWindowInsets().getInsets(WindowInsets.Type.systemBars());
                Insets displayCutoutInsets = windowManager.getCurrentWindowMetrics().getWindowInsets().getInsets(WindowInsets.Type.displayCutout());
                width -= Math.max(systemBarsInsets.left, displayCutoutInsets.left) + Math.max(systemBarsInsets.right, displayCutoutInsets.right);
                height -= Math.max(systemBarsInsets.top, displayCutoutInsets.top) + Math.max(systemBarsInsets.bottom, displayCutoutInsets.bottom);
            }
        }else{
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            width = displayMetrics.widthPixels;
            height = displayMetrics.heightPixels;
        }

        mainLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
        mainLayoutParams.x = 0;
        mainLayoutParams.y = 0;
        backgroundLayoutParams.windowAnimations = R.style.AppTheme_Default_FloatingWindowAnimation;
        if(ScreenUtils.getScreenRotationMode(rotation) == ScreenUtils.MODE_LANDSCAPE){//横
            mainLayoutParams.height = height;
            mainLayoutParams.width = width / 2;
            placeHolderLayoutParams.height = height;
            placeHolderLayoutParams.width = width / 2;
            backgroundLayout.setOrientation(LinearLayout.HORIZONTAL);
            backgroundLayoutParams.height = height;
            backgroundLayoutParams.width = width;
            //是否优化状态栏区域的显示效果
            if (rotation == Surface.ROTATION_90){
                mainLayout.setBackgroundColor(backgroundColor);
            }
        }else {//竖
            mainLayoutParams.height = height / 2;
            mainLayoutParams.width = width;
            placeHolderLayoutParams.height = height / 2;
            placeHolderLayoutParams.width = width;
            backgroundLayoutParams.height = height;
            backgroundLayoutParams.width = width;
            backgroundLayout.setOrientation(LinearLayout.VERTICAL);
            //关闭背景渐变
            mainLayout.setBackgroundColor(backgroundColor);
        }
        mainLayout.setLayoutParams(mainLayoutParams);
        backgroundLayoutParams.x = 0;
        backgroundLayoutParams.y = 0;
        backgroundLayoutParams.format = PixelFormat.RGBA_8888;
        placeHolder.setLayoutParams(placeHolderLayoutParams);
        placeHolder.setOnClickListener(null);
        LinearLayout phContent = new LinearLayout(this);
        phContent.setTag("placeHolder");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        phContent.setLayoutParams(layoutParams);
        placeHolder.addView(phContent);
        backgroundLayout.removeAllViews();
        if(ScreenUtils.getScreenRotationMode(rotation) == ScreenUtils.MODE_LANDSCAPE){
            backgroundLayout.addView(placeHolder);
            backgroundLayout.addView(mainLayout);
        }else {
            backgroundLayout.addView(mainLayout);
            backgroundLayout.addView(placeHolder);
        }
        try{
            windowManager.addView(backgroundLayout, backgroundLayoutParams);
            Timer timer = new Timer();
            final Handler handler = new Handler();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> placeHolder.setOnClickListener(v -> hideFloatingWindow()));

                }
            },250);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void changeFloatingWindowContent(int i){
        switch (i){
            case HR:
                playChangeModuleAnim(new View[]{relativeLayout_hr}, new View[]{relativeLayout_material, relativeLayout_drop, relativeLayout_planner, relativeLayout_more});
                hr.isCurrentWindow(true);
                material.isCurrentWindow(false);
                break;
            case MATERIAL:
                playChangeModuleAnim(new View[]{relativeLayout_material}, new View[]{relativeLayout_hr, relativeLayout_drop, relativeLayout_planner, relativeLayout_more});
                hr.isCurrentWindow(false);
                material.isCurrentWindow(true);
                break;
            case DROP:
                playChangeModuleAnim(new View[]{relativeLayout_drop}, new View[]{relativeLayout_material, relativeLayout_hr, relativeLayout_planner, relativeLayout_more});
                hr.isCurrentWindow(false);
                material.isCurrentWindow(false);
                break;
            case PLANNER:
                playChangeModuleAnim(new View[]{relativeLayout_planner}, new View[]{relativeLayout_material, relativeLayout_drop, relativeLayout_hr, relativeLayout_more});
                hr.isCurrentWindow(false);
                material.isCurrentWindow(false);
                break;
            case MORE:
                playChangeModuleAnim(new View[]{relativeLayout_more}, new View[]{relativeLayout_material, relativeLayout_drop, relativeLayout_planner, relativeLayout_hr});
                hr.isCurrentWindow(false);
                material.isCurrentWindow(false);
                break;
            default:
                break;
        }
    }
    public void hideFloatingWindow(){
        if(!isFloatingWindowShowing){
            return;
        }
        try{
            windowManager.removeViewImmediate(backgroundLayout);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            windowManager.removeViewImmediate(button);
        }catch (Exception e){
            e.printStackTrace();
        }
        startFloatingButton();
        final Handler handler = new Handler();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    floatingWindowPreProcess();
                    isFloatingWindowShowing = false;
                });
        }
        }, 500);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            windowManager.removeViewImmediate(button);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            windowManager.removeViewImmediate(backgroundLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.gc();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void playChangeModuleAnim(View[] in, View[] out){
        for (View v :
                in) {
            if (v.getVisibility() != VISIBLE) {
                v.setVisibility(VISIBLE);
                Animator inAnimation = AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.overlay_module_fade_in);
                inAnimation.setTarget(v);
                inAnimation.start();
            }
        }
        for (final View v :
                out) {
            if (v.getVisibility() == VISIBLE){
                Animator outAnimation = AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.overlay_module_fade_out);
                outAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                outAnimation.setTarget(v);
                outAnimation.start();
            }
        }

    }

    public void getPlan(JSONObject jsonObject) {
        changeFloatingWindowContent(PLANNER);
        tabLayout.selectTab(tabLayout.getTabAt(PLANNER));
        planner.addItems(jsonObject.entrySet());
    }
}
