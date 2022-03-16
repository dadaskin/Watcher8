package com.adaskin.android.watcher8.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.adapters.FileChooserAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FileChooserFragment extends DialogFragment {

    private FileOkListener mFileOkListener;

    public interface FileOkListener {
        void onOkClick(String selectedFileName);
    }

    // Required empty constructor
    public FileChooserFragment() {}

    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity)context;
            try {
                mFileOkListener = (FileOkListener)activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement FileOkListener");
            }
        }
    }

    private ListView mLv;
    private Dialog mDlg;
    private List<String> mFiles = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = Objects.requireNonNull(inflater).inflate(R.layout.file_chooser_list, null);
        mLv = layout.findViewById(R.id.file_list);

        mLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mFiles = getFileList();
        FileChooserAdapter itemsAdapter = new FileChooserAdapter(getActivity(), mFiles);
        mLv.setAdapter(itemsAdapter);
        mLv.setItemChecked(0, true);

        Button okButton = layout.findViewById(R.id.file_list_ok_button);
        okButton.setOnClickListener(mOkListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Import file:").setView(layout);

        mDlg = builder.create();
        mDlg.show();
        return mDlg;
    }

    private final View.OnClickListener mOkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = mLv.getCheckedItemPosition();
            mFileOkListener.onOkClick(mFiles.get(position));

            mDlg.dismiss();
            mDlg.cancel();
        }
    };

    private List<String> getFileList() {
        List<String> filenames = new ArrayList<>();
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
        for (File file : folder.listFiles()) {
            String filename = file.getName();
            if ((filename.indexOf("Watcher8") != 0) ||(filename.equals("Watcher8_Parser")))
                continue;
            filenames.add(filename);
        }
        filenames.sort(new CustomComparator());
        return filenames;
    }

    class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return s2.compareTo(s1);
        }
    }

}
