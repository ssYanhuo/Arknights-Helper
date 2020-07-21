package com.ssyanhuo.arknightshelper.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ssyanhuo.arknightshelper.service.OverlayService;

public class LaunchGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            Intent intent1 = new Intent(this, OverlayService.class);
            Intent intent2 = getPackageManager().getLaunchIntentForPackage(getIntent().getStringExtra("packageName"));
            intent2.setAction(Intent.ACTION_VIEW);
            startService(intent1);
            startActivity(intent2);
            finish();
        }catch (Exception e){
            Toast.makeText(this, "出现了错误", Toast.LENGTH_SHORT).show();
            try {
                finish();
            }catch (Exception ignored){

            }
        }

    }
}