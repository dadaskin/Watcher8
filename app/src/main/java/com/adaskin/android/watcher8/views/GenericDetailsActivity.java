package com.adaskin.android.watcher8.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.Refresher;

import java.util.Objects;

public abstract class GenericDetailsActivity extends AppCompatActivity implements Refresher.RefreshedObject {

    StockQuote mQuote;

    public GenericDetailsActivity() {
        super();
    }

    void setTitleString(String symbol) {
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getColor(android.R.color.white))); // Makes background of whole actionBar white
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customTitleView = Objects.requireNonNull(inflater).inflate(R.layout.custom_main_titlebar, null);
        TextView tv = customTitleView.findViewById(R.id.custom_main_title_bar_text);
        tv.setText(String.format("%s: %s %s", getString(R.string.app_name),symbol,getString(R.string.detail) ));
        actionBar.setCustomView(customTitleView);
    }

    public abstract void fillData();

    void detailRefreshButtonClicked() {
        Refresher refresher = new Refresher(this,this);
        refresher.refreshSingle(mQuote);
    }
}
