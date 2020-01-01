package com.ssyanhuo.arknightshelper.utiliy;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtility {
    private final static String TAG = "FileUtility";

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
}
