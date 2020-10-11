package com.ssyanhuo.arknightshelper.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.widget.IntroFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IntroCompatibilityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroCompatibilityFragment extends IntroFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IntroCompatibilityFragment() {
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
    public static IntroCompatibilityFragment newInstance(String param1, String param2) {
        IntroCompatibilityFragment fragment = new IntroCompatibilityFragment();
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
        return inflater.inflate(R.layout.fragment_intro_compatibility, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((FloatingActionButton) getView().findViewById(R.id.intro_compatibility_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
    }
}