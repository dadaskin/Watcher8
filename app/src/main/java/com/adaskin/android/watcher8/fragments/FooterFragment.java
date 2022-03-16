package com.adaskin.android.watcher8.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.models.DataModel;
import com.adaskin.android.watcher8.utilities.Constants;
import com.adaskin.android.watcher8.utilities.Refresher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FooterFragment extends Fragment implements Refresher.RefreshedObject {

    private TextView mDateTextView;
    private TextView mTimeTextView;
    private Button mRefreshButton;
    SimpleDateFormat mSdf = new SimpleDateFormat(Constants.UPDATE_DATE_FORMAT, Locale.US);
    SimpleDateFormat mStf = new SimpleDateFormat(Constants.UPDATE_TIME_FORMAT, Locale.US);

    public interface FooterListener {
        void addButtonClicked();
        void refreshButtonClicked(FooterFragment footerFragment);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        displayData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_footer, container, false);

        mRefreshButton = view.findViewById(R.id.refresh_all_btn);
        final ImageButton addButton = view.findViewById(R.id.add_button);
        mDateTextView = view.findViewById(R.id.last_update_date_text);
        mTimeTextView = view.findViewById(R.id.last_update_time_text);

        DbAdapter dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();
        new DataModel(dbAdapter);
        dbAdapter.close();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButtonClicked();
            }
        });

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshButtonClicked();
            }
        });
        return view;
    }

    private void addButtonClicked() {
        ((FooterListener) Objects.requireNonNull(getActivity())).addButtonClicked();
    }

    private void refreshButtonClicked() {
        ((FooterListener) Objects.requireNonNull(getActivity())).refreshButtonClicked(this);
    }

    private void refreshUpdateDateTime(String dateString, String timeString)
    {
        mDateTextView.setText(dateString);
        mTimeTextView.setText(timeString);
    }

    public void fillData() {
        Date now = new Date();
        String dateString = mSdf.format(now);
        String timeString = mStf.format(now);

        DbAdapter dbAdapter = new DbAdapter(getContext());
        dbAdapter.open();
        dbAdapter.removeLastUpdateRecord();
        dbAdapter.createLastUpdateRecord(dateString, timeString);
        dbAdapter.close();

        refreshUpdateDateTime(dateString, timeString);
    }

    private void displayData() {
        DbAdapter dbAdapter = new DbAdapter(getContext());
        dbAdapter.open();
        Cursor cursor = dbAdapter.fetchLastUpdateRecord();
        dbAdapter.close();
        String dateString = cursor.getString(cursor.getColumnIndex(DbAdapter.U_DATE));
        String timeString = cursor.getString(cursor.getColumnIndex(DbAdapter.U_TIME));
        cursor.close();

        refreshUpdateDateTime(dateString, timeString);
    }

    public Button getRefreshButton() {
        return mRefreshButton;
    }
}
