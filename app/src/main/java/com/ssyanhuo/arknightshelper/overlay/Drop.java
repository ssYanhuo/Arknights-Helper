package com.ssyanhuo.arknightshelper.overlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.utiliy.DpUtiliy;
import com.ssyanhuo.arknightshelper.utiliy.FileUtility;
import com.ssyanhuo.arknightshelper.widget.ChildScrollView;
import com.ssyanhuo.arknightshelper.widget.LineWrapLayout;
import com.ssyanhuo.arknightshelper.widget.TableItem;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Drop {
    final static String TAG = "Material";
    final static String BASE_URL = "https://penguin-stats.io/PenguinStats";
    final static String API_MATRIX = "/api/result/matrix";
    final static String API_ITEMS = "/api/items";
    final static String API_STAGES = "/api/stages";
    JSONObject data_matrix;
    JSONArray data_items;
    JSONArray data_stages;
    Context applicationContext;
    View contentView;
    Handler handler;
    View updater;
    ScrollView parentView;
    ArrayList<RadioButton> radioButtons = new ArrayList<>();
    Map<String, String> stageMap = new HashMap<>();
    Map<String, Integer> costMap = new HashMap<>();
    boolean isAltSelector = false;

    public void init(final Context context, View view){
        applicationContext = context;
        contentView = view;
        handler = new Handler();

        try{
            data_matrix = JSON.parseObject(FileUtility.readFile("matrix.json", applicationContext));
            //下面两个是数组形式
            data_items = JSON.parseArray(FileUtility.readFile("items.json", applicationContext));
            data_stages = JSON.parseArray(FileUtility.readFile("stages.json", applicationContext));
        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, String.valueOf(e));
            goUpdate();
            return;
        }

        for (int i =0; i < data_stages.size(); i++){
            JSONObject object = data_stages.getJSONObject(i);
            stageMap.put(object.getString("stageId"), object.getString("code"));
            costMap.put(object.getString("stageId"), object.getInteger("apCost"));
        }
        getAllRadioButtons(contentView);
        for (int i = 0; i < radioButtons.size(); i++){
            radioButtons.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        for (int j = 0; j < radioButtons.size(); j++){
                            radioButtons.get(j).setChecked(false);
                        }
                        buttonView.setChecked(true);
                        Drawable[] drawables = new Drawable[2];
                        PaintDrawable paintDrawable = new PaintDrawable(Color.argb(127, 255,255,255));
                        paintDrawable.setCornerRadius(8);
                        drawables[0] = paintDrawable;
                        drawables[1] = buttonView.getBackground();
                        LayerDrawable layerDrawable = new LayerDrawable(drawables);
                        buttonView.setBackground(layerDrawable);
                    }else {
                        LayerDrawable layerDrawable = (LayerDrawable) buttonView.getBackground();
                        buttonView.setBackground(layerDrawable.getDrawable(1)); }
                    int item = Integer.parseInt(buttonView.getTag().toString());
                    ArrayList<JSONObject> result = getResult(item);
                    showResult(item, result);
                }
            });
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.drop_desc_part_1) + " " + applicationContext.getString(R.string.drop_desc_part_4));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Log.e(TAG, "!23");
                goUpdate();
            }
        };
        spannableStringBuilder.setSpan(clickableSpan, spannableStringBuilder.length() - applicationContext.getString(R.string.drop_desc_part_4).length(), spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView)contentView.findViewById(R.id.drop_description)).setText(spannableStringBuilder);
        ((TextView)contentView.findViewById(R.id.drop_description)).setMovementMethod(LinkMovementMethod.getInstance());
    }


    public ArrayList<JSONObject> getResult(int item){
        ArrayList<JSONObject> result = new ArrayList<>();
        JSONArray matrix = data_matrix.getJSONArray("matrix");

        for (int i= 0; i < matrix.size(); i++){
            int id;
            try {//部分id并不是int
                id = matrix.getJSONObject(i).getInteger("itemId");
            }catch (NumberFormatException e){
                continue;
            }
            if (id == item){
                JSONObject object = matrix.getJSONObject(i);
                float cost = costMap.get(object.getString("stageId")) * ((float)object.getInteger("times") / (float)object.getInteger("quantity"));
                object.put("cost", String.valueOf(cost));
                result.add(object);
            }
        }
        Collections.sort(result, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                float e1 = o1.getFloat("cost");
                float e2 = o2.getFloat("cost");
                if (e1 > e2){return 1;}
                else if(e1 == e2){return 0;}
                else if(e1 < e2){return -1;}
                return 0;
            }
        });
        return result;
    }

    public void showResult(int item, ArrayList<JSONObject> result){
        ArrayList<String> stages = new ArrayList<>();
        ArrayList<Integer> times = new ArrayList<>();
        ArrayList<Integer> quantities = new ArrayList<>();
        ArrayList<Float> costs = new ArrayList<>();
        LinearLayout resultLayout = contentView.findViewById(R.id.drop_result_content);
        resultLayout.removeAllViews();
        TableItem tableTitle = new TableItem(applicationContext, "关卡", "提交次数", "总共获得", "单个理智");
        resultLayout.addView(tableTitle);
        for (int i = 0; i < result.size(); i++){
            JSONObject object = result.get(i);
            if (object.getInteger("quantity") == 0){
                continue;
            }
            String stageId = object.getString("stageId");
            String stage = stageMap.get(stageId);
            int time = object.getInteger("times");
            int quantity = object.getInteger("quantity");
            float cost = object.getFloat("cost");
            stages.add(stage);
            times.add(time);
            quantities.add(quantity);
            costs.add(cost);
            TableItem tableItem = new TableItem(applicationContext, stage, time, quantity, cost);
            resultLayout.addView(tableItem);
        }
        if (stages.size() == 0){
            resultLayout.removeAllViews();
            TextView textView =new TextView(applicationContext);
            textView.setText("并没有找到结果");
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        setAltSelector();
    }

    public void setAltSelector(){
        if (!isAltSelector){
            ScrollView scrollView = contentView.findViewById(R.id.drop_selector_scroll);
            ViewGroup.LayoutParams params = scrollView.getLayoutParams();
            params.height = DpUtiliy.dip2px(applicationContext, 128);
            scrollView.setLayoutParams(params);
            isAltSelector = true;
        }
    }

    public  void getAllRadioButtons(View view){
        ViewGroup viewGroup = (ViewGroup)view;
        for(int i = 0; i < viewGroup.getChildCount(); i++){
            if(viewGroup.getChildAt(i) instanceof RadioButton){
                radioButtons.add((RadioButton) viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LineWrapLayout){
                getAllRadioButtons(viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LinearLayout){
                getAllRadioButtons(viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof ScrollView){
                getAllRadioButtons(viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof ChildScrollView){
                getAllRadioButtons(viewGroup.getChildAt(i));
            }
        }
    }
    public void goUpdate(){
        parentView = (ScrollView) contentView.getParent();
        updater = LayoutInflater.from(applicationContext).inflate(R.layout.content_drop_updater, null);
        parentView.removeAllViews();
        parentView.addView(updater);
        updater.findViewById(R.id.drop_updater_update).setVisibility(View.VISIBLE);
        updater.findViewById(R.id.drop_updater_progress).setVisibility(View.GONE);
        updater.findViewById(R.id.drop_updater_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new UpdateRunnable()).start();
            }
        });
    }
    class UpdateRunnable implements Runnable{

        @Override
        public void run() {
            try {

                Runnable runnable = new Runnable(){

                    @Override
                    public void run() {
                        updater.findViewById(R.id.drop_updater_update).setVisibility(View.GONE);
                        updater.findViewById(R.id.drop_updater_progress).setVisibility(View.VISIBLE);

                    }
                };
                handler.post(runnable);
                FileUtility.writeFile(URLRequest(BASE_URL + API_MATRIX), "matrix.json", applicationContext);
                FileUtility.writeFile(URLRequest(BASE_URL + API_ITEMS), "items.json", applicationContext);
                FileUtility.writeFile(URLRequest(BASE_URL + API_STAGES), "stages.json", applicationContext);
                data_matrix = JSON.parseObject(FileUtility.readFile("matrix.json", applicationContext));
                //下面两个是数组形式
                data_items = JSON.parseArray(FileUtility.readFile("items.json", applicationContext));
                data_stages = JSON.parseArray(FileUtility.readFile("stages.json", applicationContext));
                Runnable runnable1 = new Runnable(){

                    @Override
                    public void run() {
                        parentView.removeAllViews();
                        parentView.addView(contentView);
                        init(applicationContext, contentView);
                    }
                };
                handler.post(runnable1);
            } catch (Exception e) {

                Log.e(TAG, String.valueOf(e));
            }
        }
        private String URLRequest(String site) throws Exception {
            URL url = new URL(site);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0,len);
            }
            inputStream.close();
            byte[] data = outputStream.toByteArray();
            return new String(data, StandardCharsets.UTF_8);
        }
    }
}
