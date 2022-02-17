package com.ssyanhuo.arknightshelper.activity;

import android.annotation.SuppressLint;
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
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ssyanhuo.arknightshelper.service.ScreenCaptureService;
import com.ssyanhuo.arknightshelper.utils.ScreenUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenCaptureActivity extends AppCompatActivity {

    final int REQUEST_CODE_SCREEN_CAPTURE = 0;
    private MediaProjectionManager mediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent,0);

    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(this, ScreenCaptureService.class);
                intent.putExtra("requestCode", requestCode);
                intent.putExtra("data", data);
                startForegroundService(intent);
            }else{
                final int screenDensityDpi = ScreenUtils.getDensityDpi(this);
                final int screenWidth = ScreenUtils.getScreenWidth(this);
                final int screenHeight = ScreenUtils.getScreenHeight(this);
                @SuppressLint("WrongConstant") ImageReader imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
                final MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                final VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", screenWidth, screenHeight, screenDensityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);
                imageReader.setOnImageAvailableListener(reader -> {

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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },1000);

                }, null);
            }
        }else if (requestCode == REQUEST_CODE_SCREEN_CAPTURE){
            Toast.makeText(this, "出现错误", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}