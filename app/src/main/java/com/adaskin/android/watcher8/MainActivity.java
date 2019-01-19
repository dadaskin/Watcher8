package com.adaskin.android.watcher8;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.adaskin.android.watcher8.adapters.NonSwipableViewPager;
import com.adaskin.android.watcher8.adapters.SectionsPagerAdapter;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.fragments.FileChooserFragment;
import com.adaskin.android.watcher8.fragments.FooterFragment;
import com.adaskin.android.watcher8.fragments.ListFragmentBase;
import com.adaskin.android.watcher8.models.StockQuote;
import com.adaskin.android.watcher8.utilities.Parsers;
import com.adaskin.android.watcher8.utilities.Refresher;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ListFragmentBase.ListFragmentListener, FooterFragment.FooterListener, FileChooserFragment.FileOkListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

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
        actionBar.setBackgroundDrawable(new ColorDrawable(getColor(android.R.color.white)));
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customTitleView = Objects.requireNonNull(inflater).inflate(R.layout.custom_main_titlebar, null);
        TextView tv = customTitleView.findViewById(R.id.custom_main_title_bar_text);
        tv.setText(getString(R.string.app_name));
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
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 11;  // Arbitrary value
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 12;   // Different arbitrary value
    private static final int PERMISSIONS_REQUEST_READ_PARSER_UPDATE = 13;      // Yet another arbitrary value

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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
            showAvailableImportFilenames();
        }
    }

    @SuppressWarnings("unused")
    public void UpdateParserCommand(MenuItem item) {
        int isOk = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (isOk != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            updateParserStrings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doPublicWrite();
                } else {
                    Toast.makeText(this, "Writing a public file is not permitted.", Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showAvailableImportFilenames();
                } else {
                    Toast.makeText(this, "Reading a public  file is not permitted.", Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSIONS_REQUEST_READ_PARSER_UPDATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    updateParserStrings();
                } else {
                    Toast.makeText(this, "Reading a public  file is not permitted.", Toast.LENGTH_LONG).show();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private String createBackupFilename() {

        @SuppressWarnings("SpellCheckingInspection") SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        Calendar calendar = Calendar.getInstance();
        String timestamp = sdf.format(calendar.getTime());
        return "Watcher8_" + timestamp;
    }

    private void showAvailableImportFilenames() {
        FragmentManager manager = getSupportFragmentManager();
        FileChooserFragment fileChooserFragment = new FileChooserFragment();
        fileChooserFragment.show(manager, "fooTag");
    }

    @Override
    public void onOkClick(String selectedFilename) {
        doPublicRead(selectedFilename);
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
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
            gson.toJson(quoteList, osWriter);
            osWriter.close();
            Toast.makeText(this, "Exported to JSON", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doPublicRead(String filename) {
        if (filename.equals("")) {
            String msg = "Nothing imported";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES), filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            Reader isReader = new InputStreamReader(fis);
            Type quoteListType = new TypeToken<List<StockQuote>>(){}.getType();
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
            List<StockQuote> quoteList = gson.fromJson(isReader, quoteListType);
            isReader.close();

            if (quoteList == null) {
                String msg = "Imported file is empty.";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                return;
            }
            String msg = "Imported " + quoteList.size() + " items from JSON.";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            DbAdapter dbAdapter = new DbAdapter(this);
            dbAdapter.open();
            dbAdapter.replaceQuoteTable(quoteList);
            dbAdapter.close();

            // Redisplay everything
            quoteAddedOrMoved();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception when importing JSON file.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateParserStrings() {
        Parsers parser = Parsers.getInstance();
        parser.LoadStrings(this);
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
    public void refreshButtonClicked(final FooterFragment footerFragment) {
        Refresher refresher = new Refresher(this, footerFragment);
        refresher.refreshAll();
    }
}
