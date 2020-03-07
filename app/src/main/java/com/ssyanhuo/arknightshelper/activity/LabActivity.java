package com.ssyanhuo.arknightshelper.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.PythonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class LabActivity extends AppCompatActivity {

    private final static String TAG = "LAB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab);
        ((LinearLayout)findViewById(R.id.lab_pytest)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PythonUtils.prepareDependencies(LabActivity.this, getWindow().getDecorView());
            }
        });
    }

    public void getDeviceInfo(View view){
        String info = "";
        info += "MANUFACTURER:" + android.os.Build.MANUFACTURER + "\n";
        info += "PRODUCT:" + android.os.Build.PRODUCT + "\n";
        info += "BRAND:" + android.os.Build.BRAND + "\n";
        info += "MODEL:" + android.os.Build.MODEL + "\n";
        info += "BOARD:" + android.os.Build.BOARD + "\n";
        info += "DEVICE:" + android.os.Build.DEVICE + "\n";
        info += "SDK_INT:" + android.os.Build.VERSION.SDK_INT + "\n";
        info += "RELEASE:" + android.os.Build.VERSION.RELEASE + "\n";
        info += "LANGUAGE:" + Locale.getDefault().getLanguage() + "\n";
        Log.e(TAG, info);
        ClipData clipData = ClipData.newPlainText(TAG, info);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        assert clipboardManager != null;
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "信息已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    public void readTest(View view){
        try {
            String data = FileUtils.readFile("Test.log", this);
            if (data == null){ throw new IOException("File is null.");}
            Toast.makeText(this, "读取Test.log成功:" + data, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e(TAG, String.valueOf(e));
            Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeTest(View view){
        try {
            String data = String.valueOf(System.currentTimeMillis());
            FileUtils.writeFile(data, "Test.log", this);
            Toast.makeText(this, "写入Test.log成功:" + data, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e(TAG, String.valueOf(e));
            Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void goApplicationManager(View view){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", this.getPackageName(), null));
        startActivity(intent);
    }

    public void getLog(View view){
        try {
            final TextView textView = new TextView(this);
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
            materialAlertDialogBuilder.setPositiveButton("复制", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String text = String.valueOf(textView.getText());
                    ClipData clipData = ClipData.newPlainText(TAG, text);
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    assert clipboardManager != null;
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(LabActivity.this, "信息已复制到剪贴板", Toast.LENGTH_SHORT).show();
                }
            });
            materialAlertDialogBuilder.setView(textView);
            AlertDialog alertDialog = materialAlertDialogBuilder.create();
            alertDialog.show();
            Process process = Runtime.getRuntime().exec("logcat -t 4096 | grep \'arknights\'");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line).append("\n");
                textView.setText(log.toString());
            }
        } catch (IOException e) {
            Log.e(TAG, String.valueOf(e));
        }
    }
}
