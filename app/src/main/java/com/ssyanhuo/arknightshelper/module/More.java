package com.ssyanhuo.arknightshelper.module;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.AboutActivity;
import com.ssyanhuo.arknightshelper.activity.MainActivity;
import com.ssyanhuo.arknightshelper.activity.SettingsActivity;
import com.ssyanhuo.arknightshelper.service.OverlayService;

public class More {
    Context applicationContext;
    LinearLayout contentView;
    LinearLayout goMain;
    LinearLayout goSetting;
    LinearLayout goQQ;
    LinearLayout goWiki;
    LinearLayout goTool;
    LinearLayout goStatistics;
    LinearLayout goPlanner;
    LinearLayout goAbout;
    TextView textView;
    public void init(final Context context, View view, final OverlayService service){
        applicationContext = context;
        contentView = (LinearLayout) view;
        textView = contentView.findViewById(R.id.more_description);
        goMain = contentView.findViewById(R.id.more_go_main);
        goSetting = contentView.findViewById(R.id.more_go_setting);
        goQQ = contentView.findViewById(R.id.more_go_qq);
        goWiki = contentView.findViewById(R.id.more_go_wiki);
        goTool = contentView.findViewById(R.id.more_go_tool);
        goStatistics = contentView.findViewById(R.id.more_go_statistics);
        goPlanner = contentView.findViewById(R.id.more_go_planner);
        goAbout = contentView.findViewById(R.id.more_go_about);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            textView.append(applicationContext.getString(R.string.get_permission_background_activity));
        }
        goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(applicationContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(applicationContext, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://jq.qq.com/?_wv=1027&k=5bPf1xW"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goWiki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://ak.mooncell.wiki"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://aktools.graueneko.xyz"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://penguin-stats.io"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goPlanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://planner.penguin-stats.io/"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(applicationContext, AboutActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
    }
}
