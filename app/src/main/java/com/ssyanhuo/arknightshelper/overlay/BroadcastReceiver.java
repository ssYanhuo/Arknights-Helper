package com.ssyanhuo.arknightshelper.overlay;

import android.content.Context;
import android.content.Intent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, BackendService.class);
        context.stopService(intent1);
    }
}
