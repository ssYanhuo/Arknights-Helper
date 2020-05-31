package com.ssyanhuo.arknightshelper.module;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.AlarmClock;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.activity.AboutActivity;
import com.ssyanhuo.arknightshelper.activity.MainActivity;
import com.ssyanhuo.arknightshelper.activity.SettingsActivity;
import com.ssyanhuo.arknightshelper.entity.StaticData;
import com.ssyanhuo.arknightshelper.service.OverlayService;

import java.util.Calendar;
import java.util.TimeZone;

public class More {
    Context applicationContext;
    private OverlayService overlayService;
    private Context originalContext;
    LinearLayout contentView;
    LinearLayout goMain;
    LinearLayout goSetting;
    LinearLayout goQQ;
    LinearLayout goWiki;
    LinearLayout goTool;
    LinearLayout goStatistics;
    LinearLayout goPlanner;
    LinearLayout goAbout;
    LinearLayout setAlarm;
    TextView hrCounterSummary;
    TextView textView;
    Button hrCounterPlus;
    Button hrCounterMinus;
    int hrCount;
    SharedPreferences preferences;
    public void init(final Context context, View view, final OverlayService service, final Context originalContext){
        this.applicationContext = context;
        this.overlayService = service;
        this.originalContext = originalContext;
        preferences = this.applicationContext.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE);
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
        hrCounterSummary = contentView.findViewById(R.id.more_hr_counter_summary);
        hrCounterPlus = contentView.findViewById(R.id.more_hr_counter_plus);
        hrCounterMinus = contentView.findViewById(R.id.more_hr_counter_minus);
        setAlarm = contentView.findViewById(R.id.more_set_alarm);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            textView.append(this.applicationContext.getString(R.string.get_permission_background_activity));
        }
        goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this.applicationContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                More.this.applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this.applicationContext, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                More.this.applicationContext.startActivity(intent);
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
                More.this.applicationContext.startActivity(intent);
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
                More.this.applicationContext.startActivity(intent);
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
                More.this.applicationContext.startActivity(intent);
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
                More.this.applicationContext.startActivity(intent);
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
                More.this.applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        goAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(More.this.applicationContext, AboutActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                More.this.applicationContext.startActivity(intent);
                service.hideFloatingWindow();
            }
        });
        hrCount = preferences.getInt("hr_count", 0);
        hrCounterPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hrCount <= 0){
                    hrCount = 0;
                }
                hrCount++;
                preferences.edit().putInt("hr_count", hrCount).apply();
                onCountChanged(hrCount);
            }
        });
        hrCounterMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hrCount > 0){
                    hrCount--;
                } else if (hrCount < 0){
                    hrCount = 0;
                }
                preferences.edit().putInt("hr_count", hrCount).apply();
                onCountChanged(hrCount);
            }
        });
        hrCounterPlus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (hrCount <= 0){
                    hrCount = 0;
                }
                hrCount += 10;
                preferences.edit().putInt("hr_count", hrCount).apply();
                onCountChanged(hrCount);
                return true;
            }
        });
        hrCounterMinus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                hrCount = 0;
                preferences.edit().putInt("hr_count", hrCount).apply();
                onCountChanged(hrCount);
                return true;
            }
        });
        onCountChanged(hrCount);
        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(originalContext, R.style.AppTheme_Default);
                LinearLayout interFace = (LinearLayout) LayoutInflater.from(contextThemeWrapper).inflate(R.layout.dialog_set_alarm, null);
                final TextView note = interFace.findViewById(R.id.more_set_alarm_note);
                final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                final NumberPicker pickerRequired = interFace.findViewById(R.id.more_set_alarm_picker_required);
                pickerRequired.setMinValue(1);
                pickerRequired.setMaxValue(150);
                pickerRequired.setValue(130);
                pickerRequired.setEnabled(true);
                builder.setTitle("设定倒计时")
                        .setView(interFace)
                        .setPositiveButton("设定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int value = pickerRequired.getValue();
                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                int minute = calendar.get(Calendar.MINUTE);
                                minute += (value * 6) % 60;
                                hour += (value * 6) / 60 + (minute / 60);
                                Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER);
                                intent.putExtra(AlarmClock.EXTRA_LENGTH, value * 6 * 60);
                                intent.putExtra(AlarmClock.EXTRA_MESSAGE, "明日方舟助手-理智回复");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                applicationContext.startActivity(intent);
                                overlayService.hideFloatingWindow();
                            }
                        })
                        .setNegativeButton("取消", null);
                final AlertDialog dialog = builder.create();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                }else {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                }
                pickerRequired.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        int value = newVal;
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        minute += value * 6;
                        hour += (minute / 60);
                        minute %= 60;
                        if(hour >= 24){
                            hour -= 24;
                            note.setText(new StringBuilder().append(applicationContext.getString(R.string.more_set_alarm_note_tomorrow, hour, minute)).append(" ").toString());
                        }else {
                            note.setText(new StringBuilder().append(applicationContext.getString(R.string.more_set_alarm_note_today, hour, minute)).append(" ").toString());
                        }
                        if (hour <= 6 || hour >= 22){
                            note.append("\n这可能会打扰到周围人的休息 ");
                        }
                    }
                });
                dialog.show();
            }
        });
    }
    private void onCountChanged(int count){
        if (count <= 0){
            hrCounterSummary.setText(R.string.more_hr_counter_summary_empty);
        }else {
            if (count < 50){
                hrCounterSummary.setText(applicationContext.getString(R.string.more_hr_counter_summary, count, 2));
            }else if (count == 99){
                hrCounterSummary.setText(R.string.more_hr_counter_summary_full);
            }else if(count >= 100){
                hrCounterSummary.setText(applicationContext.getString(R.string.more_hr_counter_summary_overflow, count));
            }else if(count >= 90){
                hrCounterSummary.setText(applicationContext.getString(R.string.more_hr_counter_summary_90, count, 2 + 2 * (count - 50)));
            }
            else {
                hrCounterSummary.setText(applicationContext.getString(R.string.more_hr_counter_summary, count, 2 + 2 * (count - 50)));
            }
        }
    }
}
