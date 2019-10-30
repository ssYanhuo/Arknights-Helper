package com.ssyanhuo.arknightshelper.overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.utiliy.JsonUtility;
import com.ssyanhuo.arknightshelper.widget.LineWrapLayout;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hr {
    private final String TAG = "Hr";
    private EasyPopup easyPopup;
    ArrayList<CheckBox> checkBoxes;
    ArrayList<String> selectedStar;
    ArrayList<String> selectedQualification;
    ArrayList<String> selectedPosition;
    ArrayList<String> selectedSex;
    ArrayList<String> selectedType;
    ArrayList<String> selectedTag;
    View contentView;
    String hrJson;
    Context applicationContext;
    SharedPreferences sharedPreferences;
    boolean fuzzy;
    final int MODE_EXACT = 0;
    final int MODE_FUZZY = 1;
    ArrayList<String> tmpArr = new ArrayList<>();
    ArrayList<String> combinations = new ArrayList<>();

    public void init (Context context, View view){
        contentView = view;
        applicationContext = context;
        hideResult(contentView.findViewById(R.id.hr_result_content));
        sharedPreferences = applicationContext.getSharedPreferences("Config", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("queryMethodSelected", false)){
            selectQueryMethod();
        }else if(sharedPreferences.getBoolean("fuzzyQuery", false)){
            fuzzy = true;
        }
        checkBoxes = new ArrayList<>();
        selectedStar = new ArrayList<>();
        selectedQualification = new ArrayList<>();
        selectedPosition = new ArrayList<>();
        selectedSex = new ArrayList<>();
        selectedType = new ArrayList<>();
        selectedTag = new ArrayList<>();
        hrJson = JsonUtility.getJsonString(context, "data/hr.json");
        getAllCheckboxes(checkBoxes, view);
        for(int i = 0; i < checkBoxes.size(); i++){
            checkBoxes.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    getSelectedItems();
                    if (!isFewerThanLimit()){
                        compoundButton.setChecked(false);
                    }
                    getSelectedItems();
                    if(fuzzy){getResultFuzzy();}
                    else {getResultExact();}

                    if(selectedStar.size() + selectedQualification.size() + selectedPosition.size() + selectedSex.size() + selectedType.size() + selectedTag.size() == 0){
                        hideResult(contentView.findViewById(R.id.hr_result_content));
                    }
                }
            });
        }
        if(!fuzzy){
            changeQueryMethod(MODE_EXACT);
        }else {
            changeQueryMethod(MODE_FUZZY);
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_result_title_part_2));
        spannableStringBuilder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                LinearLayout resultLayout = contentView.findViewById(R.id.hr_result);
                resultLayout.removeAllViews();
                for(int i = 0; i < checkBoxes.size(); i++){
                    CheckBox checkBox = checkBoxes.get(i);
                    checkBox.setChecked(false);
                }
            }
        }, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView)contentView.findViewById(R.id.hr_result_title)).append(" ");
        ((TextView)contentView.findViewById(R.id.hr_result_title)).append(spannableStringBuilder);
        ((TextView)contentView.findViewById(R.id.hr_result_title)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    public  void getAllCheckboxes(ArrayList<CheckBox> checkBoxes, View view){
        ViewGroup viewGroup = (ViewGroup)view;
        for(int i = 0; i < viewGroup.getChildCount(); i++){
            if(viewGroup.getChildAt(i) instanceof CheckBox){
                checkBoxes.add((CheckBox) viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof HorizontalScrollView){
                getAllCheckboxes(checkBoxes, viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LinearLayout){
                getAllCheckboxes(checkBoxes, viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LineWrapLayout){
                getAllCheckboxes(checkBoxes, viewGroup.getChildAt(i));
            }
        }
    }
    public void getSelectedItems(){
        selectedStar.clear();
        selectedQualification.clear();
        selectedPosition.clear();
        selectedSex.clear();
        selectedType.clear();
        selectedTag.clear();
        for(int i = 0; i < checkBoxes.size(); i++){
            CheckBox checkBox = checkBoxes.get(i);
            if (!checkBox.isChecked()){continue;}
            View parentView = (View)checkBox.getParent();
            String item = String.valueOf(parentView.getTag());
            Log.i(TAG, "Selceted:" + checkBox.getTag());
            switch (item){
                case "star":
                    selectedStar.add(String.valueOf(checkBox.getTag()));
                    break;
                case "qualification":
                    selectedQualification.add(String.valueOf(checkBox.getTag()));
                    break;
                case "position":
                    selectedPosition.add(String.valueOf(checkBox.getTag()));
                    break;
                case "sex":
                    selectedSex.add(String.valueOf(checkBox.getTag()));
                    break;
                case "type":
                    selectedType.add(String.valueOf(checkBox.getTag()));
                    break;
                case "tag":
                    selectedTag.add(String.valueOf(checkBox.getTag()));
                    break;
                default:
                    break;
            }
        }
    }

    public void getAllCombinations(ArrayList<String> strings){
        for(int i = 0; i <= strings.size(); i++){
            combine(0, strings.size(), strings);
        }
    }

    public void combine(int index, int k, ArrayList<String> arr) {
        if(k == 1){
            for (int i = index; i < arr.size(); i++) {
                tmpArr.add(arr.get(i));
                combinations.add(tmpArr.toString());
                tmpArr.remove((Object)arr.get(i));
            }
        }else if(k > 1){
            for (int i = index; i <= arr.size() - k; i++) {
                tmpArr.add(arr.get(i)); //tmpArr都是临时性存储一下
                combine(i + 1,k - 1, arr); //索引右移，内部循环，自然排除已经选择的元素
                tmpArr.remove((Object)arr.get(i)); //tmpArr因为是临时存储的，上一个组合找出后就该释放空间，存储下一个元素继续拼接组合了
            }
        }
    }

    public boolean isFewerThanLimit(){
        int num = selectedQualification.size() + selectedPosition.size() + selectedSex.size() + selectedType.size() + selectedTag.size();
        if(fuzzy){return (num <= 5);}
        else {return (num <= 3);}
    }


    public void getResultExact(){
        ArrayList<JSONObject> result = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(hrJson);
        selectedTag.addAll(selectedQualification);
        selectedTag.addAll(selectedPosition);
        for (int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ArrayList<String> type = new ArrayList<>();
            ArrayList<String> sex = new ArrayList<>();
            String name = jsonObject.getString("name");
            type.add(jsonObject.getString("type"));
            sex.add(jsonObject.getString("sex"));
            String star = jsonObject.getString("level");//星级可以多选，故在集合中查找
            JSONArray tags = jsonObject.getJSONArray("tags");
            ArrayList<String> tag = (ArrayList<String>) JSONArray.parseArray(tags.toString(), String.class);
            tag.retainAll(selectedTag);
            type.retainAll(selectedType);
            sex.retainAll(selectedSex);
            boolean hidden = jsonObject.getBoolean("hidden");
            //Log.e(TAG, name + type + star + String.valueOf(tag) + String.valueOf(hidden));
            boolean typePaired = selectedType.size() == type.size() || selectedType.size() == 0;
            boolean starPaired = selectedStar.contains(star) || selectedStar.size() == 0;//星级允许多选
            boolean sexPaired = selectedSex.size() == sex.size() ||  selectedSex.size() == 0;
            boolean tagPaired = selectedTag.size() == tag.size() || selectedTag.size() == 0;
            if(typePaired && starPaired && sexPaired && tagPaired && !hidden){
                result.add(jsonObject);
                Log.i(TAG, "Found:" + name);
            }
        }
        Collections.reverse(result);//高星级放前面
        showResultExact(result);
    }

    //模糊查询
    public void getResultFuzzy(){
        ArrayList<JSONObject> result = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(hrJson);
        selectedTag.addAll(selectedQualification);
        selectedTag.addAll(selectedPosition);
        for (int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ArrayList<String> type = new ArrayList<>();
            ArrayList<String> sex = new ArrayList<>();
            String name = jsonObject.getString("name");
            type.add(jsonObject.getString("type"));
            sex.add(jsonObject.getString("sex"));
            String star = jsonObject.getString("level");//星级可以多选，故在集合中查找
            JSONArray tags = jsonObject.getJSONArray("tags");
            ArrayList<String> tag = (ArrayList<String>) JSONArray.parseArray(tags.toString(), String.class);
            tag.retainAll(selectedTag);
            type.retainAll(selectedType);
            sex.retainAll(selectedSex);
            boolean hidden = jsonObject.getBoolean("hidden");
            boolean typePaired = type.size() != 0;
            boolean starPaired = selectedStar.contains(star) || selectedStar.size() == 0;//星级允许多选
            boolean sexPaired = sex.size() != 0;
            boolean tagPaired = tag.size() != 0;
            if((typePaired || sexPaired || tagPaired) && !hidden && starPaired){
                JSONArray matchedTags = new JSONArray();
                matchedTags.addAll(JSONArray.parseArray(JSON.toJSONString(type)));
                matchedTags.addAll(JSONArray.parseArray(JSON.toJSONString(sex)));
                matchedTags.addAll(JSONArray.parseArray(JSON.toJSONString(tag)));
                List list = JSONArray.parseArray(matchedTags.toJSONString());
                Collections.sort(list);
                matchedTags = JSONArray.parseArray(list.toString());
                jsonObject.put("matchedTags", matchedTags);
                result.add(jsonObject);
                Log.i(TAG, "Found:" + name);
            }
        }
        Collections.reverse(result);//高星级放前面
        showResultFuzzy(result);

    }

    public void hideResult(View view){
        view.setVisibility(View.GONE);
    }

    public void showResultExact(ArrayList<JSONObject> result){
        LinearLayout linearLayout = contentView.findViewById(R.id.hr_result_content);
        ScrollView scrollView = (ScrollView) linearLayout.getParent().getParent().getParent();
        LinearLayout resultLayout = linearLayout.findViewById(R.id.hr_result);
        resultLayout.removeAllViews();
        resultLayout.setOrientation(LinearLayout.HORIZONTAL);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(applicationContext);
        resultLayout.addView(horizontalScrollView);
        LinearLayout linearLayout1 = new LinearLayout(applicationContext);
        horizontalScrollView.addView(linearLayout1);
        linearLayout.setVisibility(View.VISIBLE);
        if(result.size() == 0){
            TextView textView = new TextView(applicationContext);
            textView.setText(applicationContext.getText(R.string.hr_result_empty));
            textView.setTextColor(Color.GRAY);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
            resultLayout.addView(textView);
        }
        for (int j = 0; j < result.size(); j++) {
            final JSONObject jsonObject = result.get(j);
            Button button = LayoutInflater.from(applicationContext).inflate(R.layout.hr_result_button, null).findViewById(R.id.hr_result_button);
            switch (jsonObject.getInteger("level")) {
                case 6:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_yellow));
                    break;
                case 5:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_red));
                    break;
                case 3:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_green));
                    break;
                case 2:
                case 1:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_lime));
                    break;
                default:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_blue));
                    break;
            }
            button.setText(jsonObject.getString("name"));
            button.setTag("hr_result");
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            CardView cardView = (CardView) LayoutInflater.from(applicationContext).inflate(applicationContext.getResources().getLayout(R.layout.detail_popup), null);
                            TextView textView = cardView.findViewById(R.id.detail_text);
                            JSONArray jsonArray = jsonObject.getJSONArray("tags");
                            String tags = "";
                            for (int i = 0; i < jsonArray.size(); i++){
                                tags += jsonArray.getString(i);
                                if (i != jsonArray.size() - 1){
                                    tags += " ";
                                }
                            }
                            tags += "\n" + jsonObject.getString("characteristic") + "\n" + jsonObject.getString("type") + "干员";
                            textView.setText(tags);
                            easyPopup = EasyPopup.create(applicationContext)
                                    .setContentView(cardView)
                                    .setFocusable(false)
                                    .apply();
                            easyPopup.showAtAnchorView(view, YGravity.ABOVE, XGravity.CENTER);
                             break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            easyPopup.dismiss();
                            break;
                        default:
                            break;
                    }


                    return false;
                }
            });
            linearLayout1.addView(button);
        }
    }

    public void showResultFuzzy(ArrayList<JSONObject> result){
        LinearLayout linearLayout = contentView.findViewById(R.id.hr_result_content);
        LinearLayout resultLayout = linearLayout.findViewById(R.id.hr_result);
        resultLayout.removeAllViews();
        resultLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setVisibility(View.VISIBLE);
        if(result.size() == 0){
            TextView textView = new TextView(applicationContext);
            textView.setText(applicationContext.getText(R.string.hr_result_empty));
            textView.setTextColor(Color.GRAY);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
            resultLayout.addView(textView);
        }
        for (int j = 0; j < result.size(); j++) {
            final JSONObject jsonObject = result.get(j);
            Button button = LayoutInflater.from(applicationContext).inflate(R.layout.hr_result_button, null).findViewById(R.id.hr_result_button);
            switch (jsonObject.getInteger("level")) {
                case 6:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_yellow));
                    break;
                case 5:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_red));
                    break;
                case 3:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_green));
                    break;
                case 2:
                case 1:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_lime));
                    break;
                default:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_blue));
                    break;
            }
            button.setText(jsonObject.getString("name"));
            button.setTag("hr_result");
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            CardView cardView = (CardView) LayoutInflater.from(applicationContext).inflate(applicationContext.getResources().getLayout(R.layout.detail_popup), null);
                            TextView textView = cardView.findViewById(R.id.detail_text);
                            JSONArray jsonArray = jsonObject.getJSONArray("tags");
                            String tags = "";
                            for (int i = 0; i < jsonArray.size(); i++){
                                tags += jsonArray.getString(i);
                                if (i != jsonArray.size() - 1){
                                    tags += " ";
                                }
                            }
                            tags += "\n" + jsonObject.getString("characteristic") + "\n" + jsonObject.getString("type") + "干员";
                            textView.setText(tags);
                            easyPopup = EasyPopup.create(applicationContext)
                                    .setContentView(cardView)
                                    .setFocusable(false)
                                    .apply();
                            easyPopup.showAtAnchorView(view, YGravity.ABOVE, XGravity.CENTER);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            easyPopup.dismiss();
                            break;
                        default:
                            break;
                    }


                    return false;
                }
            });
            LinearLayout layoutViewWithTag = resultLayout.findViewWithTag(String.valueOf(jsonObject.get("matchedTags")));
            if(layoutViewWithTag != null){
                HorizontalScrollView horizontalScrollView = (HorizontalScrollView)layoutViewWithTag.getChildAt(1);
                LinearLayout linearLayout1 = (LinearLayout)horizontalScrollView.getChildAt(0);
                linearLayout1.addView(button);
            }else {
                layoutViewWithTag = new LinearLayout(applicationContext);
                layoutViewWithTag.setTag(String.valueOf(jsonObject.get("matchedTags")));
                layoutViewWithTag.setGravity(Gravity.CENTER_VERTICAL);
                TextView textView = new TextView(applicationContext);
                String tagsDesc = String.valueOf(jsonObject.get("matchedTags")).replace("\"男\"", "\"男性干员\"").replace("\"女\"", "\"女性干员\"").replace("[\"", "").replace("\",\"", "\n").replace("\"]", "");
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                int padding = applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
                textView.setWidth(6 * padding);
                textView.setLineSpacing(padding / 8, 1);
                textView.setTextColor(Color.LTGRAY);//TODO 颜色定义不规范
                textView.setText(tagsDesc);
                resultLayout.addView(layoutViewWithTag);
                layoutViewWithTag.addView(textView);
                HorizontalScrollView horizontalScrollView = new HorizontalScrollView(applicationContext);
                layoutViewWithTag.addView(horizontalScrollView);
                LinearLayout linearLayout1 = new LinearLayout(applicationContext);
                horizontalScrollView.addView(linearLayout1);
                linearLayout1.addView(button);
            }

        }
    }
    public void selectQueryMethod(){
        //选择
        final ScrollView parentView = (ScrollView) contentView.getParent();
        View methodSelector = LayoutInflater.from(applicationContext).inflate(R.layout.content_hr_method_selector, null);
        parentView.removeAllViews();
        parentView.addView(methodSelector);
        //设定本次的方法
        LinearLayout exactLinearLayout = parentView.findViewById(R.id.hr_method_selector_exact);
        exactLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeQueryMethod(MODE_EXACT);
                parentView.removeAllViews();
                parentView.addView(contentView);
            }
        });
        LinearLayout fuzzyLinearLayout = parentView.findViewById(R.id.hr_method_selector_fuzzy);
        fuzzyLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeQueryMethod(MODE_FUZZY);
                parentView.removeAllViews();
                parentView.addView(contentView);
            }
        });
    }

    public void changeQueryMethod(int mode){
        LinearLayout resultLayout = contentView.findViewById(R.id.hr_result);
        resultLayout.removeAllViews();
        for(int i = 0; i < checkBoxes.size(); i++){
            CheckBox checkBox = checkBoxes.get(i);
            checkBox.setChecked(false);
        }
        if(mode == MODE_EXACT){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("fuzzyQuery", false);
            editor.putBoolean("queryMethodSelected",true);
            editor.apply();
            fuzzy = false;
            TextView textView = contentView.findViewById(R.id.hr_description);
            textView.setText(R.string.hr_desc_exact_part_1);
            textView.append(" ");
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_desc_part_2));
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    changeQueryMethod(MODE_FUZZY);
                }
            }, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(spannableStringBuilder);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }else if(mode == MODE_FUZZY){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("fuzzyQuery", true);
            editor.putBoolean("queryMethodSelected",true);
            editor.apply();
            fuzzy = true;
            TextView textView = contentView.findViewById(R.id.hr_description);
            textView.setText(R.string.hr_desc_fuzzy_part_1);
            textView.append(" ");
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_desc_part_2));
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    changeQueryMethod(MODE_EXACT);
                }
            }, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(spannableStringBuilder);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
