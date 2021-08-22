package com.ssyanhuo.arknightshelper.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ssyanhuo.arknightshelper.BuildConfig;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.LabActivity;
import com.ssyanhuo.arknightshelper.misc.StaticData;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;
import com.ssyanhuo.arknightshelper.utils.PermissionUtils;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomPermissionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomPermissionFragment extends BottomSheetDialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static String TAG = "PermissionFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    private Context context;
    private Activity activity;
    private ImageView storageState;
    private ImageView overlayState;
    private LinearLayout storage;
    private LinearLayout overlay;
    private LinearLayout shortcut;
    private LinearLayout background;

    public BottomPermissionFragment() {
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
    public static BottomPermissionFragment newInstance(String param1, String param2) {
        BottomPermissionFragment fragment = new BottomPermissionFragment();
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
        return inflater.inflate(R.layout.fragment_bottom_permissions, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        view = getView();
        context = getContext();
        activity = getActivity();
        storageState = view.findViewById(R.id.main_permission_state_storage);
        overlayState = view.findViewById(R.id.main_permission_state_overlay);
        storage = view.findViewById(R.id.main_permission_storage);
        overlay = view.findViewById(R.id.main_permission_overlay);
        shortcut = view.findViewById(R.id.main_permission_shortcut);
        background = view.findViewById(R.id.main_permission_background);
        storage.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        });
        overlay.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            startActivity(intent);
        });
        shortcut.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            startActivity(intent);
        });
        background.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            startActivity(intent);
        });

        checkState();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkState();
    }

    private void checkState(){
        storageState.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_permission_granted));
        overlayState.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_permission_granted));
        String[] requiredPermissions = {PermissionUtils.PERMISSION_STORAGE, PermissionUtils.PERMISSION_OVERLAY};
        ArrayList<String> missingPermissions = PermissionUtils.checkPermissions(requiredPermissions, context);
        for (String permission :
                missingPermissions) {
            switch (permission) {
                case PermissionUtils.PERMISSION_STORAGE:
                    storageState.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_permission_not_granted));
                    break;
                case PermissionUtils.PERMISSION_OVERLAY:
                    overlayState.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_permission_not_granted));
                    break;
            }
        }
        if (missingPermissions.size() == 0){
            dismiss();
        }
    }
}