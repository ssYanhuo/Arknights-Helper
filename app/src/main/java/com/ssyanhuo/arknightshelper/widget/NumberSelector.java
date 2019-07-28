package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.ssyanhuo.arknightshelper.R;

public class NumberSelector extends LinearLayout {
    public Button buttonMinus;
    public Button buttonPlus;
    public EditText editText;
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;
    private int step = 1;
    private int defaultValue = 0;
    public NumberSelector(Context context) {
        super(context);
    }
    public NumberSelector(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.NumberSelector);
        min = typedArray.getInt(R.styleable.NumberSelector_minValue, Integer.MIN_VALUE);
        max = typedArray.getInt(R.styleable.NumberSelector_maxValue, Integer.MAX_VALUE);
        step = typedArray.getInt(R.styleable.NumberSelector_step, 1);
        defaultValue = typedArray.getInt(R.styleable.NumberSelector_defaultValue, 0);
        LayoutInflater.from(context).inflate(R.layout.view_numberselector, this);
        buttonMinus = findViewById(R.id.button_minus);
        buttonPlus = findViewById(R.id.button_plus);
        editText = findViewById(R.id.num_result);
        editText.setText(String.valueOf(defaultValue));
        buttonMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = Integer.valueOf(editText.getText().toString());
                if(i-step >= min){
                    i -= step;
                    setInt(i);
                }
            }
        });
        buttonMinus.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setInt(min);
                return true;
            }
        });
        buttonPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = Integer.valueOf((editText.getText().toString()));
                if(i+step <= max){
                    i += step;
                    setInt(i);
                }
            }
        });
        buttonPlus.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setInt(max);
                return true;
            }
        });
    }
    public int getInt(){ return Integer.valueOf(editText.getText().toString()); }
    public void setInt(int number){ editText.setText(String.valueOf(number)); }
    public void setMin(int min){
        this.min = min;
        if(getInt() < min){
            setInt(min);
        }
    }
    public void setMax(int max){
        this.max = max;
        if(getInt() > max){
            setInt(max);
        }
    }
    public void setStep(int step) { this.step = step; }
}
