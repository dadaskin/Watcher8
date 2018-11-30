package com.adaskin.android.watcher8;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.multidex.MultiDex;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.adaskin.android.watcher8.adapters.NonSwipableViewPager;
import com.adaskin.android.watcher8.adapters.SectionsPagerAdapter;
import com.adaskin.android.watcher8.fragments.ListFragmentBase;

public class MainActivity extends AppCompatActivity implements ListFragmentBase.ListFragmentListener {

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
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customTitleView = inflater.inflate(R.layout.custom_main_titlebar, null);
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




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
