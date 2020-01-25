package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JSONUtils {
    private static String TAG = "JsonUtility";
    public static String getJSONString(Context context, String path){
        try {
            InputStream inputStream = context.getResources().getAssets().open(path);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] fileByte = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(fileByte);
            String json = new String(fileByte);
            Log.i(TAG, "Read JSON succeed, file length:" + json.length());
            bufferedInputStream.close();
            inputStream.close();
            return json;
        } catch (IOException e) {
            Log.e(TAG, "Read JSON failed!:" + e);
            return null;
        }
    }
    public static JSONObject getJSONObject(Context context, String jsonString){
        return JSON.parseObject(jsonString);
    }
}
