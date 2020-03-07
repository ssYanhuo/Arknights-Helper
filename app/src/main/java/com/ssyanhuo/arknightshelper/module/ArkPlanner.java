package com.ssyanhuo.arknightshelper.module;

/*
* ArkPlanner by ycremar at https://github.com/ycremar/ArkPlanner
* Java edition by ssYanhuo
* 基于 ycremar 的 ArkPlanner 工具编写，使其能在 Android 设备上运行
* 算法完全来自原作者，未经过任何修改
* */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.Matrix;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.SettingsActivity;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.JSONUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class ArkPlanner {
    int filter_freq;
    String[] filter_stages;
    String url_stats;
    String url_rules;
    String path_stats;
    String path_rules;
    JSONObject material_probs;
    JSONObject convertion_rules;
    SharedPreferences preferences;
    Context context;
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
        url_stats = "result/matrixTheme?show_stage_details=true&show_item_details=true";
        url_rules = "formula";
        path_stats = "data/matrix.json";
        path_rules = "data/formula.json";
        preferences = applicationContext.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE);
        try{
            material_probs = JSONUtils.getJSONObject(applicationContext, FileUtils.readData(path_stats, applicationContext));
            convertion_rules = JSONUtils.getJSONObject(applicationContext, JSONUtils.getJSONString(applicationContext, path_rules));
        }catch (Exception e){
            applicationContext.startActivity(new Intent(applicationContext, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            Toast.makeText(applicationContext, R.string.drop_updater_desc, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
    }

    public void refresh(){

    }


    public ArrayList<Object> _pre_processing(JSONObject material_probs, JSONObject convertion_rules){
        /*Compute costs, convertion rules and items probabilities from requested dictionaries.
        Args:
            material_probs: List of dictionaries recording the dropping info per stage per item.
                Keys of instances: ["itemID", "times", "itemName", "quantity", "apCost", "stageCode", "stageID"].
            convertion_rules: List of dictionaries recording the rules of composing.
                Keys of instances: ["id", "name", "level", "source", "madeof"].
         */
        Map<String, String> additional_items = new HashMap<>();
        additional_items.put("30135", "D32钢");
        additional_items.put("30125", "双极纳米片");
        additional_items.put("30115", "聚合剂");
        double exp_unit = 200 * 30.0 / 7400;
        double gold_unit = 0.004;
        Map<String, Double> exp_worths = new HashMap<>();
        exp_worths.put("2001", exp_unit);
        exp_worths.put("2002", exp_unit * 2);
        exp_worths.put("2003", exp_unit * 5);
        exp_worths.put("2004", exp_unit * 10);
        Map<String, Double> gole_worths = new HashMap<>();
        gole_worths.put("3003", gold_unit * 500);

        Map<String, String> item_dct = new HashMap<>();
        Map<String, String> stage_dct = new HashMap<>();
        JSONArray matrix = material_probs.getJSONArray("matrix");
        for (int i = 0; i <= matrix.size(); i++){
            JSONObject dct = matrix.getJSONObject(i);
            JSONObject item = dct.getJSONObject("item");
            String itemId = item.getString("itemId");
            String name = item.getString("name");
            JSONObject stage = dct.getJSONObject("stage");
            String code = stage.getString("code");
            item_dct.put(itemId, name);
            stage_dct.put(code, code);
        }
        item_dct.putAll(additional_items);

        ArrayList<String> item_array = new ArrayList<>();
        ArrayList<Float> item_id_array = new ArrayList<>();
        for(Map.Entry<String, String> entry : item_dct.entrySet()){
            try {
                float k = Float.parseFloat(entry.getKey());
                item_array.add(entry.getValue());
                item_id_array.add(k);
            }catch (Exception ignored){
            }
        }
        
        return null;
    }
}
