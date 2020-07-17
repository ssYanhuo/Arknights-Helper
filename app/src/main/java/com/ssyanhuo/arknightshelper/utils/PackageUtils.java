package com.ssyanhuo.arknightshelper.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.preference.ListPreference;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.StaticData;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageUtils {
    static final String TAG = "PackageUtils";
    static public ArrayList<String> getGameList(Context context){
        ArrayList<String> list = new ArrayList<>(Arrays.asList(StaticData.Const.PACKAGE_LIST));
        ArrayList<String> notFound = new ArrayList<>();
        for (String game :
                list) {
            if (! checkApplication(game, context)){
                notFound.add(game);
            }
        }
        list.removeAll(notFound);
        return list;
    }

    static public int getGameCount(Context context){
        int count = 0;
        if (checkApplication(StaticData.Const.PACKAGE_OFFICIAL, context)){
            count++;
        }
        if (checkApplication(StaticData.Const.PACKAGE_BILIBILI, context)){
            count++;
        }
        if (checkApplication(StaticData.Const.PACKAGE_TAIWANESE, context)){
            count++;
        }
        if (checkApplication(StaticData.Const.PACKAGE_JAPANESE, context)){
            count++;
        }
        if (checkApplication(StaticData.Const.PACKAGE_ENGLISH, context)){
            count++;
        }
        if (checkApplication(StaticData.Const.PACKAGE_KOREAN, context)){
            count++;
        }
        return count;
    }

    static public boolean checkApplication(String packageName, Context context) {
        if (packageName == null || "".equals(packageName)){
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    static public void startApplication(String packageName, Context context){
        try{
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
        }
        catch (Exception e){
            Log.e(TAG, "Start game failed!", e);
        }
    }
    static public String getName(String packageName, Context context){
        if (packageName.equals(StaticData.Const.PACKAGE_OFFICIAL)){
            return context.getString(R.string.game_official);
        }else if (packageName.equals(StaticData.Const.PACKAGE_BILIBILI)){
            return context.getString(R.string.game_bilibili);
        }else if (packageName.equals(StaticData.Const.PACKAGE_TAIWANESE)){
            return context.getString(R.string.game_taiwanese);
        } else if (packageName.equals(StaticData.Const.PACKAGE_ENGLISH)){
            return context.getString(R.string.game_english);
        }else if (packageName.equals(StaticData.Const.PACKAGE_JAPANESE)){
            return context.getString(R.string.game_japanese);
        }else if (packageName.equals(StaticData.Const.PACKAGE_KOREAN)){
            return context.getString(R.string.game_korean);
        }else {
            return "未知的游戏版本";
        }
    }
}
