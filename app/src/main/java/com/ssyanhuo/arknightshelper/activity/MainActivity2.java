package com.ssyanhuo.arknightshelper.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.fragment.BottomFeedbackFragment;
import com.ssyanhuo.arknightshelper.fragment.BottomSettingsFragment;
import com.ssyanhuo.arknightshelper.module.Material;

public class MainActivity2 extends AppCompatActivity {
    MaterialCardView startEngineCardView;
    MaterialCardView engineConfigCardView;
    MaterialCardView settingsCardView;
    MaterialCardView feedbackCardView;
    MaterialCardView aboutCardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        startEngineCardView = findViewById(R.id.main_start_engine_card_view);
        engineConfigCardView = findViewById(R.id.main_engine_config_card_view);
        settingsCardView = findViewById(R.id.main_settings_card_view);
        feedbackCardView = findViewById(R.id.main_feedback_card_view);
        aboutCardView = findViewById(R.id.main_about_card_view);
        settingsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BottomSettingsFragment().show(getSupportFragmentManager(), null);
            }
        });
        feedbackCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BottomFeedbackFragment().show(getSupportFragmentManager(), null);
            }
        });
    }
}