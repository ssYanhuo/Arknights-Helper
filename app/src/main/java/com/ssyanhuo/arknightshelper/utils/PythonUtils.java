package com.ssyanhuo.arknightshelper.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.srplab.www.starcore.StarCoreFactory;
import com.srplab.www.starcore.StarCoreFactoryPath;
import com.srplab.www.starcore.StarObjectClass;
import com.srplab.www.starcore.StarServiceClass;
import com.srplab.www.starcore.StarSrvGroupClass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener3;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.StaticData;

import java.io.File;

public class PythonUtils {

    private static final String TAG = "PythonUtils";
    public static String[] SUPPORTED_ABI = {"arm64-v8a", "armeabi-v7a"};
    final static String[] MODULE_LIST = {
            "_struct.cpython-36m.so",
            "binascii.cpython-36m.so",
            "zlib.cpython-36m.so",
            "_datetime.cpython-36m.so",
            "math.cpython-36m.so",
            "_ctypes.cpython-36m.so",
            "_posixsubprocess.cpython-36m.so",
            "select.cpython-36m.so",
            "numpy.zip",
            "scipy.zip",
            "_blake2.cpython-36m.so",
            "_datetime.cpython-36m.so",
            "_md5.cpython-36m.so",
            "_random.cpython-36m.so",
            "_sha1.cpython-36m.so",
            "_sha256.cpython-36m.so",
            "_sha3.cpython-36m.so",
            "_sha512.cpython-36m.so",
            "math.cpython-36m.so"
    };
    static final String[] DL_LIST = {
            "libpython3.6m.so",
            "libcrystax.so",
            "libgfortran.so",
            "libgnustl_shared.so",
            "libopenblas.so",
            "libcrypto.so",
            "libssl.so"

    };

