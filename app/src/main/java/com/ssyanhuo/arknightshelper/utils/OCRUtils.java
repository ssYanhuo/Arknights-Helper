package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.util.Log;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;


public class OCRUtils {
    static final String TAG = "OCR";
    public static void init(Context context){
        OCR.getInstance(context).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                Log.i(TAG, "OCR init succeed!");
            }

            @Override
            public void onError(OCRError ocrError) {
                ocrError.printStackTrace();
            }
        },context);
    }
}
