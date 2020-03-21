package com.ssyanhuo.arknightshelper.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.*;

import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.transition.Scale;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.SettingsActivity;
import com.ssyanhuo.arknightshelper.entity.ServiceNotification;
import com.ssyanhuo.arknightshelper.module.Drop;
import com.ssyanhuo.arknightshelper.module.Hr;
import com.ssyanhuo.arknightshelper.module.Material;
import com.ssyanhuo.arknightshelper.module.More;
import com.ssyanhuo.arknightshelper.utils.DpUtils;
import com.ssyanhuo.arknightshelper.utils.OCRUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class OverlayService extends Service {
    WindowManager windowManager;
    WindowManager.LayoutParams buttonLayoutParams;
    WindowManager.LayoutParams mainLayoutParams;
    WindowManager.LayoutParams backgroundLayoutParams;
    WindowManager.LayoutParams placeHolderLayoutParams;
    RelativeLayout mainLayout;
    LinearLayout linearLayout_hr;
    LinearLayout linearLayout_material;
    LinearLayout linearLayout_drop;
    LinearLayout linearLayout_planner;
    LinearLayout linearLayout_more;
    ScrollView scrollView_hr;
    ScrollView scrollView_material;
    ScrollView scrollView_drop;
    ScrollView scrollView_more;
    RelativeLayout relativeLayout_hr;
    RelativeLayout relativeLayout_material;
    RelativeLayout relativeLayout_drop;
    RelativeLayout relativeLayout_planner;
    RelativeLayout relativeLayout_more;
    BlurView tabLayout;
    ImageButton button;
    final int HR = 0;
    final int MATERIAL = 1;
    final int DROP = 2;
    final int PLANNER = -1;
    final int MORE = 3;
    final String TAG = "OverlayService";
    LinearLayout backgroundLayout;
    LinearLayout placeHolder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int orientation = -1;
    boolean isFloatingWindowShowing = false;
    Hr hr = new Hr();
    Material material = new Material();
    Drop drop = new Drop();
    More more = new More();
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
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, BroadcastReceiver.class).setAction("com.ssyanhuo.arknightshelper.stopservice");
        notificationIntent.putExtra("action", "StopService");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("notification", getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
            builder
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent)
                    .setChannelId("notification");
        }else {
            builder
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent);
        }

        Notification notification = builder.build();
        startForeground(1, ServiceNotification.build(getApplicationContext(), 1));

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
        sharedPreferences = getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        int upCount = sharedPreferences.getInt("up_count", 0) + 1;
        editor.putInt("up_count", upCount);
        editor.apply();
        startFloatingButton();
        //预处理
        floatingButtonOpacity = sharedPreferences.getInt("floating_button_opacity", 0);
        attachToEdge = sharedPreferences.getBoolean("attach_to_edge", true);
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
        Timer opacityListener = new Timer();
        opacityListener.schedule(new TimerTask() {
            @Override
            public void run() {
                if (sharedPreferences.getInt("floating_button_opacity", 0) != floatingButtonOpacity){
                    setFloatingButtonAlpha(sharedPreferences.getInt("floating_button_opacity", 0));
                    floatingButtonOpacity = sharedPreferences.getInt("floating_button_opacity", 0);
                }
            }
        }, 2000, 2000);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void startFloatingButton(){
        buttonLayoutParams.format = PixelFormat.RGBA_8888;
        buttonLayoutParams.width = DpUtils.dip2px(getApplicationContext(), 48);
        buttonLayoutParams.height = DpUtils.dip2px(getApplicationContext(), 48);
        buttonLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        buttonLayoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        buttonLayoutParams.x = sharedPreferences.getInt("lastX", 0);
        buttonLayoutParams.y = sharedPreferences.getInt("lastY", 200);
        button = new ImageButton(this);
        if (!sharedPreferences.getBoolean("button_img", false) || !new File(getApplicationContext().getFilesDir().getPath() + File.separator + "button.png").exists()){
            button.setBackground(getResources().getDrawable(R.mipmap.overlay_button));
        }else {
            button.setBackground(Drawable.createFromPath(getApplicationContext().getFilesDir().getPath() + File.separator + "button.png"));
        }

        button.setScaleType(ImageView.ScaleType.CENTER_CROP);
        button.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0,0, DpUtils.dip2px(getApplicationContext(), 48), DpUtils.dip2px(getApplicationContext(), 48));
            }
        });
        button.setClipToOutline(true);
        button.setElevation(8f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setForeground(new RippleDrawable(ColorStateList.valueOf(Color.GRAY), null, null));
        }
        setFloatingButtonAlpha(sharedPreferences.getInt("floating_button_opacity", 0));
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
                sharedPreferences = getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", MODE_PRIVATE);
                handler = new Handler();
                if (sharedPreferences.getBoolean("long_press_back_to_game", true)){
                    timer = new Timer();
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if ((Math.abs(downX - lastX) <= 10 && Math.abs(downY - lastY) <= 10) && touching){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        touching = false;
                                        final SharedPreferences.Editor editor = sharedPreferences.edit();
                                        boolean hasOfficial = checkApplication(PACKAGE_OFFICIAL);
                                        boolean hasBilibili = checkApplication(PACKAGE_BILIBILI);
                                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                        assert vibrator != null;
                                        if (vibrator.hasVibrator()){
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                vibrator.vibrate(VibrationEffect.createOneShot(32, 128));
                                            }else {
                                                vibrator.vibrate(32);
                                            }
                                        }
                                        if (sharedPreferences.getString("game_version", GAME_MANUAL).equals(GAME_OFFICIAL) || sharedPreferences.getString("game_version", GAME_MANUAL).equals(GAME_BILIBILI)){
                                            String game = sharedPreferences.getString("game_version", GAME_MANUAL);
                                            Toast.makeText(getApplicationContext(), R.string.resume_game, Toast.LENGTH_SHORT).show();
                                            switch (game){
                                                case GAME_OFFICIAL:
                                                    startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_OFFICIAL));
                                                    break;
                                                case GAME_BILIBILI:
                                                    startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_BILIBILI));
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }else{
                                            if (hasOfficial && hasBilibili){
                                                Toast.makeText(getApplicationContext(), R.string.start_multiple_apps, Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }else if (hasOfficial){
                                                try{
                                                    editor.putString("game_version", GAME_MANUAL);
                                                    editor.apply();
                                                    Toast.makeText(getApplicationContext(), R.string.resume_game, Toast.LENGTH_SHORT).show();
                                                    startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_OFFICIAL));
                                                }catch (Exception e){
                                                    Log.e(TAG, "Start game failed!", e);
                                                }
                                            }else if (hasBilibili){
                                                try{
                                                    editor.putString("game_version", GAME_MANUAL);
                                                    editor.apply();
                                                    Toast.makeText(getApplicationContext(), R.string.resume_game, Toast.LENGTH_SHORT).show();
                                                    startActivity(getPackageManager().getLaunchIntentForPackage(PACKAGE_BILIBILI));
                                                }catch (Exception e){
                                                    Log.e(TAG, "Start game failed!", e);
                                                }
                                            }
                                        }
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
                                    upX = displayMetrics.widthPixels;
                                }else if (upX <= displayMetrics.widthPixels * 0.25){
                                    upX = 0;
                                }else if (upY >= displayMetrics.heightPixels / 2){
                                    upY = displayMetrics.heightPixels;
                                }else {
                                    upY = 0;
                                }
                                buttonLayoutParams.x = upX;
                                buttonLayoutParams.y = upY;
                                editor.putInt("lastX", upX);
                                editor.putInt("lastY", upY);
                                editor.apply();
                                windowManager.updateViewLayout(view, buttonLayoutParams);
                            }else {

                                if (upY >= displayMetrics.heightPixels * 0.75){
                                    upY = displayMetrics.heightPixels;
                                }else if (upY <= displayMetrics.heightPixels * 0.25){
                                    upY = 0;
                                }else if (upX >= displayMetrics.widthPixels / 2){
                                    upX = displayMetrics.widthPixels;
                                }else {
                                    upX = 0;
                                }
                                buttonLayoutParams.x = upX;
                                buttonLayoutParams.y = upY;
                                editor.putInt("lastX", upX);
                                editor.putInt("lastY", upY);
                                editor.apply();
                                windowManager.updateViewLayout(view, buttonLayoutParams);
                            }
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

    public void setFloatingButtonAlpha(int opacity){
        button.setAlpha((255 - ((float)opacity)) / 255);
    }

    public void floatingWindowPreProcess(){
        themeNow = String.valueOf(ThemeUtils.getThemeMode(contextThemeWrapper));
        if (ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_NEW_YEAR){//太红了不好看
            backgroundColor = ThemeUtils.getColorWithAlpha(0.7f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY_DARK, contextThemeWrapper) - Color.parseColor("#00501010"));
        }else if (ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_LIGHT){//太蓝了也不好看
            backgroundColor = ThemeUtils.getColorWithAlpha(0.9f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY, contextThemeWrapper));
        } else {
            backgroundColor = ThemeUtils.getColorWithAlpha(0.7f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY_DARK, contextThemeWrapper));
        }
        contextThemeWrapper = new ContextThemeWrapper(getApplicationContext(), ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, getApplicationContext()));
        backgroundLayout = new LinearLayout(contextThemeWrapper);
        placeHolder = new LinearLayout(contextThemeWrapper);
        mainLayout = (RelativeLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.overlay_main, null);
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
        //scrollView_hr = mainLayout.findViewById(R.id.scroll_hr);
        //scrollView_material = mainLayout.findViewById(R.id.scroll_material);
        //scrollView_drop = mainLayout.findViewById(R.id.scroll_drop);
        //scrollView_more = mainLayout.findViewById(R.id.scroll_more);
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
        TabLayout tabLayout = mainLayout.findViewById(R.id.tab_main);
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
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFloatingWindow();
            }
        });
        //初始化
        hr.init(contextThemeWrapper, linearLayout_hr, relativeLayout_hr, backgroundLayout);
        material.init(contextThemeWrapper, linearLayout_material, backgroundLayout);
        drop.init(contextThemeWrapper, linearLayout_drop, this);
        more.init(contextThemeWrapper, linearLayout_more, this);
        buttonLayoutParams.windowAnimations = R.style.AppTheme_Default_FloatingButtonAnimation;
    }

    private boolean checkApplication(String packageName) {
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

    public void showFloatingWindow(){
        orientation = getApplicationContext().getResources().getConfiguration().orientation;
        isFloatingWindowShowing = true;
        try{
            windowManager.removeViewImmediate(button);
        }catch (Exception ignored){
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        mainLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
        mainLayoutParams.x = 0;
        mainLayoutParams.y = 0;
        backgroundLayoutParams.windowAnimations = R.style.AppTheme_Default_FloatingWindowAnimation;
        tabLayout = mainLayout.findViewById(R.id.overlay_tab);
        tabLayout.setupWith(mainLayout)
                .setBlurRadius(20f)
                .setBlurAlgorithm(new RenderScriptBlur(contextThemeWrapper))
                .setBlurAutoUpdate(false)
                .setHasFixedTransformationMatrix(true)
                .setFrameClearDrawable(new ColorDrawable(Color.BLACK))
                .setBlurEnabled(true);
        tabLayout.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRect(0, 0, view.getWidth(), view.getHeight());
            }
        });
        if (ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_NEW_YEAR){//太红了不好看
            tabLayout.setOverlayColor(ThemeUtils.getColorWithAlpha(0.7f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY_DARK, contextThemeWrapper) - Color.parseColor("#00501010")));
        }else if (ThemeUtils.getThemeMode(getApplicationContext()) == ThemeUtils.THEME_LIGHT){//太蓝了也不好看
            tabLayout.setOverlayColor(ThemeUtils.getColorWithAlpha(0.7f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY, contextThemeWrapper)));
        } else {
            tabLayout.setOverlayColor(ThemeUtils.getColorWithAlpha(0.7f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY_DARK, contextThemeWrapper)));
        }
        //检测屏幕方向和是否全屏
        if(rotation == 1 || rotation == 3){//横
            mainLayoutParams.height = displayMetrics.heightPixels;
            mainLayoutParams.width = displayMetrics.widthPixels / 2 + sharedPreferences.getInt("margin_fix", 0);
            placeHolderLayoutParams.height = displayMetrics.heightPixels;
            placeHolderLayoutParams.width = displayMetrics.widthPixels / 2;
            backgroundLayout.setOrientation(LinearLayout.HORIZONTAL);
            backgroundLayoutParams.height = displayMetrics.heightPixels;
            backgroundLayoutParams.width = displayMetrics.widthPixels + sharedPreferences.getInt("margin_fix", 0);
            //是否优化状态栏区域的显示效果
            if (rotation == 1){
                mainLayout.setBackgroundColor(backgroundColor);
            }
        }else {//竖
            mainLayoutParams.height = displayMetrics.heightPixels / 2;
            mainLayoutParams.width = displayMetrics.widthPixels;
            placeHolderLayoutParams.height = displayMetrics.heightPixels / 2;
            placeHolderLayoutParams.width = displayMetrics.widthPixels;
            backgroundLayoutParams.height = displayMetrics.heightPixels;
            backgroundLayoutParams.width = displayMetrics.widthPixels;
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
        if(rotation == 1 || rotation == 3){
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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            placeHolder.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    hideFloatingWindow();
                                }
                            });
                        }
                    });

                }
            },250);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void changeFloatingWindowContent(int i){
        switch (i){
            case HR:
                relativeLayout_hr.setVisibility(VISIBLE);
                relativeLayout_material.setVisibility(GONE);
                relativeLayout_drop.setVisibility(GONE);
                //relativeLayout_planner.setVisibility(GONE);
                relativeLayout_more.setVisibility(GONE);
                hr.isCurrentWindow(true);
                material.isCurrentWindow(false);
                break;
            case MATERIAL:
                relativeLayout_hr.setVisibility(GONE);
                relativeLayout_material.setVisibility(VISIBLE);
                relativeLayout_drop.setVisibility(GONE);
                //relativeLayout_planner.setVisibility(GONE);
                relativeLayout_more.setVisibility(GONE);
                hr.isCurrentWindow(false);
                material.isCurrentWindow(true);
                break;
            case DROP:
                relativeLayout_hr.setVisibility(GONE);
                relativeLayout_material.setVisibility(GONE);
                relativeLayout_drop.setVisibility(VISIBLE);
                //relativeLayout_planner.setVisibility(GONE);
                relativeLayout_more.setVisibility(GONE);
                hr.isCurrentWindow(false);
                material.isCurrentWindow(false);
                break;
            case PLANNER:
                relativeLayout_hr.setVisibility(GONE);
                relativeLayout_material.setVisibility(GONE);
                relativeLayout_drop.setVisibility(GONE);
                //relativeLayout_planner.setVisibility(VISIBLE);
                relativeLayout_more.setVisibility(GONE);
                hr.isCurrentWindow(false);
                material.isCurrentWindow(false);
                break;
            case MORE:
                relativeLayout_hr.setVisibility(GONE);
                relativeLayout_material.setVisibility(GONE);
                relativeLayout_drop.setVisibility(GONE);
                //relativeLayout_planner.setVisibility(GONE);
                relativeLayout_more.setVisibility(VISIBLE);
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
                handler.post(new Runnable() {
                @Override
                public void run() {
                    floatingWindowPreProcess();
                    isFloatingWindowShowing = false;
                }
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
        //System.exit(0);
        System.gc();
    }
}
