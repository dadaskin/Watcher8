package com.adaskin.android.watcher8.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.adaskin.android.watcher8.R;

public class FooterFragment extends Fragment {

    private View mRefreshButtonView;
    private TextView mDateTextView;
    private TextView mTimeTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_footer, container, false);

        final ImageButton refreshButton = (ImageButton)view.findViewById(R.id.refresh_button_moving);
        final ImageButton addButton = (ImageButton)view.findViewById(R.id.add_button);
        mDateTextView = (TextView)view.findViewById(R.id.last_update_date_text);
        mTimeTextView = (TextView)view.findViewById(R.id.last_update_time_text);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButtonClicked(v);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshButtonClicked(v);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void addButtonClicked(View v) {
        Toast.makeText(this.getContext(), "Add button clicked", Toast.LENGTH_LONG).show();
        //        // activityCallback.addButtonClicked();
    }

    private void refreshButtonClicked(View v) {
        mRefreshButtonView = v;
        Toast.makeText(this.getContext(), "Refresh button clicked", Toast.LENGTH_LONG).show();
        // activityCallback.refreshButtonClicked();
    }

    public void beginButtonAnimation(Animation animation) {
        animation.setRepeatCount(Animation.INFINITE);
        mRefreshButtonView.startAnimation(animation);
    }

    public void endButtonAnimation() {
        mRefreshButtonView.clearAnimation();
    }
}
