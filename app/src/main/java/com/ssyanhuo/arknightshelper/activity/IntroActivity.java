package com.ssyanhuo.arknightshelper.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.ssyanhuo.arknightshelper.fragment.IntroCompatibilityFragment;
import com.ssyanhuo.arknightshelper.fragment.IntroFinishFragment;
import com.ssyanhuo.arknightshelper.fragment.IntroPermissionFragment;
import com.ssyanhuo.arknightshelper.fragment.IntroPlannerDownloadFragment;
import com.ssyanhuo.arknightshelper.fragment.IntroPlannerInitFragment;
import com.ssyanhuo.arknightshelper.fragment.IntroThemeFragment;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.IntroFragmentPagerAdapter;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.fragment.IntroLoadingFragment;
import com.ssyanhuo.arknightshelper.fragment.IntroGameFragment;
import com.ssyanhuo.arknightshelper.fragment.IntroNetworkFragment;
import com.ssyanhuo.arknightshelper.fragment.IntroStartupFragment;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.widget.IntroFragment;
import com.ssyanhuo.arknightshelper.widget.IntroViewPager;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private IntroViewPager viewPager;
    private List<IntroFragment> fragments;

    Boolean isPythonSupported;
    Boolean isOCRSupported;

    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        viewPager = findViewById(R.id.intro_viewpager);
        preferences = getSharedPreferences(StaticData.Const.PREFERENCE_PATH, MODE_PRIVATE);
        fragments = new ArrayList<>();
        fragments.add(new IntroStartupFragment());
        fragments.add(new IntroLoadingFragment());
        fragments.add(new IntroNetworkFragment());
        if (PackageUtils.getGameCount(this) >= 2){
            fragments.add(new IntroGameFragment());
        }else if (PackageUtils.getGameCount(this) < 1){
            preferences.edit().putString("game_language", "zh-CN").apply();
        }else {
            String packageName = PackageUtils.getGamePackageNameList(this).get(0);
            if (packageName.equals(StaticData.Const.PACKAGE_OFFICIAL) || packageName.equals(StaticData.Const.PACKAGE_BILIBILI)){
                preferences.edit().putString("game_language", "zh-CN").apply();
            }else if (packageName.equals(StaticData.Const.PACKAGE_ENGLISH)){
                preferences.edit().putString("game_language", "en").apply();
            }else if (packageName.equals(StaticData.Const.PACKAGE_JAPANESE)){
                preferences.edit().putString("game_language", "jp").apply();
            }else {
                preferences.edit().putString("game_language", "zh-CN").apply();
            }
        }
        fragments.add(new IntroThemeFragment());
        fragments.add(new IntroCompatibilityFragment());
        fragments.add(new IntroPlannerDownloadFragment());
        fragments.add(new IntroPlannerInitFragment());
        fragments.add(new IntroPermissionFragment());
        fragments.add(new IntroFinishFragment());
        IntroFragmentPagerAdapter fragmentPagerAdapter = new IntroFragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, fragments);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCanScroll(false);
        Drawable vectorDrawable = getDrawable(R.drawable.intro_background);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0,0,canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        BitmapDrawable backgroundDrawable = new BitmapDrawable(getResources(), bitmap);
        backgroundDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        viewPager.setBackground(backgroundDrawable);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                fragments.get(position).onShow();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setCurrentItem(int item){
        viewPager.setCurrentItem(item);
    }

    public int getCurrentItem(){
        return viewPager.getCurrentItem();
    }

    public int getItemCount(){
        return fragments.size();
    }

    public void setOCRSupported(Boolean OCRSupported) {
        isOCRSupported = OCRSupported;
    }

    public void setPythonSupported(Boolean pythonSupported) {
        isPythonSupported = pythonSupported;
    }

    public Boolean getOCRSupported() {
        return isOCRSupported;
    }

    public Boolean getPythonSupported() {
        return isPythonSupported;
    }
}