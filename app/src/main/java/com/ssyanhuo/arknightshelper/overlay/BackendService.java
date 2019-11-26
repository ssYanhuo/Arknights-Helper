package com.ssyanhuo.arknightshelper.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.android.material.tabs.TabLayout;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.utiliy.DpUtiliy;
import com.ssyanhuo.arknightshelper.utiliy.BroadcastReceiver;
import com.ssyanhuo.arknightshelper.utiliy.OCRUtility;

import java.util.Timer;
import java.util.TimerTask;


public class BackendService extends Service {
    WindowManager windowManager;
    WindowManager.LayoutParams floatingWindowLayoutParams;
    WindowManager.LayoutParams backgroundLayoutParams;
    WindowManager.LayoutParams placeHolderLayoutParams;
    LinearLayout linearLayout;
    LinearLayout linearLayout_hr;
    LinearLayout linearLayout_material;
    LinearLayout linearLayout_drop;
    ScrollView scrollView_hr;
    ScrollView scrollView_exp;
    ScrollView scrollView_material;
    Button button;
    final int HR = 0;
    final int MATERIAL = 1;
    final int DROP = 2;
    final String TAG = "BackgroundService";
    LinearLayout backgroundLayout;
    LinearLayout placeHolder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int orientation = -1;
    boolean isFloatingWindowShowing = false;
    Hr hr = new Hr();
    Material material = new Material();
    Drop drop = new Drop();

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
        startForeground(1, notification);

        //启动悬浮窗

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        floatingWindowLayoutParams = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            floatingWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            floatingWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        backgroundLayoutParams = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            backgroundLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            backgroundLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        backgroundLayoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        placeHolderLayoutParams = new WindowManager.LayoutParams();
        sharedPreferences = getSharedPreferences("Config", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        startFloatingButton();
        //预处理
        floatingWindowPreProcess();
        OCRUtility.init(getApplicationContext());
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

    public void startFloatingButton(){
        floatingWindowLayoutParams.format = PixelFormat.RGBA_8888;
        floatingWindowLayoutParams.width = DpUtiliy.dip2px(getApplicationContext(), 48);
        floatingWindowLayoutParams.height = DpUtiliy.dip2px(getApplicationContext(), 48);
        floatingWindowLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        floatingWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        floatingWindowLayoutParams.x = sharedPreferences.getInt("lastX", 0);
        floatingWindowLayoutParams.y = sharedPreferences.getInt("lastY", 200);
        button = new Button(this);
        button.setBackground(getResources().getDrawable(R.mipmap.overlay_button));
        button.setOnTouchListener(new View.OnTouchListener() {
            int x;
            int y;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
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
                        floatingWindowLayoutParams.x = floatingWindowLayoutParams.x + movedX;
                        floatingWindowLayoutParams.y = floatingWindowLayoutParams.y + movedY;

                        // 更新悬浮窗控件布局
                        windowManager.updateViewLayout(view, floatingWindowLayoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        editor.putInt("lastX", floatingWindowLayoutParams.x);
                        editor.putInt("lastY", floatingWindowLayoutParams.y);
                        editor.commit();
                    default:
                        break;
                }
                return false;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFloatingWindow();
            }
        });
        windowManager.addView(button, floatingWindowLayoutParams);

    }

