package com.adaskin.android.watcher8.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adaskin.android.watcher8.R;
import com.adaskin.android.watcher8.utilities.Constants;

import java.util.Locale;
import java.util.Objects;

public class ChangeParameterActivity extends AppCompatActivity {
    private String mParamName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameter_change);

        setTitleString();

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            Toast.makeText(this, "ChangeParameter Bundle is null", Toast.LENGTH_LONG).show();
            return;
        }
        String symbol = bundle.getString(Constants.SYMBOL_BUNDLE_KEY);
        mParamName = bundle.getString(Constants.PARAM_NAME_BUNDLE_KEY);
        float oldValue = bundle.getFloat(Constants.OLD_VALUE_BUNDLE_KEY);

        setNameAndOriginalValue(symbol, mParamName, oldValue);
    }

    private void setTitleString()
    {
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getColor(android.R.color.white)));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customTitleView = Objects.requireNonNull(inflater).inflate(R.layout.custom_main_titlebar, null);
        TextView tv = customTitleView.findViewById(R.id.custom_main_title_bar_text);
        tv.setTextSize(18f);
        tv.setText(String.format("%s %s", getString(R.string.app_name), getString(R.string.change_parameter_title)));
        actionBar.setCustomView(customTitleView);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void setNameAndOriginalValue(String symbol, String paramName, float oldValue) {
        String format = Constants.NUM_SHARES_FORMAT;
        if (paramName.contains("Gain Target"))
        {	format = Constants.PERCENTAGE_FORMAT;
        } else if (paramName.contains("Strike Price")) {
            format = Constants.CURRENCY_FORMAT;
        }

        TextView nameView = findViewById(R.id.param_change_name);
        nameView.setText(String.format("%s %s %s %s", getString(R.string.change), paramName, getString(R.string.parameter_on), symbol));

        TextView oldValueView = findViewById(R.id.param_change_old_value);
        oldValueView.setText(String.format("%s %s", getString(R.string.old_value_label), String.format(Locale.US,format, oldValue)));
    }

    @SuppressWarnings("UnusedParameters")
    public void doneButtonClicked(View v) {
        EditText newValueField = findViewById(R.id.param_change_new_value);
        float newValue;
        try {
            newValue = Float.parseFloat(newValueField.getText().toString());
        } catch(NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, Constants.EMPTY_FIELD_ERR_MSG, Toast.LENGTH_LONG).show();
            return;
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constants.PARAM_NAME_BUNDLE_KEY, mParamName);
        returnIntent.putExtra(Constants.PARAM_NEW_VALUE_BUNDLE_KEY, newValue);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
