package com.adaskin.android.watcher8.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.adaskin.android.watcher8.models.BuyBlock;
import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.Constants;
import com.adaskin.android.watcher8.utilities.QuoteStatus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {

    // String constants
    private static final String ST_OWNED = "OWNED";
    private static final String ST_WATCH = "WATCH";
    private static final String ST_SOLD = "SOLD";

    // Database constants
    public static final String DATABASE_NAME = "watcher8";
    public static final int DATABASE_VERSION = 1;
    public static final String DB_VIEW_NAME = "db_view";
    public static final String DB_TRIGGER_NAME = "db_trigger";


    public static final String LAST_UPDATE_TABLE = "last_update_table";
    public static final String U_ROW_ID = "last_update_row_id";             // integer primary key
    public static final String U_DATE = "last_update_date";                 // text
    public static final String U_TIME = "last_update_time";                 // text

    public static final String QUOTE_TABLE = "quote_table";
    public static final String Q_ROW_ID = "quote_row_id";                   // integer primary key
    public static final String Q_ACCOUNT_COLOR="quote_account_color";       // integer
    public static final String Q_OPINION = "quote_analysts_opinion";        // real
    public static final String Q_DIV_SHARE = "quote_div_per_share";         // real
    public static final String Q_FULL_NAME = "quote_full_name";             // text
    public static final String Q_CHANGE_VS_CLOSE = "quote_change_vs_close"; // real
    public static final String Q_GAIN_TARGET = "quote_gain_target";         // real
    public static final String Q_PPS = "quote_pps";                         // real
    public static final String Q_STATUS = "quote_status";                   // text
    public static final String Q_STRIKE = "quote_strike";                   // real
    public static final String Q_CHANGE_VS_BUY = "quote_change_vs_buy";     // real
    public static final String Q_SYMBOL = "quote_symbol";                   // text
    public static final String Q_YR_MAX = "quote_yr_max";                   // real
    public static final String Q_YR_MIN = "quote_yr_min";                   // real

    public static final String BUY_BLOCK_TABLE= "buy_block_table";
    public static final String B_ROW_ID = "buy_row_id";                     // integer primary key
    public static final String B_COMM_SHARE = "buy_comm_share";             // real
    public static final String B_DATE = "buy_date";                         // text
    public static final String B_PPS = "buy_pps";                           // real
    public static final String B_EFF_YIELD = "buy_eff_yield";               // real
    public static final String B_NUM_SHARES = "buy_num_shares";             // real
    public static final String B_CHANGE_VS_BUY = "buy_change_vs_buy";       // real
    public static final String B_PARENT = "buy_parent";                     // integer foreign key
    public static final String B_ACCOUNT = "buy_account";                   // integer foreign key

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DbHelper mDbHelper;

    public DbAdapter(Context context) {
        mContext = context;
    }

    public void open() throws SQLException {
        mDbHelper = DbHelper.getInstance(mContext);
        mDb = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }



    private ContentValues createLastUpdateCV(String dateString, String timeString) {
        ContentValues cv = new ContentValues();
        cv.put(U_DATE, dateString);
        cv.put(U_TIME, timeString);

        return cv;
    }

    private ContentValues createQuoteCV(StockQuote quote) {
        ContentValues cv = new ContentValues();
        cv.put(Q_CHANGE_VS_CLOSE, quote.mPctChangeSinceLastClose);
        cv.put(Q_STRIKE, quote.mStrikePrice);
        cv.put(Q_CHANGE_VS_BUY, quote.mPctChangeSinceBuy);
        cv.put(Q_DIV_SHARE, quote.mDivPerShare);
        cv.put(Q_OPINION, quote.mAnalystsOpinion);
        cv.put(Q_FULL_NAME, quote.mFullName);
        cv.put(Q_GAIN_TARGET, quote.mPctGainTarget);
        cv.put(Q_PPS, quote.mPPS);
        cv.put(Q_SYMBOL, quote.mSymbol);
        cv.put(Q_YR_MAX, quote.mYrMax);
        cv.put(Q_YR_MIN, quote.mYrMin);
        cv.put(Q_ACCOUNT_COLOR, quote.mOverallAccountColor);
        cv.put(Q_STATUS, statusAsString(quote.mStatus));

        return cv;
    }

    private ContentValues createBuyBlockCV(BuyBlock block, long parentId) {
        ContentValues cv = new ContentValues();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        String dateString = sdf.format(block.mBuyDate);
        cv.put(B_DATE, dateString);
        cv.put(B_CHANGE_VS_BUY, block.mPctChangeSinceBuy);
        cv.put(B_EFF_YIELD, block.mEffDivYield);
        cv.put(B_NUM_SHARES, block.mNumShares);
        cv.put(B_PPS, block.mBuyCostBasis);
        cv.put(B_PARENT, parentId);
        cv.put(B_ACCOUNT, block.mAccountColor);
        return cv;
    }

    // Create methods
    public void createLastUpdateRecord(String dateString, String timeString) {
        // First remove all existing records from LAST_UPDATE_TABLE
        mDb.execSQL("delete from " + DbAdapter.LAST_UPDATE_TABLE);
        mDb.execSQL("vacuum");

        // Then insert the new cv.
        ContentValues cv = createLastUpdateCV(dateString, timeString);
        mDb.insert(LAST_UPDATE_TABLE, "", cv);
    }

    public void createQuoteRecord(StockQuote quote) {
        quote.determineOverallAccountColor();
        ContentValues cv = createQuoteCV(quote);
        long newRow = mDb.insert(QUOTE_TABLE, "", cv);
        if (quote.mBuyBlockList != null){
            for (BuyBlock bb : quote.mBuyBlockList) {
                createBuyBlockRecord(bb, newRow);
            }
        }
    }

    private void  createBuyBlockRecord(BuyBlock block, long parentId) {
        ContentValues cv = createBuyBlockCV(block, parentId);
        mDb.insert(BUY_BLOCK_TABLE, "", cv);

    }

    // Read methods
    public boolean lastUpdateRecordExists() {
        String sql = "select " + U_ROW_ID + " as _id, * from " + LAST_UPDATE_TABLE;
        Cursor cursor = mDb.rawQuery(sql, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // User must manage cursor
    public Cursor fetchLastUpdateRecord() {
        String sql = "select " + U_ROW_ID + " as _id, * from " + LAST_UPDATE_TABLE;
        Cursor cursor = mDb.rawQuery(sql, null);
        cursor.moveToFirst();
        return cursor;
    }

    // User must manage cursor
    public Cursor fetchAllQuoteRecordsByStatus(QuoteStatus status) {
        String statusAsString = statusAsString(status);
        String sql = "select " + Q_ROW_ID + " as _id, * from " + QUOTE_TABLE +
                " where " + Q_STATUS + "=? order by " + Q_SYMBOL;
        Cursor cursor = mDb.rawQuery(sql, new String[] {statusAsString});
        cursor.moveToFirst();
        return cursor;
    }

    // User must manage cursor
    public Cursor fetchBuyBlockRecordsForThisSymbol(String symbol) {

        String sql = "select * from " + DB_VIEW_NAME +
                " where " + Q_SYMBOL + "=? order by " + B_DATE;
        Cursor cursor = mDb.rawQuery(sql, new String[] {symbol});
        cursor.moveToFirst();
        return cursor;
    }

    public long fetchQuoteIdFromSymbol(String symbol) {
        String[] params = new String[] { symbol };
        String sql = "select " + Q_ROW_ID + " from " + QUOTE_TABLE + " where " + Q_SYMBOL + "=?";
        Cursor cursor = mDb.rawQuery(sql, params);
        cursor.moveToFirst();
        if (cursor.isAfterLast()) { return -1; }
        int idx = cursor.getColumnIndex(Q_ROW_ID);
        if (cursor.isNull(idx)) { return -1; }
        long id = cursor.getLong(idx);
        cursor.close();
        return id;
    }

    // User must manage cursor
    private Cursor fetchQuoteRecordFromId(long id) {
        String[] params = new String[] { String.valueOf(id) };
        String sql = "select * from " + QUOTE_TABLE + " where " + Q_ROW_ID + "=?";
        Cursor cursor = mDb.rawQuery(sql, params);
        cursor.moveToFirst();
        return cursor;
    }

    public StockQuote fetchQuoteObjectFromId(long id) {
        // Get elements of the Quote object
        Cursor qCursor = fetchQuoteRecordFromId(id);
        return makeQuoteFromCursor(qCursor);
    }

    public StockQuote fetchQuoteObjectFromSymbol(String symbol) {
        return fetchQuoteObjectFromId(fetchQuoteIdFromSymbol(symbol));
    }

    public List<StockQuote> fetchStockQuoteList() {

        List<StockQuote> quoteList = new ArrayList<>();
        Cursor cursor = this.fetchAllQuoteRecords();

        while (!cursor.isAfterLast()) {
            StockQuote quote = makeQuoteFromCursor(cursor);
            quoteList.add(quote);
            cursor.moveToNext();
        }

        return quoteList;
    }

    private BuyBlock makeBuyBlockFromCursor(Cursor bCursor) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);

        int account = bCursor.getInt(bCursor.getColumnIndex(B_ACCOUNT));
        float changeVsBuy = bCursor.getFloat(bCursor.getColumnIndex(B_CHANGE_VS_BUY));
        String dateString = bCursor.getString(bCursor.getColumnIndex(B_DATE));
        Date buyDate = new Date();
        try {
            buyDate = sdf.parse(dateString);
        } catch(ParseException e) {
            e.printStackTrace();
        }
        float effYield = bCursor.getFloat(bCursor.getColumnIndex(B_EFF_YIELD));
        float numShares = bCursor.getFloat(bCursor.getColumnIndex(B_NUM_SHARES));
        float bPPS = bCursor.getFloat(bCursor.getColumnIndex(B_PPS));

        BuyBlock bb = new BuyBlock(buyDate, numShares, bPPS, 0.0f, account);
        bb.mEffDivYield = effYield;
        bb.mPctChangeSinceBuy = changeVsBuy;
        return bb;
    }

    private StockQuote makeQuoteFromCursor(Cursor qCursor) {
        String symbol = qCursor.getString(qCursor.getColumnIndex(Q_SYMBOL));
        float changeVsClose = qCursor.getFloat(qCursor.getColumnIndex(Q_CHANGE_VS_CLOSE));
        float divPS = qCursor.getFloat(qCursor.getColumnIndex(Q_DIV_SHARE));
        String fullName = qCursor.getString(qCursor.getColumnIndex(Q_FULL_NAME));
        float gain = qCursor.getFloat(qCursor.getColumnIndex(Q_GAIN_TARGET));
        float opinion = qCursor.getFloat(qCursor.getColumnIndex(Q_OPINION));
        float pps = qCursor.getFloat(qCursor.getColumnIndex(Q_PPS));
        String statusStr = qCursor.getString(qCursor.getColumnIndex(Q_STATUS));
        QuoteStatus status = statusFromString(statusStr);
        float strike = qCursor.getFloat(qCursor.getColumnIndex(Q_STRIKE));
        float changeVsBuy = qCursor.getFloat(qCursor.getColumnIndex(Q_CHANGE_VS_BUY));
        float yrMax = qCursor.getFloat(qCursor.getColumnIndex(Q_YR_MAX));
        float yrMin = qCursor.getFloat(qCursor.getColumnIndex(Q_YR_MIN));
        int accountColor = qCursor.getInt(qCursor.getColumnIndex(Q_ACCOUNT_COLOR));

        // Get elements of BuyBlock objects associated with this symbol
        Cursor bCursor = fetchBuyBlockRecordsForThisSymbol(symbol);
        ArrayList<BuyBlock> bbList = new ArrayList<>();
        while (!bCursor.isAfterLast()) {
            BuyBlock bb = makeBuyBlockFromCursor(bCursor);
            bbList.add(bb);
            bCursor.moveToNext();
        }

        bCursor.close();

        StockQuote quote = new StockQuote(symbol, pps, strike, divPS, yrMax, yrMin);
        quote.mAnalystsOpinion = opinion;
        quote.mBuyBlockList = bbList;
        quote.mFullName = fullName;
        quote.mPctChangeSinceBuy = changeVsBuy;
        quote.mPctChangeSinceLastClose = changeVsClose;
        quote.mPctGainTarget = gain;
        quote.mStatus = status;
        quote.mOverallAccountColor = accountColor;

        return quote;
    }

    // User must manage cursor
    private Cursor fetchAllQuoteRecords() {
        String sql = "select " + Q_ROW_ID + " as _id, * from " + QUOTE_TABLE;
        Cursor cursor = mDb.rawQuery(sql, new String[] {});
        cursor.moveToFirst();
        return cursor;
    }


    // Update methods
    public void changeQuoteRecord(long id, StockQuote newQuote) {
        ContentValues quoteCV = createQuoteCV(newQuote);
        for (BuyBlock bb : newQuote.mBuyBlockList)
        {
            ContentValues bbCV = this.createBuyBlockCV(bb, id);
            String dateString = bbCV.getAsString(B_DATE);
            mDb.update(BUY_BLOCK_TABLE,
                    bbCV,
                    B_PARENT + "=? and " + B_DATE + "=?",
                    new String[] { String.valueOf(id), dateString });
        }

        mDb.update(QUOTE_TABLE,
                quoteCV,
                Q_ROW_ID + "=?",
                new String[] {String.valueOf(id)});
    }

    // Delete methods
    public void removeLastUpdateRecord() {
        Cursor cursor = fetchLastUpdateRecord();
        long id = cursor.getLong(cursor.getColumnIndex(U_ROW_ID));
        mDb.delete(LAST_UPDATE_TABLE, U_ROW_ID + "=?", new String[] {String.valueOf(id)});
        cursor.close();
    }

    public void removeQuoteRecord(String symbol) {
        long id = fetchQuoteIdFromSymbol(symbol);
        mDb.delete(BUY_BLOCK_TABLE, B_PARENT  + "=?", new String[] {String.valueOf(id)});
        mDb.delete(QUOTE_TABLE, Q_ROW_ID + "=?", new String[] {String.valueOf(id)});
    }

    public void removeBuyBlockRecord(String symbol, String dateString) {
        // Get QuoteId corresponding to symbol
        long parentId = this.fetchQuoteIdFromSymbol(symbol);

        // Delete the BB from the BB table that corresponds to
        //       B_DATE == dateString && B_PARENT == QuoteID.
        String whereClause = B_DATE + "=? and " +  B_PARENT + "=?";
        String[] whereArgs = new String[] { dateString, String.valueOf(parentId) };
        mDb.delete(BUY_BLOCK_TABLE, whereClause, whereArgs);
    }




    // Converter methods

    // Converts DB value of status field into Status enum
    public QuoteStatus getStatus(Cursor cursor) {
        int idx = cursor.getColumnIndex(Q_STATUS);
        if (cursor.isClosed()) { return QuoteStatus.SOLD; }
        String statusString = cursor.getString(idx);
        return statusFromString(statusString);
    }

    public float getBestChangeSinceBuy(String symbol) {
        Cursor blockCursor = fetchBuyBlockRecordsForThisSymbol(symbol);
        blockCursor.moveToFirst();
        float change = Float.NEGATIVE_INFINITY;
        int idx = blockCursor.getColumnIndex(B_CHANGE_VS_BUY);
        while (!blockCursor.isAfterLast()) {
            float thisValue = blockCursor.getFloat(idx);
            if (thisValue > change) {
                change= thisValue;
            }
            blockCursor.moveToNext();
        }
        blockCursor.close();
        putBestChangeSinceBuyInQuote(symbol, change);

        return change;
    }

    private void putBestChangeSinceBuyInQuote(String symbol, float changeSinceBuy) {
        long id = fetchQuoteIdFromSymbol(symbol);
        Cursor cursor = fetchQuoteRecordFromId(id);

        ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, cv);
        cv.put(Q_CHANGE_VS_BUY, changeSinceBuy);
        mDb.update(QUOTE_TABLE,
                cv,
                Q_ROW_ID + "=?",
                new String[] {String.valueOf(id)});
    }

    private String statusAsString(QuoteStatus status) {
        switch (status) {
            case OWNED:
                return ST_OWNED;
            case WATCH:
                return ST_WATCH;
            default:
                return ST_SOLD;
        }
    }

    private QuoteStatus statusFromString(String statusString) {
        QuoteStatus status;
        if (statusString.contentEquals(ST_OWNED)) { status = QuoteStatus.OWNED;}
        else if (statusString.contentEquals(ST_WATCH)) {status = QuoteStatus.WATCH; }
        else { status = QuoteStatus.SOLD; }
        return status;
    }

    public void  replaceQuoteTable(List<StockQuote> newList) {
        removeAllQuotesAndBlocks();
        for (StockQuote quote : newList) {
            createQuoteRecord(quote);
        }
    }

    private void removeAllQuotesAndBlocks() {
        mDb.execSQL("DELETE FROM " + QUOTE_TABLE);
        mDb.execSQL("DELETE FROM " + BUY_BLOCK_TABLE);
    }
}
