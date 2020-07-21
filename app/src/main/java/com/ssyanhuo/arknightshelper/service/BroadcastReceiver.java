package com.ssyanhuo.arknightshelper.service;

import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
//            Intent serviceIntent = new Intent(context, AutoStartService.class);
//            context.startService(serviceIntent);
//        }
        if(Objects.requireNonNull(intent.getStringExtra("action")).equals("StopService")){
            Intent intent1 = new Intent(context, OverlayService.class);
            context.stopService(intent1);
        }
    }
}
