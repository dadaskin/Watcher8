package com.adaskin.android.watcher8.adapters;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NonSwipableViewPager extends ViewPager {

    public NonSwipableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonSwipableViewPager(Context context) {
        super(context);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Never allow swiping to switch between pages
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        // Never allow swiping to switch between pages
        performClick();
        return false;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean performClick() {
        return super.performClick();
    }

}
