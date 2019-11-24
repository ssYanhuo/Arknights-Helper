package com.ssyanhuo.arknightshelper.utiliy;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ImageUtility {
    final static String TAG = "ImageUtility";
    public static ArrayList<MediaInfo> getPictures(Context context){
        ArrayList<MediaInfo> result= new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Images.ImageColumns.DATE_MODIFIED;
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        cursor.moveToLast();
        for(int i = 0; i < 20; i++){
            if (cursor.moveToPrevious()){
                MediaInfo mediaInfo = new MediaInfo();
                mediaInfo.date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                mediaInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                mediaInfo.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaInfo.id);
                    try {
                        mediaInfo.thumbnail = contentResolver.loadThumbnail(contentUri, new Size(240, 240), null);
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
        return result;
    }
}
