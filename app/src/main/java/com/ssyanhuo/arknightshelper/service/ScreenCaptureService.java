package com.ssyanhuo.arknightshelper.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.misc.ServiceNotification;
import com.ssyanhuo.arknightshelper.utils.ScreenUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenCaptureService extends Service {
    public ScreenCaptureService() {

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder builder = new Notification.Builder(this);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("notification", getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
            builder
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_title_recording))
                    .setChannelId("notification");

        Notification notification = builder.build();
        startForeground(2, ServiceNotification.build(getApplicationContext(), 2));
        final int resultCode = intent.getIntExtra("resultCode", -1);
        final Intent data = intent.getParcelableExtra("data");
        final int screenDensityDpi = ScreenUtils.getDensityDpi(this);
        final int screenWidth = ScreenUtils.getScreenWidth(this);
        final int screenHeight = ScreenUtils.getScreenHeight(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("WrongConstant") ImageReader imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
                final MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                final MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                final VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", screenWidth, screenHeight, screenDensityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);
                imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(final ImageReader reader) {

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Image image = reader.acquireLatestImage();
                                    Image.Plane[] planes = image.getPlanes();
                                    ByteBuffer byteBuffer = planes[0].getBuffer();
                                    int pixelStride = planes[0].getPixelStride();
                                    int rowStride = planes[0].getRowStride();
                                    int rowPadding = rowStride - pixelStride * screenWidth;
                                    int width = screenWidth + rowPadding / pixelStride;
                                    int height = screenHeight;
                                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
                                    bitmap.copyPixelsFromBuffer(byteBuffer);
                                    reader.close();
                                    String compressedPath = getExternalCacheDir() + File.separator + "ScreenCapture.jpg.processing";
                                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(compressedPath));
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                                    outputStream.flush();
                                    outputStream.close();
                                    virtualDisplay.release();
                                    mediaProjection.stop();
                                    File oldFile = new File(compressedPath);
                                    File newFile = new File(getExternalCacheDir() + File.separator + "ScreenCapture.jpg");
                                    oldFile.renameTo(newFile);
                                    onDestroy();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    onDestroy();
                                }
                            }
                        },1000);

                    }
                }, null);
            }
        }, 200);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}