    public void floatingWindowPreProcess(){
        backgroundLayout = new LinearLayout(this);
        placeHolder = new LinearLayout(this);
        linearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.overlay_main, null);
        //实例化view
        scrollView_hr = linearLayout.findViewById(R.id.scroll_hr);
        scrollView_exp = linearLayout.findViewById(R.id.scroll_exp);
        scrollView_material = linearLayout.findViewById(R.id.scroll_material);
        linearLayout_hr = linearLayout.findViewById(R.id.hr_content);
        linearLayout_material = linearLayout.findViewById(R.id.material_content);
        linearLayout_drop = linearLayout.findViewById(R.id.drop_content);
        scrollView_hr.setVisibility(View.VISIBLE);
        scrollView_exp.setVisibility(View.GONE);
        scrollView_material.setVisibility(View.GONE);
        TabLayout tabLayout = linearLayout.findViewById(R.id.tab_main);
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
        ImageButton imageButton = linearLayout.findViewById(R.id.overlay_close);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFloatingWindow();
            }
        });
        //初始化
        hr.init(getApplicationContext(), linearLayout_hr, backgroundLayout);
        material.init(getApplicationContext(), linearLayout_material, backgroundLayout);
        drop.init(getApplicationContext(), linearLayout_drop);
    }

    public void showFloatingWindow(){
        orientation = getApplicationContext().getResources().getConfiguration().orientation;
        isFloatingWindowShowing = true;
        windowManager.removeView(button);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        floatingWindowLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
        floatingWindowLayoutParams.x = 0;
        floatingWindowLayoutParams.y = 0;
        //检测屏幕方向和是否全屏
        if(rotation == 1 || rotation == 3){//横
            floatingWindowLayoutParams.height = displayMetrics.heightPixels;
            floatingWindowLayoutParams.width = displayMetrics.widthPixels / 2;
            placeHolderLayoutParams.height = displayMetrics.heightPixels;
            placeHolderLayoutParams.width = displayMetrics.widthPixels / 2;
            backgroundLayout.setOrientation(LinearLayout.HORIZONTAL);
            backgroundLayoutParams.height = displayMetrics.heightPixels;
            backgroundLayoutParams.width = displayMetrics.widthPixels;
            //是否优化状态栏区域的显示效果
            if (rotation == 1){linearLayout.setBackgroundColor(Color.parseColor("#aa000000"));}
        }else {//竖
            floatingWindowLayoutParams.height = displayMetrics.heightPixels / 2;
            floatingWindowLayoutParams.width = displayMetrics.widthPixels;
            placeHolderLayoutParams.height = displayMetrics.heightPixels / 2;
            placeHolderLayoutParams.width = displayMetrics.widthPixels;
            backgroundLayoutParams.height = displayMetrics.heightPixels;
            backgroundLayoutParams.width = displayMetrics.widthPixels;
            backgroundLayout.setOrientation(LinearLayout.VERTICAL);
            //关闭背景渐变
            linearLayout.setBackgroundColor(Color.parseColor("#aa000000"));
        }
        linearLayout.setLayoutParams(floatingWindowLayoutParams);
        backgroundLayoutParams.x = 0;
        backgroundLayoutParams.y = 0;
        backgroundLayoutParams.format = PixelFormat.RGBA_8888;
        placeHolder.setLayoutParams(placeHolderLayoutParams);
        placeHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFloatingWindow();
            }
        });
        LinearLayout phContent = new LinearLayout(this);
        phContent.setTag("placeHolder");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        phContent.setLayoutParams(layoutParams);
        placeHolder.addView(phContent);
        backgroundLayout.removeAllViews();
        if(rotation == 1 || rotation == 3){
            backgroundLayout.addView(placeHolder);
            backgroundLayout.addView(linearLayout);
        }else {
            backgroundLayout.addView(linearLayout);
            backgroundLayout.addView(placeHolder);
        }

        windowManager.addView(backgroundLayout, backgroundLayoutParams);
    }
    public void changeFloatingWindowContent(int i){
        switch (i){
            case HR:
                scrollView_hr.setVisibility(View.VISIBLE);
                scrollView_exp.setVisibility(View.GONE);
                scrollView_material.setVisibility(View.GONE);
                hr.isCurrentWindow(true);
                material.isCurrentWindow(false);
                break;
            case MATERIAL:
                scrollView_hr.setVisibility(View.GONE);
                scrollView_exp.setVisibility(View.VISIBLE);
                scrollView_material.setVisibility(View.GONE);
                hr.isCurrentWindow(false);
                material.isCurrentWindow(true);
                break;
            case DROP:
                scrollView_hr.setVisibility(View.GONE);
                scrollView_exp.setVisibility(View.GONE);
                scrollView_material.setVisibility(View.VISIBLE);
                hr.isCurrentWindow(false);
                material.isCurrentWindow(false);
                break;
            default:
                break;
        }
    }
    public void hideFloatingWindow(){
        isFloatingWindowShowing = false;
        windowManager.removeView(backgroundLayout);
        startFloatingButton();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Looper.prepare();
                floatingWindowPreProcess();
                Looper.loop();
            }
        },1);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            windowManager.removeView(button);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            windowManager.removeView(backgroundLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
        System.gc();
    }
}
