package com.ssyanhuo.arknightshelper.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.srplab.www.starcore.StarCoreFactory;
import com.srplab.www.starcore.StarObjectClass;
import com.srplab.www.starcore.StarServiceClass;
import com.srplab.www.starcore.StarSrvGroupClass;
import com.ssyanhuo.arknightshelper.utils.PythonUtils;

public class PythonService extends Service {
    StarSrvGroupClass srvGroup;
    StarCoreFactory starCore;
    StarServiceClass service;
    StarObjectClass python;
    StarObjectClass pythonSys;
    StarObjectClass pythonPath;
    public PythonService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PythonBinder();
    }

    public class PythonBinder extends Binder {
        public PythonBinder(){
            Log.i("PythonService", "Created!");
        }
        public String callArkplanner(String required){
            return getPlan(required);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (PythonUtils.checkEnvironment(getApplicationContext())){
            python = PythonUtils.init(getApplicationContext());
        }else if (PythonUtils.isAbiSupported()) {
            //TODO 忘了这是干啥的
            //PythonUtils.setupEnvironment(getApplicationContext());
            //service = PythonUtils.init(getApplicationContext());
        }else {
            Toast.makeText(this, "不支持的CPU架构", Toast.LENGTH_SHORT).show();
        }
    }

    public String getPlan(String required){
        Object obj = python._Call("plan", required, "{}");
        return obj.toString();
    }


}
