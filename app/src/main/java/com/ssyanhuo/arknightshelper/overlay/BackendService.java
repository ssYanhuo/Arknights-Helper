package com.ssyanhuo.arknightshelper.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.tabs.TabLayout;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.utiliy.DpUtiliy;
import com.ssyanhuo.arknightshelper.utiliy.JsonUtility;
import com.ssyanhuo.arknightshelper.utiliy.BroadcastReceiver;

import java.util.ArrayList;


public class BackendService extends Service {
    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;
    LinearLayout linearLayout;
    LinearLayout linearLayout_hr;
    LinearLayout linearLayout_exp;
    LinearLayout linearLayout_material;
    ScrollView scrollView_hr;
    ScrollView scrollView_exp;
    ScrollView scrollView_material;
    Button button;
    final int HR = 0;
    final int EXP = 1;
    final int MATERIAL = 2;
    final int CLOSE = 3;
    final String TAG = "BackgroundService";
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
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent)
                    .setChannelId("notification");
        }else {
            builder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent);
        }

        Notification notification = builder.build();
        startForeground(1, notification);

        //启动悬浮窗

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        startFloatingButton();
        //预处理
        floatingWindowPreProcess();
    }

    public void startFloatingButton(){
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = DpUtiliy.dip2px(getApplicationContext(), 48);
        layoutParams.height = DpUtiliy.dip2px(getApplicationContext(), 48);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.x = 0;
        layoutParams.y = 200;
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
                        layoutParams.x = layoutParams.x + movedX;
                        layoutParams.y = layoutParams.y + movedY;

                        // 更新悬浮窗控件布局
                        windowManager.updateViewLayout(view, layoutParams);
                        break;
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
        windowManager.addView(button, layoutParams);

    }

    public void floatingWindowPreProcess(){
        linearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.overlay_main, null);
        //实例化view
        scrollView_hr = linearLayout.findViewById(R.id.scroll_hr);
        scrollView_exp = linearLayout.findViewById(R.id.scroll_exp);
        scrollView_material = linearLayout.findViewById(R.id.scroll_material);
        linearLayout_hr = linearLayout.findViewById(R.id.hr_content);
        linearLayout_exp = linearLayout.findViewById(R.id.exp_content);
        linearLayout_material = linearLayout.findViewById(R.id.material_content);
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
                    case EXP:
                        changeFloatingWindowContent(EXP);
                        break;
                    case MATERIAL:
                        changeFloatingWindowContent(MATERIAL);
                        break;
                    case CLOSE:
                        hideFloatingWindow();
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
        //初始化
        Hr hr = new Hr();
        hr.init(getApplicationContext(), linearLayout_hr);
        Exp exp = new Exp();
        exp.init(getApplicationContext(), linearLayout_exp);
        Material material = new Material();
        material.init(getApplicationContext(), linearLayout_material);
    }

    public void showFloatingWindow(){
        windowManager.removeView(button);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        layoutParams.gravity = Gravity.END | Gravity.TOP;
        layoutParams.x = 0;
        layoutParams.y = 0;
        if(rotation == 1 || rotation == 3){
            layoutParams.height = displayMetrics.heightPixels;
            layoutParams.width = displayMetrics.widthPixels / 2;
        }else {
            layoutParams.height = displayMetrics.heightPixels / 2;
            layoutParams.width = displayMetrics.widthPixels;
        }
        windowManager.addView(linearLayout, layoutParams);

    }
    public void changeFloatingWindowContent(int i){
        switch (i){
            case HR:
                scrollView_hr.setVisibility(View.VISIBLE);
                scrollView_exp.setVisibility(View.GONE);
                scrollView_material.setVisibility(View.GONE);
                layoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.updateViewLayout(linearLayout, layoutParams);
                break;
            case EXP:
                scrollView_hr.setVisibility(View.GONE);
                scrollView_exp.setVisibility(View.VISIBLE);
                scrollView_material.setVisibility(View.GONE);
                layoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.updateViewLayout(linearLayout, layoutParams);
                break;
            case MATERIAL:
                scrollView_hr.setVisibility(View.GONE);
                scrollView_exp.setVisibility(View.GONE);
                scrollView_material.setVisibility(View.VISIBLE);
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                windowManager.updateViewLayout(linearLayout, layoutParams);
                break;
            default:
                break;
        }
    }
    public void hideFloatingWindow(){
        windowManager.removeView(linearLayout);
        startFloatingButton();
        floatingWindowPreProcess();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
