package com.adaskin.android.watcher8.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.utilities.Constants;
import com.adaskin.android.watcher8.utilities.QuoteStatus;

import java.util.ArrayList;
import java.util.List;

public class DataModel implements Parcelable {
    private List<StockQuote> mMasterList;
    private DbAdapter mDbAdapter;

    // Constructors
    public DataModel(DbAdapter dbAdapter) {
        mMasterList = new ArrayList<>();
        mDbAdapter = dbAdapter;
        if (!mDbAdapter.lastUpdateRecordExists()) {
            mDbAdapter.createLastUpdateRecord("Aug 31, 2012", "12:22 PM PDT");
        }
        mMasterList = mDbAdapter.fetchStockQuoteList();
    }

    // Constructor for Parcelable
    private DataModel(Parcel in) {
        readFromParcel(in);
    }

    public void addOwned(String symbol, float gainTargetPct, BuyBlock firstBuyBlock) {
        StockQuote q = new StockQuote(symbol, 0.0f, 0.0f, gainTargetPct);
        q.mBuyBlockList.add(firstBuyBlock);
        addNewStockQuote(q);
    }

    public void addOrOverwriteWatch(String symbol, float strikePrice) {
        StockQuote newQuote = new StockQuote(symbol, 0.0f, strikePrice, 0.0f, 0.0f, 0.0f);

        // Check to see if this symbol is already in the master list
        boolean foundMatch = false;
        for (StockQuote quote: mMasterList) {
            if (quote.mSymbol.equals(symbol))
            {
                foundMatch = true;
                break;
            }
        }

        if (!foundMatch) {
            mDbAdapter.createQuoteRecord(newQuote);
            mMasterList.add(newQuote);
        }
    }

    private void addNewStockQuote(StockQuote newQuote) {
        // Check to see if this symbol is already in the master list
        boolean foundMatch = false;
        float existingStrikePrice = 0f;
        StockQuote quoteToRemove = null;
        for (StockQuote quote: mMasterList) {
            if (quote.mSymbol.equals(newQuote.mSymbol))
            {
                quoteToRemove = quote;
                existingStrikePrice = quote.mStrikePrice;
                foundMatch = true;
                break;
            }
        }

        if (foundMatch) {
            if (newQuote.mStrikePrice < Constants.MINIMUM_SIGNIFICANT_VALUE) {
                newQuote.mStrikePrice = existingStrikePrice;
            }
            removeEntryFromDB(quoteToRemove.mSymbol);
            mMasterList.remove(quoteToRemove);
        }

        mDbAdapter.createQuoteRecord(newQuote);
        mMasterList.add(newQuote);
    }

    public void removeEntryFromDB(String symbol){
        StockQuote quote = findStockQuoteBySymbol(symbol);
        if (quote != null)
            mDbAdapter.removeQuoteRecord(symbol);
    }

    public void changeStatusFromOwnedToWatch(String symbol) {
        StockQuote quote = findStockQuoteBySymbol(symbol);
        long id = mDbAdapter.fetchQuoteIdFromSymbol(symbol);

        quote.mStatus = QuoteStatus.WATCH;
        quote.mOverallAccountColor = Constants.ACCOUNT_UNKNOWN;
        quote.mBuyBlockList = new ArrayList<>();
        mDbAdapter.changeQuoteRecord(id, quote);
    }

    public StockQuote findStockQuoteBySymbol(String symbol) {
        StockQuote theQuote = null;
        for (StockQuote quote: mMasterList){
            if (quote.mSymbol.equals(symbol)) {
                theQuote = quote;
                break;
            }
        }

        return theQuote;
    }

    // Parcelable methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mMasterList);
    }

    private void readFromParcel(Parcel in) {
        mMasterList = new ArrayList<>();
        in.readTypedList(mMasterList, StockQuote.CREATOR);
    }

    public static final Parcelable.Creator<DataModel> CREATOR = new Parcelable.Creator<DataModel>() {
        @Override
        public DataModel createFromParcel(Parcel in) {
            return new DataModel(in);
        }

        public DataModel[] newArray(int size) {
            return new DataModel[size];
        }
    };
}
