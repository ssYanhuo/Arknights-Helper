package com.ssyanhuo.arknightshelper.overlay;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.staticdata.StaticData;
import com.ssyanhuo.arknightshelper.utiliy.JsonUtility;
import com.ssyanhuo.arknightshelper.widget.NumberSelector;

import java.util.ArrayList;

public class Exp {
    public ArrayList<NumberSelector> numberSelectors;
    private NumberSelector stageNow;
    private NumberSelector levelNow;
    private NumberSelector pointNow;
    private NumberSelector stageTarget;
    private NumberSelector levelTarget;
    private Spinner starNow;
    private String jsonString;
    private JSONObject jsonObject;
    private int characterStar;
    private boolean showedOnce = false;
    private final String TAG = "Exp";
    public void getAllNumberSelectors(View view){
        ViewGroup viewGroup = (ViewGroup)view;
        for(int i = 0; i < viewGroup.getChildCount(); i++){
            if(viewGroup.getChildAt(i) instanceof NumberSelector){
                numberSelectors.add((NumberSelector) viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof HorizontalScrollView){
                getAllNumberSelectors(viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LinearLayout){
                getAllNumberSelectors(viewGroup.getChildAt(i));
            }
        }
    }
    public void init(final View view){
        jsonString = JsonUtility.getJsonString(view.getContext(), "data/exp.json");
        jsonObject = JsonUtility.getJsonObject(view.getContext(), jsonString);
        numberSelectors = new ArrayList<>();
        stageNow = view.findViewById(R.id.exp_selector_stage_now);
        levelNow = view.findViewById(R.id.exp_selector_level_now);
        stageTarget = view.findViewById(R.id.exp_selector_stage_target);
        levelTarget = view.findViewById(R.id.exp_selector_level_target);
        starNow = view.findViewById(R.id.exp_selector_star);
        getAllNumberSelectors(view);
        starNow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view1, int i, long l) {//这里默认会调用触发器的view，所以改成view1
                characterStar = 6 - i;
                checkValue();
                showResult(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        for (int i = 0; i < numberSelectors.size(); i++){
            numberSelectors.get(i).editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    checkValue();
                    showResult(view);
                }
            });
        }
    }
    public void checkValue(){
        //不同星级时，精英化（目标+当前）最值随之变化，若变化前已经超出新值范围，则自动缩小为最值
        int maxStage = getMaxStage(characterStar);
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
        int maxLevelNow = jsonObject.getJSONArray("maxLevel").getJSONArray(characterStar - 1).getInteger(stageNow.getInt());
        int maxLevelTarget = jsonObject.getJSONArray("maxLevel").getJSONArray(characterStar - 1).getInteger(stageTarget.getInt());
        levelNow.setMax(maxLevelNow - 1);
        if(levelNow.getInt() > maxLevelNow - 1){
            levelNow.setInt(maxLevelNow - 1);
        }
        levelTarget.setMax(maxLevelTarget);
        if(levelTarget.getInt() > maxLevelTarget){
            levelTarget.setInt(maxLevelTarget);
        }
    }
    public void showResult(View view){
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
            moneyEvolve = jsonObject.getJSONArray("evolveGoldCost").getJSONArray(characterStar - 1).getInteger(0) + jsonObject.getJSONArray("evolveGoldCost").getJSONArray(characterStar - 1).getInteger(1);
        }else if(stageTo - stageFrom == 1){
            if (stageFrom == 0){
                moneyEvolve = jsonObject.getJSONArray("evolveGoldCost").getJSONArray(characterStar - 1).getInteger(0);
            }else if(stageFrom == 1){
                moneyEvolve = jsonObject.getJSONArray("evolveGoldCost").getJSONArray(characterStar - 1).getInteger(1);
            }
        }
        for (int stage = stageFrom; stage <= stageTo; stage++){
            boolean isLastStage = stage == stageTo;
            boolean isFirstStage = stage ==stageFrom;
            if(isFirstStage && isLastStage){
                for (int i = levelFrom; i < levelTo; i++){
                    exp += jsonObject.getJSONArray("characterExpMap").getJSONArray(stage).getInteger(i - 1);
                    moneyUpgrade += jsonObject.getJSONArray("characterUpgradeCostMap").getJSONArray(stage).getInteger(i - 1);
                }
            }else if(isFirstStage){
                for (int i = levelFrom; i < jsonObject.getJSONArray("maxLevel").getJSONArray(characterStar - 1).getInteger(stage); i++){
                    exp += jsonObject.getJSONArray("characterExpMap").getJSONArray(stage).getInteger(i - 1);
                    moneyUpgrade += jsonObject.getJSONArray("characterUpgradeCostMap").getJSONArray(stage).getInteger(i - 1);
                }
            }else  if(isLastStage){
                for (int i = 1; i < levelTo; i++){
                    exp += jsonObject.getJSONArray("characterExpMap").getJSONArray(stage).getInteger(i - 1);
                    moneyUpgrade += jsonObject.getJSONArray("characterUpgradeCostMap").getJSONArray(stage).getInteger(i - 1);
                }
            }else {
                for (int i = 1; i < jsonObject.getJSONArray("maxLevel").getJSONArray(characterStar - 1).getInteger(stage); i++){
                    exp += jsonObject.getJSONArray("characterExpMap").getJSONArray(stage).getInteger(i - 1);
                    moneyUpgrade += jsonObject.getJSONArray("characterUpgradeCostMap").getJSONArray(stage).getInteger(i - 1);
                }
            }
        }
        expRound = (exp % StaticData.Exp.ExpLevel.LS_5 == 0) ? (exp / StaticData.Exp.ExpLevel.LS_5) : (exp / StaticData.Exp.ExpLevel.LS_5 + 1);
        money = moneyEvolve + moneyUpgrade;
        moneyRound = ((money - expRound * StaticData.Exp.MoneyLevel.LS_5) % StaticData.Exp.MoneyLevel.CE_5 == 0) ? ((money - expRound * StaticData.Exp.MoneyLevel.LS_5) / StaticData.Exp.MoneyLevel.CE_5) : ((money - expRound * StaticData.Exp.MoneyLevel.LS_5) / StaticData.Exp.MoneyLevel.CE_5  + 1);
        expStamina = expRound * StaticData.Exp.Stamina.LS_5;
        moneyStamina = moneyRound * StaticData.Exp.Stamina.CE_5;
        stamina = expStamina + moneyStamina;
        TextView expResult = view.findViewById(R.id.exp_result_exp);
        TextView moneyResult = view.findViewById(R.id.exp_result_money);
        TextView staminaResult = view.findViewById(R.id.exp_result_stamina);
        expResult.setText(String.valueOf(exp));
        moneyResult.setText(money + " = " + moneyUpgrade + view.getContext().getResources().getString(R.string.exp_money_upgrade) + " + " + moneyEvolve + view.getContext().getResources().getString(R.string.exp_money_evolve));
        staminaResult.setText(stamina + " = " + StaticData.Exp.Stamina.LS_5 + " * " + expRound + view.getContext().getResources().getString(R.string.exp_round_exp) + " + " + StaticData.Exp.Stamina.CE_5 + " * " + moneyRound + view.getContext().getResources().getString(R.string.exp_round_money));
        if(showedOnce){
            view.findViewById(R.id.exp_result_content).setVisibility(View.VISIBLE);
        }else {
            showedOnce = true;
        }
        ScrollView scrollView = (ScrollView) view.getParent();
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
    public int getMaxStage(int star){
        switch (star){
            case 1:
                return StaticData.Exp.limit.stage.STAR_1;
            case 2:
                return StaticData.Exp.limit.stage.STAR_2;
            case 3:
                return StaticData.Exp.limit.stage.STAR_3;
            case 4:
                return StaticData.Exp.limit.stage.STAR_4;
            case 5:
                return StaticData.Exp.limit.stage.STAR_5;
            case 6:
                return StaticData.Exp.limit.stage.STAR_6;
            default:
                return 0;
        }
    }
}
