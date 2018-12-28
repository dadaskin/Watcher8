package com.adaskin.android.watcher8.views;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.models.StockQuote;

import java.util.Locale;
import java.util.Objects;

public abstract class GenericDetailsActivity extends AppCompatActivity {

    View mRefreshButtonView;
    StockQuote mQuote;

    public GenericDetailsActivity() {
        super();
    }

    public void setTitleString(String symbol) {
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getColor(android.R.color.white))); // Makes background of whole actionBar white
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customTitleView = inflater.inflate(R.layout.custom_main_titlebar, null);
        TextView tv = customTitleView.findViewById(R.id.custom_main_titlebar_text);
        tv.setText(getString(R.string.app_name) + ": " + symbol + getString(R.string.detail) );
        actionBar.setCustomView(customTitleView);
    }

    public void detailRefreshButtonClicked(View v) {
        mRefreshButtonView = v;
//		String msg = mQuote.mPPS + "\t" + mQuote.mDivPerShare + "\t" + mQuote.mAnalystsOpinion;
//		Log.d("myTag", msg);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_1sec_center);
        animation.setRepeatCount(Animation.INFINITE);
        mRefreshButtonView.startAnimation(animation);
        updateSingleSymbol();
    }

    protected void updateQuoteInDB(StockQuote updatedQuote) {
        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        dbAdapter.changeQuoteRecord(dbAdapter.fetchQuoteIdFromSymbol(updatedQuote.mSymbol), updatedQuote);
        dbAdapter.close();
    }

    protected abstract void singleSymbolUpdateCompleted(StockQuote updatedQuote);

    private void updateSingleSymbol() {
        // Get A VolleyRequest instance
        // Setup the 3 listeners
        // Start

        // After response is received:
        //    Parse
        //    call singleSymbolUpdateCompleted() in derived class.
        //    stop animation
    }


}
