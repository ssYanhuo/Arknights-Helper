package com.ssyanhuo.arknightshelper.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.ListPreference;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.LaunchGameActivity;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.service.OverlayService;

import java.lang.reflect.Array;
import java.nio.channels.OverlappingFileLockException;
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
            Log.e(TAG, "Start game failed!" + packageName, e);
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
            return context.getString(R.string.game_unknow);
        }
    }

    static public ArrayList<String> getGameNameList(Context context){
        ArrayList<String> result = new ArrayList<>();
        for (String packageName :
                getGameList(context)) {
            result.add(getName(packageName, context));
        }
        return result;
    }

    static public Bitmap getAppIconBitmap(Context context, String packageName) {
        if (context == null) {
            return null;
        }

        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            Drawable drawable = packageManager.getApplicationIcon(packageName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (drawable instanceof BitmapDrawable) {
                    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable});
                    int width = layerDrawable.getIntrinsicWidth();
                    int height = layerDrawable.getIntrinsicHeight();

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    layerDrawable.draw(canvas);
                    return bitmap;
                } else if (drawable instanceof AdaptiveIconDrawable) {
                    Drawable[] drr = new Drawable[2];
                    drr[0] = ((AdaptiveIconDrawable) drawable).getBackground();
                    drr[1] = ((AdaptiveIconDrawable) drawable).getForeground();

                    LayerDrawable layerDrawable = new LayerDrawable(drr);
                    int width = layerDrawable.getIntrinsicWidth();
                    int height = layerDrawable.getIntrinsicHeight();

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    layerDrawable.draw(canvas);
                    return bitmap;
                }
            } else {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public void addShortCut(Context context, String packageName, String shortcutName){
        try {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)){
                Intent intent = new Intent(context, LaunchGameActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("packageName", packageName);
                ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, packageName)
                        .setShortLabel(shortcutName)
                        .setLongLabel(shortcutName)
                        .setIcon(IconCompat.createWithResource(context, R.drawable.shortcut_icon))
                        .setIntent(intent)
                        .build();
                ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
            }
        } catch (Exception e) {
            Toast.makeText(context, R.string.add_shortcut_error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
