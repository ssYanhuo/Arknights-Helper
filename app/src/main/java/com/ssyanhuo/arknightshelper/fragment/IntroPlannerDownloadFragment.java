package com.ssyanhuo.arknightshelper.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.utils.IntentUtils;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.widget.IntroFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IntroPlannerDownloadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroPlannerDownloadFragment extends IntroFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LinearLayout downloadFromCoolapk;
    private MaterialButton downloadFromLanzou;
    private MaterialButton notNow;
    private LinearLayout downloadFinished;

    public IntroPlannerDownloadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IntroPlannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IntroPlannerDownloadFragment newInstance(String param1, String param2) {
        IntroPlannerDownloadFragment fragment = new IntroPlannerDownloadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro_planner_download, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadFromCoolapk = getView().findViewById(R.id.intro_planner_download_from_coolapk);
        downloadFromLanzou = getView().findViewById(R.id.intro_planner_download_from_lanzou);
        notNow = getView().findViewById(R.id.intro_planner_download_not_now);
        downloadFinished = getView().findViewById(R.id.intro_planner_download_finished);
        if (PackageUtils.checkApplication(StaticData.Const.PLANNER_PLUGIN_PACKAGE_NAME, getContext())){
            downloadFromCoolapk.setVisibility(View.GONE);
            downloadFromLanzou.setVisibility(View.GONE);
            notNow.setVisibility(View.GONE);
            downloadFinished.setVisibility(View.VISIBLE);
            ((FloatingActionButton) getView().findViewById(R.id.intro_planner_download_next)).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        downloadFromCoolapk = getView().findViewById(R.id.intro_planner_download_from_coolapk);
        downloadFromCoolapk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.openURL("https://www.coolapk.com/apk/263387", getActivity());
            }
        });
        downloadFromLanzou = getView().findViewById(R.id.intro_planner_download_from_lanzou);
        downloadFromLanzou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.openURL("https://ww.lanzous.com/ico7bfe", getActivity());
            }
        });
        notNow = getView().findViewById(R.id.intro_planner_download_not_now);
        notNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "日后可以从设置中重新启用刷图规划功能", Toast.LENGTH_SHORT).show();
                goNext();
            }
        });
        downloadFinished = getView().findViewById(R.id.intro_planner_download_finished);
        ((FloatingActionButton) getView().findViewById(R.id.intro_planner_download_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
    }

}