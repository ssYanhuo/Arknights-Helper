package com.ssyanhuo.arknightshelper.widget;

import androidx.fragment.app.Fragment;

import com.ssyanhuo.arknightshelper.activity.IntroActivity;

public class IntroFragment extends Fragment {
    public void setCurrentItem(int item){
        ((IntroActivity) getActivity()).setCurrentItem(item);
    }

    public int getCurrentItem(){
        return ((IntroActivity) getActivity()).getCurrentItem();
    }

    public int getItemCount(){
        return ((IntroActivity) getActivity()).getItemCount();
    }

    public void goNext(){
        if (getCurrentItem() + 1 >= getItemCount()){
            setCurrentItem(getItemCount() - 1);
        }else {
            setCurrentItem(getCurrentItem() + 1);
        }
    }

    public void goPrevious(){
        if (getCurrentItem() - 1 < 0){
            setCurrentItem(0);
        }else {
            setCurrentItem(getCurrentItem() - 1);
        }
    }

    public void onShow(){

    }
}
