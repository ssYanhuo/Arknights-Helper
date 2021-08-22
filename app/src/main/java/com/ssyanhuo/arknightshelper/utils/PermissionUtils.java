package com.ssyanhuo.arknightshelper.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDataStore;

import java.util.ArrayList;

public class PermissionUtils {
    public static final String TAG = "PackageUtils";
    public static final String PERMISSION_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String PERMISSION_OVERLAY = "android.permission.SYSTEM_OVERLAY_WINDOW";
    public static final String PERMISSION_ADD_SHORTCUT = "ADD_SHORTCUT";
    public static final String PERMISSION_POPUP_IN_BACKGROUND = "POPUP_IN_BACKGROUND";


    public static boolean checkPermission(String permission, Context context){
        switch (permission){
            case PERMISSION_STORAGE:
                return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            case PERMISSION_OVERLAY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return Settings.canDrawOverlays(context);
                } else {
                    return true;
                }
            default:
                return true;
        }
    }

    public static ArrayList<String> checkPermissions(String [] permissions, Context context){
        ArrayList<String> result = new ArrayList<>();
        for (String permission :
                permissions) {
            if (!checkPermission(permission, context)){
                result.add(permission);
            }
        }
        return result;
    }
}
