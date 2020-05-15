package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentUtils {
    public static void openURL(String url, Context context){
        openURL(url, context, false);
    }
    public static void openURL(String url, Context context, boolean newTask){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (newTask){
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
