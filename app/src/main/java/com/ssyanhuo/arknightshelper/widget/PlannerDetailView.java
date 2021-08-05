package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssyanhuo.arknightshelper.R;

public class PlannerDetailView  extends LinearLayout {

    private final TextView title;
    private final TextView content;
    private final CheckBox checkBox;

    public PlannerDetailView(final Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.planner_detail_view, this);
        title = findViewById(R.id.planner_detail_view_title);
        content = findViewById(R.id.planner_detail_view_content);
        checkBox = findViewById(R.id.planner_detail_view_checkBox);
        ((LinearLayout) title.getParent().getParent()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (content.getVisibility() == View.GONE){
                    unfold();
                }else {
                    fold();
                }
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    content.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    title.setTypeface(title.getTypeface(), Typeface.ITALIC);
                    content.setTypeface(title.getTypeface(), Typeface.ITALIC);
                    title.setAlpha(0.6f);
                    content.setAlpha(0.6f);
                }else {
                    title.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    content.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    title.setTypeface(title.getTypeface(), Typeface.NORMAL);
                    content.setTypeface(title.getTypeface(), Typeface.NORMAL);
                    title.setAlpha(1);
                    content.setAlpha(1);
                }
            }
        });

    }
    public void setTitleText(CharSequence text){
        title.setText(text);
    }
    public void setContentText(CharSequence text){
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
