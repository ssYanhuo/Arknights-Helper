package com.ssyanhuo.arknightshelper.activity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.ssyanhuo.arknightshelper.R;

public class PrefFragment extends PreferenceFragment {

    final String GAME_OFFICIAL = "0";
    final String GAME_BILIBILI = "1";
    final String GAME_MANUAL = "-1";
    final String PACKAGE_OFFICIAL = "com.hypergryph.arknights";
    final String PACKAGE_BILIBILI = "com.hypergryph.arknights.bilibili";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference long_press_back_to_game = findPreference("long_press_back_to_game");
        Preference game_version = findPreference("game_version");
        if (!checkApplication(PACKAGE_OFFICIAL) || !checkApplication(PACKAGE_BILIBILI)){
            game_version.setEnabled(false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ListView)(getView()).findViewById(android.R.id.list)).setDivider(null);

    }

    public boolean checkApplication(String packageName) {
        if (packageName == null || "".equals(packageName)){
            return false;
        }
        try {
            ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


}
