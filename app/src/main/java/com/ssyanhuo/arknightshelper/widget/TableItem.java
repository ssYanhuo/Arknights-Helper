package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssyanhuo.arknightshelper.R;

public class TableItem extends LinearLayout {
    TextView stageCell;
    TextView timeCell;
    TextView quantityCell;
    TextView costCell;
    public TableItem(Context context) {
        super(context);
    }
    public TableItem(Context context, String stage, int time, int quantity, float cost) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_tableitem, this);
        stageCell = findViewById(R.id.tableitem_stage);
        timeCell = findViewById(R.id.tableitem_time);
        quantityCell = findViewById(R.id.tableitem_quantity);
        costCell = findViewById(R.id.tableitem_cost);
        stageCell.setText(String.valueOf(stage));
        timeCell.setText(String.valueOf(time));
        quantityCell.setText(String.valueOf(quantity));
        costCell.setText(String.valueOf(cost));
    }
    public TableItem(Context context, String stage, String time, String quantity, String cost) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_tableitem, this);
        stageCell = findViewById(R.id.tableitem_stage);
        timeCell = findViewById(R.id.tableitem_time);
        quantityCell = findViewById(R.id.tableitem_quantity);
        costCell = findViewById(R.id.tableitem_cost);
        stageCell.setText(String.valueOf(stage));
        timeCell.setText(String.valueOf(time));
        quantityCell.setText(String.valueOf(quantity));
        costCell.setText(String.valueOf(cost));
    }
}
