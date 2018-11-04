package com.adaskin.android.watcher8.utilities;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.adaskin.android.watcher8.R;

public class Themes {
    public static void adjustBuyBlockTextColor(Context context, TextView view, float value, float gainThreshold) {

        int normalBackgroundColor = ContextCompat.getColor(context, R.color.list_background_color);
        int highlightBackgroundColor = ContextCompat.getColor(context, R.color.over_gain_target_color);

        int positiveTextColor = ContextCompat.getColor(context, R.color.positive_text_color);
        int neutralTextColor = ContextCompat.getColor(context, R.color.neutral_text_color);
        int negativeTextColor = ContextCompat.getColor(context, R.color.negative_text_color);

        if (value > Constants.POSITIVE_ONE_DECIMAL_LIMIT) {
            view.setTextColor(positiveTextColor);
        } else if (value < Constants.NEGATIVE_ONE_DECIMAL_LIMIT) {
            view.setTextColor(negativeTextColor);
        } else {
            view.setTextColor(neutralTextColor);
        }

        view.setBackgroundColor(normalBackgroundColor);
        if (value > gainThreshold) {
            view.setBackgroundColor(highlightBackgroundColor);
            view.setTextColor(neutralTextColor);
        }
    }

    public static void adjustOverallTextColor(Context context, TextView view, float value) {

        int backgroundColor = ContextCompat.getColor(context, R.color.view_background_color);
        view.setBackgroundColor(backgroundColor);

        int positiveTextColor = ContextCompat.getColor(context, R.color.positive_text_color);
        int neutralTextColor = ContextCompat.getColor(context, R.color.neutral_text_color);
        int negativeTextColor = ContextCompat.getColor(context, R.color.negative_text_color);

        if (value > Constants.POSITIVE_ONE_DECIMAL_LIMIT) {
            view.setTextColor(positiveTextColor);
        } else if (value < Constants.NEGATIVE_ONE_DECIMAL_LIMIT) {
            view.setTextColor(negativeTextColor);
        } else {
            view.setTextColor(neutralTextColor);
        }
    }
}
