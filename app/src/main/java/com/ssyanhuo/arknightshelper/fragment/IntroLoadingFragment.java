package com.ssyanhuo.arknightshelper.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.IntroActivity;
import com.ssyanhuo.arknightshelper.utils.OCRUtils;
import com.ssyanhuo.arknightshelper.utils.PythonUtils;
import com.ssyanhuo.arknightshelper.widget.IntroFragment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IntroLoadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroLoadingFragment extends IntroFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IntroLoadingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IntroCompatibilityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IntroLoadingFragment newInstance(String param1, String param2) {
        IntroLoadingFragment fragment = new IntroLoadingFragment();
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
        return inflater.inflate(R.layout.fragment_intro_loading, container, false);
    }

    @Override
    public void onShow() {
        super.onShow();
        ((IntroActivity) getActivity()).setPythonSupported(PythonUtils.isSupported());
        ((IntroActivity) getActivity()).setOCRSupported(OCRUtils.isAbiSupported());
        final Handler handler = new Handler();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        goNext();
                    }
                });

            }
        }, 5000);
    }
}