package com.ssyanhuo.arknightshelper.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.misc.StaticData;
import com.ssyanhuo.arknightshelper.utils.PackageUtils;


public class GameSelectorItem extends LinearLayout {

    String gameName;
    String gamePackage;
    boolean launchGame = true;
    TextView desc;
    Context context;

    public GameSelectorItem(Context context) {
        super(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_game_selector_item, this);
        desc = findViewById(R.id.game_selector_item_desc);
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
        desc.setText(String.format(context.getString(R.string.game_selector_item_desc), gameName));
    }

    public String getGameName() {
        return gameName;
    }

    public void setGamePackage(String gamePackage) {
        this.gamePackage = gamePackage;
        setGameName(PackageUtils.getName(gamePackage, context));
    }

    public String getGamePackage() {
        return gamePackage;
    }

    public void setLaunchGame(boolean launchGame) {
        this.launchGame = launchGame;
        if (!launchGame){
            desc.setText(R.string.game_selector_item_desc_no_game);
            this.gamePackage = StaticData.Const.PACKAGE_NONE;
        }
    }

    public boolean isLaunchGame() {
        return launchGame;
    }
}