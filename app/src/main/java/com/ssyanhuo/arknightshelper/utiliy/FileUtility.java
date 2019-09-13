package com.ssyanhuo.arknightshelper.utiliy;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtility {
    private final static String TAG = "FileUtility";

    public static void writeFile(String data, String name, Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(data);
            bufferedWriter.close();
            fileOutputStream.close();
        } catch (IOException e){
            Log.e(TAG, String.valueOf(e));
        }
    }

    public static String readFile(String name, Context context){
        try {
            FileInputStream fileInputStream = context.openFileInput(name);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            StringBuilder data = null;
            while ((line = bufferedReader.readLine()) != null){
                data = (data == null ? new StringBuilder() : data).append(line);
            }
            assert data != null;
            return data.toString();
        } catch (IOException e){
            Log.e(TAG, String.valueOf(e));
            return  null;
        }
    }
}
