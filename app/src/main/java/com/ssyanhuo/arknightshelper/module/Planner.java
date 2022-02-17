package com.ssyanhuo.arknightshelper.module;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Message;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.cardview.widget.CardView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.utils.ScreenUtils;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.JSONUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;
import com.ssyanhuo.arknightshelper.widget.AnimatedProgressBar;
import com.ssyanhuo.arknightshelper.widget.PlannerDetailView;
import com.ssyanhuo.arknightshelper.widget.PlannerItemView;
import com.zyyoona7.popup.EasyPopup;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class Planner {
    private Context applicationContext;
    private View contentView;
    private RelativeLayout relativeLayout_planner;
    private LinearLayout backgroundLayout;
    private OverlayService overlayService;
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
    JSONObject resultObject;
    private LinearLayout resultLoot;
    private LinearLayout resultSynthesis;
    private FloatingActionButton fab;
    private AnimatedProgressBar progressBar;

    public void init(final ContextThemeWrapper context, View view, RelativeLayout relativeLayout, LinearLayout backgroundLayout, final OverlayService overlayService) {

        this.applicationContext = context;
        this.contentView = view;
        this.relativeLayout_planner = relativeLayout;
        this.backgroundLayout = backgroundLayout;
        this.overlayService = overlayService;
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
                if (itemList.contains(materials.get(i).getString("name"))) {
                    itemMap.put(materials.get(i).getString("name"), materials.get(i));
                }
            }
        } catch (IOException ignored) {
        }
        scrollView = relativeLayout.findViewById(R.id.scroll_planner);
        addItemLinearLayout = contentView.findViewById(R.id.planner_add_item);
        itemContainer = contentView.findViewById(R.id.planner_item_container);
        resultContainer = contentView.findViewById(R.id.planner_result);
        versionInfo = contentView.findViewById(R.id.planner_info);
        resultLoot = contentView.findViewById(R.id.planner_result_loot);
        resultSynthesis = contentView.findViewById(R.id.planner_result_synthesis);
        fab = relativeLayout.findViewById(R.id.planner_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getResult();
            }
        });
        progressBar = relativeLayout.findViewById(R.id.planner_loading);

        addItemLinearLayout.setOnClickListener(v -> {
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
                linearLayout.setOnClickListener(v1 -> {
                    final CharSequence name = textView.getText();
                    Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(((JSONObject) linearLayout.getTag()).getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                    final PlannerItemView plannerItemView = new PlannerItemView(applicationContext, drawable, name);
                    plannerItemView.setOnButtonClickListener(v11 -> {
                        plannerItemView.removeItem();
                        itemException.remove(name);
                    });
                    itemContainer.addView(plannerItemView);
                    itemException.add((String) name);
                    easyPopup.dismiss();
                    //getResult();
                    scrollView.post(() -> {
                        int y;
                        y = itemContainer.getBottom() - scrollView.getHeight() + ScreenUtils.dip2px(applicationContext, 64);
                        if (y > 0) {
                            scrollView.smoothScrollTo(0, y);
                        }
                    });
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
        });
    }

    private void pinWindow() {
        ArrayList<View> views = new ArrayList<>();
        JSONArray lootArray = resultObject.getJSONArray("stages");
        JSONArray synthesisArray = resultObject.getJSONArray("syntheses");
        int padding = applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        TextView lootText = new TextView(applicationContext);
        lootText.setText(R.string.planner_result_loot_title);
        lootText.setPadding(0, padding, 0, 0);
        TextView synthesisText = new TextView(applicationContext);
        synthesisText.setText(R.string.planner_result_synthesis_title);
        synthesisText.setPadding(0, padding, 0, 0);
        views.add(lootText);
        for (int i = 0; i < lootArray.size(); i++) {
            JSONObject obj = lootArray.getJSONObject(i);
            PlannerDetailView plannerDetailView = new PlannerDetailView(applicationContext);
            if (obj.getFloat("count") < 0.5) {
                continue;
            }
            plannerDetailView.setTitleText(obj.getString("stage") + " " + (int) Math.ceil(obj.getFloat("count")) + applicationContext.getString(R.string.planner_times));
            JSONObject items = obj.getJSONObject("items");
            for(Map.Entry<String, Object> entry : items.entrySet()) {
                if (Double.parseDouble(entry.getValue().toString()) <= 0) {
                    continue;
                }
                plannerDetailView.appendContent(entry.getKey() + "  " + entry.getValue());
            }
            views.add(plannerDetailView);
        }
        views.add(synthesisText);
        for (int i = 0; i < synthesisArray.size(); i++) {
            JSONObject obj = synthesisArray.getJSONObject(i);
            PlannerDetailView plannerDetailView = new PlannerDetailView(applicationContext);
            if (obj.getFloat("count") < 0.5) {
                continue;
            }
            plannerDetailView.setTitleText(obj.getString("target") + " " + (int) Math.ceil(obj.getFloat("count")) + applicationContext.getString(R.string.planner_pcs));
            JSONObject items = obj.getJSONObject("materials");
            for(Map.Entry<String, Object> entry : items.entrySet()) {
                if (Double.parseDouble(entry.getValue().toString()) <= 0) {
                    continue;
                }
                plannerDetailView.appendContent(entry.getKey() + "  " + entry.getValue());
            }
            views.add(plannerDetailView);
        }

        overlayService.showPinnedWindow(views);
    }

    public void addItems(Set<Map.Entry<String, Object>> items) {
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

    private void getResult() {
        hideFab(fab, applicationContext);
        JSONObject requestJson = new JSONObject();
        JSONObject required = new JSONObject();
        for (int i = 0; i < itemContainer.getChildCount(); i++) {
            PlannerItemView plannerItemView = (PlannerItemView) itemContainer.getChildAt(i);
            requiredMaterials.put(plannerItemView.getName(), plannerItemView.getNum());
            required.put(plannerItemView.getName(), plannerItemView.getNum());
        }
        if (required.size() == 0) {
            return;
        }
        requestJson.put("required", required);
        final String[] result = {""};
        final String URL = "https://service-62eb1sz2-1259458632.gz.apigw.tencentcs.com/release/plan";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(TYPE_JSON, requestJson.toJSONString());
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Message message = Message.obtain();
                message.what = 0;
                message.obj = e.getMessage();
                Log.e(TAG, "response: " + message.obj.toString());
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        showFab(fab, applicationContext);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = response.body().string();//string不能调用两次 被调用一次就关闭了，这里调用两次会报异常
                int code = response.code();
                result[0] = message.obj.toString();
                contentView.post(() -> {
                    showFab(fab, applicationContext);
                    try {
                        if(code != 200){
                            throw new IOException("刷图规划服务超时或出现错误，请重试：" + code);
                        }
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

                        ((TextView) versionInfo.findViewById(R.id.planner_error)).setText("");
                        resultObject = JSON.parseObject(result[0]);
                        Log.e(TAG, resultObject.toJSONString());
                        JSONArray lootArray = resultObject.getJSONArray("stages");
                        JSONArray synthesisArray = resultObject.getJSONArray("syntheses");
                        for (int i = 0; i < lootArray.size(); i++) {
                            JSONObject obj = lootArray.getJSONObject(i);
                            PlannerDetailView plannerDetailView = new PlannerDetailView(applicationContext);
                            if (obj.getFloat("count") < 0.5) {
                                continue;
                            }
                            plannerDetailView.setTitleText(obj.getString("stage") + " " + (int) Math.ceil(obj.getFloat("count")) + applicationContext.getString(R.string.planner_times));
                            JSONObject items = obj.getJSONObject("items");
                            for(Map.Entry<String, Object> entry : items.entrySet()) {
                                plannerDetailView.appendContent(entry.getKey() + "  " + entry.getValue());
                            }
                                resultLoot.addView(plannerDetailView);
                        }
                        for (int i = 0; i < synthesisArray.size(); i++) {
                            JSONObject obj = synthesisArray.getJSONObject(i);
                            PlannerDetailView plannerDetailView = new PlannerDetailView(applicationContext);
                            if (obj.getFloat("count") < 0.5) {
                                continue;
                            }
                            plannerDetailView.setTitleText(obj.getString("target") + " " + (int) Math.ceil(obj.getFloat("count")) + applicationContext.getString(R.string.planner_pcs));
                            JSONObject items = obj.getJSONObject("materials");
                            for(Map.Entry<String, Object> entry : items.entrySet()) {
                                plannerDetailView.appendContent(entry.getKey() + "  " + entry.getValue());
                            }
                            resultSynthesis.addView(plannerDetailView);
                        }
                        resultContainer.setVisibility(View.VISIBLE);
                        versionInfo.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Log.e(TAG, "Get result failed:" + "\n" + result[0]);
                        e.printStackTrace();
                        resultContainer.setVisibility(View.GONE);
                        ((TextView) versionInfo.findViewById(R.id.planner_error)).setText(e.toString());
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
