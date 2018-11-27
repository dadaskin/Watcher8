package com.adaskin.android.watcher8.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.adapters.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragmentA extends Fragment {

    private ListView listView;
    private MyAdapter adapter;
    private List<String> item;

    public FragmentA() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a, container, false);

        listView = (ListView) view.findViewById(R.id.listViewA);
        item = new ArrayList<>();

        item.add("one");
        item.add("two");
        item.add("three");
        item.add("four");
        item.add("five");
        item.add("six");
        item.add("seven");
        item.add("eight");
        item.add("nine");
        item.add("ten");

        adapter = new MyAdapter(getContext(), R.layout.content_a, item);
        listView.setAdapter(adapter);

        return view;
    }
}
