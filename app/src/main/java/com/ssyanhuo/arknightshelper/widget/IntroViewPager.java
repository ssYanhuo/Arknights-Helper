package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

public class IntroViewPager extends ViewPager {
    private boolean canScroll = true;

    public IntroViewPager(@NonNull Context context) {
        super(context);
    }

    public IntroViewPager(Context context, AttributeSet attributeSet){
        super(context, attributeSet);

    }

    public void setCanScroll(boolean canScroll){

        this.canScroll = canScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return canScroll && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return canScroll && super.onTouchEvent(ev);
    }
}
