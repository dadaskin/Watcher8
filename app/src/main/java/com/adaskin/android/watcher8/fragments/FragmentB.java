package com.adaskin.android.watcher8.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;

public class FragmentB extends Fragment {
    public FragmentB() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_b, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.fragmentB_tv);
        textView.setText("Hello from Fragment B!");
        return rootView;
    }
}
