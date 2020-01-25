package com.ssyanhuo.arknightshelper.module;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.MediaInfo;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.utils.*;
import com.ssyanhuo.arknightshelper.widget.LineWrapLayout;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Hr {
    private static final int TYPE_PROCESSING = 1;
    private static final int TYPE_NETWORK_ERROR = 283504;
    private static final int TYPE_RECOGNIZE_ERROR = 216630;
    private static final int TYPE_IMAGE_SIZE_ERROR = 216202;
    private static final int TYPE_EMPTY_ERROR = -1;
    private static final int TYPE_SUCCEED = 0;
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
    private ScrollView selector;
    LinearLayout rootLayout;
    LinearLayout placeHolder;
    ArrayList<String> tagList = new ArrayList<>();
    ContextThemeWrapper contextThemeWrapper;
    EasyPopup scrollToResultPopup;
    boolean builtin;
    private ArrayList<String> combineTempArr = new ArrayList<>();
    WindowManager windowManager;


    public void init(Context context, View view, LinearLayout backgroundLayout) {
        contentView = view;
        applicationContext = context;
        rootLayout = backgroundLayout;
        contextThemeWrapper = new ContextThemeWrapper(applicationContext, ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, applicationContext));
        hideResult(contentView.findViewById(R.id.hr_result_content));
        sharedPreferences = applicationContext.getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("queryMethodSelected", false)) {
            selectQueryMethod();
        } else if (sharedPreferences.getBoolean("fuzzyQuery", false)) {
            fuzzy = true;
        }
        builtin = sharedPreferences.getBoolean("use_builtin_data", true);
        selector = (ScrollView) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.overlay_hr_sub_ocr, null);
        checkBoxes = new ArrayList<>();
        selectedStar = new ArrayList<>();
        selectedQualification = new ArrayList<>();
        selectedPosition = new ArrayList<>();
        selectedSex = new ArrayList<>();
        selectedType = new ArrayList<>();
        selectedTag = new ArrayList<>();
        try {
            hrJson = FileUtils.readData("akhr.json", context, builtin);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAllCheckboxes(checkBoxes, view);
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    getSelectedItems();
                    if (!isFewerThanLimit()) {
                        compoundButton.setChecked(false);
                    }
                    getSelectedItems();
                    if (fuzzy) {
                        getResultFuzzy();
                    } else {
                        getResultExact();
                    }

                    if (selectedStar.size() + selectedQualification.size() + selectedPosition.size() + selectedSex.size() + selectedType.size() + selectedTag.size() == 0) {
                        hideResult(contentView.findViewById(R.id.hr_result_content));
                    }
                }
            });
        }
        if (!fuzzy) {
            changeQueryMethod(MODE_EXACT);
        } else {
            changeQueryMethod(MODE_FUZZY);
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_result_title_part_2));
        spannableStringBuilder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                LinearLayout resultLayout = contentView.findViewById(R.id.hr_result);
                resultLayout.removeAllViews();
                for (int i = 0; i < checkBoxes.size(); i++) {
                    CheckBox checkBox = checkBoxes.get(i);
                    checkBox.setChecked(false);
                }
            }
        }, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) contentView.findViewById(R.id.hr_result_title)).append(" ");
        ((TextView) contentView.findViewById(R.id.hr_result_title)).append(spannableStringBuilder);
        ((TextView) contentView.findViewById(R.id.hr_result_title)).setMovementMethod(LinkMovementMethod.getInstance());
        placeHolder = rootLayout.findViewWithTag("placeHolder");
        contextThemeWrapper = new ContextThemeWrapper(applicationContext, ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, applicationContext));
        windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
    }
    //按星级排序
    private class StarComparator implements Comparator<JSONObject>{

        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            return o2.getInteger(("level")) - o1.getInteger("level");
        }
    }

    public void getAllCheckboxes(ArrayList<CheckBox> checkBoxes, View view) {
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof CheckBox) {
                checkBoxes.add((CheckBox) viewGroup.getChildAt(i));
            } else if (viewGroup.getChildAt(i) instanceof HorizontalScrollView) {
                getAllCheckboxes(checkBoxes, viewGroup.getChildAt(i));
            } else if (viewGroup.getChildAt(i) instanceof LinearLayout) {
                getAllCheckboxes(checkBoxes, viewGroup.getChildAt(i));
            } else if (viewGroup.getChildAt(i) instanceof LineWrapLayout) {
                getAllCheckboxes(checkBoxes, viewGroup.getChildAt(i));
            }
        }
    }

    public void getSelectedItems() {
        selectedStar.clear();
        selectedQualification.clear();
        selectedPosition.clear();
        selectedSex.clear();
        selectedType.clear();
        selectedTag.clear();
        for (int i = 0; i < checkBoxes.size(); i++) {
            CheckBox checkBox = checkBoxes.get(i);
            if (!checkBox.isChecked()) {
                continue;
            }
            View parentView = (View) checkBox.getParent();
            String item = String.valueOf(parentView.getTag());
            Log.i(TAG, "Selected:" + checkBox.getTag());
            switch (item) {
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

    public boolean isFewerThanLimit() {
        int num = selectedQualification.size() + selectedPosition.size() + selectedSex.size() + selectedType.size() + selectedTag.size();
        if (fuzzy) {
            return (num <= 5);
        } else {
            return (num <= 3);
        }
    }


    public void getResultExact() {
        ArrayList<JSONObject> result = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(hrJson);
        selectedTag.addAll(selectedQualification);
        selectedTag.addAll(selectedPosition);
        for (int i = 0; i < jsonArray.size(); i++) {
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
            boolean sexPaired = selectedSex.size() == sex.size() || selectedSex.size() == 0;
            boolean tagPaired = selectedTag.size() == tag.size() || selectedTag.size() == 0;
            if (typePaired && starPaired && sexPaired && tagPaired && !hidden) {
                if ((!selectedStar.contains("6") && !selectedTag.contains("高级资深干员")) && star.equals("6")){continue;}
                result.add(jsonObject);
                Log.i(TAG, "Found:" + name);
            }
        }
        Collections.sort(result, new StarComparator());//高星级放前面
        showResultExact(result);
    }

    //模糊查询
    public void getResultFuzzy() {
        ArrayList<JSONObject> result = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(hrJson);
        selectedTag.addAll(selectedQualification);
        selectedTag.addAll(selectedPosition);
        for (int i = 0; i < jsonArray.size(); i++) {
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
            if ((typePaired || sexPaired || tagPaired) && !hidden && starPaired) {
                if ((!selectedStar.contains("6") && !selectedTag.contains("高级资深干员")) && star.equals("6")){continue;}
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
        Collections.sort(result, new StarComparator());//高星级放前面
        showResultFuzzy(result);

    }

    public void hideResult(View view) {
        view.setVisibility(View.GONE);
    }

    public void showResultExact(ArrayList<JSONObject> result) {
        LinearLayout linearLayout = contentView.findViewById(R.id.hr_result_content);
        LinearLayout resultLayout = linearLayout.findViewById(R.id.hr_result);
        resultLayout.removeAllViews();
        resultLayout.setOrientation(LinearLayout.HORIZONTAL);
        LineWrapLayout lineWrapLayout = new LineWrapLayout(applicationContext);
        linearLayout.setVisibility(View.VISIBLE);
        if (result.size() == 0) {
            TextView textView = new TextView(applicationContext);
            textView.setText(applicationContext.getText(R.string.hr_result_empty));
            textView.setTextColor(Color.GRAY);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
            resultLayout.addView(textView);
            return;
        }
        resultLayout.addView(lineWrapLayout);
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
            button.setTag(jsonObject);
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            CardView cardView = (CardView) LayoutInflater.from(applicationContext).inflate(applicationContext.getResources().getLayout(R.layout.detail_popup), null);
                            TextView textView = cardView.findViewById(R.id.detail_text);
                            JSONArray jsonArray = jsonObject.getJSONArray("tags");
                            String tags = "";
                            for (int i = 0; i < jsonArray.size(); i++) {
                                tags += jsonArray.getString(i);
                                if (i != jsonArray.size() - 1) {
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
            lineWrapLayout.addView(button);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showResultFuzzy(ArrayList<JSONObject> result) {
        ArrayList<LinearLayout> resultLayouts = new ArrayList<>();
        LinearLayout linearLayout = contentView.findViewById(R.id.hr_result_content);
        LinearLayout resultLayout = linearLayout.findViewById(R.id.hr_result);
        resultLayout.removeAllViews();
        resultLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setVisibility(View.VISIBLE);
        if (result.size() == 0) {//无匹配
            TextView textView = new TextView(applicationContext);
            textView.setText(applicationContext.getText(R.string.hr_result_empty));
            textView.setTextColor(Color.GRAY);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
            resultLayout.addView(textView);
        }
        for (int j = 0; j < result.size(); j++) {//创建干员对象
            final JSONObject jsonObject = result.get(j);
            //解析结果数据
            ArrayList<String> tags = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("matchedTags");
            for(int i = 0; i < jsonArray.size(); i++){
                tags.add(jsonArray.getString(i));
            }
            ArrayList<ArrayList<String>> matchedTags = CombinationUtils.combine(tags);
            for(ArrayList<String> tagCom : matchedTags){
                //存在高级标签但未选中时，不显示按钮
                if (!tagCom.contains("高级资深干员") && jsonObject.getInteger("level") == 6){
                    continue;
                }
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
                button.setTag(jsonObject);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                CardView cardView = (CardView) LayoutInflater.from(applicationContext).inflate(applicationContext.getResources().getLayout(R.layout.detail_popup), null);
                                TextView textView = cardView.findViewById(R.id.detail_text);
                                JSONArray jsonArray = jsonObject.getJSONArray("tags");
                                String tags = "";
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    tags += jsonArray.getString(i);
                                    if (i != jsonArray.size() - 1) {
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
                LinearLayout resultViewWithTag = resultLayout.findViewWithTag(String.valueOf(tagCom));
                if (resultViewWithTag != null) {
                    LineWrapLayout lineWrapLayout = (LineWrapLayout) resultViewWithTag.getChildAt(1);
                    lineWrapLayout.addView(button);
                } else {
                    resultViewWithTag = new LinearLayout(applicationContext);
                    resultViewWithTag.setTag(String.valueOf(tagCom));
                    resultViewWithTag.setGravity(Gravity.CENTER_VERTICAL);
                    TextView textView = new TextView(applicationContext);
                    String tagsDesc = String.valueOf(tagCom).replace("\"男\"", "\"男性干员\"").replace("\"女\"", "\"女性干员\"").replace("[\"", "").replace("\",\"", "\n").replace("\"]", "").replace("[", "").replace("]","").replace(", ", "\n");
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    int padding = applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
                    textView.setWidth(6 * padding);
                    textView.setLineSpacing(padding / 8, 1);
                    if (ThemeUtils.getThemeMode(applicationContext) == ThemeUtils.THEME_LIGHT){
                        textView.setTextColor(Color.BLACK);
                    }else {
                        textView.setTextColor(Color.LTGRAY);
                    }
                    textView.setText(tagsDesc);
                    textView.setTag("result_desc");
                    resultLayout.addView(resultViewWithTag);
                    resultViewWithTag.addView(textView);
                    LineWrapLayout lineWrapLayout = new LineWrapLayout(applicationContext);
                    lineWrapLayout.setTag("result_box");
                    resultViewWithTag.addView(lineWrapLayout);
                    lineWrapLayout.addView(button);
                    resultLayouts.add(resultViewWithTag);
                }
            }
        }
        //奇怪的排序
        for (LinearLayout layout : resultLayouts){
            Set<Integer> stars = new HashSet<>();
            for (int i = 0; i < layout.getChildCount(); i++){
                LineWrapLayout lineWrapLayout = layout.findViewWithTag("result_box");
                if (lineWrapLayout != null){
                    for (int j = 0; j < lineWrapLayout.getChildCount(); j++){
                        if (lineWrapLayout.getChildAt(j) instanceof Button){
                            Button button = (Button) lineWrapLayout.getChildAt(j);
                            JSONObject jsonObject = (JSONObject) button.getTag();
                            stars.add(jsonObject.getInteger("level"));
                        }
                    }
                    if (!(stars.contains(1) || stars.contains(2) || stars.contains(3))){
                        resultLayout.removeView(layout);
                        resultLayout.addView(layout, 0);
                        layout.getChildAt(0).setTag("senior");
                    }
                }
            }
        }


        for (LinearLayout layout : resultLayouts){
            Set<Integer> stars = new HashSet<>();
            for (int i = 0; i < layout.getChildCount(); i++){
                if(!layout.getChildAt(0).getTag().equals("senior")){
                    continue;
                }
                LineWrapLayout lineWrapLayout = layout.findViewWithTag("result_box");
                if (lineWrapLayout != null){
                    for (int j = 0; j < lineWrapLayout.getChildCount(); j++){
                        if (lineWrapLayout.getChildAt(j) instanceof Button){
                            Button button = (Button) lineWrapLayout.getChildAt(j);
                            JSONObject jsonObject = (JSONObject) button.getTag();
                            stars.add(jsonObject.getInteger("level"));
                        }
                    }
                    if (stars.contains(4) && stars.size() == 1){
                        resultLayout.removeView(layout);
                        resultLayout.addView(layout, 0);
                    }
                }
            }
        }
        for (LinearLayout layout : resultLayouts){
            Set<Integer> stars = new HashSet<>();
            for (int i = 0; i < layout.getChildCount(); i++){
                if(!layout.getChildAt(0).getTag().equals("senior")){
                    continue;
                }
                LineWrapLayout lineWrapLayout = layout.findViewWithTag("result_box");
                if (lineWrapLayout != null){
                    for (int j = 0; j < lineWrapLayout.getChildCount(); j++){
                        if (lineWrapLayout.getChildAt(j) instanceof Button){
                            Button button = (Button) lineWrapLayout.getChildAt(j);
                            JSONObject jsonObject = (JSONObject) button.getTag();
                            stars.add(jsonObject.getInteger("level"));
                        }
                    }
                    if (stars.contains(5)){
                        //Log.e(TAG, (String) ((Button) ((LineWrapLayout) layout.getChildAt(1)).getChildAt(1)).getText());
                        resultLayout.removeView(layout);
                        resultLayout.addView(layout, 0);
                    }
                }
            }
        }
        for (LinearLayout layout : resultLayouts){
            Set<Integer> stars = new HashSet<>();
            for (int i = 0; i < layout.getChildCount(); i++){
                if(!layout.getChildAt(0).getTag().equals("senior")){
                    continue;
                }
                LineWrapLayout lineWrapLayout = layout.findViewWithTag("result_box");
                if (lineWrapLayout != null){
                    for (int j = 0; j < lineWrapLayout.getChildCount(); j++){
                        if (lineWrapLayout.getChildAt(j) instanceof Button){
                            Button button = (Button) lineWrapLayout.getChildAt(j);
                            JSONObject jsonObject = (JSONObject) button.getTag();
                            stars.add(jsonObject.getInteger("level"));
                        }
                    }
                    if (stars.contains(5) && stars.size() == 1){
                        resultLayout.removeView(layout);
                        resultLayout.addView(layout, 0);
                    }
                }
            }
        }
        for (LinearLayout layout : resultLayouts){
            Set<Integer> stars = new HashSet<>();
            for (int i = 0; i < layout.getChildCount(); i++){
                if(!layout.getChildAt(0).getTag().equals("senior")){
                    continue;
                }
                LineWrapLayout lineWrapLayout = layout.findViewWithTag("result_box");
                if (lineWrapLayout != null){
                    for (int j = 0; j < lineWrapLayout.getChildCount(); j++){
                        if (lineWrapLayout.getChildAt(j) instanceof Button){
                            Button button = (Button) lineWrapLayout.getChildAt(j);
                            JSONObject jsonObject = (JSONObject) button.getTag();
                            stars.add(jsonObject.getInteger("level"));
                        }
                    }
                    if (stars.contains(6)){
                        //Log.e(TAG, (String) ((Button) ((LineWrapLayout) layout.getChildAt(1)).getChildAt(1)).getText());
                        resultLayout.removeView(layout);
                        resultLayout.addView(layout, 0);
                    }
                }
            }
        }
        for (LinearLayout layout : resultLayouts){
            Set<Integer> stars = new HashSet<>();
            for (int i = 0; i < layout.getChildCount(); i++){
                if(!layout.getChildAt(0).getTag().equals("senior")){
                    continue;
                }
                LineWrapLayout lineWrapLayout = layout.findViewWithTag("result_box");
                if (lineWrapLayout != null){
                    for (int j = 0; j < lineWrapLayout.getChildCount(); j++){
                        if (lineWrapLayout.getChildAt(j) instanceof Button){
                            Button button = (Button) lineWrapLayout.getChildAt(j);
                            JSONObject jsonObject = (JSONObject) button.getTag();
                            stars.add(jsonObject.getInteger("level"));
                        }
                    }
                    if (stars.contains(6) && stars.size() == 1){
                        resultLayout.removeView(layout);
                        resultLayout.addView(layout, 0);
                    }
                }
            }
        }
        GradientDrawable drawableLight = new GradientDrawable();
        drawableLight.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawableLight.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        drawableLight.setColors(new int[]{Color.parseColor("#aaff9800"), Color.TRANSPARENT});
        GradientDrawable drawableDark = new GradientDrawable();
        drawableDark.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawableDark.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        drawableDark.setColors(new int[]{Color.parseColor("#aaad6700"), Color.TRANSPARENT});
        for(int i = 0; i < resultLayout.getChildCount(); i++){
            if(resultLayout.getChildAt(i) instanceof LinearLayout){
                LinearLayout layout = (LinearLayout) resultLayout.getChildAt(i);
                if(layout.getChildAt(0).getTag() != "senior"){
                    layout.setBackgroundColor(Color.TRANSPARENT);
                    if(i % 2 == 0){
                        layout.setBackgroundColor(Color.argb(128, 128, 128, 128));
                    }
                }else {
                    if(i % 2 == 1){
                        layout.setBackground(drawableDark);
                    }else {
                        layout.setBackground(drawableLight);
                    }
                }
            }
        }
    }

    public void combine(int index, int k, JSONArray arr, ArrayList<ArrayList<String>> result) {
        if(k == 1){
            for (int i = index; i < arr.size(); i++) {
                combineTempArr.add(arr.getString(i));
                result.add(new ArrayList<>(combineTempArr));
                combineTempArr.remove((Object)arr.getString(i));
            }
        }else if(k > 1){
            for (int i = index; i <= arr.size() - k; i++) {
                combineTempArr.add(arr.getString(i)); //tmpArr都是临时性存储一下

                combine(i + 1,k - 1, arr, result); //索引右移，内部循环，自然排除已经选择的元素
                combineTempArr.remove((Object)arr.getString(i)); //tmpArr因为是临时存储的，上一个组合找出后就该释放空间，存储下一个元素继续拼接组合了
            }
        }
    }



    public void selectQueryMethod() {
        //选择
        final RelativeLayout parentView = (RelativeLayout) contentView.getParent();
        View methodSelector = LayoutInflater.from(applicationContext).inflate(R.layout.overlay_hr_method_selector, null);
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

    public void changeQueryMethod(int mode) {
        LinearLayout resultLayout = contentView.findViewById(R.id.hr_result);
        resultLayout.removeAllViews();
        for (int i = 0; i < checkBoxes.size(); i++) {
            CheckBox checkBox = checkBoxes.get(i);
            checkBox.setChecked(false);
        }
        if (mode == MODE_EXACT) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("fuzzyQuery", false);
            editor.putBoolean("queryMethodSelected", true);
            editor.apply();
            fuzzy = false;
            TextView textView = contentView.findViewById(R.id.hr_description);
            textView.setText(R.string.hr_desc_part_1_exact);
            textView.append(" ");
            SpannableStringBuilder spannableStringBuilder1 = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_desc_part_2));
            spannableStringBuilder1.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    showSubWindow();
                }
            }, 0, spannableStringBuilder1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(spannableStringBuilder1);
            textView.append(" ");
            textView.append(applicationContext.getString(R.string.hr_desc_part_3));
            textView.append(" ");
            SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_desc_part_4));
            spannableStringBuilder2.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    changeQueryMethod(MODE_FUZZY);
                }
            }, 0, spannableStringBuilder2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(spannableStringBuilder2);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        } else if (mode == MODE_FUZZY) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("fuzzyQuery", true);
            editor.putBoolean("queryMethodSelected", true);
            editor.apply();
            fuzzy = true;
            TextView textView = contentView.findViewById(R.id.hr_description);
            textView.setText(R.string.hr_desc_part_1_fuzzy);
            textView.append(" ");
            SpannableStringBuilder spannableStringBuilder1 = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_desc_part_2));
            spannableStringBuilder1.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    showSubWindow();
                }
            }, 0, spannableStringBuilder1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(spannableStringBuilder1);
            textView.append(" ");
            textView.append(applicationContext.getString(R.string.hr_desc_part_3));
            textView.append(" ");
            SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_desc_part_4));
            spannableStringBuilder2.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    changeQueryMethod(MODE_EXACT);
                }
            }, 0, spannableStringBuilder2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(spannableStringBuilder2);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public void isCurrentWindow(boolean current) {
        if (!current) {
            placeHolder = rootLayout.findViewWithTag("placeHolder");
            placeHolder.removeAllViews();
        }
    }

    public void showSubWindow() {
        placeHolder = rootLayout.findViewWithTag("placeHolder");
        placeHolder.removeAllViews();
        int backgroundColor;
        if (ThemeUtils.getThemeMode(applicationContext) == ThemeUtils.THEME_NEW_YEAR){//太红了不好看
            backgroundColor = ThemeUtils.getColorWithAlpha(0.7f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY_DARK, contextThemeWrapper) - Color.parseColor("#00501010"));
        }else if (ThemeUtils.getThemeMode(applicationContext) == ThemeUtils.THEME_LIGHT){//太蓝了也不好看
            backgroundColor = ThemeUtils.getColorWithAlpha(0.9f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY, contextThemeWrapper));
        } else {
            backgroundColor = ThemeUtils.getColorWithAlpha(0.7f, ThemeUtils.getColor(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_PRIMARY_DARK, contextThemeWrapper));
        }
        selector = (ScrollView) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.overlay_hr_sub_ocr, null);
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, Color.TRANSPARENT});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        selector.setBackground(gradientDrawable);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        assert windowManager != null;
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (rotation == 0 || rotation == 3) {
            selector.setBackgroundColor(backgroundColor);
        }
        int width = placeHolder.getWidth();
        int height = placeHolder.getHeight();
        selector.setMinimumHeight(height);
        selector.setMinimumWidth(width);
        selector.getChildAt(0).setMinimumWidth(width);
        selector.getChildAt(0).setMinimumHeight(height);
        placeHolder.addView(selector);
        ((GridLayout) selector.findViewById(R.id.hr_ocr_selector)).removeAllViews();
        Animator animator;
        if (rotation == 0 || rotation == 2){
            animator = AnimatorInflater.loadAnimator(applicationContext, R.animator.overlay_sub_show_portrait);
        }else {
            animator = AnimatorInflater.loadAnimator(applicationContext, R.animator.overlay_sub_show_landspace);
        }
        animator.setDuration(150);
        animator.setTarget(selector);
        animator.start();
        final Handler getPictureHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                selector.findViewById(R.id.hr_ocr_loading).setVisibility(View.GONE);
                ArrayList<MediaInfo> mediaInfos = (ArrayList<MediaInfo>) msg.obj;
                for (int i = 0; i < mediaInfos.size(); i++) {
                    MediaInfo mediaInfo = mediaInfos.get(i);
                    final ImageView imageView = new ImageView(applicationContext);
                    imageView.setImageBitmap(mediaInfo.thumbnail);
                    imageView.setClickable(true);
                    imageView.setFocusable(true);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    TypedValue typedValue = new TypedValue();
                    applicationContext.getTheme()
                            .resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
                    int[] attribute = new int[]{android.R.attr.selectableItemBackground};
                    TypedArray typedArray = applicationContext.getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        imageView.setForeground(typedArray.getDrawable(0));
                    }
                    int margin = applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) / 2;
                    int childWidth = (placeHolder.getWidth() - 4 * margin) / 4 - 2 * margin;
                    int childHeight = (placeHolder.getWidth() - 4 * margin) / 4 - 2 * margin;
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(childWidth, childHeight);
                    layoutParams.setMargins(margin, margin, margin, margin);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setTag(mediaInfo.uri);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getOCRResult((Uri) v.getTag(), applicationContext);

                        }
                    });
                    ((GridLayout) selector.findViewById(R.id.hr_ocr_selector)).addView(imageView);
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Thread getPictureThread = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        ArrayList<MediaInfo> mediaInfos = ImageUtils.getPictures(applicationContext);
                        getPictureHandler.sendMessage(getPictureHandler.obtainMessage(0, mediaInfos));
                    }
                };
                getPictureThread.start();
            }
        }, 500);
    }

    public void hideSubWindow() {DisplayMetrics displayMetrics = new DisplayMetrics();
        assert windowManager != null;
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        placeHolder = rootLayout.findViewWithTag("placeHolder");
        Animator animator;
        if (rotation == 0 || rotation == 2){
            animator = AnimatorInflater.loadAnimator(applicationContext, R.animator.overlay_sub_hide_portrait);
        }else {
            animator = AnimatorInflater.loadAnimator(applicationContext, R.animator.overlay_sub_hide_landspace);
        }
        animator.setDuration(150);
        animator.setTarget(selector);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public void getOCRResult(Uri uri, Context context) {
        final Handler onResultHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                showOcrResult(msg.arg1);
            }
        };
        String compressedPath = context.getExternalCacheDir() + String.valueOf((int)(Math.random()*100000000));
        try{
            Bitmap bitmap = BitmapFactory.decodeStream(applicationContext.getContentResolver().openInputStream(uri));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(compressedPath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        selector.removeAllViews();
        showOcrResult(TYPE_PROCESSING);
        GeneralParams params = new GeneralParams();
        params.setDetectDirection(true);
        final File compressedFile = new File(compressedPath);
        params.setImageFile(compressedFile);
        tagList.clear();
        OCR.getInstance(context).recognizeGeneral(params, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult generalResult) {
                for (WordSimple wordSimple : generalResult.getWordList()) {
                    Log.i(TAG, wordSimple.getWords());
                    //wordList.add(wordSimple.getWords());
                    for (String tag : StaticData.HR.tagList){
                        if (wordSimple.getWords().contains(tag)){
                            tagList.add(wordSimple.getWords());
                        }
                    }
                }
                Log.i(TAG, "OCR result: " + tagList.toString());
                if (tagList.size() <= 0){
                    onResultHandler.sendMessage(onResultHandler.obtainMessage(0, TYPE_EMPTY_ERROR , 0));
                    return;
                }
                showOcrResult(TYPE_SUCCEED);
                changeQueryMethod(MODE_FUZZY);
                for (CheckBox checkBox : checkBoxes) {
                    checkBox.setChecked(false);
                    for (String result : tagList) {
                        if (result.equals(checkBox.getText().toString())) {
                            checkBox.setChecked(true);
                        }
                    }
                }
                //hideSubWindow();
                compressedFile.delete();
            }

            @Override
            public void onError(OCRError ocrError) {
                ocrError.printStackTrace();
                int errorCode = ocrError.getErrorCode();
                onResultHandler.sendMessage(onResultHandler.obtainMessage(0, errorCode, 0));
            }
        });

    }

    public void showOcrResult(int type) {
        selector.removeAllViews();
        LinearLayout linearLayout = new LinearLayout(applicationContext, null, ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, applicationContext));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.width = selector.getWidth();
        lp.height = selector.getHeight();
        linearLayout.setLayoutParams(lp);
        linearLayout.setMinimumWidth(selector.getWidth());
        linearLayout.setMinimumHeight(selector.getHeight() - selector.getPaddingBottom() - selector.getPaddingTop());
        linearLayout.setVerticalGravity(Gravity.CENTER_VERTICAL);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        int padding = applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        switch (type){
            case TYPE_IMAGE_SIZE_ERROR:
            case TYPE_NETWORK_ERROR:
            case TYPE_RECOGNIZE_ERROR:
            case TYPE_EMPTY_ERROR:
                ImageView errorImage = new ImageView(contextThemeWrapper);
                errorImage.setImageDrawable(applicationContext.getDrawable(R.drawable.ic_ocr_error));
                errorImage.setMinimumHeight(DpUtils.dip2px(applicationContext,144));
                errorImage.setMinimumWidth(DpUtils.dip2px(applicationContext,144));
                errorImage.setPadding(padding, padding, padding, padding);
                linearLayout.addView(errorImage);
                TextView errorText = new TextView(contextThemeWrapper);
                errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                errorText.setText(applicationContext.getString(R.string.hr_ocr_error) + "(" + type + ")");
                linearLayout.addView(errorText);
                selector.addView(linearLayout);
                break;
            case TYPE_PROCESSING:
                ProgressBar progressBar = new ProgressBar(contextThemeWrapper);
                progressBar.setPadding(padding, padding, padding, padding);
                linearLayout.addView(progressBar);
                TextView processingText = new TextView(contextThemeWrapper);
                processingText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                processingText.setText(R.string.hr_ocr_getting_result);
                linearLayout.addView(processingText);
                selector.addView(linearLayout);
                break;
            case TYPE_SUCCEED:
                ImageView succeedImage = new ImageView(contextThemeWrapper);
                Drawable succeedDrawable = applicationContext.getDrawable(R.drawable.ic_ocr_succeed);
                if (ThemeUtils.getThemeMode(applicationContext) == ThemeUtils.THEME_LIGHT){
                    succeedDrawable.setColorFilter(new PorterDuffColorFilter(applicationContext.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY));
                }
                succeedImage.setImageDrawable(succeedDrawable);
                succeedImage.setMinimumHeight(DpUtils.dip2px(applicationContext,144));
                succeedImage.setMinimumWidth(DpUtils.dip2px(applicationContext,144));
                succeedImage.setPadding(padding, padding, padding, padding);
                linearLayout.addView(succeedImage);
                TextView succeedText = new TextView(contextThemeWrapper);
                succeedText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                succeedText.setText(applicationContext.getString(R.string.hr_ocr_succeed) + tagList.size());
                linearLayout.addView(succeedText);
                selector.addView(linearLayout);
                ((ScrollView)contentView.getParent().getParent()).post(new Runnable() {
                    @Override
                    public void run() {
                        ((ScrollView)contentView.getParent().getParent()).smoothScrollTo(0, rootLayout.findViewById(R.id.hr_result_content).getTop() + ((ScrollView)contentView.getParent().getParent()).getHeight() - 2 * applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
                    }
                });
                final Handler hideSubWindowHandler = new Handler();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                hideSubWindow();
                            }
                        };
                        hideSubWindowHandler.post(runnable);
                    }
                }, 2000);
                break;
        }
    }
}
