package com.ssyanhuo.arknightshelper.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.widget.IntroFragment;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IntroPlannerInitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroPlannerInitFragment extends IntroFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IntroPlannerInitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IntroPlannerInitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IntroPlannerInitFragment newInstance(String param1, String param2) {
        IntroPlannerInitFragment fragment = new IntroPlannerInitFragment();
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
        return inflater.inflate(R.layout.fragment_intro_planner_init, container, false);
    }

    @Override
    public void onShow() {
        super.onShow();
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String CACHE_PATH = getContext().getCacheDir().toString();
                final String ASSETS_PATH = CACHE_PATH + File.separator + "plugin" + File.separator + "assets";
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(getContext(),getContext().getString(R.string.py_error) + e.getMessage(), Toast.LENGTH_LONG).show();
                    Looper.loop();
                }

            }
        });
        thread.start();
    }
}