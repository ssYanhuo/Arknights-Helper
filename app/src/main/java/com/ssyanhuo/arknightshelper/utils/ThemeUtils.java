package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.ssyanhuo.arknightshelper.R;

public class ThemeUtils {
    static public final int THEME_UNSPECIFIED = -1;
    static public final int THEME_DEFAULT = 0;
    static public final int THEME_NEW_YEAR = 1;
    static public final int THEME_LIGHT = 2;
    static public final int TYPE_MAIN = 0;
    static public final int TYPE_NO_ACTION_BAR = 1;
    static public final int TYPE_FLOATING_WINDOW = 2;
    static public final int TYPE_APP_BAR_OVERLAY = 3;
    static public final int TYPE_PRIMARY = 0;
    static public final int TYPE_PRIMARY_DARK = 1;
    static public final int[][] matrixTheme = {
            {R.style.AppTheme_Default, R.style.AppTheme_Default_NoActionBar, R.style.AppTheme_Default_FloatingWindow, R.style.AppTheme_Default_AppBarOverlay},
            {R.style.AppTheme_NewYear, R.style.AppTheme_NewYear_NoActionBar, R.style.AppTheme_NewYear_FloatingWindow, R.style.AppTheme_Default_AppBarOverlay},
            {R.style.AppTheme_Light, R.style.AppTheme_Light_NoActionBar, R.style.AppTheme_Light_FloatingWindow, R.style.AppTheme_Light_AppBarOverlay}
    };
    static public final int[][] matrixColor = {
            {R.color.colorPrimary, R.color.colorPrimaryDark},
            {R.color.colorPrimaryNewYear, R.color.colorPrimaryDarkNewYear},
            {R.color.colorPrimaryLight, R.color.colorPrimaryDarkLight}
    };
    static public int getThemeId(int theme, int type, Context context){
            if (theme == THEME_UNSPECIFIED){
            try{
                SharedPreferences sharedPreferences = context.getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", Context.MODE_PRIVATE);
                theme = Integer.parseInt(sharedPreferences.getString("theme", "0"));
            }catch (Exception e){
                e.printStackTrace();
                theme = 0;
            }
        }
        try{
            return matrixTheme[theme][type];
        }catch (Exception e){
            return matrixTheme[0][0];
        }

    }
    static public int getThemeMode(Context context){
            try{
                SharedPreferences sharedPreferences = context.getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", Context.MODE_PRIVATE);
                return Integer.parseInt(sharedPreferences.getString("theme", "0"));
            }catch (Exception e){
                e.printStackTrace();
                return 0;
            }
    }

    static public int getColorId(int theme, int type, Context context){
        if (theme == THEME_UNSPECIFIED){
            try{
                SharedPreferences sharedPreferences = context.getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", Context.MODE_PRIVATE);
                theme = Integer.parseInt(sharedPreferences.getString("theme", "0"));
            }catch (Exception e){
                e.printStackTrace();
                theme = 0;
            }
        }
        try{
            return matrixColor[theme][type];
        }catch (Exception e){
            return matrixColor[0][0];
        }
    }
    static public int getColor(int theme, int type, Context context){
        return context.getResources().getColor(getColorId(theme, type, context));
    }
    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }


}
