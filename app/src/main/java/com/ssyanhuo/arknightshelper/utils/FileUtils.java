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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.xml.xpath.XPath;

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
    public static String readData(String name, Context context) throws IOException {
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

    static public void copyFilesFromAssets(Context c, String[] list){
        for (String name:
                list) {
            copyFileFromAssets(c, c.getFilesDir().getPath(), name);
        }
    }

    static public void copyFileFromAssets(Context c, String path, String Name) {
        File outfile = new File(new File(path), Name);
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

    static public boolean checkFiles(Context context, String folderPath, String[] files){
        for (String file :
                files) {
            if (!checkFile(context, folderPath + File.separator + file)) {
                return false;
            }
        }
        return true;
    }

    static public boolean checkFile(Context context, String path){
        try{
            File file = new File(path);
            return file.exists();
        }catch(Exception ignored){
            return false;
        }
    }

    static public boolean delFile(Context context, String path){
        File file = new File(path);
        if (file.exists()){
           return file.delete();
        }else {
            return false;
        }
    }
}
