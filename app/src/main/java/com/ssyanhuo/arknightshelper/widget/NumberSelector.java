package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.cardview.widget.CardView;
import com.ssyanhuo.arknightshelper.R;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

public class NumberSelector extends LinearLayout {
    private ContextThemeWrapper contextThemeWrapper;
    public Button buttonMinus;
    public Button buttonPlus;
    public EditText editText;
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;
    private int step = 1;
    private int defaultValue = 0;
    private String text = "";
    private TextView textView;
    private KeyBoard keyBoard;
    private final String TAG = "NumberSelector";
    private int num;
    EasyPopup easyPopup;
    public NumberSelector(Context context) {
        super(context);
    }
    public NumberSelector(final Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.NumberSelector);
        contextThemeWrapper = new ContextThemeWrapper(context, R.style.AppTheme_Default_FloatingWindow);
        min = typedArray.getInt(R.styleable.NumberSelector_minValue, Integer.MIN_VALUE);
        max = typedArray.getInt(R.styleable.NumberSelector_maxValue, Integer.MAX_VALUE);
        step = typedArray.getInt(R.styleable.NumberSelector_step, 1);
        defaultValue = typedArray.getInt(R.styleable.NumberSelector_defaultNum, 0);
        text = typedArray.getString(R.styleable.NumberSelector_text);
        LayoutInflater.from(context).inflate(R.layout.view_numberselector, this);
        buttonMinus = findViewById(R.id.button_minus);
        buttonPlus = findViewById(R.id.button_plus);
        editText = findViewById(R.id.num_result);
        editText.setText(String.valueOf(defaultValue));
        textView = this.findViewById(R.id.textView);
        setText(text);
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
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                easyPopup = EasyPopup.create(contextThemeWrapper);
                num = getInt();
                keyBoard = new KeyBoard(contextThemeWrapper);
                keyBoard.setText(num);
                CardView cardView = new CardView(contextThemeWrapper);
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                cardView.addView(new KeyBoard(contextThemeWrapper));
                easyPopup.setContentView(cardView)
                        .setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                try {
                                    setInt(num);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setFocusable(false)
                        .apply();
                easyPopup.showAtAnchorView(buttonMinus, YGravity.CENTER, XGravity.ALIGN_LEFT);
            }
        });
    }
    public int getInt(){ return Integer.valueOf(editText.getText().toString()); }
    public void setInt(int number){
        if (number > max){
            editText.setText(String.valueOf(max));
        }else if (number < min){
            editText.setText(String.valueOf(min));
        }else{
            editText.setText(String.valueOf(number));
        }
    }
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
    public void setText(String text){
        textView.setText(text);
    }
    public class KeyBoard extends GridLayout {
        TextView textView;
        Button btn_0;
        Button btn_1;
        Button btn_2;
        Button btn_3;
        Button btn_4;
        Button btn_5;
        Button btn_6;
        Button btn_7;
        Button btn_8;
        Button btn_9;
        ImageButton btn_back;
        ImageButton btn_return;
        final String TAG = "KeyBoard";
        public KeyBoard(Context context) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.view_keyboard, this);
            textView = findViewById(R.id.keyboard_textview);
            btn_0 = findViewById(R.id.keyboard_0);
            btn_1 = findViewById(R.id.keyboard_1);
            btn_2 = findViewById(R.id.keyboard_2);
            btn_3 = findViewById(R.id.keyboard_3);
            btn_4 = findViewById(R.id.keyboard_4);
            btn_5 = findViewById(R.id.keyboard_5);
            btn_6 = findViewById(R.id.keyboard_6);
            btn_7 = findViewById(R.id.keyboard_7);
            btn_8 = findViewById(R.id.keyboard_8);
            btn_9 = findViewById(R.id.keyboard_9);
            btn_back = findViewById(R.id.keyboard_back);
            btn_return = findViewById(R.id.keyboard_return);
            btn_0.setOnClickListener(generateOnClickListener("0"));
            btn_1.setOnClickListener(generateOnClickListener("1"));
            btn_2.setOnClickListener(generateOnClickListener("2"));
            btn_3.setOnClickListener(generateOnClickListener("3"));
            btn_4.setOnClickListener(generateOnClickListener("4"));
            btn_5.setOnClickListener(generateOnClickListener("5"));
            btn_6.setOnClickListener(generateOnClickListener("6"));
            btn_7.setOnClickListener(generateOnClickListener("7"));
            btn_8.setOnClickListener(generateOnClickListener("8"));
            btn_9.setOnClickListener(generateOnClickListener("9"));
            btn_back.setOnClickListener(generateOnClickListener("back"));
            btn_back.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    textView.setText("");
                    return true;
                }
            });
            btn_return.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    easyPopup.dismiss();
                }
            });
        }

        private OnClickListener generateOnClickListener(final String action){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!action.equals("back")){
                        if (textView.length() <= 6){
                            textView.append(action);
                        }
                        try{
                            num = Integer.parseInt(String.valueOf(textView.getText()));
                        }catch (Exception e){
                            num = 0;
                            e.printStackTrace();
                        }
                    }
                    if (action.equals("back") && textView.getText().length() >= 1){
                        textView.setText(textView.getText().subSequence(0, textView.getText().length() - 1));
                        try{
                            num = Integer.parseInt(String.valueOf(textView.getText()));
                        }catch (Exception e){
                            num = 0;
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
        public void setText (int number){
            textView.setText(String.valueOf(number));
        }
    }

}
