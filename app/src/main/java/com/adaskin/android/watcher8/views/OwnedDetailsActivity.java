package com.adaskin.android.watcher8.views;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.fragments.AccountSelectionFragment;
import com.adaskin.android.watcher8.models.AccountModel;
import com.adaskin.android.watcher8.models.BuyBlock;
import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.Constants;
import com.adaskin.android.watcher8.utilities.Themes;

import java.util.List;
import java.util.Locale;

public class OwnedDetailsActivity extends GenericDetailsActivity implements AccountSelectionFragment.AlertOkListener {

    private BuyBlock mChangingBlock;
    //private AccountSelectionFragment mAccountsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.owned_details);

        Bundle bundle = getIntent().getExtras();
        String symbol = bundle.getString(Constants.SYMBOL_BUNDLE_KEY);
        setTitleString(symbol);

        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();

        long parentId = dbAdapter.fetchQuoteIdFromSymbol(symbol);
        mQuote = dbAdapter.fetchQuoteObjectFromId(parentId);
        dbAdapter.close();

        registerForContextMenu(findViewById(android.R.id.list));

        fillData();
    }

    private void fillData() {
        TextView nameField = findViewById(R.id.owned_full_name_field);
        TextView ppsField = findViewById(R.id.owned_pps_field);
        TextView divPSField = findViewById(R.id.owned_divps_field);
        TextView analOpField = findViewById(R.id.owned_anal_op_field);
        TextView yrMinField = findViewById(R.id.owned_yr_min_field);
        TextView yrMaxField = findViewById(R.id.owned_yr_max_field);
        TextView strikeField = findViewById(R.id.owned_strike_price_field);
        TextView gainField = findViewById(R.id.owned_gain_target_field);

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
        gainField.setText(String.format(Locale.US,Constants.PERCENTAGE_FORMAT, mQuote.mPctGainTarget));


        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        Cursor cursor = dbAdapter.fetchBuyBlockRecordsForThisSymbol(mQuote.mSymbol);
        dbAdapter.close();

        displayTotalInvestment(cursor, mQuote);

//        ListView blockListView = findViewById(android.R.id.list);
//        String[] fields = new String[] {DbAdapter.B_ACCOUNT,
//                DbAdapter.B_DATE,
//                DbAdapter.B_NUM_SHARES,
//                DbAdapter.B_PPS,
//                DbAdapter.B_CHANGE_VS_BUY,
//                DbAdapter.B_EFF_YIELD};
//
//        int[] ids = new int[] { R.id.account_color_field_id,
//                R.id.date_field_id,
//                R.id.num_shares_field_id,
//                R.id.buy_pps_field_id,
//                R.id.chng_buy_field_id,
//                R.id.eff_div_field_id};
//
//        BuyBlockCursorAdapter bbca
//                = new BuyBlockCursorAdapter(this, cursor, mQuote.mPctGainTarget, fields, ids);
//
//        blockListView.setAdapter(bbca);
    }

