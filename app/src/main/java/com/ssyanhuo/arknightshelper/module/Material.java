package com.ssyanhuo.arknightshelper.module;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.staticdata.StaticData;
import com.ssyanhuo.arknightshelper.utiliy.FileUtility;
import com.ssyanhuo.arknightshelper.utiliy.JSONUtility;
import com.ssyanhuo.arknightshelper.widget.ItemDetailView;
import com.ssyanhuo.arknightshelper.widget.LineWrapLayout;
import com.ssyanhuo.arknightshelper.widget.NumberSelector;
import com.zyyoona7.popup.EasyPopup;

import java.io.IOException;
import java.util.*;

public class Material {
    public ArrayList<NumberSelector> numberSelectors;
    private NumberSelector stageNow;
    private NumberSelector levelNow;
    private NumberSelector pointNow;
    private NumberSelector stageTarget;
    private NumberSelector levelTarget;
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
    private boolean builtin;

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
    public void init(final Context context, final View view, LinearLayout backgroundLayout){
        charNow = null;
        applicationContext = context;
        rootLayout = backgroundLayout;
        contentView = view;
        sharedPreferences = applicationContext.getSharedPreferences("com.ssyanhuo.arknightshelper_preferences", Context.MODE_PRIVATE);
        builtin = sharedPreferences.getBoolean("use_builtin_data", true);
        try {
            expJsonObject = JSONUtility.getJSONObject(applicationContext, FileUtility.readData("aklevel.json", applicationContext, builtin));
            characterJsonObject = JSONUtility.getJSONObject(applicationContext, FileUtility.readData("charMaterials.json", applicationContext, builtin));
            materialJsonObject = JSONUtility.getJSONObject(applicationContext, FileUtility.readData("material.json", applicationContext, builtin));
        } catch (IOException e) {
            e.printStackTrace();
        }
        numberSelectors = new ArrayList<>();
        stageNow = view.findViewById(R.id.material_selector_stage_now);
        levelNow = view.findViewById(R.id.material_selector_level_now);
        stageTarget = view.findViewById(R.id.material_selector_stage_target);
        levelTarget = view.findViewById(R.id.material_selector_level_target);
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
        selector = (ScrollView)LayoutInflater.from(applicationContext).inflate(R.layout.content_material_sub_selector, null);
        ArrayList<String> characters = new ArrayList<>(characterJsonObject.keySet());
        for(int i = 0; i < characters.size(); i++){
            String name = characters.get(i);
            JSONObject jsonObject = characterJsonObject.getJSONObject(name);
            if(jsonObject.getString("profession").equals("其它")){continue;}
            characterIndexMap.put(name, i);
            Button button = new Button(applicationContext);
            button.setText(name);
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
    }
    public void checkValue(){
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
        levelNow.setMax(maxLevelNow - 1);
        if(levelNow.getInt() > maxLevelNow - 1){
            levelNow.setInt(maxLevelNow - 1);
        }
        levelTarget.setMax(maxLevelTarget);
        if(levelTarget.getInt() > maxLevelTarget){
            levelTarget.setInt(maxLevelTarget);
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
        LinearLayout resultContent = contentView.findViewById(R.id.material_result_content);
        resultContent.removeAllViews();
        ItemDetailView itemDetailView;

        itemDetailView = new ItemDetailView(applicationContext);
        itemDetailView.setItemName("龙门币（升级）");
        itemDetailView.setImage(applicationContext.getResources().getDrawable(R.mipmap.gold));
        itemDetailView.setNumber(moneyUpgrade);
        resultContent.addView(itemDetailView);

        if(moneyEvolve > 0){
            itemDetailView = new ItemDetailView(applicationContext);
            itemDetailView.setItemName("龙门币（精英化）");
            itemDetailView.setImage(applicationContext.getResources().getDrawable(R.mipmap.gold));
            itemDetailView.setNumber(moneyEvolve);
            resultContent.addView(itemDetailView);
        }

        itemDetailView = new ItemDetailView(applicationContext);
        itemDetailView.setItemName("经验");
        itemDetailView.setImage(applicationContext.getResources().getDrawable(R.mipmap.sprite_exp_card_t4));
        itemDetailView.setNumber(exp);
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
                }
            }
        }

        contentView.findViewById(R.id.material_result_content).setVisibility(View.VISIBLE);

    }
    @SuppressLint("SetTextI18n")
    public void onCharacterSelected(View characterBtn){
        JSONObject jsonObject = (JSONObject) characterBtn.getTag();
        charNow = jsonObject;
        Drawable drawable = characterBtn.getBackground();
        Button nowCharBtn = contentView.findViewById(R.id.material_character_now_btn);
        characterBtn.setVisibility(View.VISIBLE);
        nowCharBtn.setBackground(drawable);
        nowCharBtn.setTag(jsonObject);
        nowCharBtn.setText(jsonObject.getString("name"));
        contentView.findViewById(R.id.material_character_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                showSubWindow();
            }
        });
        characterStar = jsonObject.getInteger("rarity");
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
        WindowManager windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        assert windowManager != null;
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if(rotation == 0 || rotation == 3){selector.setBackgroundColor(Color.parseColor("#aa000000"));}
        placeHolder = rootLayout.findViewWithTag("placeHolder");
        placeHolder.removeAllViews();
        placeHolder.addView(selector);
        Animator animator = AnimatorInflater.loadAnimator(applicationContext, R.animator.anim_overlay_sub_show);
        animator.setDuration(150);
        animator.setTarget(selector);
        animator.start();
    }
    public void hideSubWindow(){

        placeHolder = rootLayout.findViewWithTag("placeHolder");
        Animator animator = AnimatorInflater.loadAnimator(applicationContext, R.animator.anim_overlay_sub_hide);
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
                ArrayList<String> characters = new ArrayList<>(characterJsonObject.keySet());
                for(int i = 0; i < characters.size(); i++){
                    String name = characters.get(i);
                    JSONObject jsonObject = characterJsonObject.getJSONObject(name);
                    if(jsonObject.getString("profession").equals("其它")){continue;}
                    characterIndexMap.put(name, i);
                    Button button = new Button(applicationContext);
                    button.setText(name);
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
