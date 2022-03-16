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


public class FileChooserAdapter extends ArrayAdapter<String> {
    public FileChooserAdapter(Context context, List<String> files){
        super(context, 0, files);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.file_chooser_list_item, parent, false);
        }

        String filename = getItem(position);
        TextView nameView = convertView.findViewById(R.id.dialog_file_name_text_view);
        if (filename != null) {
            nameView.setText(filename);
        }
        else {
            nameView.setText("");
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {

        String item = getItem(position);
        if (item != null)
            return item.hashCode();

        return -1L;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
