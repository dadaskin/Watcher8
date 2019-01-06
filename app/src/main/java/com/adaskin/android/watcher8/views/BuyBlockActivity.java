package com.adaskin.android.watcher8.views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.fragments.AccountSelectionFragment;
import com.adaskin.android.watcher8.fragments.DatePickerDialogFragment;
import com.adaskin.android.watcher8.models.AccountModel;
import com.adaskin.android.watcher8.utilities.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BuyBlockActivity extends AppCompatActivity implements AccountSelectionFragment.AlertOkListener, DatePickerDialog.OnDateSetListener {
    private Calendar mCalendar;
    private String mSymbol;
    private Button mDateButton;
    private int mAccountColor;
    private final SimpleDateFormat mDateFormatter = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_block_add);

        mCalendar = Calendar.getInstance();
        mSymbol = getIntent().getStringExtra(Constants.BUY_BLOCK_SYMBOL_KEY);
        mDateButton = findViewById(R.id.buy_block_date);
        mDateButton.setOnClickListener(mDateButtonOnClickListener);
        Button accountChangeButton = findViewById(R.id.buy_block_account_change_button);
        accountChangeButton.setOnClickListener(mAccountChangeButtonOnClickListener);
        mDateButton.setText(mDateFormatter.format(mCalendar.getTime()));
        mAccountColor = Constants.ACCOUNT_UNKNOWN;
        TextView accountColorBlock = findViewById(R.id.buy_block_account_color_field);
        accountColorBlock.setBackgroundColor(mAccountColor);
        TextView accountName = findViewById(R.id.buy_block_account_name_text_view);
        accountName.setText(getString(R.string.unknown_account));

        setTitleString();
    }

    private void setTitleString()
    {
        ActionBar actionBar = this.getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getColor(android.R.color.white)));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customTitleView = Objects.requireNonNull(inflater).inflate(R.layout.custom_main_titlebar, null);
        TextView tv = customTitleView.findViewById(R.id.custom_main_title_bar_text);
        tv.setTextSize(18f);
        tv.setText(String.format("%s %s %s",getString(R.string.app_name),getString(R.string.add_buy_block),mSymbol));
        actionBar.setCustomView(customTitleView);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @SuppressWarnings("UnusedParameters")
    public void doneButtonClicked(View view) {

        EditText buyPriceField = findViewById(R.id.buy_block_price);
        EditText numSharesField = findViewById(R.id.buy_block_num_shares);

        String buyDateString = mDateButton.getText().toString();
        float buyPrice;
        float numShares;
        try {
            buyPrice = Float.parseFloat(buyPriceField.getText().toString());
            numShares = Float.parseFloat(numSharesField.getText().toString());
        } catch(NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, Constants.EMPTY_FIELD_ERR_MSG, Toast.LENGTH_LONG).show();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BUY_BLOCK_SYMBOL_KEY, mSymbol);
        bundle.putString(Constants.BUY_BLOCK_DATE_KEY,buyDateString);
        bundle.putFloat(Constants.BUY_BLOCK_PRICE_KEY, buyPrice);
        bundle.putFloat(Constants.BUY_BLOCK_NUM_KEY, numShares);
        bundle.putInt(Constants.BUY_BLOCK_ACCOUNT_COLOR_KEY, mAccountColor);

        Intent returnIntent = new Intent();
        returnIntent.putExtras(bundle);

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    // Date Picker methods
    private final View.OnClickListener mDateButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showDatePicker();
        }
    };

    private void showDatePicker() {
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        DialogFragment newFragment = DatePickerDialogFragment.newInstance(year, month, day);
        newFragment.show(getSupportFragmentManager(), "DatePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        mCalendar.set(year, month, day);
        mDateButton.setText(mDateFormatter.format(mCalendar.getTime()));
    }


    // Account change methods
    private final View.OnClickListener mAccountChangeButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAccountSelectionDialog();
        }
    };

    private void showAccountSelectionDialog(){
        FragmentManager manager = getSupportFragmentManager();
        AccountSelectionFragment accountFragment = new AccountSelectionFragment();

        // Use current AccountColor as default
        Bundle args = new Bundle();
        args.putInt("color", mAccountColor);

        accountFragment.setArguments(args);
        accountFragment.show(manager, "Account Selection Dialog");
    }

    @Override
    public void onOkClick(int position) {
        List<Integer> colorList = AccountModel.getBlockAccountColorList();
        List<CharSequence> nameList = AccountModel.getBlockAccountNameList();

        mAccountColor = colorList.get(position);
        CharSequence accountName = nameList.get(position);

        TextView accountColorView = findViewById(R.id.buy_block_account_color_field);
        TextView accountNameView = findViewById(R.id.buy_block_account_name_text_view);

        accountColorView.setBackgroundColor(mAccountColor);
        accountNameView.setText(accountName);
    }
}
