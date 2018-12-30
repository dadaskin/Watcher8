package com.adaskin.android.watcher8.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.Constants;

import java.util.Locale;

public class WatchDetailsActivity extends GenericDetailsActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_details);

        String symbol = getIntent().getExtras().getString(Constants.SYMBOL_BUNDLE_KEY);
        setTitleString(symbol);

        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();

        long id = dbAdapter.fetchQuoteIdFromSymbol(symbol);
        mQuote = dbAdapter.fetchQuoteObjectFromId(id);
        dbAdapter.close();

        Log.d("myTag", "WatchDetailsActivity started.");

        fillData();
    }

    private void fillData() {
        TextView nameField = findViewById(R.id.watch_full_name_field);
        TextView ppsField = findViewById(R.id.watch_pps_field);
        TextView divPSField = findViewById(R.id.watch_divps_field);
        TextView analOpField = findViewById(R.id.watch_anal_op_field);
        TextView yrMinField = findViewById(R.id.watch_yr_min_field);
        TextView yrMaxField = findViewById(R.id.watch_yr_max_field);
        TextView strikeField = findViewById(R.id.watch_strike_price_field);

        nameField.setText(mQuote.mFullName);
        ppsField.setText(String.format(Locale.US,Constants.CURRENCY_FORMAT, mQuote.mPPS));
        String divPSAndYieldMsg = String.format(Locale.US,Constants.CURRENCY_FORMAT, mQuote.mDivPerShare) +
                "  (" +
                String.format(Locale.US,Constants.PERCENTAGE_FORMAT, mQuote.mDivPerShare*100f/mQuote.mPPS) +
                ")";
        divPSField.setText(divPSAndYieldMsg);

        analOpField.setText(String.format(Locale.US,Constants.OPINION_FORMAT, mQuote.mAnalystsOpinion));
        yrMinField.setText(String.format(Locale.US,Constants.CURRENCY_FORMAT, mQuote.mYrMin));
        yrMaxField.setText(String.format(Locale.US,Constants.CURRENCY_FORMAT, mQuote.mYrMax));
        strikeField.setText(String.format(Locale.US,Constants.CURRENCY_FORMAT, mQuote.mStrikePrice));
    }

    @SuppressWarnings("UnusedParameters")
    public void changeButtonClicked(View v) {
//        Toast.makeText(this, "TBD: ChangeButtonClicked", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ChangeParameterActivity.class);
        intent.putExtra(Constants.SYMBOL_BUNDLE_KEY, mQuote.mSymbol);
        intent.putExtra(Constants.PARAM_NAME_BUNDLE_KEY, "Strike Price");
        intent.putExtra(Constants.OLD_VALUE_BUNDLE_KEY, mQuote.mStrikePrice);
        startActivityForResult(intent, Constants.PARAMETER_CHANGE_ACTIVITY);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//        if (resultCode == Activity.RESULT_OK) {
//            DbAdapter dbAdapter = new DbAdapter(this);
//            dbAdapter.open();
//            dbAdapter.removeQuoteRecord(mQuote.mSymbol);
//            mQuote.mStrikePrice = intent.getFloatExtra(Constants.PARAM_NEW_VALUE_BUNDLE_KEY, 0.0f);
//            dbAdapter.createQuoteRecord(mQuote);
//            dbAdapter.close();
//            fillData();
//        }
//    }

    @Override
    protected void singleSymbolUpdateCompleted(StockQuote updatedQuote) {
//        String msg = mQuote.mPPS + "\t" + mQuote.mDivPerShare + "\t" + mQuote.mAnalystsOpinion;
//        Log.d("myTag", msg);

        Toast.makeText(this, "TBD: WatchDetailsActivity.singleSymbolUpdateComleted()", Toast.LENGTH_LONG).show();

//        updateQuoteInDB(updatedQuote);
//        fillData();
    }


}
