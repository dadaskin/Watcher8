package com.adaskin.android.watcher8.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.adapters.AccountAdapter;
import com.adaskin.android.watcher8.models.AccountModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AccountSelectionFragment extends DialogFragment {

    private AlertOkListener alertOkListener;

    // An interface to be implemented on the hosting activity for the OK button
    public interface AlertOkListener {
        void onOkClick(int position);
    }

    // Required empty constructor
    public AccountSelectionFragment() {}

    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
            try {
                alertOkListener = (AlertOkListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement AlertOkListener");
            }
        }
    }

    private ListView mLv;
    private Dialog mDlg;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        int accountColor = Objects.requireNonNull(bundle).getInt("color");
        int index = AccountModel.getBlockColorIndex(accountColor);

        LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = Objects.requireNonNull(inflater).inflate(R.layout.account_list, null);
        mLv = layout.findViewById(R.id.account_list);

        mLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        AccountAdapter accountAdapter = new AccountAdapter(getActivity(), getAccountModelList());
        mLv.setAdapter(accountAdapter);
        mLv.setItemChecked(index, true);

        Button okButton = layout.findViewById(R.id.account_list_ok_button);
        okButton.setOnClickListener(mOkListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Account:");
        builder.setView(layout);

        mDlg = builder.create();
        mDlg.show();
        return mDlg;
    }

    private final View.OnClickListener mOkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = mLv.getCheckedItemPosition();
            alertOkListener.onOkClick(position);

            mDlg.dismiss();
            mDlg.cancel();
        }
    };

    private List<AccountModel> getAccountModelList() {
        CharSequence[] nameArray = new CharSequence[0];
        nameArray = AccountModel.getBlockAccountNameList().toArray(nameArray);

        Integer[] colorArray = new Integer[0];
        colorArray = AccountModel.getBlockAccountColorList().toArray(colorArray);

        List<AccountModel> accountList = new LinkedList<>();
        for (int i=0; i< nameArray.length; i++) {
            String name = nameArray[i].toString();
            int color = colorArray[i];
            accountList.add(new AccountModel(name,color));
        }

        return  accountList;
    }
}
