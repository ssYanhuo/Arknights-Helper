package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ChildScrollView extends ScrollView {
    public ChildScrollView(Context context) {
        this(context,null);
    }
    public ChildScrollView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public ChildScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }
}