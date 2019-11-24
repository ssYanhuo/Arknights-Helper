package com.ssyanhuo.arknightshelper.utiliy;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.ssyanhuo.arknightshelper.staticdata.StaticData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


public class OCRUtility {
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
    @Deprecated
    public static ArrayList<String> getOCRResult(Context context, String path) {
        //final ArrayList<String> wordList = new ArrayList<>();
        final ArrayList<String> tagList = new ArrayList<>();
        GeneralParams params = new GeneralParams();
        params.setDetectDirection(true);
        params.setImageFile(new File(path));
        OCR.getInstance(context).recognizeGeneral(params, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult generalResult) {
                for (WordSimple wordSimple : generalResult.getWordList()) {
                    //wordList.add(wordSimple.getWords());
                    for (String tag : StaticData.HR.tagList){
                        if (wordSimple.getWords().contains(tag)){
                            tagList.add(wordSimple.getWords());
                        }
                    }
                }
                //wordList.retainAll(Arrays.asList(StaticData.HR.tagList));
                Log.i(TAG, "OCR result: " + tagList.toString());
            }
            @Override
            public void onError(OCRError ocrError) {
                ocrError.printStackTrace();
            }
        });
        return tagList;
    }
}
