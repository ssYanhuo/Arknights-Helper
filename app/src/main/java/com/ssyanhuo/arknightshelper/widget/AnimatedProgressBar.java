package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ssyanhuo.arknightshelper.R;

import java.util.jar.Attributes;

public class AnimatedProgressBar extends androidx.appcompat.widget.AppCompatImageView {
    private AttributeSet attributeSet;
    private Context context;

    public AnimatedProgressBar(Context context) {
        super(context);

        this.context = context;
        init();
    }

    public AnimatedProgressBar(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        this.context = context;
        this.attributeSet = attributeSet;
        init();
    }
    private void init(){
        setImageDrawable(context.getDrawable(R.drawable.ic_progress_animated));
        try{
            ((AnimatedVectorDrawable)getDrawable()).start();
        }catch (Exception ignored) {
        }
    }
    public void startAnim(){
        try{
            ((AnimatedVectorDrawable)getDrawable()).start();
        }catch (Exception ignored) {
        }
    }
    public void stopAnim(){
        try{
            ((AnimatedVectorDrawable)getDrawable()).stop();
        }catch (Exception ignored) {
        }
    }
}
