package com.adaskin.android.watcher8.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.adapters.QuoteCursorAdapter;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.models.DataModel;
import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.Constants;
import com.adaskin.android.watcher8.utilities.QuoteStatus;
import com.adaskin.android.watcher8.views.OwnedAddActivity;
import com.adaskin.android.watcher8.views.WatchAddActivity;
import com.adaskin.android.watcher8.views.WatchDetailsActivity;

import java.util.Objects;


public class WatchFragment extends ListFragmentBase {

    private int mSelectedPosition = -1;

    public WatchFragment() {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());

        fillData();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Objects.requireNonNull(getActivity()).getMenuInflater().inflate(R.menu.longpress_watch, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        String symbol = getSymbolFromRow(info.targetView);

        switch(item.getItemId()){
            case R.id.menu_watch_to_owned:
                buyBlockOfThisStock(symbol);
                fillData();
                return true;
            case R.id.menu_watch_delete:
                createAndShowConfirmDialog(symbol);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void buyBlockOfThisStock(String symbol) {
        Intent intent = new Intent(getActivity(), OwnedAddActivity.class);
        intent.putExtra(Constants.BUY_BLOCK_SYMBOL_KEY, symbol);
        startActivityForResult(intent, Constants.OWNED_ADD_ACTIVITY);
    }

    private void createAndShowConfirmDialog(String symbol) {
        String msg = "Delete symbol: " + symbol + "\n";

        final String finalSymbol = symbol;

        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle("Confirm Delete")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDelete(finalSymbol);
                    }
                })
                .show();
    }

    private void doDelete(String symbol) {
        DbAdapter dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();

        DataModel model = new DataModel(dbAdapter);
        model.removeEntryFromDB(symbol);

        dbAdapter.close();

        fillData();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        StockQuote quote = getQuoteFromRow(v);
        if (quote != null)
        {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.SYMBOL_BUNDLE_KEY, quote.mSymbol);
            Intent intent = new Intent(getActivity(), WatchDetailsActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constants.WATCH_DETAIL_ACTIVITY);
        }
    }

    private void fillData() {
        DbAdapter dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();
        Cursor cursor = dbAdapter.fetchAllQuoteRecordsByStatus(QuoteStatus.WATCH);
        dbAdapter.close();

        String[] fields = new String[] { DbAdapter.Q_SYMBOL,
                DbAdapter.Q_PPS,
                DbAdapter.Q_CHANGE_VS_CLOSE,
                DbAdapter.Q_STRIKE};

        int[] ids = new int[] {R.id.symbol_field_id,
                R.id.pps_field_id,
                R.id.chng_close_field_id,
                R.id.last_q_field_id};

        QuoteCursorAdapter qcs = new QuoteCursorAdapter(getActivity(),
                cursor,
                fields,
                ids);

        this.setListAdapter(qcs);
    }

    @Override
    public void addAQuote() {
        Intent intent = new Intent(getActivity(),WatchAddActivity.class);
        startActivityForResult(intent, Constants.WATCH_ADD_ACTIVITY);
    }

    @Override
    public void redisplayList() {
        fillData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.WATCH_DETAIL_ACTIVITY) {
            fillData();
            if (mSelectedPosition > 0) {
                getListView().setSelection(mSelectedPosition);
                mSelectedPosition = -1;
            }
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.WATCH_ADD_ACTIVITY) {
                grabNewQuoteInfoAndStore(data);
                ((ListFragmentListener) Objects.requireNonNull(getActivity())).quoteAddedOrMoved();
            }
            if (requestCode == Constants.OWNED_ADD_ACTIVITY) {
                ((ListFragmentListener) Objects.requireNonNull(getActivity())).moveToOwned(data);
            }
        }
    }

    private void grabNewQuoteInfoAndStore(Intent data){
        Bundle bundle = data.getExtras();
        String symbol = Objects.requireNonNull(bundle).getString(Constants.WATCH_ADD_SYMBOL_BUNDLE_KEY);
        float strikePrice = bundle.getFloat(Constants.WATCH_ADD_STRIKE_PRICE_BUNDLE_KEY);

        DbAdapter dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();
        DataModel model = new DataModel(dbAdapter);
        model.addOrOverwriteWatch(symbol, strikePrice);
        dbAdapter.close();
    }

}

