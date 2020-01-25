package com.ssyanhuo.arknightshelper.entity;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class TipSnackBarHelper {
    public class Tip{
        public int upTimesFromInstall;
        public int upTimesFromUpdate;
        public String content;
        public View.OnClickListener actionOnClickListener;
        public String action;
        public int duration;
        public String name;
    }
    
}
