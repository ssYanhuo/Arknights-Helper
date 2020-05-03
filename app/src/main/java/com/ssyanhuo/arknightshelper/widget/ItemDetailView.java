package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssyanhuo.arknightshelper.R;

import java.util.ArrayList;

public class ItemDetailView extends LinearLayout{

    String itemName;
    ArrayList<String> stages;
    int number;
    boolean queryable;
    Drawable image;
    TextView itemNameTextView;
    TextView numberTextView;
    TextView stagesTextView;
    ImageView imageView;
    Button queryButton;


    public ItemDetailView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_itemdetailview, this);
        itemNameTextView = findViewById(R.id.itemdetailview_name);
        stagesTextView = findViewById(R.id.itemdetailview_stages);
        numberTextView = findViewById(R.id.itemdetailview_number);
        imageView = findViewById(R.id.itemdetailview_image);
        queryButton = findViewById(R.id.itemdetailview_query);
    }
    public void setItemName(String itemName){
        this.itemName = itemName;
        itemNameTextView.setText(itemName);
        itemNameTextView.setVisibility(VISIBLE);
    }
    public String getItemName(){
        return itemName;
    }

    public void setStages(ArrayList<String> stages){
        this.stages = stages;
        StringBuilder stage = new StringBuilder();
        for (int i = 0; i < stages.size(); i++){
            stage.append(stages.get(i));
            if (i != stages.size() - 1){
                stage.append("\n");
            }
        }
        stagesTextView.setText(stage);
        stagesTextView.setVisibility(VISIBLE);
    }

    public ArrayList<String> getStages(){
        return stages;
    }

    public void setImage(Drawable image){
        this.image = image;
        imageView.setImageDrawable(image);
    }

    public Drawable getImage() {
        return image;
    }

    public void setNumber(int number) {
        this.number = number;
        numberTextView.setText(String.valueOf(number));
        numberTextView.setVisibility(VISIBLE);
    }

    public void setText(String text) {
        numberTextView.setText(text);
        numberTextView.setVisibility(VISIBLE);
    }

    public void appendText(String text) {
        numberTextView.append(text);
        numberTextView.setVisibility(VISIBLE);
    }

    public int getNumber() {
        return number;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
        if (queryable){queryButton.setVisibility(VISIBLE);}
    }
}