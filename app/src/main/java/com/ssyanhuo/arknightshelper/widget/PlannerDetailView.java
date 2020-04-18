package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssyanhuo.arknightshelper.R;

public class PlannerDetailView  extends LinearLayout {

    private final TextView title;
    private final TextView content;

    public PlannerDetailView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.planner_detail_view, this);
        title = findViewById(R.id.planner_detail_view_title);
        content = findViewById(R.id.planner_detail_view_content);
        ((LinearLayout) title.getParent()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (content.getVisibility() == View.GONE){
                    unfold();
                }else {
                    fold();
                }
            }
        });

    }
    public void setTitleText(CharSequence text){
        title.setText(text);
    }
    public  void  setContentText(CharSequence text){
        content.setText(text);
    }
    public void appendContent(CharSequence text){
        content.append("\n" + text);
    }
    public void fold(){
        content.setVisibility(GONE);
    }
    public void unfold(){
        content.setVisibility(VISIBLE);
    }
}
