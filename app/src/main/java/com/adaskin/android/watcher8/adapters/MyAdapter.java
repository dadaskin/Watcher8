package com.adaskin.android.watcher8.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;

import java.util.List;

public class MyAdapter extends ArrayAdapter<String>{
    private int mResourceId;

    public MyAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        mResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        String content = getItem(position);
        View view;

        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(mResourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.tv = view.findViewById(R.id.itemTextView);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tv.setText(content);
        return view;
    }

    class ViewHolder {
        TextView tv;
    }

}
