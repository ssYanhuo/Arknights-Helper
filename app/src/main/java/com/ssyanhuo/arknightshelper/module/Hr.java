package com.ssyanhuo.arknightshelper.module;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.ScreenCaptureActivity;
import com.ssyanhuo.arknightshelper.misc.MediaInfo;
import com.ssyanhuo.arknightshelper.misc.StaticData;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.utils.*;
import com.ssyanhuo.arknightshelper.utils.I18nUtils.Helper;
import com.ssyanhuo.arknightshelper.widget.AnimatedProgressBar;
import com.ssyanhuo.arknightshelper.widget.LineWrapLayout;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
    private OverlayService overlayService;
    LinearLayout placeHolder;
    ArrayList<String> tagList = new ArrayList<>();
    androidx.appcompat.view.ContextThemeWrapper contextThemeWrapper;
    EasyPopup scrollToResultPopup;
    private ArrayList<String> combineTempArr = new ArrayList<>();
    WindowManager windowManager;
    I18nUtils translationUtils;
    Helper tagHelper;
    Helper nameHelper;
    SwitchMaterial hideLowLevel;
    private TextView hideLowLevelNote;
    private RelativeLayout relativeLayout;
    private ScrollView scrollView;
    private FloatingActionButton fab;
    Handler handler = new Handler();
    private LinearLayout autoOCRProgressLayout;
    private boolean canceled = false;

    public void init(Context context, final View contentView, RelativeLayout relativeLayout, LinearLayout backgroundLayout, OverlayService overlayService) {
        this.contentView = contentView;
        applicationContext = context;
        this.relativeLayout = relativeLayout;
        rootLayout = backgroundLayout;
        this.overlayService = overlayService;

        contextThemeWrapper = new androidx.appcompat.view.ContextThemeWrapper(applicationContext, ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, applicationContext));
        sharedPreferences = applicationContext.getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("fuzzyQuery", true)) {
            fuzzy = true;
        }
        selector = (ScrollView) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.overlay_hr_sub_ocr, null);
        checkBoxes = new ArrayList<>();
        selectedStar = new ArrayList<>();
        selectedQualification = new ArrayList<>();
        selectedPosition = new ArrayList<>();
        selectedSex = new ArrayList<>();
        selectedType = new ArrayList<>();
        selectedTag = new ArrayList<>();
        try {
            hrJson = FileUtils.readData("akhr.json", context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            translationUtils = new I18nUtils();
            tagHelper = translationUtils.getHelper(applicationContext, I18nUtils.CATEGORY_TAG);
            nameHelper = translationUtils.getHelper(applicationContext, I18nUtils.CATEGORY_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getAllCheckboxes(checkBoxes, contentView);
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
                        hideResult(Hr.this.contentView.findViewById(R.id.hr_result_content));
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
                LinearLayout resultLayout = Hr.this.contentView.findViewById(R.id.hr_result);
                resultLayout.removeAllViews();
                for (int i = 0; i < checkBoxes.size(); i++) {
                    CheckBox checkBox = checkBoxes.get(i);
                    checkBox.setChecked(false);
                }
            }
        }, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) this.contentView.findViewById(R.id.hr_result_title)).append(" ");
        ((TextView) this.contentView.findViewById(R.id.hr_result_title)).append(spannableStringBuilder);
        ((TextView) this.contentView.findViewById(R.id.hr_result_title)).setMovementMethod(LinkMovementMethod.getInstance());
        placeHolder = rootLayout.findViewWithTag("placeHolder");
        contextThemeWrapper = new androidx.appcompat.view.ContextThemeWrapper(applicationContext, ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, applicationContext));
        windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        hideLowLevel = this.contentView.findViewById(R.id.hr_hide_low_level);
        hideLowLevelNote = this.contentView.findViewById(R.id.hr_hide_low_level_note);
        hideLowLevel.setChecked(sharedPreferences.getBoolean("hide_low_level", false));
        if (hideLowLevel.isChecked()){
            hideLowLevelNote.setVisibility(View.VISIBLE);
        }else {
            hideLowLevelNote.setVisibility(View.GONE);
        }
        hideLowLevel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSelectedItems();
                LinearLayout linearLayout = contentView.findViewById(R.id.hr_result_content);
                LinearLayout resultLayout = linearLayout.findViewById(R.id.hr_result);
                resultLayout.removeAllViews();
                if (fuzzy) {
                    getResultFuzzy();
                } else {
                    getResultExact();
                }
                if (isChecked){
                    hideLowLevelNote.setVisibility(View.VISIBLE);
                }else {
                    hideLowLevelNote.setVisibility(View.GONE);
                }
                sharedPreferences.edit().putBoolean("hide_low_level", isChecked).apply();
            }
        });
        try{
            fab = relativeLayout.findViewById(R.id.hr_fab);
            scrollView = relativeLayout.findViewById(R.id.scroll_hr);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        if (scrollY + scrollView.getHeight() > (scrollView.findViewById(R.id.hr_result_scroll_point).getTop() + applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) * 6)){
                            hideFab(fab, contextThemeWrapper);
                        }
                    }
                });
            }
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollToResult();
                    hideFab(v, contextThemeWrapper);
                }
            });
        }catch (Exception ignored){

        }
        hideResult(this.contentView.findViewById(R.id.hr_result_content));
    }

    public void refresh(){

    }

    //按星级排序
    private static class StarComparator implements Comparator<JSONObject>{

        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            return o2.getInteger(("level")) - o1.getInteger("level");
        }
    }

    public void getAllCheckboxes(ArrayList<CheckBox> checkBoxes, View view) {
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof CheckBox) {
                checkBoxes.add((CheckBox) child);
            } else if (child instanceof HorizontalScrollView) {
                getAllCheckboxes(checkBoxes, child);
            } else if (child instanceof LinearLayout) {
                getAllCheckboxes(checkBoxes, child);
            } else if (child instanceof LineWrapLayout) {
                getAllCheckboxes(checkBoxes, child);
            }
        }
        for (CheckBox checkBox :
                checkBoxes) {
            checkBox.setText(tagHelper.get(checkBox.getText().toString()));
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
            boolean hiddenInHr = jsonObject.getBoolean("hidden");
            boolean hiddenInI18n = nameHelper.isHidden(name, I18nUtils.FILTER_HR);
            //Log.e(TAG, name + type + star + String.valueOf(tag) + String.valueOf(hidden));
            boolean typePaired = selectedType.size() == type.size() || selectedType.size() == 0;
            boolean starPaired = selectedStar.contains(star) || selectedStar.size() == 0;//星级允许多选
            boolean sexPaired = selectedSex.size() == sex.size() || selectedSex.size() == 0;
            boolean tagPaired = selectedTag.size() == tag.size() || selectedTag.size() == 0;
            if (typePaired && starPaired && sexPaired && tagPaired && !hiddenInHr && !hiddenInI18n) {
                if (Integer.parseInt(star) <= 2 && hideLowLevel.isChecked()){continue;}
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
            boolean hiddenInHr = jsonObject.getBoolean("hidden");
            boolean hiddenInI18n = nameHelper.isHidden(name, I18nUtils.FILTER_HR);
            boolean typePaired = type.size() != 0;
            boolean starPaired = selectedStar.contains(star) || selectedStar.size() == 0;//星级允许多选
            boolean sexPaired = sex.size() != 0;
            boolean tagPaired = tag.size() != 0;
            if ((typePaired || sexPaired || tagPaired) && !hiddenInHr && !hiddenInI18n && starPaired) {
                if (Integer.parseInt(star) <= 2 && hideLowLevel.isChecked()){continue;}
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
        try{
            hideFab(fab, contextThemeWrapper);
        }catch (Exception ignored){

        }
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
            button.setText(nameHelper.get(jsonObject.getString("name")));
            button.setTag(jsonObject);
            button.setOnTouchListener((view, motionEvent) -> {

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
            });
            lineWrapLayout.addView(button);
        }
        if (scrollView.getScrollY() + scrollView.getHeight() <= (scrollView.findViewById(R.id.hr_result_scroll_point).getTop() + applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) * 6)){
            showFab(fab, contextThemeWrapper);
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
            ArrayList<ArrayList<String>> matchedTags = combine(tags);
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
                button.setText(nameHelper.get(jsonObject.getString("name")));
                button.setTag(jsonObject);
                button.setOnTouchListener((view, motionEvent) -> {

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            CardView cardView = (CardView) LayoutInflater.from(applicationContext).inflate(applicationContext.getResources().getLayout(R.layout.detail_popup), null);
                            TextView textView = cardView.findViewById(R.id.detail_text);
                            JSONArray jsonArray1 = jsonObject.getJSONArray("tags");
                            String tags1 = "";

                            if (!sharedPreferences.getString("game_language", I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE).equals(I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE)){
                                tags1 += jsonObject.getString("name") + "\n";
                            }
                            for (int i = 0; i < jsonArray1.size(); i++) {
                                tags1 += jsonArray1.getString(i);
                                if (i != jsonArray1.size() - 1) {
                                    tags1 += " ";
                                }
                            }
                            tags1 += "\n" + jsonObject.getString("characteristic") + "\n" + jsonObject.getString("type") + "干员";
                            textView.setText(tags1);
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
                        GradientDrawable drawableDark = new GradientDrawable();
                        drawableDark.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                        drawableDark.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                        drawableDark.setColors(new int[]{Color.parseColor("#aaad6700"), Color.TRANSPARENT});
                        layout.setBackground(drawableDark);
                    }else {
                        GradientDrawable drawableLight = new GradientDrawable();
                        drawableLight.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                        drawableLight.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                        drawableLight.setColors(new int[]{Color.parseColor("#aaff9800"), Color.TRANSPARENT});
                        layout.setBackground(drawableLight);
                    }
                }
            }
        }
        if (scrollView.getScrollY() + scrollView.getHeight() <= (scrollView.findViewById(R.id.hr_result_scroll_point).getTop() + applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) * 6)){
            showFab(fab, contextThemeWrapper);
        }
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
            if (sharedPreferences.getString("game_language", I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE).equals(I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE) && OCRUtils.isAbiSupported()){
                textView.append(" ");
                SpannableStringBuilder spannableStringBuilder1 = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_desc_part_2));
                spannableStringBuilder1.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        OCR();
                    }
                }, 0, spannableStringBuilder1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.append(spannableStringBuilder1);
                textView.append(" ");
            }
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
            if (sharedPreferences.getString("game_language", I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE).equals(I18nUtils.LANGUAGE_SIMPLIFIED_CHINESE) && OCRUtils.isAbiSupported()){
                textView.append(" ");
                SpannableStringBuilder spannableStringBuilder1 = new SpannableStringBuilder(applicationContext.getResources().getString(R.string.hr_desc_part_2));
                spannableStringBuilder1.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        OCR();
                    }
                }, 0, spannableStringBuilder1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.append(spannableStringBuilder1);
                textView.append(" ");
            }
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

    public void OCR() {
        try {
            if (!sharedPreferences.getBoolean("auto_catch_screen", true)) {
                placeHolder = rootLayout.findViewWithTag("placeHolder");
                placeHolder.removeAllViews();
                int backgroundColor = ThemeUtils.getBackgroundColor(applicationContext, contextThemeWrapper);
                selector = (ScrollView) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.overlay_hr_sub_ocr, null);
                GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, Color.TRANSPARENT});
                gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                assert windowManager != null;
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                int rotation = ScreenUtils.getScreenRotation(applicationContext);
                selector.setBackgroundColor(backgroundColor);
                int width = placeHolder.getWidth();
                int height = placeHolder.getHeight();
                if (rotation == Surface.ROTATION_90){
                    selector.setBackground(gradientDrawable);
                }
                selector.setMinimumHeight(height);
                selector.setMinimumWidth(width);
                selector.getChildAt(0).setMinimumWidth(width);
                selector.getChildAt(0).setMinimumHeight(height);
                placeHolder.addView(selector);
                ((GridLayout) selector.findViewById(R.id.hr_ocr_selector)).removeAllViews();
                Animator animator;
                if (ScreenUtils.getScreenRotationMode(rotation) == ScreenUtils.MODE_PORTRAIT){
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
                                    getOCRResult((Uri) v.getTag(), contextThemeWrapper, false);

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
            }else {
                File file = new File(applicationContext.getExternalCacheDir() + File.separator + "ScreenCapture.jpg");
                if (file.exists()){file.delete();}
                if (!sharedPreferences.getBoolean("tip_allow_background_window", false)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                    AlertDialog dialog = builder.setMessage(R.string.hr_auto_capture_note)
                            .setPositiveButton(R.string.hr_auto_capture_note_confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sharedPreferences.edit().putBoolean("tip_allow_background_window", true).apply();
                                }
                            })
                            .create();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                }else {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                }
                dialog.show();

            }else {
                    overlayService.hideAllComponentsTemporarily();
                    Intent captureIntent = new Intent(applicationContext, ScreenCaptureActivity.class);
                    captureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    applicationContext.startActivity(captureIntent);
                    final int[] count = {0};
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            if (count[0] >= 30){
                                Looper.prepare();
                                Toast.makeText(applicationContext, R.string.hr_time_out, Toast.LENGTH_SHORT).show();
                                overlayService.resumeFloatingWindow();
                                this.cancel();
                                Looper.loop();
                            }
                            final File file = new File(applicationContext.getExternalCacheDir() + File.separator + "ScreenCapture.jpg");
                            if (!file.exists()){
                                count[0]++;
                                return;
                            }


                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showAutoOCRProgress();
                                    getOCRResult(Uri.fromFile(file), contextThemeWrapper, true);
                                }
                            });
                            this.cancel();
                        }
                    }, 1000, 500);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideSubWindow() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        assert windowManager != null;
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = ScreenUtils.getScreenRotation(applicationContext);
        placeHolder = rootLayout.findViewWithTag("placeHolder");
        Animator animator;
        if (ScreenUtils.getScreenRotationMode(rotation) == ScreenUtils.MODE_PORTRAIT){
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

    public void getOCRResult(Uri uri, Context context, final boolean autoCapture) {
        final Handler onResultHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                showOcrResult(msg.arg1);
            }
        };
        String compressedPath = context.getExternalCacheDir() + String.valueOf((int)(Math.random()*100000000)) + ".png";
        try{
            Bitmap bitmap = BitmapFactory.decodeStream(applicationContext.getContentResolver().openInputStream(uri));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(compressedPath));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        selector.removeAllViews();
        showOcrResult(TYPE_PROCESSING);
        GeneralBasicParams params = new GeneralParams();
        params.setDetectDirection(true);
        final File compressedFile = new File(compressedPath);
        params.setImageFile(compressedFile);
        tagList.clear();
        changeQueryMethod(MODE_FUZZY);
        OCR.getInstance(context).recognizeGeneralBasic(params, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(final GeneralResult generalResult) {
                if (autoCapture){
                    if (canceled){
                        canceled = false;
                        overlayService.resumeFloatingWindow();
                        return;
                    }
                }
                if(compressedFile.exists()){
                    compressedFile.delete();
                }
                        for (WordSimple wordSimple : generalResult.getWordList()) {
                            Log.i(TAG, wordSimple.getWords());
                            //wordList.add(wordSimple.getWords());
                            for (String tag : StaticData.HR.tagList){
                                if (wordSimple.getWords().contains(tag)){
                                    tagList.add(wordSimple.getWords());
                                }
                            }
                        }
                        tagList = new ArrayList<>(new HashSet<>(tagList));//去重
                        Log.i(TAG, "OCR result: " + tagList.toString());
                        if (tagList.size() <= 0){
                            hideAutoOCRProgress();
                            overlayService.resumeFloatingWindow();
                            onResultHandler.sendMessage(onResultHandler.obtainMessage(0, TYPE_EMPTY_ERROR , 0));
                            return;
                        }
                        showOcrResult(TYPE_SUCCEED);
                        for (CheckBox checkBox : checkBoxes) {
                            checkBox.setChecked(false);
                            for (String result : tagList) {
                                if (result.equals(checkBox.getText().toString())) {
                                    checkBox.setChecked(true);
                                }
                            }
                        }
                        if (autoCapture){
                            hideAutoOCRProgress();
                            overlayService.resumeFloatingWindow();
                        }
                        scrollToResult();
                        //hideSubWindow();
                        compressedFile.delete();
                        File file = new File(applicationContext.getExternalCacheDir() + File.separator + "ScreenCapture.jpg");
                        if (file.exists()){file.delete();}

            }

            @Override
            public void onError(OCRError ocrError) {
                if (autoCapture){
                    if (canceled){
                        canceled = false;
                        overlayService.resumeFloatingWindow();
                        return;
                    }
                    overlayService.resumeFloatingWindow();
                }
                if(compressedFile.exists()){
                    compressedFile.delete();
                }
                hideAutoOCRProgress();
                ocrError.printStackTrace();
                Toast.makeText(contextThemeWrapper, "出现错误：" + ocrError.getErrorCode(), Toast.LENGTH_SHORT).show();
                int errorCode = ocrError.getErrorCode();
                onResultHandler.sendMessage(onResultHandler.obtainMessage(0, errorCode, 0));
            }
        });

    }


    public void showAutoOCRProgress(){
        autoOCRProgressLayout = (LinearLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.view_auto_ocr_progress, null);
        ProgressBar progressBar = (ProgressBar) autoOCRProgressLayout.findViewById(R.id.auto_ocr_progress);
        Button button = (Button) autoOCRProgressLayout.findViewById(R.id.auto_ocr_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAutoOCR();
            }
        });

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.x = 0;
        layoutParams.y = ScreenUtils.getScreenHeight(applicationContext);
        layoutParams.windowAnimations = R.style.AppTheme_Default_FloatingWindowAnimation;
        autoOCRProgressLayout.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK}));
        windowManager.addView(autoOCRProgressLayout, layoutParams);
    }

    public void cancelAutoOCR(){
        hideAutoOCRProgress();
        canceled = true;
    }

    public void hideAutoOCRProgress(){
        try{
            windowManager.removeViewImmediate(autoOCRProgressLayout);
        }
        catch (Exception ignored) {}
    }

    private void restartService(){
        List<ActivityManager.RunningServiceInfo> runningServiceInfoList = ((ActivityManager)applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE);
        if(runningServiceInfoList.size() > 0){
            for (int i = 0; i < runningServiceInfoList.size(); i++){
                if(runningServiceInfoList.get(i).service.getClassName().equals("com.ssyanhuo.arknightshelper.service.OverlayService")){
                    Intent intent = new Intent(applicationContext, OverlayService.class);
                    applicationContext.stopService(intent);
                    applicationContext.startService(intent);
                    return;
                }
            }
        }
    }

    private void stopService(){
        List<ActivityManager.RunningServiceInfo> runningServiceInfoList = ((ActivityManager)applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE);
        if(runningServiceInfoList.size() > 0){
            for (int i = 0; i < runningServiceInfoList.size(); i++){
                if(runningServiceInfoList.get(i).service.getClassName().equals("com.ssyanhuo.arknightshelper.service.OverlayService")){
                    Intent intent = new Intent(applicationContext, OverlayService.class);
                    applicationContext.stopService(intent);
                    return;
                }
            }
        }
    }



    public void showOcrResult(int type) {
        try{
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
                    errorImage.setMinimumHeight(ScreenUtils.dip2px(applicationContext,144));
                    errorImage.setMinimumWidth(ScreenUtils.dip2px(applicationContext,144));
                    errorImage.setPadding(padding, padding, padding, padding);
                    linearLayout.addView(errorImage);
                    TextView errorText = new TextView(contextThemeWrapper);
                    errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    errorText.setText(applicationContext.getString(R.string.hr_ocr_error) + " (" + type + ")");
                    linearLayout.addView(errorText);
                    selector.addView(linearLayout);
                    break;
                case TYPE_PROCESSING:
                    AnimatedProgressBar animatedProgressBar = new AnimatedProgressBar(contextThemeWrapper);
                    linearLayout.addView(animatedProgressBar);
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
                    succeedImage.setMinimumHeight(ScreenUtils.dip2px(applicationContext,144));
                    succeedImage.setMinimumWidth(ScreenUtils.dip2px(applicationContext,144));
                    succeedImage.setPadding(padding, padding, padding, padding);
                    linearLayout.addView(succeedImage);
                    TextView succeedText = new TextView(contextThemeWrapper);
                    succeedText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    succeedText.setText(applicationContext.getString(R.string.hr_ocr_succeed) + tagList.size());
                    linearLayout.addView(succeedText);
                    selector.addView(linearLayout);
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
        catch (Exception e){
            e.printStackTrace();
        }

    }
    private void scrollToResult(){
        (scrollView).post(new Runnable() {
            @Override
            public void run() {
                (scrollView).smoothScrollTo(0, scrollView.findViewById(R.id.hr_result_scroll_point).getTop() - applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) * 3);
            }
        });
    }
    public static ArrayList<ArrayList<String>> combine (ArrayList<String> array){
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        int size = array.size();
        int nBit = (int) Math.pow(2, size) - 1;
        for (int i = 1; i <= nBit; i++){
            ArrayList<String> tmp = new ArrayList<>();
            for(int j = 0 ; j < size; j++){
                int bit = 1 << j;
                if ((bit & i) != 0){
                    tmp.add(array.get(j));
                }
            }
            Collections.sort(tmp);
            result.add(new ArrayList<String>(tmp));
        }
        return result;
    }
    private void showFab(View fab, Context context){
        fab.setEnabled(true);
        if (fab.getVisibility() != VISIBLE) {
            fab.setVisibility(VISIBLE);
            Animator inAnimation = AnimatorInflater.loadAnimator(context, R.animator.overlay_module_fade_in);
            inAnimation.setTarget(fab);
            inAnimation.start();
        }
    }
    private void hideFab(final View fab, Context context){
        fab.setEnabled(false);
        if (fab.getVisibility() == VISIBLE && !fab.isEnabled()){
            Animator outAnimation = AnimatorInflater.loadAnimator(context, R.animator.overlay_module_fade_out);
            outAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fab.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            outAnimation.setTarget(fab);
            outAnimation.start();
        }
    }
}
