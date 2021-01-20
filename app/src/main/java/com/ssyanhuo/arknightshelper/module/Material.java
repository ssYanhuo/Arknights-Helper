package com.ssyanhuo.arknightshelper.module;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.service.OverlayService;
import com.ssyanhuo.arknightshelper.utils.FileUtils;
import com.ssyanhuo.arknightshelper.utils.JSONUtils;
import com.ssyanhuo.arknightshelper.utils.PythonUtils;
import com.ssyanhuo.arknightshelper.utils.ScreenUtils;
import com.ssyanhuo.arknightshelper.utils.ThemeUtils;
import com.ssyanhuo.arknightshelper.utils.I18nUtils;
import com.ssyanhuo.arknightshelper.widget.ItemDetailView;
import com.ssyanhuo.arknightshelper.widget.LineWrapLayout;
import com.ssyanhuo.arknightshelper.widget.NumberSelector;
import com.zyyoona7.popup.EasyPopup;

import java.io.IOException;
import java.util.*;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class Material {
    public ArrayList<NumberSelector> numberSelectors;
    private NumberSelector stageNow;
    private NumberSelector levelNow;
    private NumberSelector pointNow;
    private NumberSelector stageTarget;
    private NumberSelector levelTarget;
    private NumberSelector skillAllNow;
    private NumberSelector skillAllTarget;
    private NumberSelector skill1Now;
    private NumberSelector skill1Target;
    private NumberSelector skill2Now;
    private NumberSelector skill2Target;
    private NumberSelector skill3Now;
    private NumberSelector skill3Target;
    private LinearLayout skillAllContainer;
    private LinearLayout skill1Container;
    private LinearLayout skill2Container;
    private LinearLayout skill3Container;
    private CheckBox skillAllCheckBox;
    private CheckBox skill1CheckBox;
    private CheckBox skill2CheckBox;
    private CheckBox skill3CheckBox;
    private CheckBox levelCheckbox;
    private String expJsonString;
    private JSONObject expJsonObject;
    private String characterJsonString;
    private JSONObject characterJsonObject;
    private int characterStar;
    private boolean showedOnce = false;
    private ScrollView selector;
    private Context applicationContext;
    //TODO 删除下面这个变量
    private Map<String, Integer> characterIndexMap = new HashMap<>();
    private final String TAG = "Material";
    private LinearLayout rootLayout;
    private LinearLayout placeHolder;
    private View contentView;
    private EasyPopup easyPopup;
    private JSONObject charNow;
    private JSONObject materialJsonObject;
    private boolean onlyRare;
    private SharedPreferences sharedPreferences;
    private ContextThemeWrapper contextThemeWrapper;
    WindowManager windowManager;
    I18nUtils.Helper nameHelper;
    private LinearLayout resultContent;
    private OverlayService overlayService;

    public void getAllNumberSelectors(View view){
        ViewGroup viewGroup = (ViewGroup)view;
        for(int i = 0; i < viewGroup.getChildCount(); i++){
            if(viewGroup.getChildAt(i) instanceof NumberSelector){
                numberSelectors.add((NumberSelector) viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof HorizontalScrollView){
                getAllNumberSelectors(viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LinearLayout){
                getAllNumberSelectors(viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LineWrapLayout){
                getAllNumberSelectors(viewGroup.getChildAt(i));
            }
        }
    }
    public void init(final Context context, final View view, LinearLayout backgroundLayout, OverlayService overlayService){
        this.overlayService = overlayService;
        charNow = null;
        applicationContext = context;
        rootLayout = backgroundLayout;
        contentView = view;
        sharedPreferences = applicationContext.getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", Context.MODE_PRIVATE);
        try {
            expJsonObject = JSONUtils.getJSONObject(applicationContext, FileUtils.readData("aklevel.json", applicationContext));
            characterJsonObject = JSONUtils.getJSONObject(applicationContext, FileUtils.readData("charMaterials.json", applicationContext));
            materialJsonObject = JSONUtils.getJSONObject(applicationContext, FileUtils.readData("material.json", applicationContext));
        } catch (IOException e) {
            e.printStackTrace();
        }
        numberSelectors = new ArrayList<>();
        stageNow = view.findViewById(R.id.material_selector_stage_now);
        levelNow = view.findViewById(R.id.material_selector_level_now);
        stageTarget = view.findViewById(R.id.material_selector_stage_target);
        levelTarget = view.findViewById(R.id.material_selector_level_target);
        skillAllNow = view.findViewById(R.id.material_selector_skill_all_now);
        skillAllTarget = view.findViewById(R.id.material_selector_skill_all_target);
        skill1Now = view.findViewById(R.id.material_selector_skill_1_now);
        skill1Target = view.findViewById(R.id.material_selector_skill_1_target);
        skill2Now = view.findViewById(R.id.material_selector_skill_2_now);
        skill2Target = view.findViewById(R.id.material_selector_skill_2_target);
        skill3Now = view.findViewById(R.id.material_selector_skill_3_now);
        skill3Target = view.findViewById(R.id.material_selector_skill_3_target);
        skillAllContainer = view.findViewById(R.id.material_skill_all_container);
        skill1Container = view.findViewById(R.id.material_skill_1_container);
        skill2Container = view.findViewById(R.id.material_skill_2_container);
        skill3Container = view.findViewById(R.id.material_skill_3_container);
        skillAllCheckBox = view.findViewById(R.id.material_skill_all_checkBox);
        skill1CheckBox = view.findViewById(R.id.material_skill_1_checkBox);
        skill2CheckBox = view.findViewById(R.id.material_skill_2_checkBox);
        skill3CheckBox = view.findViewById(R.id.material_skill_3_checkBox);
        skillAllCheckBox.setChecked(false);
        skill1CheckBox.setChecked(false);
        skill2CheckBox.setChecked(false);
        skill3CheckBox.setChecked(false);
        skillAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showResult();
            }
        });
        skill1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showResult();
            }
        });
        skill2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showResult();
            }
        });
        skill3CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showResult();
            }
        });
        levelCheckbox = view.findViewById(R.id.material_level_checkBox);
        levelCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showResult();
            }
        });
        contextThemeWrapper = new ContextThemeWrapper(applicationContext, ThemeUtils.getThemeId(ThemeUtils.THEME_UNSPECIFIED, ThemeUtils.TYPE_FLOATING_WINDOW, applicationContext));
        getAllNumberSelectors(view);
        for (int i = 0; i < numberSelectors.size(); i++){
            numberSelectors.get(i).editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    checkValue();
                    if (charNow != null){showResult();}

                }
            });
        }
        try {
            nameHelper = new I18nUtils().getHelper(applicationContext, I18nUtils.CATEGORY_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        selector = (ScrollView)LayoutInflater.from(contextThemeWrapper).inflate(R.layout.overlay_material_sub_selector, null);
        ArrayList<String> characters = new ArrayList<>(characterJsonObject.keySet());
        for(int i = 0; i < characters.size(); i++){
            String name = characters.get(i);
            if (nameHelper.isHidden(name, I18nUtils.FILTER_ALL)){
                continue;
            }
            JSONObject jsonObject = characterJsonObject.getJSONObject(name);
            if(jsonObject.getString("profession").equals("其它")){continue;}
            characterIndexMap.put(name, i);
            Button button = new Button(applicationContext);
            button.setText(nameHelper.get(name));
            button.setMinWidth(applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
            button.setTextColor(Color.BLACK);
            button.setTag(jsonObject);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCharacterSelected(v);
                }
            });
            switch (jsonObject.getInteger("rarity")){
                case 5:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_yellow));
                    ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_6)).addView(button);
                    break;
                case 4:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_red));
                    ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_5)).addView(button);
                    break;
                case 3:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_blue));
                    ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_4)).addView(button);
                    break;
                case 2:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_green));
                    ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_3)).addView(button);
                    break;
                case 1:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_lime));
                    ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_2)).addView(button);
                    break;
                case 0:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_lime));
                    ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_1)).addView(button);
                    break;
                default:
                    button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_blue));
                    break;
            }
        }
        ((Switch)contentView.findViewById(R.id.material_rare)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onlyRare = isChecked;
                if(charNow != null){
                    checkValue();
                    showResult();
                }
            }
        });
        int backgroundColor = ThemeUtils.getBackgroundColor(applicationContext, contextThemeWrapper);
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, Color.TRANSPARENT});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        selector.setBackground(gradientDrawable);
        WindowManager windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        assert windowManager != null;
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (rotation == 0 || rotation == 3) {
            selector.setBackgroundColor(backgroundColor);
        }
    }


    public void refresh(){

    }

    public void checkValue(){//检查取值并解析技能
        //不同星级时，精英化（目标+当前）最值随之变化，若变化前已经超出新值范围，则自动缩小为最值
        int maxStage = getMaxStage(characterStar + 1);
        if (levelNow.getInt() == 0){
            levelNow.setInt(1);
        }
        if (levelNow.getInt() == 0){
            levelNow.setInt(levelNow.getInt() + 1);
        }
        stageNow.setMax(maxStage);
        if(stageNow.getInt() > maxStage){
            stageNow.setInt(maxStage);
        }
        stageTarget.setMax(maxStage);
        if(stageTarget.getInt() > maxStage){
            stageTarget.setInt(maxStage);
        }
        //目标精英化不小于当前
        if(stageTarget.getInt() < stageNow.getInt()){
            stageTarget.setInt(stageNow.getInt());
        }
        //同一精英化时，目标等级大于当前
        if(stageNow.getInt() == stageTarget.getInt() && levelTarget.getInt() <= levelNow.getInt()){
            levelTarget.setInt(levelNow.getInt() + 1);
        }
        //不同精英化时，等级（目标+当前）最值随之变化，若变化前已经超出新值范围，则自动缩小为最值，当前等级最大为最大等级减一
        //先做精英化阶段修正，再获取最大等级，不然会GG
        int maxLevelNow = expJsonObject.getJSONArray("maxLevel").getJSONArray(characterStar).getInteger(stageNow.getInt());
        int maxLevelTarget = expJsonObject.getJSONArray("maxLevel").getJSONArray(characterStar).getInteger(stageTarget.getInt());
        if (stageTarget.getInt() > stageNow.getInt()){
            levelNow.setMax(maxLevelNow);
            if(levelNow.getInt() > maxLevelNow){
                levelNow.setInt(maxLevelNow);
            }
        }else {
            levelNow.setMax(maxLevelNow - 1);
            if(levelNow.getInt() > maxLevelNow - 1){
                levelNow.setInt(maxLevelNow - 1);
            }
        }
        levelTarget.setMax(maxLevelTarget);
        if(levelTarget.getInt() > maxLevelTarget){
            levelTarget.setInt(maxLevelTarget);
        }

        if (skill3Now.getInt() >= skill3Target.getInt()){
            skill3Now.setInt(skill3Target.getInt() - 1);
        }
        if (skill2Now.getInt() >= skill2Target.getInt()){
            skill2Now.setInt(skill2Target.getInt() - 1);
        }
        if (skill1Now.getInt() >= skill1Target.getInt()){
            skill1Now.setInt(skill1Target.getInt() - 1);
        }
        if (skillAllNow.getInt() >= skillAllTarget.getInt()){
            skillAllNow.setInt(skillAllTarget.getInt() - 1);
        }
    }
    @SuppressLint("SetTextI18n")
    public void showResult(){
        int money = 0;
        int exp = 0;
        int moneyEvolve = 0;
        int moneyUpgrade = 0;
        int moneyRound = 0;
        int expRound = 0;
        int moneyStamina = 0;
        int expStamina = 0;
        int stamina = 0;
        resultContent = contentView.findViewById(R.id.material_result_content);
        resultContent.removeAllViews();
        SpannableStringBuilder spannableStringBuilder1 = new SpannableStringBuilder(applicationContext.getString(R.string.material_pin));
        spannableStringBuilder1.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                overlayService.hideFloatingWindow();
                pinWindow();
            }
        }, 0, spannableStringBuilder1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) contentView.findViewById(R.id.material_pin)).setText(spannableStringBuilder1);
        ((TextView) contentView.findViewById(R.id.material_pin)).setMovementMethod(LinkMovementMethod.getInstance());
        if ((PythonUtils.isSupported() && sharedPreferences.getBoolean("disable_planner", false)) ) {//TODO 暂时禁用了刷图规划，逻辑似乎有问题|| true
            ((TextView) contentView.findViewById(R.id.material_plan)).setVisibility(GONE);
        } else {
            SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(applicationContext.getString(R.string.material_go_planner));
            spannableStringBuilder2.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    plan();
                }
            }, 0, spannableStringBuilder2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) contentView.findViewById(R.id.material_plan)).setText(spannableStringBuilder2);
            ((TextView) contentView.findViewById(R.id.material_plan)).setMovementMethod(LinkMovementMethod.getInstance());
        }
        ItemDetailView itemDetailView;
        if (levelCheckbox.isChecked() && charNow != null){
            int stageFrom = stageNow.getInt();
            int stageTo = stageTarget.getInt();
            int levelFrom = levelNow.getInt();
            int levelTo = levelTarget.getInt();
            if (stageTo - stageFrom <= 0){
                moneyEvolve = 0;
            }else if(stageTo - stageFrom >= 2){
                moneyEvolve = expJsonObject.getJSONArray("evolveGoldCost").getJSONArray(characterStar).getInteger(0) + expJsonObject.getJSONArray("evolveGoldCost").getJSONArray(characterStar).getInteger(1);
            }else if(stageTo - stageFrom == 1){
                if (stageFrom == 0){
                    moneyEvolve = expJsonObject.getJSONArray("evolveGoldCost").getJSONArray(characterStar).getInteger(0);
                }else if(stageFrom == 1){
                    moneyEvolve = expJsonObject.getJSONArray("evolveGoldCost").getJSONArray(characterStar).getInteger(1);
                }
            }
            for (int stage = stageFrom; stage <= stageTo; stage++){
                boolean isLastStage = stage == stageTo;
                boolean isFirstStage = stage ==stageFrom;
                if(isFirstStage && isLastStage){
                    for (int i = levelFrom; i < levelTo; i++){
                        exp += expJsonObject.getJSONArray("characterExpMap").getJSONArray(stage).getInteger(i - 1);
                        moneyUpgrade += expJsonObject.getJSONArray("characterUpgradeCostMap").getJSONArray(stage).getInteger(i - 1);
                    }
                }else if(isFirstStage){
                    for (int i = levelFrom; i < expJsonObject.getJSONArray("maxLevel").getJSONArray(characterStar).getInteger(stage); i++){
                        exp += expJsonObject.getJSONArray("characterExpMap").getJSONArray(stage).getInteger(i - 1);
                        moneyUpgrade += expJsonObject.getJSONArray("characterUpgradeCostMap").getJSONArray(stage).getInteger(i - 1);
                    }
                }else  if(isLastStage){
                    for (int i = 1; i < levelTo; i++){
                        exp += expJsonObject.getJSONArray("characterExpMap").getJSONArray(stage).getInteger(i - 1);
                        moneyUpgrade += expJsonObject.getJSONArray("characterUpgradeCostMap").getJSONArray(stage).getInteger(i - 1);
                    }
                }else {
                    for (int i = 1; i < expJsonObject.getJSONArray("maxLevel").getJSONArray(characterStar).getInteger(stage); i++){
                        exp += expJsonObject.getJSONArray("characterExpMap").getJSONArray(stage).getInteger(i - 1);
                        moneyUpgrade += expJsonObject.getJSONArray("characterUpgradeCostMap").getJSONArray(stage).getInteger(i - 1);
                    }
                }
            }
            expRound = (exp % StaticData.Exp.ExpLevel.LS_5 == 0) ? (exp / StaticData.Exp.ExpLevel.LS_5) : (exp / StaticData.Exp.ExpLevel.LS_5 + 1);
            money = moneyEvolve + moneyUpgrade;
            moneyRound = ((money - expRound * StaticData.Exp.MoneyLevel.LS_5) % StaticData.Exp.MoneyLevel.CE_5 == 0) ? ((money - expRound * StaticData.Exp.MoneyLevel.LS_5) / StaticData.Exp.MoneyLevel.CE_5) : ((money - expRound * StaticData.Exp.MoneyLevel.LS_5) / StaticData.Exp.MoneyLevel.CE_5  + 1);
            expStamina = expRound * StaticData.Exp.Stamina.LS_5;
            moneyStamina = moneyRound * StaticData.Exp.Stamina.CE_5;
            stamina = expStamina + moneyStamina;
        /*TextView expResult = view.findViewById(R.id.material_result_exp);
        TextView moneyResult = view.findViewById(R.id.material_result_money);
        TextView staminaResult = view.findViewById(R.id.material_result_stamina);
        expResult.setText(String.valueOf(exp));
        moneyResult.setText(money + " = " + moneyUpgrade + view.getContext().getResources().getString(R.string.material_money_upgrade) + " + " + moneyEvolve + view.getContext().getResources().getString(R.string.material_money_evolve));
        staminaResult.setText(stamina + " = " + StaticData.Exp.Stamina.LS_5 + " * " + expRound + view.getContext().getResources().getString(R.string.material_round_exp) + " + " + StaticData.Exp.Stamina.CE_5 + " * " + moneyRound + view.getContext().getResources().getString(R.string.material_round_money));
        */

            itemDetailView = new ItemDetailView(applicationContext);
            itemDetailView.setItemName(applicationContext.getString(R.string.material_money_upgrade));
            itemDetailView.setImage(applicationContext.getResources().getDrawable(R.mipmap.gold));
            itemDetailView.setNumber(moneyUpgrade);
            resultContent.addView(itemDetailView);

            if(moneyEvolve > 0){
                itemDetailView = new ItemDetailView(applicationContext);
                itemDetailView.setItemName(applicationContext.getString(R.string.material_money_evolve));
                itemDetailView.setImage(applicationContext.getResources().getDrawable(R.mipmap.gold));
                itemDetailView.setNumber(moneyEvolve);
                resultContent.addView(itemDetailView);
            }

            itemDetailView = new ItemDetailView(applicationContext);
            itemDetailView.setItemName(applicationContext.getString(R.string.material_exp));
            itemDetailView.setImage(applicationContext.getResources().getDrawable(R.mipmap.sprite_exp_card_t4));
            itemDetailView.setNumber(exp);
            itemDetailView.appendText("  " + (int)Math.ceil(((double) exp)/((double)StaticData.Exp.Book.EXP_BOOK_1)) + " / " + (int)Math.ceil(((double) exp)/((double)StaticData.Exp.Book.EXP_BOOK_2)) + " / " + (int)Math.ceil(((double) exp)/((double)StaticData.Exp.Book.EXP_BOOK_3)) + " / " + (int)Math.ceil(((double) exp)/((double)StaticData.Exp.Book.EXP_BOOK_4)) + " pcs");
            resultContent.addView(itemDetailView);

            JSONArray evolveCosts = charNow.getJSONArray("evolveCosts");
            if(evolveCosts.size() >= 2){
                if (stageFrom == 0 && stageTo == 1){
                    JSONArray evolveCosts1 = evolveCosts.getJSONArray(1);
                    for (int i = 0; i < evolveCosts1.size(); i++){
                        JSONObject item = evolveCosts1.getJSONObject(i);
                        JSONObject material = materialJsonObject.getJSONObject(item.getString("id"));
                        if(onlyRare && material.getInteger("rarity") <= 3){continue;}
                        itemDetailView = new ItemDetailView(applicationContext);
                        itemDetailView.setItemName(material.getString("name"));
                        Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(material.getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                        itemDetailView.setImage(drawable);
                        itemDetailView.setNumber(item.getInteger("count"));
                        resultContent.addView(itemDetailView);
                        itemDetailView.setTag(material);
                    }
                }
                if (stageFrom == 1 && stageTo == 2){
                    JSONArray evolveCosts2 = evolveCosts.getJSONArray(2);
                    for (int i = 0; i < evolveCosts2.size(); i++){
                        JSONObject item = evolveCosts2.getJSONObject(i);
                        JSONObject material = materialJsonObject.getJSONObject(item.getString("id"));
                        if(onlyRare && material.getInteger("rarity") <= 3){continue;}
                        itemDetailView = new ItemDetailView(applicationContext);
                        itemDetailView.setItemName(material.getString("name"));
                        Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(material.getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                        itemDetailView.setImage(drawable);
                        itemDetailView.setNumber(item.getInteger("count"));
                        resultContent.addView(itemDetailView);
                        itemDetailView.setTag(material);
                    }
                }
                if (stageFrom == 0 && stageTo == 2){
                    JSONArray evolveCosts1 = evolveCosts.getJSONArray(1);
                    for (int i = 0; i < evolveCosts1.size(); i++){
                        JSONObject item = evolveCosts1.getJSONObject(i);
                        JSONObject material = materialJsonObject.getJSONObject(item.getString("id"));
                        if(onlyRare && material.getInteger("rarity") <= 3){continue;}
                        itemDetailView = new ItemDetailView(applicationContext);
                        itemDetailView.setItemName(material.getString("name"));
                        Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(material.getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                        itemDetailView.setImage(drawable);
                        itemDetailView.setNumber(item.getInteger("count"));
                        resultContent.addView(itemDetailView);
                        itemDetailView.setTag(material);
                    }
                    JSONArray evolveCosts2 = evolveCosts.getJSONArray(2);
                    for (int i = 0; i < evolveCosts2.size(); i++){
                        JSONObject item = evolveCosts2.getJSONObject(i);
                        JSONObject material = materialJsonObject.getJSONObject(item.getString("id"));
                        if(onlyRare && material.getInteger("rarity") <= 3){continue;}
                        itemDetailView = new ItemDetailView(applicationContext);
                        itemDetailView.setItemName(material.getString("name"));
                        Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(material.getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                        itemDetailView.setImage(drawable);
                        itemDetailView.setNumber(item.getInteger("count"));
                        resultContent.addView(itemDetailView);
                        itemDetailView.setTag(material);
                    }
                }
            }
        }

        if (skillAllCheckBox.isChecked()){
            int saFrom = skillAllNow.getInt();
            int saTo = skillAllTarget.getInt();
            JSONArray jsonArray = (JSONArray) skillAllContainer.getTag();
            for (int i = saFrom - 1; i < saTo - 1; i++) {
                JSONArray itemArray = jsonArray.getJSONObject(i).getJSONArray("lvlUpCost");
                for (int j = 0; j < itemArray.size(); j++) {
                    boolean find = false;
                    JSONObject item = itemArray.getJSONObject(j);
                    JSONObject material = materialJsonObject.getJSONObject(item.getString("id"));
                    for (int k = 0; k < resultContent.getChildCount(); k++) {
                        ItemDetailView child = (ItemDetailView) resultContent.getChildAt(k);
                        if (child.getTag() == null){continue;}
                        if(child.getTag().equals(material)){
                            child.setNumber(child.getNumber() + item.getInteger("count"));
                            find = true;
                            break;
                        }
                    }
                    if (find){
                        continue;
                    }else {
                        if(onlyRare && material.getInteger("rarity") <= 3){continue;}
                        itemDetailView = new ItemDetailView(applicationContext);
                        itemDetailView.setItemName(material.getString("name"));
                        Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(material.getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                        itemDetailView.setImage(drawable);
                        itemDetailView.setNumber(item.getInteger("count"));
                        resultContent.addView(itemDetailView);
                        itemDetailView.setTag(material);
                    }
                }
            }
        }

        if (skill3CheckBox.isChecked()){
            int s3From = skill3Now.getInt();
            int s3To = skill3Target.getInt();
            JSONArray jsonArray = ((JSONObject) skill3Container.getTag()).getJSONArray("levelUpCost");
            for (int i = s3From; i < s3To; i++) {
                JSONArray itemArray = jsonArray.getJSONObject(i).getJSONArray("levelUpCost");
                for (int j = 0; j < itemArray.size(); j++) {
                    boolean find = false;
                    JSONObject item = itemArray.getJSONObject(j);
                    JSONObject material = materialJsonObject.getJSONObject(item.getString("id"));
                    for (int k = 0; k < resultContent.getChildCount(); k++) {
                        ItemDetailView child = (ItemDetailView) resultContent.getChildAt(k);
                        if (child.getTag() == null){continue;}
                        if(child.getTag().equals(material)){
                            child.setNumber(child.getNumber() + item.getInteger("count"));
                            find = true;
                            break;
                        }
                    }
                    if (find){
                        continue;
                    }else {
                        if(onlyRare && material.getInteger("rarity") <= 3){continue;}
                        itemDetailView = new ItemDetailView(applicationContext);
                        itemDetailView.setItemName(material.getString("name"));
                        Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(material.getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                        itemDetailView.setImage(drawable);
                        itemDetailView.setNumber(item.getInteger("count"));
                        resultContent.addView(itemDetailView);
                        itemDetailView.setTag(material);
                    }
                }
            }
        }

        if (skill2CheckBox.isChecked()){
            int s2From = skill2Now.getInt();
            int s2To = skill2Target.getInt();
            JSONArray jsonArray = ((JSONObject) skill2Container.getTag()).getJSONArray("levelUpCost");
            for (int i = s2From; i < s2To; i++) {
                JSONArray itemArray = jsonArray.getJSONObject(i).getJSONArray("levelUpCost");
                for (int j = 0; j < itemArray.size(); j++) {
                    boolean find = false;
                    JSONObject item = itemArray.getJSONObject(j);
                    JSONObject material = materialJsonObject.getJSONObject(item.getString("id"));
                    //Log.e(TAG, item.toJSONString());
                    for (int k = 0; k < resultContent.getChildCount(); k++) {
                        ItemDetailView child = (ItemDetailView) resultContent.getChildAt(k);
                        if (child.getTag() == null){continue;}
                        if(child.getTag().equals(material)){
                            child.setNumber(child.getNumber() + item.getInteger("count"));
                            find = true;
                            break;
                        }
                    }
                    if (find){
                        continue;
                    }else {
                        if(onlyRare && material.getInteger("rarity") <= 3){continue;}
                        itemDetailView = new ItemDetailView(applicationContext);
                        itemDetailView.setItemName(material.getString("name"));
                        Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(material.getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                        itemDetailView.setImage(drawable);
                        itemDetailView.setNumber(item.getInteger("count"));
                        resultContent.addView(itemDetailView);
                        itemDetailView.setTag(material);
                    }
                }
            }
        }

        if (skill1CheckBox.isChecked()){
            int s1From = skill1Now.getInt();
            int s1To = skill1Target.getInt();
            JSONArray jsonArray = ((JSONObject) skill1Container.getTag()).getJSONArray("levelUpCost");
            for (int i = s1From; i < s1To; i++) {
                JSONArray itemArray = jsonArray.getJSONObject(i).getJSONArray("levelUpCost");
                for (int j = 0; j < itemArray.size(); j++) {
                    boolean find = false;
                    JSONObject item = itemArray.getJSONObject(j);
                    JSONObject material = materialJsonObject.getJSONObject(item.getString("id"));
                    Log.e(TAG, item.toJSONString());
                    for (int k = 0; k < resultContent.getChildCount(); k++) {
                        ItemDetailView child = (ItemDetailView) resultContent.getChildAt(k);
                        if (child.getTag() == null){continue;}
                        if(child.getTag().equals(material)){
                            child.setNumber(child.getNumber() + item.getInteger("count"));
                            find = true;
                            break;
                        }
                    }
                    if (find){
                        continue;
                    }else {
                        if(onlyRare && material.getInteger("rarity") <= 3){continue;}
                        itemDetailView = new ItemDetailView(applicationContext);
                        itemDetailView.setItemName(material.getString("name"));
                        Drawable drawable = applicationContext.getResources().getDrawable(applicationContext.getResources().getIdentifier(material.getString("icon").toLowerCase(), "mipmap", applicationContext.getPackageName()));
                        itemDetailView.setImage(drawable);
                        itemDetailView.setNumber(item.getInteger("count"));
                        resultContent.addView(itemDetailView);
                        itemDetailView.setTag(material);
                    }
                }
            }
        }
        contentView.findViewById(R.id.material_result_content).setVisibility(VISIBLE);
        if(((LinearLayout) contentView.findViewById(R.id.material_result_content)).getChildCount() <= 0){
            (contentView.findViewById(R.id.material_tools)).setVisibility(GONE);
        }else{
            (contentView.findViewById(R.id.material_tools)).setVisibility(VISIBLE);
        }
    }

    private void plan() {
        JSONObject jsonObject = new JSONObject();
        ArrayList<String> itemList = new ArrayList<>(Arrays.asList(applicationContext.getResources().getStringArray(R.array.planner_materials)));
        for (int i = 0; i < resultContent.getChildCount(); i++) {
            ItemDetailView attachedView = (ItemDetailView) resultContent.getChildAt(i);
            if (!itemList.contains(attachedView.getItemName())){
                continue;
            }
            jsonObject.put(attachedView.getItemName(), attachedView.getNumber());
        }
        if (jsonObject.entrySet().size() > 0){
            overlayService.getPlan(jsonObject);
        }else {
            Toast.makeText(applicationContext, R.string.material_nothing_plannable, Toast.LENGTH_SHORT).show();
        }
        //Log.e(TAG, "plan:  " +  jsonObject.toJSONString());

    }

    private void pinWindow(){
        ArrayList<View> views = new ArrayList<>();
        for (int i = 0; i < resultContent.getChildCount(); i++) {
            ItemDetailView itemDetailView = new ItemDetailView(applicationContext);
            ItemDetailView attachedView = (ItemDetailView) resultContent.getChildAt(i);
            itemDetailView.setItemName(attachedView.getItemName());
            itemDetailView.setImage(attachedView.getImage());
            itemDetailView.setText(attachedView.getText());
            views.add(itemDetailView);
        }
        overlayService.showPinnedWindow(views);
    }

    @SuppressLint("SetTextI18n")
    public void onCharacterSelected(View characterBtn){
        skill3Container.setVisibility(GONE);
        skill2Container.setVisibility(GONE);
        skill1Container.setVisibility(GONE);
        skillAllContainer.setVisibility(GONE);
        skill3CheckBox.setChecked(false);
        skill2CheckBox.setChecked(false);
        skill1CheckBox.setChecked(false);
        skillAllCheckBox.setChecked(false);
        JSONObject jsonObject = (JSONObject) characterBtn.getTag();
        charNow = jsonObject;
        Button nowCharBtn = contentView.findViewById(R.id.material_character_now_btn);
        characterStar = jsonObject.getInteger("rarity");
        switch (characterStar){
            case 5:
                nowCharBtn.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_yellow));
                break;
            case 4:
                nowCharBtn.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_red));
                break;
            case 3:
                nowCharBtn.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_blue));
                break;
            case 2:
                nowCharBtn.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_green));
                break;
            case 1:
                nowCharBtn.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_lime));
                break;
            case 0:
                nowCharBtn.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_lime));
                break;
            default:
                nowCharBtn.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_blue));
                break;
        }
        JSONArray sSkills = jsonObject.getJSONArray("sskillCosts");
        JSONArray aSkills = jsonObject.getJSONArray("askillCosts");
        if (sSkills.size() > 0 && sSkills.getJSONObject(0).getJSONArray("levelUpCost").size() > 0){
                switch (sSkills.size()){
                    case 3:
                        skill3Container.setVisibility(VISIBLE);
                        skill3Container.setTag(sSkills.get(2));
                        skill2Container.setVisibility(VISIBLE);
                        skill2Container.setTag(sSkills.get(1));
                        skill1Container.setVisibility(VISIBLE);
                        skill1Container.setTag(sSkills.get(0));
                        skillAllContainer.setVisibility(VISIBLE);
                        skillAllContainer.setTag(aSkills);
                        break;
                    case 2:
                        skill2Container.setVisibility(VISIBLE);
                        skill2Container.setTag(sSkills.get(1));
                        skill1Container.setVisibility(VISIBLE);
                        skill1Container.setTag(sSkills.get(0));
                        skillAllContainer.setVisibility(VISIBLE);
                        skillAllContainer.setTag(aSkills);
                        break;
                    case 1:
                        skill1Container.setVisibility(VISIBLE);
                        skill1Container.setTag(sSkills.get(0));
                        skillAllContainer.setVisibility(VISIBLE);
                        skillAllContainer.setTag(aSkills);
                        break;
                    default:
                        break;
            }
        }else if (aSkills.size() > 0){
            skillAllContainer.setVisibility(VISIBLE);
            skillAllContainer.setTag(aSkills);
        }
        characterBtn.setVisibility(VISIBLE);
        nowCharBtn.setTag(jsonObject);
        nowCharBtn.setText(nameHelper.get(jsonObject.getString("name")));
        contentView.findViewById(R.id.material_character_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                showSubWindow();
            }
        });

            checkValue();//检查当前值是否匹配新干员
            showResult();
            hideSubWindow();
        }
        public int getMaxStage(int star){
            switch (star){
                case 1:
                    return StaticData.Exp.Limit.Stage.STAR_1;
            case 2:
                return StaticData.Exp.Limit.Stage.STAR_2;
            case 3:
                return StaticData.Exp.Limit.Stage.STAR_3;
            case 4:
                return StaticData.Exp.Limit.Stage.STAR_4;
            case 5:
                return StaticData.Exp.Limit.Stage.STAR_5;
            case 6:
                return StaticData.Exp.Limit.Stage.STAR_6;
            default:
                return 0;
        }
    }
    public void isCurrentWindow(boolean current){
        if (current){
            if(charNow == null){showSubWindow();}
            contentView.findViewById(R.id.material_character_select).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setClickable(false);
                    showSubWindow();
                }
            });
        }else{
            placeHolder = rootLayout.findViewWithTag("placeHolder");
            placeHolder.removeAllViews();
        }
    }
    public void showSubWindow(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        assert windowManager != null;
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = ScreenUtils.getScreenRotation(applicationContext);
        int backgroundColor = ThemeUtils.getBackgroundColor(applicationContext, contextThemeWrapper);
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, Color.TRANSPARENT});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        selector.setBackgroundColor(backgroundColor);
        if(rotation == Surface.ROTATION_90){selector.setBackground(gradientDrawable);}
        placeHolder = rootLayout.findViewWithTag("placeHolder");
        placeHolder.removeAllViews();
        placeHolder.addView(selector);
        Animator animator;
        if (ScreenUtils.getScreenRotationMode(rotation) == ScreenUtils.MODE_PORTRAIT){
            animator = AnimatorInflater.loadAnimator(applicationContext, R.animator.overlay_sub_show_portrait);
        }else {
            animator = AnimatorInflater.loadAnimator(applicationContext, R.animator.overlay_sub_show_landspace);
        }
        animator.setDuration(150);
        animator.setTarget(selector);
        animator.start();
    }
    public void hideSubWindow(){
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
                //重新初始化，不然会出现奇怪的问题
                ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_6)).removeAllViews();
                ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_5)).removeAllViews();
                ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_4)).removeAllViews();
                ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_3)).removeAllViews();
                ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_2)).removeAllViews();
                ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_1)).removeAllViews();
                int backgroundColor = ThemeUtils.getBackgroundColor(applicationContext, contextThemeWrapper);
                GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, backgroundColor, Color.TRANSPARENT});
                gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                selector.setBackground(gradientDrawable);
                WindowManager windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                assert windowManager != null;
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                int rotation = windowManager.getDefaultDisplay().getRotation();
                if (rotation == 0 || rotation == 3) {
                    selector.setBackgroundColor(backgroundColor);
                }
                ArrayList<String> characters = new ArrayList<>(characterJsonObject.keySet());
                for(int i = 0; i < characters.size(); i++){
                    String name = characters.get(i);
                    if (nameHelper.isHidden(name, I18nUtils.FILTER_ALL)){
                        continue;
                    }
                    JSONObject jsonObject = characterJsonObject.getJSONObject(name);
                    if(jsonObject.getString("profession").equals("其它")){continue;}
                    characterIndexMap.put(name, i);
                    Button button = new Button(applicationContext);
                    button.setText(nameHelper.get(name));
                    button.setMinWidth(applicationContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
                    button.setTag(jsonObject);
                    button.setTextColor(Color.BLACK);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onCharacterSelected(v);
                        }
                    });
                    switch (jsonObject.getInteger("rarity")){
                        case 5:
                            button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_yellow));
                            ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_6)).addView(button);
                            break;
                        case 4:
                            button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_red));
                            ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_5)).addView(button);
                            break;
                        case 3:
                            button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_blue));
                            ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_4)).addView(button);
                            break;
                        case 2:
                            button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_green));
                            ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_3)).addView(button);
                            break;
                        case 1:
                            button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_lime));
                            ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_2)).addView(button);
                            break;
                        case 0:
                            button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_lime));
                            ((LineWrapLayout)selector.findViewById(R.id.material_character_selector_1)).addView(button);
                            break;
                        default:
                            button.setBackground(applicationContext.getResources().getDrawable(R.drawable.checkbox_background_blue));
                            break;
                    }
                }
                if (selector.getParent() != null){
                    ((LinearLayout)selector.getParent()).removeView(selector);
                }
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
}
