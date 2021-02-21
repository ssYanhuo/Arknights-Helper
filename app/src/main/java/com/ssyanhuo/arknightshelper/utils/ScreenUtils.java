package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.Surface;
import android.view.WindowManager;

import com.ssyanhuo.arknightshelper.entity.StaticData;

public class ScreenUtils {
    public static int MODE_PORTRAIT = 0;
    public static int MODE_LANDSCAPE = 1;
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getDensityDpi(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    public static int getScreenWidth(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context){
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenRotation(Context context){
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        SharedPreferences preferences = context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE);
        int raw = windowManager.getDefaultDisplay().getRotation();
        if (preferences.getBoolean("emulator_mode", false)){
            switch (raw){
                case Surface.ROTATION_0:
                    return Surface.ROTATION_90;
                case Surface.ROTATION_90:
                    return Surface.ROTATION_180;
                case Surface.ROTATION_180:
                    return Surface.ROTATION_270;
                case Surface.ROTATION_270:
                    return Surface.ROTATION_0;
            }
        }
        else {
            return raw;
        }
        return Surface.ROTATION_0;
    }

    public static int getScreenRotationMode(Context context){
        return getScreenRotationMode(getScreenRotation(context));
    }

    public static int getScreenRotationMode(int rotation){
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270){
            return MODE_LANDSCAPE;
        }else {
            return MODE_PORTRAIT;
        }
    }
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }
}
