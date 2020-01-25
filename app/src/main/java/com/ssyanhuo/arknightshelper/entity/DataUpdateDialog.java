package com.ssyanhuo.arknightshelper.entity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import com.ssyanhuo.arknightshelper.R;

public class DataUpdateDialog{

    public void showDialog(Context context){
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.AppTheme_Default);
        AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        LinearLayout content = (LinearLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.content_data_updater, null);
        AlertDialog dialog = builder.setTitle(R.string.settings_data_update_title)
                .setView(content)
                .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        dialog.show();
    }
}
