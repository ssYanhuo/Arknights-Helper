package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.ssyanhuo.arknightshelper.misc.StaticData;


public class OCRUtils {
    static final String TAG = "OCR";
    public static String[] SUPPORTED_ABI = {"arm64-v8a", "armeabi-v7a"};
    public static String[] SUPPORTED_LANG = {I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE};
    public static void init(Context context){
        OCR.getInstance(context).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                Log.i(TAG, "OCR init succeed!");
            }

            @Override
            public void onError(OCRError ocrError) {
                ocrError.printStackTrace();
            }
        },context);
    }

    public static boolean isSupported(Context context){
        return isAbiSupported() && isLanguageSupported(context);
    }


    public static boolean isAbiSupported(){
        return isAbiSupported(Build.SUPPORTED_ABIS[0]);
    }

    public static boolean isAbiSupported(String abi){
        for (String s :
                SUPPORTED_ABI) {
            if (abi.equals(s)){
                return true;
            }
        }
        return false;
    }

    public static boolean isLanguageSupported(String lang){
        if (lang == null){
            return true;
        }
        for (String l :
                SUPPORTED_LANG) {
            if (l.equals(lang)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLanguageSupported(Context context){
        return isLanguageSupported(context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE).getString("game_language", I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE));
    }
}
