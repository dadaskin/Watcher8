package com.adaskin.android.watcher8.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.database.DbAdapter;
import com.adaskin.android.watcher8.models.DataModel;

import java.util.Objects;

public class FooterFragment extends Fragment {

    private TextView mDateTextView;
    private TextView mTimeTextView;
    private View mRefreshButtonView;
    private AnimationDrawable mD;

    private ProgressBar mSpinner;

    public interface FooterListener {
        void addButtonClicked();
        void refreshButtonClicked(FooterFragment footerFragment, View buttonView);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_footer, container, false);

//        final ImageButton refreshButton = view.findViewById(R.id.refresh_button_moving);
//        final ImageButton refreshButton = view.findViewById(R.id.refresh_button_stationary);
        final Button refreshButton = view.findViewById(R.id.detail_refresh_btn);
        final ImageButton addButton = view.findViewById(R.id.add_button);
        mDateTextView = view.findViewById(R.id.last_update_date_text);
        mTimeTextView = view.findViewById(R.id.last_update_time_text);

        DbAdapter dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();
        new DataModel(dbAdapter);
        dbAdapter.close();

//       mSpinner = view.findViewById(R.id.progressBar);
//       mSpinner.setVisibility(View.GONE);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButtonClicked(v);
            }
        });

        mD = (AnimationDrawable)refreshButton.getCompoundDrawables()[0];;
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshButtonClicked(v);
            }
        });
        return view;
    }

    public void addButtonClicked(View v) {
        Toast.makeText(this.getContext(), "Add button clicked in FooterFragment", Toast.LENGTH_LONG).show();
        ((FooterListener) Objects.requireNonNull(getActivity())).addButtonClicked();
    }

    public void refreshButtonClicked(View view) {
        Toast.makeText(this.getContext(), "Refresh button clicked in FooterFragment", Toast.LENGTH_LONG).show();
        mRefreshButtonView = view;
        beginButtonAnimation(view);
        ((FooterListener) Objects.requireNonNull(getActivity())).refreshButtonClicked(this, view);
    }

    private void beginButtonAnimation(View view) {
//        Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_1sec_center);
//        rotation.setRepeatCount(Animation.INFINITE);
//        view.startAnimation(rotation);

//       mSpinner.setVisibility(View.VISIBLE);

        mD.start();

    }

    /// call this from something in Main Activity
    public void endButtonAnimation() {
//        mRefreshButtonView.clearAnimation();

//      mSpinner.setVisibility(View.GONE);

        mD.stop();
    }

    public void refreshUpdateDateTime(String dateString, String timeString)
    {
        mDateTextView.setText(dateString);
        mTimeTextView.setText(timeString);
    }
}
