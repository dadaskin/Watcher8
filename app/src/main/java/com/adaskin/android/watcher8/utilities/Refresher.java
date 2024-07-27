package com.adaskin.android.watcher8.utilities;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;

import com.adaskin.android.watcher8.MainActivity;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.models.StockQuote;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Refresher {

    public interface RefreshedObject {
        Button getRefreshButton();
        void fillData();
    }

    private final Context mContext;
    private final RequestQueue mRequestQueue;
    private final List<String> mInvalidSymbols = new ArrayList<>();
    private int mUnansweredRequests;
    private final RefreshedObject mRefreshedObject;
    private final AnimationDrawable mDrawable;

    public Refresher(Context context, RefreshedObject refreshedObject) {
        mContext = context;
        mRefreshedObject = refreshedObject;

        mRequestQueue = Volley.newRequestQueue(context);
        Button button = mRefreshedObject.getRefreshButton();
        mDrawable = (AnimationDrawable)button.getCompoundDrawables()[0];
    }

    public void refreshAll() {
        Log.d("foo", "Starting Refresher.refreshAll().");

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        final List<StockQuote> quoteList = dbAdapter.fetchStockQuoteList();
        dbAdapter.close();

        mDrawable.start();

        mInvalidSymbols.clear();
        mUnansweredRequests = quoteList.size();
        mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<StringRequest>() {
            @Override
            public void onRequestFinished(Request<StringRequest> request) {
                if (mUnansweredRequests <= 0) {
                   handleInvalidSymbols(quoteList);
                   updateDb(quoteList);
                   updateFragments();
                   mRefreshedObject.fillData();
                   mDrawable.stop();
                }
            }
        });

        for (StockQuote quote : quoteList) {
            doSingleWebRequest(quote);
//            try {
//                sleep(1000);
//            } catch (InterruptedException e) {
//
//            }
        }
    }



    public void refreshSingle(final StockQuote quote) {
        String logMsg = "Start Refresher.refreshSingle() for " + quote.mSymbol;
        Log.d("foo", logMsg);

        mDrawable.start();  // Spin the double arrows while sending requests

        mInvalidSymbols.clear();
        mUnansweredRequests = 1;
        mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<StringRequest>() {
            @Override
            public void onRequestFinished(Request<StringRequest> request) {
                if (mUnansweredRequests == 0) {
                    handleInvalidSymbols(quote);
                    updateDb(quote);
                    mRefreshedObject.fillData();
                    mDrawable.stop();  // Stop spinning the dobule arrows.
                }
            }
        });

        doSingleWebRequest(quote);
    }


    private void doSingleWebRequest(final StockQuote quote){
        String url = "https://finance.yahoo.com/quote/" + quote.mSymbol;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                handleWebResponse(quote, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleWebError(quote.mSymbol, error);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<>();
                params.put("User-Agent", "Mozilla/5.0");
                return params;
            }
        }
        ;

        Log.d("foo","Request: " + url);
        mRequestQueue.add(stringRequest);
    }

    private void handleWebResponse(StockQuote quote, String response) {
        Parsers parser = Parsers.getInstance();
        boolean isValidSymbol = parser.parseYAHOOResponse(mContext, quote, response);
        mUnansweredRequests--;
        String s = "o ";
        if (!isValidSymbol) {
            mInvalidSymbols.add(quote.mSymbol);
            s = "X ";
        }
        String msg = quote.mSymbol + " Response received. " + s + mUnansweredRequests + " left. ";

        msg = msg + (quote.mPPS + "   " + quote.mPrevClose + "   " + quote.mDivPerShare);
        Log.d("foo", msg);
    }

    private void handleWebError(String symbol, VolleyError error) {
        Log.d("foo", "Volley Response.ErrorListener for " + symbol + ": "+ error.getMessage());
        mDrawable.stop();
    }

    private void updateDb(List<StockQuote> quoteList) {
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        for (StockQuote quote: quoteList) {
            dbAdapter.changeQuoteRecord(dbAdapter.fetchQuoteIdFromSymbol(quote.mSymbol), quote);
        }
        dbAdapter.close();
    }

    private void updateDb(StockQuote quote) {
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.changeQuoteRecord(dbAdapter.fetchQuoteIdFromSymbol(quote.mSymbol), quote);
        dbAdapter.close();
    }

    private void updateFragments(){
        ((MainActivity)mContext).quoteAddedOrMoved();
    }

    // Handle Invalid Symbols
    private String createInvalidSymbolMessage(List<String> symbolList){
        StringBuilder msg = new StringBuilder();
        String lastSymbol = symbolList.get(symbolList.size()-1);
        if (symbolList.size() == 1)	 {
            msg.append("The symbol: ");
            msg.append(symbolList.get(0));
            msg.append(".");
          //  msg.append(" is invalid.\nDeleting.");
        } else {
            msg.append("The symbols: ");
            for (String s : symbolList) {
                msg.append(s);
                if (!s.equals(lastSymbol)) {
                    msg.append(", ");
                }
            }
            msg.append(".");
         //   msg.append(" are invalid.\n Deleting.");
        }

        return msg.toString();
    }

    private void removeInvalidSymbolsFromDb(List<String> symbolList, List<StockQuote> quoteList) {
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        for(String symbol : symbolList) {
            StockQuote badQuote = dbAdapter.fetchQuoteObjectFromSymbol(symbol);
            quoteList.remove(badQuote);
            dbAdapter.removeQuoteRecord(symbol);
        }

        dbAdapter.close();
    }

    private void handleInvalidSymbols(StockQuote quote) {
        int length = mInvalidSymbols.size();
        if (length == 0)
            return;
        String msg = createInvalidSymbolMessage(mInvalidSymbols);
        mInvalidSymbols.clear();

        // Build and display dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(length + " Symbols had no data:")
                .setMessage(msg)
                .setPositiveButton("OK",null)
                .setCancelable(false)
                .show();
    }


    private void handleInvalidSymbols(List<StockQuote> quoteList) {
        int length = mInvalidSymbols.size();

        String msg1 = length + " Invalid out of " + quoteList.size();
        Log.d("foo", msg1);

        if (length == 0)
            return;

        String msg = createInvalidSymbolMessage(mInvalidSymbols);
        //removeInvalidSymbolsFromDb(mInvalidSymbols, quoteList);
        mInvalidSymbols.clear();

        // Build and display dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(length + " Symbols had no data:")
                .setMessage(msg)
                .setPositiveButton("OK",null)
                .setCancelable(false)
                .show();
    }

}
