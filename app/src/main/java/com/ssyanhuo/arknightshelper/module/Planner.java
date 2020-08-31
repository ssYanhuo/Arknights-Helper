package com.ssyanhuo.arknightshelper.module;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.cardview.widget.CardView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.service.PythonService;
import com.ssyanhuo.arknightshelper.utils.ScreenUtils;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.JSONUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;
import com.ssyanhuo.arknightshelper.widget.PlannerDetailView;
import com.ssyanhuo.arknightshelper.widget.PlannerItemView;
import com.zyyoona7.popup.EasyPopup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Planner {
    private Context applicationContext;
    private View contentView;
    private RelativeLayout relativeLayout_planner;
    private LinearLayout backgroundLayout;
    private OverlayService overlayService;
    private IBinder pythonService;
    private JSONObject material;
    private ArrayList<JSONObject> materials;
    private ScrollView scrollView;
    private LinearLayout addItemLinearLayout;
    private LinearLayout itemContainer;
    private LinearLayout resultContainer;
    private LinearLayout versionInfo;
    private Map<String, JSONObject> itemMap;
    private ArrayList<String> itemList;
    private ArrayList<String> itemException;
    private Map<String, Integer> requiredMaterials;
    private final String TAG = "Planner";
    private PythonService.PythonBinder[] newBinder;
    JSONObject resultObject;
    private LinearLayout resultLoot;
    private LinearLayout resultSynthesis;
    public void init(final ContextThemeWrapper context, View view, RelativeLayout relativeLayout, LinearLayout backgroundLayout, final OverlayService overlayService, final IBinder pythonService){

        this.applicationContext = context;
        this.contentView = view;
        this.relativeLayout_planner = relativeLayout;
        this.backgroundLayout = backgroundLayout;
        this.overlayService = overlayService;
        this.pythonService = pythonService;
        //Log.e("Planner", pythonService.toString());
        itemMap = new HashMap<>();
        requiredMaterials = new HashMap<>();
        materials = new ArrayList<>();
        itemException = new ArrayList<>();
            try {
                itemList = new ArrayList<>(Arrays.asList(applicationContext.getResources().getStringArray(R.array.planner_materials)));
                material = JSONUtils.getJSONObject(applicationContext, FileUtils.readData("material.json", applicationContext));
                for (Map.Entry entry :
                        material.entrySet()) {
                    materials.add((JSONObject) entry.getValue());
                }
                for (int i = 0; i < materials.size(); i++) {
                    if (itemList.contains(materials.get(i).getString("name"))){
                        itemMap.put(materials.get(i).getString("name"), materials.get(i));
                    }
                }
            } catch (IOException ignored) {

            }
        scrollView = relativeLayout.findViewById(R.id.scroll_planner);
            addItemLinearLayout = contentView.findViewById(R.id.planner_add_item);
            itemContainer = contentView.findViewById(R.id.planner_item_container);
        final Button queryButton = contentView.findViewById(R.id.planner_request);
        final LinearLayout loadingNote = contentView.findViewById(R.id.planner_loading);
        resultContainer = contentView.findViewById(R.id.planner_result);
        versionInfo = contentView.findViewById(R.id.planner_version_info);
        resultLoot = contentView.findViewById(R.id.planner_result_loot);
        resultSynthesis = contentView.findViewById(R.id.planner_result_synthesis);
//        resultItems = contentView.findViewById(R.id.planner_result_items);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getResult();
            }
        });
            addItemLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int padding = applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) / 2;
                    final EasyPopup easyPopup = EasyPopup.create(applicationContext);
                    CardView cardView = new CardView(applicationContext);
                    cardView.setCardBackgroundColor(ThemeUtils.getBackgroundColor(applicationContext, context));
                    LinearLayout itemSelector = new LinearLayout(applicationContext);
                    itemSelector.setOrientation(LinearLayout.VERTICAL);
                    final ScrollView listScrollView = new ScrollView(applicationContext);
                    ArrayList<String> tempArray = (ArrayList<String>) itemList.clone();
                    tempArray.removeAll(itemException);
                    for (String item :
                            tempArray) {
                        final LinearLayout linearLayout = new LinearLayout(applicationContext);
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        ImageView imageView = new ImageView(applicationContext);
                        final TextView textView = new TextView(applicationContext);
                        linearLayout.setPadding(padding, padding, padding, padding);
                        textView.setText(item);
                        linearLayout.setBackground(new RippleDrawable(ColorStateList.valueOf(Color.GRAY), null, null));
                        linearLayout.setClickable(true);
                        linearLayout.setFocusable(true);
                        linearLayout.setTag(itemMap.get(item));
                        linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence name = ((TextView)textView).getText();
                                Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(((JSONObject) linearLayout.getTag()).getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                                PlannerItemView plannerItemView = new PlannerItemView(applicationContext, drawable, name);
                                itemContainer.addView(plannerItemView);
                                itemException.add((String) name);
                                easyPopup.dismiss();
                                //getResult();
                                scrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int y;
                                        y = itemContainer.getBottom() - scrollView.getHeight() + ScreenUtils.dip2px(applicationContext, 64);
                                        if (y > 0){
                                            scrollView.smoothScrollTo(0, y);
                                        }

                                    }
                                });

                            }
                        });
                        imageView.setImageDrawable(applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(((JSONObject) linearLayout.getTag()).getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName())));
                        linearLayout.addView(imageView);
                        linearLayout.addView(textView);
                        itemSelector.addView(linearLayout);
                    }
                    listScrollView.addView(itemSelector);
                    cardView.addView(listScrollView);
                    easyPopup.setContentView(cardView)
                            .setHeight(ScreenUtils.dip2px(applicationContext, 256))
                            .showAsDropDown(v, (v.getWidth() - cardView.getWidth()) / 2, 0);
            }
});

            final Handler enableButtonHandler = new Handler();

        newBinder = new PythonService.PythonBinder[1];
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PythonService.PythonBinder pythonBinder = overlayService.getPythonService();
                if (pythonBinder != null){
                    newBinder[0] = pythonBinder;
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            queryButton.setEnabled(true);
                            loadingNote.setVisibility(View.GONE);
                        }
                    };
                    enableButtonHandler.post(runnable);
                    this.cancel();
                }
            }
        }, 1000, 1000);
    }
    private void pinWindow(){
        ArrayList<View> views = new ArrayList<>();
        JSONArray lootArray = resultObject.getJSONArray("loot");
        JSONArray synthesisArray = resultObject.getJSONArray("synthesis");
//            JSONArray itemsArray = resultObject.getJSONArray("items");
        int padding = applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        TextView lootText = new TextView(applicationContext);
        lootText.setText(R.string.planner_result_loot_title);
        lootText.setPadding(0, padding, 0, 0);
        TextView synthesisText = new TextView(applicationContext);
        synthesisText.setText(R.string.planner_result_synthesis_title);
        synthesisText.setPadding(0, padding,0,0);
        views.add(lootText);
        for (int i = 0; i < lootArray.size(); i++) {
            JSONObject obj = lootArray.getJSONObject(i);
            PlannerDetailView plannerDetailView = new PlannerDetailView(applicationContext);
            if (obj.getFloat("times") < 0.5){continue;}
            plannerDetailView.setTitleText(obj.getString("stage") + " " + (int)Math.ceil(obj.getFloat("times")) + applicationContext.getString(R.string.planner_times));
            JSONArray items = obj.getJSONArray("items");
            for (int j = 0; j < items.size(); j++) {
                JSONObject item = items.getJSONObject(j);
                Map.Entry<String, Object> entry= item.entrySet().iterator().next();
                if (Double.parseDouble(entry.getValue().toString()) <= 0){continue;}
                plannerDetailView.appendContent(entry.getKey() + "  " + entry.getValue());
            }
            views.add(plannerDetailView);
        }
        views.add(synthesisText);
        for (int i = 0; i < synthesisArray.size(); i++) {
            JSONObject obj = synthesisArray.getJSONObject(i);
            PlannerDetailView plannerDetailView = new PlannerDetailView(applicationContext);
            if (obj.getFloat("count") < 0.5){continue;}
            plannerDetailView.setTitleText(obj.getString("target") + " " + (int)Math.ceil(obj.getFloat("count")) + applicationContext.getString(R.string.planner_pcs));
            JSONArray items = obj.getJSONArray("items");
            for (int j = 0; j < items.size(); j++) {
                JSONObject item = items.getJSONObject(j);
                Map.Entry<String, Object> entry= item.entrySet().iterator().next();
                if (Double.parseDouble(entry.getValue().toString()) <= 0){continue;}
                plannerDetailView.appendContent(entry.getKey() + "  " + entry.getValue());
            }
            views.add(plannerDetailView);
        }

        overlayService.showPinnedWindow(views);
    }
    public void addItems(Set<Map.Entry<String, Object>> items){
        for (Map.Entry entry :
                items) {
            CharSequence name = (CharSequence) entry.getKey();
            Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(((JSONObject) itemMap.get(name)).getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
            PlannerItemView plannerItemView = new PlannerItemView(applicationContext, drawable, name);
            plannerItemView.setNum((Integer) entry.getValue());
            itemContainer.addView(plannerItemView);
            itemException.add((String) name);
        }

    }
    private void getResult(){
        JSONObject required = new JSONObject();
        for (int i = 0; i < itemContainer.getChildCount(); i++) {
            PlannerItemView plannerItemView = (PlannerItemView) itemContainer.getChildAt(i);
            requiredMaterials.put(plannerItemView.getName(), plannerItemView.getNum());
            required.put(plannerItemView.getName(), plannerItemView.getNum());
        }
        if (required.size() == 0){
            return;
        }
        Log.e("", required.toString());
        if (pythonService == null){
            pythonService = newBinder[0];
        }
        String result = ((PythonService.PythonBinder) pythonService).callArkplanner(required.toJSONString());
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(applicationContext.getString(R.string.planner_pin));
        spannableStringBuilder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                overlayService.hideFloatingWindow();
                pinWindow();
            }
        }, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) resultContainer.findViewById(R.id.planner_pin)).setText(spannableStringBuilder);
        ((TextView) resultContainer.findViewById(R.id.planner_pin)).setMovementMethod(LinkMovementMethod.getInstance());
        try{
            ((TextView) versionInfo.findViewById(R.id.planner_error)).setText("");
            resultObject = JSON.parseObject(result);
            JSONArray lootArray = resultObject.getJSONArray("loot");
            JSONArray synthesisArray = resultObject.getJSONArray("synthesis");
//            JSONArray itemsArray = resultObject.getJSONArray("items");
            for (int i = 0; i < lootArray.size(); i++) {
                JSONObject obj = lootArray.getJSONObject(i);
                PlannerDetailView plannerDetailView = new PlannerDetailView(applicationContext);
                if (obj.getFloat("times") < 0.5){continue;}
                //我不知道为啥结果里面有LS-5
                if(obj.getString("stage").equals("LS-5")){continue;}
                plannerDetailView.setTitleText(obj.getString("stage") + " " + (int)Math.ceil(obj.getFloat("times")) + applicationContext.getString(R.string.planner_times));
                JSONArray items = obj.getJSONArray("items");
                for (int j = 0; j < items.size(); j++) {
                    JSONObject item = items.getJSONObject(j);
                    Map.Entry<String, Object> entry= item.entrySet().iterator().next();
                    if (Double.parseDouble(entry.getValue().toString()) <= 0){continue;}
                    plannerDetailView.appendContent(entry.getKey() + "  " + entry.getValue());
                }
                resultLoot.addView(plannerDetailView);
            }
            for (int i = 0; i < synthesisArray.size(); i++) {
                JSONObject obj = synthesisArray.getJSONObject(i);
                PlannerDetailView plannerDetailView = new PlannerDetailView(applicationContext);
                if (obj.getFloat("count") < 0.5){continue;}
                plannerDetailView.setTitleText(obj.getString("target") + " " + (int)Math.ceil(obj.getFloat("count")) + applicationContext.getString(R.string.planner_pcs));
                JSONArray items = obj.getJSONArray("items");
                for (int j = 0; j < items.size(); j++) {
                    JSONObject item = items.getJSONObject(j);
                    Map.Entry<String, Object> entry= item.entrySet().iterator().next();
                    if (Double.parseDouble(entry.getValue().toString()) <= 0){continue;}
                    plannerDetailView.appendContent(entry.getKey() + "  " + entry.getValue());
                }
                resultSynthesis.addView(plannerDetailView);
            }
            resultContainer.setVisibility(View.VISIBLE);
            versionInfo.setVisibility(View.GONE);
//            for (int i = 0; i < itemsArray.size(); i++) {
//                JSONObject item = itemsArray.getJSONObject(i);
//                Map.Entry<String, Object> entry= item.entrySet().iterator().next();
//                resultItems.append(entry.getKey() + "  " + entry.getValue() + "\n");
//            }
        }catch (Exception e){
            Log.e(TAG, "Get result failed:" + "\n" + result);
            e.printStackTrace();
            resultContainer.setVisibility(View.GONE);
            versionInfo.setVisibility(View.VISIBLE);
            ((TextView) versionInfo.findViewById(R.id.planner_error)).setText(e.toString());
        }
        //Log.e("", result);
    }

}
