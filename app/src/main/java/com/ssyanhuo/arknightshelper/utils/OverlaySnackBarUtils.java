package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ssyanhuo.arknightshelper.R;

import eightbitlab.com.blurview.BlurView;

public class OverlaySnackBarUtils {
    public static void show(CharSequence text, Drawable image, Context context, RelativeLayout layout){
        BlurView snackBarView = (BlurView) LayoutInflater.from(context).inflate(R.layout.view_overlaysnackbar, layout);

    }
}
