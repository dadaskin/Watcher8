package com.adaskin.android.watcher8.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.models.DataModel;
import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.Constants;

abstract public class ListFragmentBase extends ListFragment {

    public interface ListFragmentListener {
        void quoteAddedOrMoved();
        void moveToOwned(Intent data);
    }

    int mTopVisiblePosition = -1;
    int mTopPadding = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_list, container, false);
    }

    public abstract void addAQuote();

    public abstract void redisplayList();

    StockQuote getQuoteFromRow(View v)
    {
        ListView lv = getListView();
        View firstChildView = lv.getChildAt(0);
        mTopVisiblePosition = lv.getFirstVisiblePosition();
        mTopPadding = (firstChildView == null)?0:firstChildView.getTop() - lv.getPaddingTop();

        DbAdapter dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();

        DataModel model = new DataModel(dbAdapter);
        dbAdapter.close();
        String symbol = getSymbolFromRow(v);
        return model.findStockQuoteBySymbol(symbol);
    }

    String getSymbolFromRow(View v) {
        LinearLayout ll = (LinearLayout)v;
        TextView tv = (TextView)ll.getChildAt(Constants.SYMBOL_VIEW_IN_QUOTE);
        return tv.getText().toString();
    }
}
