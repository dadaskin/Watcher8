package com.adaskin.android.watcher8;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.adaskin.android.watcher8.adapters.NonSwipableViewPager;
import com.adaskin.android.watcher8.adapters.SectionsPagerAdapter;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.fragments.FooterFragment;
import com.adaskin.android.watcher8.fragments.ListFragmentBase;
import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.Constants;
import com.adaskin.android.watcher8.utilities.Parsers;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ListFragmentBase.ListFragmentListener, FooterFragment.FooterListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private RequestQueue mRequestQueue;
    private List<String> mInvalidSymbols = new ArrayList<>();
    private int mUnansweredRequests;
    private FooterFragment mFooterFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the "hamburger" in upper right corner
        Toolbar toolbar = findViewById(R.id.hamburger);
        setSupportActionBar(toolbar);

        // Display App Icon on Action Bar
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customTitleView = Objects.requireNonNull(inflater).inflate(R.layout.custom_main_titlebar, null);
        actionBar.setCustomView(customTitleView);

        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (NonSwipableViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout mTabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == mTabLayout.getTabAt(0)) {
                    mViewPager.setCurrentItem(0);
                } else if (tab == mTabLayout.getTabAt(1)){
                    mViewPager.setCurrentItem(1);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // JSON Import/Export methods
    static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 11;  // Arbitrary value
    static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 12;   // Different arbitrary value

    public void ExportJsonCommand(MenuItem item) {
        int isOk = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If user has not previous granted permission, pop up the dialog asking for it.
        //      Once the user has responded to the dialog, execution resumes at onRequestPermissionsResult()
        //           Do the write there.
        // Else just do the write.
        if (isOk != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            doPublicWrite();
        }
    }

    public void ImportJsonCommand(MenuItem item) {
        int isOk = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        // If user has not previous granted permission, pop up the dialog asking for it.
        //      Once the user has responded to the dialog, execution resumes at onRequestPermissionsResult()
        //           Do the write there.
        // Else just do the write.
        if (isOk != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            doPublicRead();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if ((grantResults.length > 0) && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                doPublicWrite();
            } else {
                Toast.makeText(this, "Writing a public file is not permitted.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if ((grantResults.length > 0) && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                doPublicRead();
            } else {
                Toast.makeText(this, "Reading a public  file is not permitted.", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private String createBackupFilename() {
        return "Watcher8_timestamp.json";
    }

    private String findBestBackupFilename() {
        return "Watcher8_timestamp.json";
    }

    private void doPublicWrite() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES), createBackupFilename());
        try {
            DbAdapter dbAdapter = new DbAdapter(this);
            dbAdapter.open();
            List<StockQuote> quoteList = dbAdapter.fetchStockQuoteList();
            dbAdapter.close();

            FileOutputStream fos = new FileOutputStream(file);
            Writer osWriter = new OutputStreamWriter(fos);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(quoteList, osWriter);
            osWriter.close();
            Toast.makeText(this, "Exported to JSON", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doPublicRead() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES), findBestBackupFilename());
        try {
            FileInputStream fis = new FileInputStream(file);
            Reader isReader = new InputStreamReader(fis);
            Type quoteListType = new TypeToken<List<StockQuote>>(){}.getType();
            List<StockQuote> quoteList = Collections.synchronizedList((List<StockQuote>)gson.fromJson(isReader, quoteListType));
            isReader.close();

            String msg = "Imported " + quoteList.size() + " items from JSON.";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            DbAdapter dbAdapter = new DbAdapter(this);
            dbAdapter.open();
            dbAdapter.replaceQuoteTable(quoteList);
            dbAdapter.close();

            // Redisplay everything
            updateFragments();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    // Old Import/Export methods
//    public void ExportCommand(MenuItem item) {
//        DbAdapter dbAdapter = new DbAdapter(this);
//        dbAdapter.open();
//        boolean isSuccessful = dbAdapter.exportDB();
//        dbAdapter.close();
//        sendEmail();
//
//        CharSequence msg = "Database exported";
//        if (!isSuccessful)
//            msg = "Export error.";
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//    }
//
//    public void ImportCommand(MenuItem item) {
//        DbAdapter dbAdapter = new DbAdapter(this);
//        dbAdapter.open();
//        boolean isSuccessful = dbAdapter.importDB();
//        dbAdapter.close();
//
//        CharSequence msg = "Database imported";
//        if (!isSuccessful)
//            msg = "Import error.";
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//
//        if (isSuccessful)
//            quoteAddedOrMoved();
//    }
//
//    public void EmailTest(MenuItem item) {
////        Toast.makeText(this, "TBD: Email Test", Toast.LENGTH_LONG).show();
//        sendEmail();
//    }
//
//    private void sendEmail() {
//        String[] emailParameters = new String[] {"Foo", "Bar"};
////        String attachmentFileName = Environment.getExternalStorageDirectory() + "/stockwatcher4_backup.db";
////        String message = Build.MODEL + "  "  + Build.SERIAL;
////        String[] emailParameters = new String[] {attachmentFileName, message};
//        new SendEmailTask().execute(emailParameters);
//    }
//
//    private class SendEmailTask extends AsyncTask<String,Void,String> {
//        @Override
//        protected String doInBackground(String... inputStrings) {
//            String senderUsername = "executive@adaskin.com";
//            String senderPassword = "~.^voA^ZVsMD";
//            Mail m = new Mail(senderUsername, senderPassword);
//            String[] recipientArray = {"dave@adaskin.com"};
//            m.sendTo(recipientArray);
//            m.sendFrom(senderUsername);
//            m.setSubject("Stockwatcher4 DB update from: " + inputStrings[1]);
//            m.setBody("<See attachment>");
//
//            String result = "Email: ";
//            try {
//                m.addAttachment(inputStrings[0]);
//                if (m.send()) {
//                    result += "sent";
//                } else {
//                    result += "not sent";
//                }
//            } catch (Exception e) {
//                result +=  e.toString();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
//        }
//    }
//

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    // ListFragmentListener implementation
    @Override
    public void quoteAddedOrMoved() {
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            ListFragmentBase fragment = mSectionsPagerAdapter.getItem(i);
            fragment.redisplayList();
        }
    }

    @Override
    public void moveToOwned(Intent data) {
        mSectionsPagerAdapter.moveToOwned(data);
        mViewPager.setCurrentItem(0);
    }


    // FooterListener implementation
    @Override
    public void addButtonClicked() {
        int currentItem = mViewPager.getCurrentItem();
        ListFragmentBase fragment = mSectionsPagerAdapter.getItem(currentItem);
        fragment.addAQuote();
    }

    @Override
    public void refreshButtonClicked(final FooterFragment footerFragment, View buttonView) {
        mFooterFragment = footerFragment;

        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();

        final List<StockQuote> quoteList = dbAdapter.fetchStockQuoteList();
        dbAdapter.close();

        Log.d("foo", "Starting web requests.");
        mInvalidSymbols.clear();
        mRequestQueue = Volley.newRequestQueue(this);
        mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<StringRequest>() {
            @Override
            public void onRequestFinished(Request<StringRequest> request) {
                Log.d("foo", "RequestFinishedListener.");

                if (mUnansweredRequests == 0) {
                    Log.d("foo", " All requests have been responded to.");
                    handleInvalidSymbols(mInvalidSymbols, quoteList);
                    updateDb(quoteList);
                    updateFragments();
                    updateDateStrings(footerFragment);
                    footerFragment.endButtonAnimation();
                }
            }});

        mUnansweredRequests = quoteList.size();
        for (StockQuote q : quoteList) {
            doSingleWebRequest(q);
        }
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
        });
        Log.d("foo","Request: " + quote.mSymbol);
        mRequestQueue.add(stringRequest);
    }

    private void handleWebResponse(StockQuote quote, String response) {
        boolean isValidSymbol = Parsers.parseYAHOOResponse(quote, response);
        mUnansweredRequests--;
        if (!isValidSymbol)
            mInvalidSymbols.add(quote.mSymbol);

        String msg = quote.mSymbol + " Response received.  " + mUnansweredRequests + " remaining.";
        Log.d("foo", msg);
    }

    private void handleWebError(String symbol, VolleyError error) {
        Log.d("foo", "Volley Response.ErrorListener for " + symbol + ": "+ error.getMessage());
        mFooterFragment.endButtonAnimation();
    }

    private void handleInvalidSymbols(List<String> symbolList, List<StockQuote> quoteList) {
        int length = symbolList.size();
        if (length == 0)
            return;

        // Set up message for dialog
        StringBuilder msg = new StringBuilder();
        String lastSymbol = symbolList.get(length-1);
        if (symbolList.size() == 1)	 {
            msg.append("The symbol: ");
            msg.append(symbolList.get(0));
            msg.append(" is invalid.\nDeleting.");
        } else {
            msg.append("The symbols: ");
            for (String s : symbolList) {
                msg.append(s);
                if (!s.equals(lastSymbol)) {
                    msg.append(",");
                }
            }
            msg.append(" are invalid.\n Deleting.");
        }

        // Remove invalid symbols from DB
        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();

        for(String symbol : symbolList) {
            StockQuote badQuote = dbAdapter.fetchQuoteObjectFromSymbol(symbol);
            quoteList.remove(badQuote);
            dbAdapter.removeQuoteRecord(symbol);
        }

        dbAdapter.close();

        // Buld and display dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Invalid Symbol(s)")
                .setMessage(msg.toString())
                .setPositiveButton("OK",null)
                .setCancelable(false)
                .show();
    }

    private void updateDb(List<StockQuote> quoteList) {
        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        for (StockQuote quote: quoteList) {
            dbAdapter.changeQuoteRecord(dbAdapter.fetchQuoteIdFromSymbol(quote.mSymbol), quote);
        }
        dbAdapter.close();
    }

    private void updateFragments() {
        quoteAddedOrMoved();
    }

    private void updateDateStrings(FooterFragment footerFragment)
    {
        Date now = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.UPDATE_DATE_FORMAT, Locale.US);
        SimpleDateFormat stf = new SimpleDateFormat(Constants.UPDATE_TIME_FORMAT, Locale.US);

        String dateString = sdf.format(now);
        String timeString = stf.format(now);

        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        dbAdapter.removeLastUpdateRecord();
        dbAdapter.createLastUpdateRecord(dateString, timeString);
        dbAdapter.close();

        footerFragment.refreshUpdateDateTime(dateString, timeString);
    }

}
