package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {
    private final static String TAG = "FileUtils";

    public static void writeFile(String data, String name, Context context) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        bufferedWriter.write(data);
        bufferedWriter.close();
        fileOutputStream.close();
    }

    public static String readFile(String name, Context context) throws IOException {
        FileInputStream fileInputStream = context.openFileInput(name);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line;
        StringBuilder data = null;
        while ((line = bufferedReader.readLine()) != null){
            data = (data == null ? new StringBuilder() : data).append(line);
        }
        assert data != null;
        return data.toString();
    }
    public static String readData(String name, Context context, boolean builtin) throws IOException {
        if (builtin){
            InputStream inputStream = context.getResources().getAssets().open("data/" + name);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] fileByte = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(fileByte);
            String data = new String(fileByte);
            Log.i(TAG, "Read builtin file \"" + name +"\" succeed, file length:" + data.length());
            bufferedInputStream.close();
            inputStream.close();
            return data;
        }else{
            FileInputStream fileInputStream = context.openFileInput(name);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            StringBuilder data = null;
            while ((line = bufferedReader.readLine()) != null){
                data = (data == null ? new StringBuilder() : data).append(line);
            }
            assert data != null;
            Log.i(TAG, "Read file \"" + name +"\" succeed, file length:" + data.length());
            bufferedReader.close();
            fileInputStream.close();
            return data.toString();
        }
    }

    static public void copyFiles(Context c, String[] list){
        for (String name:
                list) {
            copyFile(c, name);
        }
    }

    static public void copyFile(Context c, String Name) {
        File outfile = new File(c.getFilesDir(), Name);
        BufferedOutputStream outStream = null;
        BufferedInputStream inStream = null;

        try {
            outStream = new BufferedOutputStream(new FileOutputStream(outfile));
            inStream = new BufferedInputStream(c.getAssets().open("data" + File.separator + Name));

            byte[] buffer = new byte[1024 * 10];
            int readLen = 0;
            while ((readLen = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, readLen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
