package com.adaskin.android.watcher8.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.adapters.AccountAdapter;
import com.adaskin.android.watcher8.models.AccountModel;

import java.util.LinkedList;
import java.util.List;

public class AccountSelectionFragment extends DialogFragment {

    private AlertOkListener alertOkListener;

    // An interface to be implemented on the hosting activity for the OK button
    public interface AlertOkListener {
        void onOkClick(int position);
    }

    // Required empty constructor
    public AccountSelectionFragment() {
    }

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
        Activity activity = getActivity();
        if (activity== null) {
            Log.d("foo", "AccountSelectionFragment.onCreateDialog(): getActivity() returns null");
            return mDlg;
        }
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            Toast.makeText(activity, "AccountSelection: Inflater is null", Toast.LENGTH_LONG).show();
            return mDlg;
        }
        View layout = inflater.inflate(R.layout.account_list, null);
        Bundle bundle = getArguments();
        if (bundle == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("AccountSelection: Bundle is null").setView(layout);
            mDlg = builder.create();
            mDlg.show();
            return mDlg;
        }
        int accountColor = bundle.getInt("color");
        int index = AccountModel.getBlockColorIndex(accountColor);

        mLv = layout.findViewById(R.id.account_list);

        mLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        AccountAdapter accountAdapter = new AccountAdapter(activity, getAccountModelList());
        mLv.setAdapter(accountAdapter);
        mLv.setItemChecked(index, true);

        Button okButton = layout.findViewById(R.id.account_list_ok_button);
        okButton.setOnClickListener(mOkListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