    public static int getPluginVersion(Context context){
        PackageManager packageManager = context.getPackageManager();
        try{
            PackageInfo packageInfo = packageManager.getPackageInfo(StaticData.Const.PLANNER_PLUGIN_PACKAGE_NAME, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isSupported(){
        return isOSVersionSupported() && isAbiSupported();
    }

    public static boolean isOSVersionSupported(){
        return isOSVersionSupported(Build.VERSION.SDK_INT);
    }

    public static boolean isOSVersionSupported(int v){
        return v >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isAbiSupported(){
        return isAbiSupported(Build.SUPPORTED_ABIS[0]);
    }

    public static boolean isAbiSupported(String abi){
        for (String s :
                SUPPORTED_ABI) {
            if (abi.equals(s)){
                return true;
            }
        }
        return false;
    }

    public static void prepareDependencies(final Activity activity, @Nullable final View view){
        final Context context = activity.getApplicationContext();
        if (!PythonUtils.isSupported()){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setMessage(R.string.py_unsupported)
                    .show();
        }
        String baseUrl = context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE).getString("update_site", "0");
        if (baseUrl.equals("1")){
            baseUrl = "https://ssyanhuo.github.io/Arknights-Helper-Dependencies/";
        }else {
            baseUrl = "http://ssyanhuo.gitee.io/arknights-helper-dependencies/";
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LinearLayout pythonDownloader = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_python_downloader, null);
        final ProgressBar progressBar1 = pythonDownloader.findViewById(R.id.pythonDownloader_progressBar_1);
        final ProgressBar progressBar2 = pythonDownloader.findViewById(R.id.pythonDownloader_progressBar_2);
        final ProgressBar progressBar3 = pythonDownloader.findViewById(R.id.pythonDownloader_progressBar_3);
        dialogBuilder.setTitle(R.string.py_download)
                .setCancelable(false)
                .setView(pythonDownloader);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        final DownloadTask task1 = new  DownloadTask.Builder(baseUrl + Build.SUPPORTED_ABIS[0] + ".zip", context.getCacheDir())
                .setFilename(Build.SUPPORTED_ABIS[0] + ".zip")
                .setPassIfAlreadyCompleted(true)
                .build();
        final DownloadTask task2 = new  DownloadTask.Builder(baseUrl + "universal" + ".zip", context.getCacheDir())
                .setFilename("universal" + ".zip")
                .setPassIfAlreadyCompleted(true)
                .build();
        final DownloadTask task3 = new  DownloadTask.Builder(baseUrl + "ArkPlanner" + ".zip", context.getCacheDir())
                .setFilename("ArkPlanner" + ".zip")
                .setPassIfAlreadyCompleted(true)
                .build();
        DownloadTask[] list = {task1, task2, task3};
        DownloadTask.enqueue(list, new DownloadListener3() {
            int completedCount = 0;
            @Override
            protected void started(@NonNull DownloadTask task) {

            }

            @Override
            protected void completed(@NonNull DownloadTask task) {
                completedCount ++;
                if (task.equals(task1)){
                    progressBar1.setIndeterminate(true);
                }else if(task.equals(task2)){
                    progressBar2.setIndeterminate(true);
                }else if(task.equals(task3)){
                    progressBar3.setIndeterminate(true);
                }
                if (completedCount >= 3){
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ZipUtils.UnZipFolder(context.getCacheDir() + File.separator + Build.SUPPORTED_ABIS[0] + ".zip", context.getFilesDir() + File.separator + "python");
                                ZipUtils.UnZipFolder(context.getCacheDir() + File.separator + "universal" + ".zip", context.getFilesDir() + File.separator + "python");
                                ZipUtils.UnZipFolder(context.getCacheDir() + File.separator + "ArkPlanner" + ".zip", context.getFilesDir() + File.separator + "python");
                                ZipUtils.UnZipFolder(context.getFilesDir() + File.separator + "python" + File.separator + "numpy" + ".zip", context.getFilesDir() + File.separator + "python" + File.separator + "numpy");
                                ZipUtils.UnZipFolder(context.getFilesDir() + File.separator + "python" + File.separator + "scipy" + ".zip", context.getFilesDir() + File.separator + "python" + File.separator + "scipy");
                                FileUtils.delFile(context, context.getCacheDir() + File.separator + Build.SUPPORTED_ABIS[0] + ".zip");
                                FileUtils.delFile(context, context.getCacheDir() + File.separator + "universal" + ".zip");
                                FileUtils.delFile(context, context.getCacheDir() + File.separator + "ArkPlanner" + ".zip");
                                String result = getArkPlannerString(context, "{\"聚合剂\":192}", "{\"聚合剂\":0}");
                                dialog.dismiss();
                                //new AlertDialog.Builder(activity).setMessage(result).show();
                                Log.e("TAG", result);
                                //Snackbar.make(view, result.replaceAll("\n", " "), Snackbar.LENGTH_LONG).show();
                                Looper.prepare();
                                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                                Looper.loop();
                            } catch (Exception e) {
                                dialog.dismiss();
                                FileUtils.delFile(context, context.getCacheDir() + File.separator + Build.SUPPORTED_ABIS[0] + ".zip");
                                FileUtils.delFile(context, context.getCacheDir() + File.separator + "universal" + ".zip");
                                FileUtils.delFile(context, context.getCacheDir() + File.separator + "ArkPlanner" + ".zip");
                                if (view == null){
                                    Toast.makeText(context, context.getString(R.string.py_error) + e.getMessage(), Toast.LENGTH_LONG).show();
                                }else {
                                    Snackbar.make(view, context.getString(R.string.py_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }

            @Override
            protected void canceled(@NonNull DownloadTask task) {

            }

            @Override
            protected void error(@NonNull DownloadTask task, @NonNull Exception e) {
                dialog.dismiss();
                if (view == null){
                    Toast.makeText(context, context.getString(R.string.py_error) + e.getMessage(), Toast.LENGTH_LONG).show();
                }else {
                    Snackbar.make(view, context.getString(R.string.py_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                e.printStackTrace();
            }

            @Override
            protected void warn(@NonNull DownloadTask task) {

            }

            @Override
            public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {

            }

            @Override
            public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {

            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
                if (task.equals(task1)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar1.setProgress((int) (((float)currentOffset) / ((float)totalLength) * 100), true);
                    }else {
                        progressBar1.setProgress((int) (((float)currentOffset) / ((float)totalLength) * 100));
                    }
                }else if(task.equals(task2)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar2.setProgress((int) (((float)currentOffset) / ((float)totalLength) * 100), true);
                    }else{
                        progressBar2.setProgress((int) (((float)currentOffset) / ((float)totalLength) * 100));
                    }
                }else if(task.equals(task3)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar3.setProgress((int) (((float)currentOffset) / ((float)totalLength) * 100), true);
                    }else{
                        progressBar3.setProgress((int) (((float)currentOffset) / ((float)totalLength) * 100));
                    }
                }
                //Log.e("", String.valueOf(currentOffset) + ' ' + String.valueOf(totalLength) + " " + ((int) (((float)currentOffset) / ((float)totalLength) * 100)));
            }
        });
    }
    public class Helper{
        Context context;
        String appLib;
        File appFile;
        StarSrvGroupClass srvGroup;
        StarCoreFactory starCore;
        StarServiceClass service;
        StarObjectClass python;
        StarObjectClass pythonSys;
        StarObjectClass pythonPath;
        public Helper(Context context){
            this.context = context;
            loadLibraries(context, DL_LIST);
            appFile = new File(context.getFilesDir() + File.separator + "python");
            appLib = context.getApplicationInfo().nativeLibraryDir;
            StarCoreFactoryPath.StarCoreCoreLibraryPath = appLib;
            StarCoreFactoryPath.StarCoreShareLibraryPath = appLib;
            StarCoreFactoryPath.StarCoreOperationPath = appFile.getPath();
            starCore = StarCoreFactory.GetFactory();
            service = starCore._InitSimple("python", "python", 0, 0);
            srvGroup = (StarSrvGroupClass) service._Get("_ServiceGroup");
            service._CheckPassword(false);
            srvGroup._InitRaw("python36", service);
            python = service._ImportRawContext("python", "", false, "");
            pythonSys = python._GetObject("sys");
            pythonPath = (StarObjectClass) pythonSys._Get("path");
            pythonPath._Call("insert", 0, appFile.getPath()+ File.separator +"python3.6.zip");
            pythonPath._Call("insert", 0, appLib);
            pythonPath._Call("insert", 0, appFile.getPath());
        }
        public String getArkPlannerString (String required, String owned){
            service._DoFile("python", appFile.getPath() + "/main.py", "");
            Object result = python._Call("plan", required, owned);
            //srvGroup._ClearService();
            //Log.d("", String.valueOf(result));
            return (String) result;
        }
    }
    public static String getArkPlannerString (Context context, String required, String owned){
        String appLib;
        File appFile;
        StarSrvGroupClass srvGroup;
        StarCoreFactory starcore;
        StarServiceClass service;
        StarObjectClass python;
        StarObjectClass pythonSys;
        StarObjectClass pythonPath;
        loadLibraries(context, DL_LIST);
        appFile = new File(context.getFilesDir() + File.separator + "python");
        appLib = context.getApplicationInfo().nativeLibraryDir;
        StarCoreFactoryPath.StarCoreCoreLibraryPath = appLib;
        StarCoreFactoryPath.StarCoreShareLibraryPath = appLib;
        StarCoreFactoryPath.StarCoreOperationPath = appFile.getPath();
        if (true){
            starcore = StarCoreFactory.GetFactory();
            service = starcore._InitSimple("python", "python", 0, 0);
            srvGroup = (StarSrvGroupClass) service._Get("_ServiceGroup");
            service._CheckPassword(false);
            srvGroup._InitRaw("python36", service);
            python = service._ImportRawContext("python", "", false, "");
            pythonSys = python._GetObject("sys");
            pythonPath = (StarObjectClass) pythonSys._Get("path");

        }
        pythonPath._Call("insert", 0, appFile.getPath()+ File.separator +"python3.6.zip");
        pythonPath._Call("insert", 0, appLib);
        pythonPath._Call("insert", 0, appFile.getPath());
        service._DoFile("python", appFile.getPath() + "/main.py", "");
        Object result = python._Call("plan", required, owned);
        //service._Exit();
        //srvGroup._ClearService();
        //Log.d("", String.valueOf(result));
        return (String) result;
    }
    public static void loadLibraries(Context context, String[] list){
        for (String name :
                list) {
            Log.e("", name);
            System.load(context.getFilesDir() + File.separator + "python" + File.separator + name);
        }
    }
    public static StarObjectClass init(Context context){
        String appLib;
        File appFile;
        StarSrvGroupClass srvGroup;
        StarCoreFactory starcore;
        StarServiceClass service;
        StarObjectClass python;
        StarObjectClass pythonSys;
        StarObjectClass pythonPath;
        loadLibraries(context, DL_LIST);
        appFile = new File(context.getFilesDir() + File.separator + "python");
        appLib = context.getApplicationInfo().nativeLibraryDir;
        StarCoreFactoryPath.StarCoreCoreLibraryPath = appLib;
        StarCoreFactoryPath.StarCoreShareLibraryPath = appLib;
        StarCoreFactoryPath.StarCoreOperationPath = appFile.getPath();
        starcore = StarCoreFactory.GetFactory();
        service = starcore._InitSimple("python", "python", 0, 0);
        srvGroup = (StarSrvGroupClass) service._Get("_ServiceGroup");
        service._CheckPassword(false);
        srvGroup._InitRaw("python36", service);
        python = service._ImportRawContext("python", "", false, "");
        pythonSys = python._GetObject("sys");
        pythonPath = (StarObjectClass) pythonSys._Get("path");
        pythonPath._Call("insert", 0, appFile.getPath()+ File.separator +"python3.6.zip");
        pythonPath._Call("insert", 0, appLib);
        pythonPath._Call("insert", 0, appFile.getPath());
        service._DoFile("python", appFile.getPath() + "/main.py", "");
        return python;
    }
    public static boolean checkEnvironment(Context context){
        if (context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE).getBoolean("python_finished", false)){
            return true;
        }else {
            return false;
        }
    }
    public static void setupEnvironment(final Context context, final Activity activity, final View snackbarView) {
        if (!PythonUtils.isSupported()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setMessage(R.string.py_unsupported)
                    .show();
            return;
        }
        Log.e(TAG, "setupEnvironment: " + PackageUtils.checkApplication(StaticData.Const.PLANNER_PLUGIN_PACKAGE_NAME, context) + PythonUtils.getPluginVersion(context) + StaticData.Const.PLANNER_PLUGIN_MIN_VERSION);
        if (!PackageUtils.checkApplication(StaticData.Const.PLANNER_PLUGIN_PACKAGE_NAME, context) || PythonUtils.getPluginVersion(context) < StaticData.Const.PLANNER_PLUGIN_MIN_VERSION) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("安装或升级插件")
                    .setMessage("安装或升级插件来启用刷图规划功能")// TODO: 2021/1/21 翻译
                    .setPositiveButton("酷安", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            IntentUtils.openURL("https://www.coolapk.com/apk/263387", activity);
                        }
                    })
                    .setNeutralButton("备用（密码: 9vf7）", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            IntentUtils.openURL("https://wwe.lanzous.com/b01c1mm4d", activity);
                        }
                    })
                    .setNegativeButton("暂时不要", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE).edit().putBoolean("disable_planner", true).apply();
                            Snackbar.make(snackbarView, "日后可以从设置中重新启用刷图规划功能", Snackbar.LENGTH_LONG).show();

                        }
                    })
            .show();
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(R.layout.dialog_python_init)
                .setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String CACHE_PATH = context.getCacheDir().toString();
                final String ASSETS_PATH = CACHE_PATH + File.separator + "plugin" + File.separator + "assets";
                try {
                    PackageManager pm = context.getPackageManager();
                    String path = pm.getApplicationInfo("com.ssyanhuo.akrnightshelper_plannerplugin", 0).sourceDir;
                    FileUtils.copyFile(context, path, CACHE_PATH + File.separator + "plugin.apk");
                    ZipUtils.UnZipFolder(CACHE_PATH + File.separator + "plugin.apk", CACHE_PATH + File.separator + "plugin");
                    ZipUtils.UnZipFolder(ASSETS_PATH + File.separator + Build.SUPPORTED_ABIS[0] + ".zip", context.getFilesDir() + File.separator + "python");
                    ZipUtils.UnZipFolder(ASSETS_PATH + File.separator + "universal" + ".zip", context.getFilesDir() + File.separator + "python");
                    ZipUtils.UnZipFolder(ASSETS_PATH + File.separator + "ArkPlanner" + ".zip", context.getFilesDir() + File.separator + "python");
                    ZipUtils.UnZipFolder(context.getFilesDir() + File.separator + "python" + File.separator + "numpy" + ".zip", context.getFilesDir() + File.separator + "python" + File.separator + "numpy");
                    ZipUtils.UnZipFolder(context.getFilesDir() + File.separator + "python" + File.separator + "scipy" + ".zip", context.getFilesDir() + File.separator + "python" + File.separator + "scipy");
                    FileUtils.delFile(context, CACHE_PATH + File.separator + "plugin.apk");
                    FileUtils.deleteDirectory(CACHE_PATH + File.separator + "plugin");
                    FileUtils.copyFileFromAssets(context, context.getFilesDir().getPath() + File.separator + "python" + File.separator + "data", "formula.json");
                    FileUtils.copyFileFromAssets(context, context.getFilesDir().getPath() + File.separator + "python" + File.separator + "data", "matrix.json");
                    Snackbar.make(snackbarView, "组件初始化成功，请重新启动服务", Snackbar.LENGTH_LONG).show();
                    context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE).edit().putBoolean("python_finished", true).apply();
                    dialog.dismiss();
                } catch (Exception e) {
                    Snackbar.make(snackbarView, context.getString(R.string.py_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }
//    public static void setupEnvironmentOld(final Context context, final Activity activity) {
//        if (!PythonUtils.isAbiSupported()) {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
//            dialogBuilder.setMessage(R.string.py_unsupported_arch)
//                    .show();
//        }
//        String baseUrl = context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE).getString("update_site", "0");
//        if (baseUrl.equals("1")) {
//            baseUrl = "https://ssyanhuo.github.io/Arknights-Helper-Dependencies/";
//        } else {
//            baseUrl = "http://ssyanhuo.gitee.io/arknights-helper-dependencies/";
//        }
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
//        LinearLayout pythonDownloader = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_python_downloader, null);
//        final ProgressBar progressBar1 = pythonDownloader.findViewById(R.id.pythonDownloader_progressBar_1);
//        final ProgressBar progressBar2 = pythonDownloader.findViewById(R.id.pythonDownloader_progressBar_2);
//        final ProgressBar progressBar3 = pythonDownloader.findViewById(R.id.pythonDownloader_progressBar_3);
//        dialogBuilder.setTitle(R.string.py_download)
//                .setCancelable(false)
//                .setView(pythonDownloader);
//        final AlertDialog dialog = dialogBuilder.create();
//        dialog.show();
//
//        final DownloadTask task1 = new DownloadTask.Builder(baseUrl + Build.SUPPORTED_ABIS[0] + ".zip", context.getCacheDir())
//                .setFilename(Build.SUPPORTED_ABIS[0] + ".zip")
//                .setPassIfAlreadyCompleted(true)
//                .build();
//        final DownloadTask task2 = new DownloadTask.Builder(baseUrl + "universal" + ".zip", context.getCacheDir())
//                .setFilename("universal" + ".zip")
//                .setPassIfAlreadyCompleted(true)
//                .build();
//        final DownloadTask task3 = new DownloadTask.Builder(baseUrl + "ArkPlanner" + ".zip", context.getCacheDir())
//                .setFilename("ArkPlanner" + ".zip")
//                .setPassIfAlreadyCompleted(true)
//                .build();
//        DownloadTask[] list = {task1, task2, task3};
//        DownloadTask.enqueue(list, new DownloadListener3() {
//            int completedCount = 0;
//
//            @Override
//            protected void started(@NonNull DownloadTask task) {
//
//            }
//
//            @Override
//            protected void completed(@NonNull DownloadTask task) {
//                completedCount++;
//                if (task.equals(task1)) {
//                    progressBar1.setIndeterminate(true);
//                } else if (task.equals(task2)) {
//                    progressBar2.setIndeterminate(true);
//                } else if (task.equals(task3)) {
//                    progressBar3.setIndeterminate(true);
//                }
//                if (completedCount >= 3) {
//                    Thread thread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                ZipUtils.UnZipFolder(context.getCacheDir() + File.separator + Build.SUPPORTED_ABIS[0] + ".zip", context.getFilesDir() + File.separator + "python");
//                                ZipUtils.UnZipFolder(context.getCacheDir() + File.separator + "universal" + ".zip", context.getFilesDir() + File.separator + "python");
//                                ZipUtils.UnZipFolder(context.getCacheDir() + File.separator + "ArkPlanner" + ".zip", context.getFilesDir() + File.separator + "python");
//                                ZipUtils.UnZipFolder(context.getFilesDir() + File.separator + "python" + File.separator + "numpy" + ".zip", context.getFilesDir() + File.separator + "python" + File.separator + "numpy");
//                                ZipUtils.UnZipFolder(context.getFilesDir() + File.separator + "python" + File.separator + "scipy" + ".zip", context.getFilesDir() + File.separator + "python" + File.separator + "scipy");
//                                FileUtils.delFile(context, context.getCacheDir() + File.separator + Build.SUPPORTED_ABIS[0] + ".zip");
//                                FileUtils.delFile(context, context.getCacheDir() + File.separator + "universal" + ".zip");
//                                FileUtils.delFile(context, context.getCacheDir() + File.separator + "ArkPlanner" + ".zip");
//                                //String result = getArkPlannerString(context, "{\"聚合剂\":192}", "{\"聚合剂\":0}");
//                                dialog.dismiss();
//                                //new AlertDialog.Builder(activity).setMessage(result).show();
//                                //Log.e("TAG", result);
//                                Snackbar.make(activity.getWindow().getDecorView().getRootView().findViewById(R.id.fab), "组件初始化成功，请重新启动服务", Snackbar.LENGTH_LONG).show();
//                                context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE).edit().putBoolean("python_finished", true).apply();
//                            } catch (Exception e) {
//                                dialog.dismiss();
//                                FileUtils.delFile(context, context.getCacheDir() + File.separator + Build.SUPPORTED_ABIS[0] + ".zip");
//                                FileUtils.delFile(context, context.getCacheDir() + File.separator + "universal" + ".zip");
//                                FileUtils.delFile(context, context.getCacheDir() + File.separator + "ArkPlanner" + ".zip");
//                                Snackbar.make(activity.getWindow().getDecorView().getRootView().findViewById(R.id.fab), context.getString(R.string.py_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    thread.start();
//                }
//            }
//
//            @Override
//            protected void canceled(@NonNull DownloadTask task) {
//
//            }
//
//            @Override
//            protected void error(@NonNull DownloadTask task, @NonNull Exception e) {
//                dialog.dismiss();
//                Snackbar.make(activity.getWindow().getDecorView().getRootView().findViewById(R.id.fab), context.getString(R.string.py_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
//                e.printStackTrace();
//            }
//
//            @Override
//            protected void warn(@NonNull DownloadTask task) {
//
//            }
//
//            @Override
//            public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {
//
//            }
//
//            @Override
//            public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {
//
//            }
//
//            @Override
//            public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
//                if (task.equals(task1)) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        progressBar1.setProgress((int) (((float) currentOffset) / ((float) totalLength) * 100), true);
//                    } else {
//                        progressBar1.setProgress((int) (((float) currentOffset) / ((float) totalLength) * 100));
//                    }
//                } else if (task.equals(task2)) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        progressBar2.setProgress((int) (((float) currentOffset) / ((float) totalLength) * 100), true);
//                    } else {
//                        progressBar2.setProgress((int) (((float) currentOffset) / ((float) totalLength) * 100));
//                    }
//                } else if (task.equals(task3)) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        progressBar3.setProgress((int) (((float) currentOffset) / ((float) totalLength) * 100), true);
//                    } else {
//                        progressBar3.setProgress((int) (((float) currentOffset) / ((float) totalLength) * 100));
//                    }
//                }
//                //Log.e("", String.valueOf(currentOffset) + ' ' + String.valueOf(totalLength) + " " + ((int) (((float)currentOffset) / ((float)totalLength) * 100)));
//            }
//        });
//    }

}
