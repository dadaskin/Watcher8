package com.adaskin.android.watcher8.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.utilities.Constants;
import com.adaskin.android.watcher8.utilities.Themes;

import java.util.Locale;

public class BuyBlockCursorAdapter extends SimpleCursorAdapter {

    private static class ViewHolder {
        TextView colorView;
        TextView dateView;
        TextView numSharesView;
        TextView ppsBuyView;
        TextView pctChangeSinceBuyView;
        TextView effYieldView;
    }


    private final LayoutInflater mInflater;
    private final int mAccountIdx;
    private final int mDateIdx;
    private final int mNumSharesIdx;
    private final int mPPSBuyIdx;
    private final int mPctChangeSinceBuyIdx;
    private final int mEffYieldIdx;

    private final float mGainTarget;


    public BuyBlockCursorAdapter(Context context,
                                 Cursor cursor,
                                 float gainTarget,
                                 String[] fields,
                                 int[] ids) {
        super(context, R.layout.buy_block_row, cursor, fields, ids, 0);
        mInflater = LayoutInflater.from(context);

        mAccountIdx = cursor.getColumnIndex(DbAdapter.B_ACCOUNT);
        mDateIdx = cursor.getColumnIndex(DbAdapter.B_DATE);
        mNumSharesIdx = cursor.getColumnIndex(DbAdapter.B_NUM_SHARES);
        mPPSBuyIdx = cursor.getColumnIndex(DbAdapter.B_PPS);
        mPctChangeSinceBuyIdx = cursor.getColumnIndex(DbAdapter.B_CHANGE_VS_BUY);
        mEffYieldIdx = cursor.getColumnIndex(DbAdapter.B_EFF_YIELD);

        mGainTarget = gainTarget;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.buy_block_row, parent, false);
        ViewHolder  holder = new ViewHolder();
        holder.colorView = view.findViewById(R.id.account_color_field_id);
        holder.dateView = view.findViewById(R.id.date_field_id);
        holder.numSharesView = view.findViewById(R.id.num_shares_field_id);
        holder.ppsBuyView = view.findViewById(R.id.buy_pps_field_id);
        holder.pctChangeSinceBuyView = view.findViewById(R.id.chng_buy_field_id);
        holder.effYieldView = view.findViewById(R.id.eff_div_field_id);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder)view.getTag();

        // Color
        int colorValue = cursor.getInt(mAccountIdx);
        holder.colorView.setBackgroundColor(colorValue);

        // Date
        String dateStr = cursor.getString(mDateIdx);
        holder.dateView.setText(dateStr);

        // Number of Shares
        float numShares = cursor.getFloat(mNumSharesIdx);
        holder.numSharesView.setText(String.format(Locale.US, Constants.NUM_SHARES_FORMAT, numShares));

        // Buy prices per share
        float ppsBuy = cursor.getFloat(mPPSBuyIdx);
        holder.ppsBuyView.setText(String.format(Locale.US,Constants.CURRENCY_FORMAT, ppsBuy));

        // % change since buy
        float chngVsBuy = cursor.getFloat(mPctChangeSinceBuyIdx);
        showZeroWithoutSign(holder.pctChangeSinceBuyView, chngVsBuy);
        Themes.adjustBuyBlockTextColor(context, holder.pctChangeSinceBuyView, chngVsBuy, mGainTarget);

        // Effective Yield
        float effYield = cursor.getFloat(mEffYieldIdx);
        if (effYield > Constants.POSITIVE_ONE_DECIMAL_LIMIT) {
            holder.effYieldView.setText(String.format(Locale.US,Constants.PERCENTAGE_FORMAT, effYield));
        } else {
            holder.effYieldView.setText("--");
        }
    }

    private void showZeroWithoutSign(TextView view, float value) {
        if ((value < Constants.POSITIVE_ONE_DECIMAL_LIMIT) &&
                (value > Constants.NEGATIVE_ONE_DECIMAL_LIMIT))	{
            view.setText(String.format(Locale.US,Constants.PERCENTAGE_FORMAT, 0.0f));
        } else {
            view.setText(String.format(Locale.US,Constants.PERCENTAGE_FORMAT, value));
        }
    }
}
