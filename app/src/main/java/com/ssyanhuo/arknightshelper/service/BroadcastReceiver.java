package com.ssyanhuo.arknightshelper.service;

import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, OverlayService.class);
        if(Objects.requireNonNull(intent.getStringExtra("action")).equals("StopService")){
            context.stopService(intent1);
        }
    }
}
