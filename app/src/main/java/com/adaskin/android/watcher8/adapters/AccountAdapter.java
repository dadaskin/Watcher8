package com.adaskin.android.watcher8.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.models.AccountModel;
import com.adaskin.android.watcher8.utilities.Constants;

import java.util.List;

public class AccountAdapter extends ArrayAdapter<AccountModel> {
    public AccountAdapter(Context context, List<AccountModel> accounts) {
        super(context, 0, accounts);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_list_item, parent, false);
        }

        AccountModel account = getItem(position);
        TextView nameView = convertView.findViewById(R.id.dialog_account_name_text_view);
        TextView colorView = convertView.findViewById(R.id.dialog_account_color_field);
        if (account != null) {
            nameView.setText(account.getName());
            colorView.setBackgroundColor(account.getColor());
        }
        else {
            nameView.setText(Constants.ACCOUNT_UNKNOWN);
            colorView.setBackgroundColor(Constants.ACCOUNT_UNKNOWN);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {

        AccountModel item = getItem(position);
        if (item != null)
            return item.getName().hashCode();

        return -1L;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