//   // Create context menu and dispatch selections
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater mi = getMenuInflater();
//        mi.inflate(R.menu.longpress_buyblock, menu);
//    }

    private void displayTotalInvestment(Cursor cursor, StockQuote quote)
    {
        TextView amountView = findViewById(R.id.owned_total_investment_amount);
        TextView gainView = findViewById(R.id.owned_total_investment_gain);
        TextView divyView = findViewById(R.id.owned_total_investment_divy);

        float totalShares = 0.0f;
        float totalAmount = 0.0f;

        while (!cursor.isAfterLast()) {
            float numShares = cursor.getFloat(cursor.getColumnIndex(DbAdapter.B_NUM_SHARES));
            totalShares += numShares;
            float pps = cursor.getFloat(cursor.getColumnIndex(DbAdapter.B_PPS));
            totalAmount += numShares * pps;
            cursor.moveToNext();
        }

        float overallGain = ((quote.mPPS * totalShares)/totalAmount - 1) * 100.0f;
        float overallEffectiveDividend = ((quote.mDivPerShare * totalShares)/totalAmount) * 100.0f;

        amountView.setText(String.format(Locale.US, Constants.CURRENCY_FORMAT_INTEGER, totalAmount));

        gainView.setText(String.format(Locale.US, Constants.PERCENTAGE_FORMAT, overallGain));
        Themes.adjustOverallTextColor(this, gainView, overallGain);

        if (mQuote.mDivPerShare > Constants.MINIMUM_SIGNIFICANT_VALUE)
            divyView.setText(String.format(Locale.US, Constants.PERCENTAGE_FORMAT, overallEffectiveDividend));
        else
            divyView.setText("--");
    }

    @Override
    public void onOkClick(int position) {
        List<Integer> colorList = AccountModel.getBlockAccountColorList();
        int accountColor = colorList.get(position);
     //   mAccountsFragment.dismiss();

        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        dbAdapter.removeQuoteRecord(mQuote.mSymbol);
        mQuote.mBuyBlockList.remove(mChangingBlock);
        mChangingBlock.mAccountColor = accountColor;
        mQuote.mBuyBlockList.add(mChangingBlock);
        dbAdapter.createQuoteRecord(mQuote);
        dbAdapter.close();
        fillData();
    }

    // Handle Add Another Block button
    @SuppressWarnings("UnusedParameters")
    public void addAnotherBlockButtonClicked(View v) {
        Toast.makeText(this, "TBD: Start BuyBlockAddActivity", Toast.LENGTH_LONG).show();
//        Intent intent = new Intent(this, BuyBlockAddActivity.class);
//        intent.putExtra(Constants.BUY_BLOCK_SYMBOL_KEY, mQuote.mSymbol);
//        startActivityForResult(intent, Constants.BUY_BLOCK_ADD_ACTIVITY);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//
//        if (resultCode == Activity.RESULT_OK) {
//            DbAdapter dbAdapter = new DbAdapter(this);
//            dbAdapter.open();
//            dbAdapter.removeQuoteRecord(mQuote.mSymbol);
//            switch(requestCode) {
//                case Constants.BUY_BLOCK_ADD_ACTIVITY:
//                    BuyBlock newBB = grabBuyBlockInfo(intent.getExtras());
//                    mQuote.mBuyBlockList.add(newBB);
//                    break;
//                case Constants.PARAMETER_CHANGE_ACTIVITY:
//                    String paramName = intent.getStringExtra(Constants.PARAM_NAME_BUNDLE_KEY);
//                    float newValue =  intent.getFloatExtra(Constants.PARAM_NEW_VALUE_BUNDLE_KEY, 0.0f);
//                    if (paramName.contains("Strike Price")) {
//                        mQuote.mStrikePrice = newValue;
//                    } else if (paramName.contains("Gain Target")) {
//                        mQuote.mPctGainTarget = newValue;
//                    } else {
//                        mQuote.mBuyBlockList.remove(mChangingBlock);
//                        mChangingBlock.mNumShares = newValue;
//                        mQuote.mBuyBlockList.add(mChangingBlock);
//                    }
//                    break;
//            }
//            dbAdapter.createQuoteRecord(mQuote);
//            dbAdapter.close();
//            fillData();
//        }
//    }

    // Handle parameter change buttons
    @SuppressWarnings("UnusedParameters")
    public void changeButtonClicked_StrikePrice(View v) {
        Toast.makeText(this, "TBD: Call ChangeParameterActivity for Strike Price", Toast.LENGTH_LONG).show();
//        Intent intent = new Intent(this, ChangeParameterActivity.class);
//        intent.putExtra(Constants.SYMBOL_BUNDLE_KEY, mQuote.mSymbol);
//        intent.putExtra(Constants.PARAM_NAME_BUNDLE_KEY, "Strike Price");
//        intent.putExtra(Constants.OLD_VALUE_BUNDLE_KEY, mQuote.mStrikePrice);
//        startActivityForResult(intent, Constants.PARAMETER_CHANGE_ACTIVITY);
    }

    @SuppressWarnings("UnusedParameters")
    public void changeButtonClicked_GainTarget(View v) {
        Toast.makeText(this, "TBD Call ChangeParameterActivity for Gain Target", Toast.LENGTH_LONG).show();
//        Intent intent = new Intent(this, ChangeParameterActivity.class);
//        intent.putExtra(Constants.SYMBOL_BUNDLE_KEY, mQuote.mSymbol);
//        intent.putExtra(Constants.PARAM_NAME_BUNDLE_KEY, "Gain Target");
//        intent.putExtra(Constants.OLD_VALUE_BUNDLE_KEY, mQuote.mPctGainTarget);
//        startActivityForResult(intent, Constants.PARAMETER_CHANGE_ACTIVITY);
    }

    @Override
    protected void singleSymbolUpdateCompleted(StockQuote updatedQuote) {
//		String msg = mQuote.mPPS + "\t" + mQuote.mDivPerShare + "\t" + mQuote.mAnalystsOpinion;
//		Log.d("myTag", msg);

        Toast.makeText(this, "TBD: OwnedDetailsActivity.singleSymbolUpdateComleted()", Toast.LENGTH_LONG).show();
//        updateQuoteInDB(updatedQuote);
//        fillData();
    }
}
