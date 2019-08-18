package com.ssyanhuo.arknightshelper.overlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.widget.PopupWindowCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.R;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Hr {
    private final String TAG = "Hr";
    private EasyPopup easyPopup;
    public  void getAllCheckboxes(ArrayList<CheckBox> checkBoxes, View view){
        ViewGroup viewGroup = (ViewGroup)view;
        for(int i = 0; i < viewGroup.getChildCount(); i++){
            if(viewGroup.getChildAt(i) instanceof CheckBox){
                checkBoxes.add((CheckBox) viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof HorizontalScrollView){
                getAllCheckboxes(checkBoxes, viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LinearLayout){
                getAllCheckboxes(checkBoxes, viewGroup.getChildAt(i));
            }
        }
    }
    public void getSelectedItems(ArrayList<CheckBox> checkBoxes, ArrayList<String> selectedStar, ArrayList<String> selectedQualification, ArrayList<String> selectedPosition, ArrayList<String> selectedSex, ArrayList<String> selectedType, ArrayList<String> selectedTag){
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

    public boolean isFewerThan3(ArrayList<String> selectedQualification, ArrayList<String> selectedPosition, ArrayList<String> selectedSex, ArrayList<String> selectedType, ArrayList<String> selectedTag){
        int num = selectedQualification.size() + selectedPosition.size() + selectedSex.size() + selectedType.size() + selectedTag.size();
        return (num <= 3);
    }


    public ArrayList<JSONObject> getResult(String jsonString, ArrayList<String> selectedStar, ArrayList<String> selectedQualification, ArrayList<String> selectedPosition, ArrayList<String> selectedSex, ArrayList<String> selectedType, ArrayList<String> selectedTag){
        ArrayList<JSONObject> result = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(jsonString);
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
        return result;
    }

    public void hideResult(LinearLayout linearLayout){
        linearLayout.setVisibility(View.GONE);
    }

    public void showResult(ArrayList<JSONObject> result, LinearLayout linearLayout, ScrollView scrollView, final Context context){

        LinearLayout resultLayout = linearLayout.findViewById(R.id.hr_result);
        resultLayout.removeAllViews();
        linearLayout.setVisibility(View.VISIBLE);
        if(result.size() == 0){
            TextView textView = new TextView(context);
            textView.setText(context.getText(R.string.hr_result_empty));
            textView.setTextColor(Color.GRAY);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
            resultLayout.addView(textView);
        }
        for (int j = 0; j < result.size(); j++) {
            final JSONObject jsonObject = result.get(j);
            Button button = LayoutInflater.from(context).inflate(R.layout.hr_result_button, null).findViewById(R.id.hr_result_button);
            switch (jsonObject.getInteger("level")) {
                case 6:
                    button.setBackground(context.getResources().getDrawable(R.drawable.checkbox_background_yellow));
                    break;
                case 5:
                    button.setBackground(context.getResources().getDrawable(R.drawable.checkbox_background_red));
                    break;
                case 2:
                    button.setBackground(context.getResources().getDrawable(R.drawable.checkbox_background_lime));
                    break;
                case 1:
                    button.setBackground(context.getResources().getDrawable(R.drawable.checkbox_background_lime));
                    break;
                default:
                    button.setBackground(context.getResources().getDrawable(R.drawable.checkbox_background_blue));
                    break;
            }
            button.setText(jsonObject.getString("name"));
            button.setTag("hr_result");
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            CardView cardView = (CardView) LayoutInflater.from(context).inflate(context.getResources().getLayout(R.layout.detail_popup), null);
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
                            easyPopup = EasyPopup.create(context)
                                    .setContentView(cardView)
                                    .apply();
                             easyPopup.showAtAnchorView(view, YGravity.ABOVE, XGravity.CENTER);
                             break;
                        case MotionEvent.ACTION_UP:
                            easyPopup.dismiss();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            easyPopup.dismiss();
                            break;
                        default:
                            break;
                    }


                    return false;
                }
            });
            resultLayout.addView(button);
        }
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
}
