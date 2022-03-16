package com.adaskin.android.watcher8.adapters;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.adaskin.android.watcher8.fragments.ListFragmentBase;
import com.adaskin.android.watcher8.fragments.OwnedFragment;
import com.adaskin.android.watcher8.fragments.WatchFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private OwnedFragment mOwnedFragment = null;
    private WatchFragment mWatchFragment = null;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public ListFragmentBase getItem(int position) {
        ListFragmentBase fragment = null;
        switch(position) {
            case 0:
                if (mOwnedFragment == null) {
                    mOwnedFragment = new OwnedFragment();
                }
                fragment = mOwnedFragment;
                break;
            case 1:
                if (mWatchFragment == null) {
                    mWatchFragment = new WatchFragment();
                }
                fragment = mWatchFragment;
                break;
        }
       return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    public void moveToOwned(Intent data) {
        mOwnedFragment.moveToOwned(data);
    }

}

