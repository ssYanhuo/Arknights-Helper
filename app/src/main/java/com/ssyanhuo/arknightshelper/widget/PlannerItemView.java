package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.IntegerRes;

import com.ssyanhuo.arknightshelper.R;

import java.util.List;


public class PlannerItemView extends LinearLayout {

    private final TextView textView;
    private final ImageView imageView;
    private final NumberSelector numberSelector;
    private final Button button;

    public PlannerItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_planner_item_view, this);
        textView = findViewById(R.id.planneritem_textview);
        imageView = findViewById(R.id.planneritem_imageview);
        numberSelector = findViewById(R.id.planneritem_numberseletor);
        button = findViewById(R.id.planneritem_button);
    }
    public PlannerItemView(Context context, Drawable drawable, CharSequence name) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_planner_item_view, this);
        textView = findViewById(R.id.planneritem_textview);
        imageView = findViewById(R.id.planneritem_imageview);
        numberSelector = findViewById(R.id.planneritem_numberseletor);
        button = findViewById(R.id.planneritem_button);
        setMaterialImage(drawable);
        setMaterialName(name);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem();
            }
        });
    }
    public void setMaterialImage(Drawable drawable){
        imageView.setImageDrawable(drawable);
    }
    public void setMaterialImage(int resId){
        imageView.setImageResource(resId);
    }
    public void setMaterialName(CharSequence name){
        textView.setText(name);
    }
    public void setMaterialName(int resId){
        textView.setText(resId);
    }
    public int getNum(){
        return numberSelector.getInt();
    }
    public void setNum(int num){
        numberSelector.setInt(num);
    }
    public String getName(){
        return (String) textView.getText();
    }
    public void removeItem(){
        try{
            ((ViewGroup)this.getParent()).removeView(this);
        }catch (Exception ignored){

        }
    }
}
