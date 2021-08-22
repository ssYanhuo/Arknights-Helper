package com.ssyanhuo.arknightshelper.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.LabActivity;
import com.ssyanhuo.arknightshelper.misc.StaticData;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomAboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomAboutFragment extends BottomSheetDialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static String TAG = "AboutFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    int count;
    int line;
    boolean cancelEasterTimer;
    boolean showedEaster;
    Handler easterHandler;
    ScrollView scrollView;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BottomAboutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BottomAboutFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BottomAboutFragment newInstance(String param1, String param2) {
        BottomAboutFragment fragment = new BottomAboutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppTheme2_BottomSheetDialogFragment);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_about, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        Context context = getContext();
        count = 0;
        line = 0;
        cancelEasterTimer = false;
        easterHandler = new Handler();
        showedEaster = false;
        scrollView = view.findViewById(R.id.about_scroll);
        LinearLayout goGithub = view.findViewById(R.id.about_go_github);
        LinearLayout goProject = view.findViewById(R.id.about_go_project);
        LinearLayout goRelease = view.findViewById(R.id.about_go_release);
        LinearLayout goQQ = view.findViewById(R.id.about_go_qq);
        goGithub.setOnClickListener(this::goGithub);
        goProject.setOnClickListener(this::goProject);
        goRelease.setOnClickListener(this::goRelease);
        goQQ.setOnClickListener(this::goQQ);
        SharedPreferences preferences = getContext().getSharedPreferences(StaticData.Const.PREFERENCE_PATH, MODE_PRIVATE);
        if(preferences.getBoolean("enable_dark_mode", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        try {
            TextView versionText = view.findViewById(R.id.about_version_text);
            versionText.setText(BuildConfig.VERSION_NAME);
        }catch (Exception e){
            e.printStackTrace();
        }
        LinearLayout versionLayout = view.findViewById(R.id.about_version);
        versionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showedEaster){return;}
                count++;
                if(count >= 12){
                    count = 0;
                    showedEaster = true;
                    LinearLayout easterLinearLayout = view.findViewById(R.id.about_easter);
                    easterLinearLayout.setVisibility(View.VISIBLE);
                    easterLinearLayout.setLongClickable(true);
                    easterLinearLayout.setOnLongClickListener(view1 -> {
                        goLab(view1);
                        return true;
                    });
                }
            }
        });
    }

    public void goGithub(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://github.com/ssYanhuo"));
        startActivity(intent);
    }
    public void goProject(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://github.com/ssYanhuo/Arknights-Helper"));
        startActivity(intent);
    }
    public void goRelease(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.coolapk.com/apk/com.ssyanhuo.arknightshelper"));
        startActivity(intent);
    }
    public void goQQ(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://jq.qq.com/?_wv=1027&k=5bPf1xW"));
        startActivity(intent);
    }
    public void goLab(View view){
        Intent intent = new Intent(getContext(), LabActivity.class);
        startActivity(intent);
    }
}