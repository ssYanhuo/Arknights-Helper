package com.ssyanhuo.arknightshelper.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import com.ssyanhuo.arknightshelper.entity.MediaInfo;

import java.io.IOException;
import java.util.ArrayList;

public class ImageUtils {
    final static String TAG = "ImageUtils";
    public static ArrayList<MediaInfo> getPictures(Context context){
        ArrayList<MediaInfo> result= new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Images.ImageColumns.DATE_MODIFIED;
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        try{
            cursor.moveToLast();
        }catch (Exception e){
            e.printStackTrace();
            cursor.close();
            return result;
        }

        try{
            cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
        }catch (Exception e){
            return result;
        }

        for(int i = 0; i < 20; i++){
            if (i == 0 || cursor.moveToPrevious()){//如果是第一次获取，就不向前移动指针
                MediaInfo mediaInfo = new MediaInfo();
                mediaInfo.date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                mediaInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                mediaInfo.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                mediaInfo.uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaInfo.id);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        mediaInfo.thumbnail = contentResolver.loadThumbnail(mediaInfo.uri, new Size(240, 240), null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8;
                    mediaInfo.thumbnail = BitmapFactory.decodeFile(mediaInfo.path, options);
                }
                result.add(mediaInfo);
                //Log.e(TAG, String.valueOf(mediaInfo));
            }
            else {
                break;
            }
        }
        cursor.close();
        return result;
    }
}
