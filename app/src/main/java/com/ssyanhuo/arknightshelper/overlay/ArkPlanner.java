package com.ssyanhuo.arknightshelper.overlay;

/*
* ArkPlanner by ycremar at https://github.com/ycremar/ArkPlanner
* Java edition by ssYanhuo
* 基于 ycremar 的 ArkPlanner 工具编写，使其能在 Android 设备上运行
* 算法完全来自原作者，未经过任何修改
* */

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.utiliy.JSONUtility;

public class ArkPlanner {
    int filter_freq;
    String[] filter_stages;
    String url_stats;
    String url_rules;
    String path_stats;
    String path_rules;
    JSONObject material_probs;
    JSONObject convertion_rules;
    public void init(Context applicationContext){
        /*Object initialization.
        Args:
            filter_freq: int or None. The lowest frequency that we consider.
                No filter will be applied if None.
            url_stats: string. url to the dropping rate stats data.
            url_rules: string. url to the composing rules data.
            path_stats: string. local path to the dropping rate stats data.
            path_rules: string. local path to the composing rules data.
         */
        filter_freq = 20;
        filter_stages = null;
        url_stats = "result/matrix?show_stage_details=true&show_item_details=true";
        url_rules = "formula";
        path_stats = "data/matrix.json";
        path_rules = "data/formula.json";
        try{
            material_probs = JSONUtility.getJSONObject(applicationContext, JSONUtility.getJSONString(applicationContext, path_stats));
            convertion_rules = JSONUtility.getJSONObject(applicationContext, JSONUtility.getJSONString(applicationContext, path_rules));
        }catch (Exception e){

        }

    }
}
