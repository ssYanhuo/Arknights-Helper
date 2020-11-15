package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.entity.StaticData;

public class CompatUtils {
    static public void check(Context context){
        SharedPreferences preferences = context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE);
        final int versionLast = preferences.getInt("versionLast", -1);
        if (versionLast < BuildConfig.VERSION_CODE){
            if (versionLast < 20){
                try {
                    Integer.parseInt(preferences.getString("game_version", StaticData.Const.PACKAGE_OFFICIAL));
                    preferences.edit().putString("game_version", StaticData.Const.PACKAGE_MANUAL).apply();
                }catch (Exception ignored){

                }
            }
        }else if (versionLast <= 0){

        }else {

        }
    }
}
