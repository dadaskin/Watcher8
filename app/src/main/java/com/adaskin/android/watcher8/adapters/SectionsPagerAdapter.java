package com.adaskin.android.watcher8.adapters;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.adaskin.android.watcher8.MainActivity;
import com.adaskin.android.watcher8.fragments.FragmentA;
import com.adaskin.android.watcher8.fragments.FragmentB;
import com.adaskin.android.watcher8.fragments.ListFragmentBase;
import com.adaskin.android.watcher8.fragments.OwnedFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private OwnedFragment mOwnedFragment = null;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) { //ListFragmentBase getItem(int position) {

     //   ListFragmentBase fragment = null;

        switch(position) {
            case 0:
                return new FragmentA();
            case 1:
                return new FragmentB();
            default:
                return new FragmentB();
        }

     //   return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "FragA";
            case 1:
                return "FragB";
        }
        return null;
    }

    public void moveToOwned(Intent data) {
        mOwnedFragment.moveToOwned(data);
    }

}

