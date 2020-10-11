package com.ssyanhuo.arknightshelper.entity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ssyanhuo.arknightshelper.widget.IntroFragment;

import java.util.List;

public class IntroFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<IntroFragment> mFragments;
    public IntroFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior, List<IntroFragment> fragments) {
        super(fm, behavior);
        this.mFragments = fragments;
    }

    @NonNull
    @Override
    public IntroFragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
