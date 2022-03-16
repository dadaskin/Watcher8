package com.adaskin.android.watcher8.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Objects;

/**
 * <p>This class provides a usable {@link DatePickerDialog} wrapped as a {@link DialogFragment},
 * using the compatibility package v4. Its main advantage is handling Issue 34833
 * automatically for you.</p>
 *
 * <p>Current implementation (because I wanted that way =) ):</p>
 *
 * <ul>
 * <li>Only two buttons, a {@code BUTTON_POSITIVE} and a {@code BUTTON_NEGATIVE}.
 * <li>Buttons labeled from {@code android.R.string.ok} and {@code android.R.string.cancel}.
 * </ul>
 *
 * <p><strong>Usage sample:</strong></p>
 *
 * <pre>class YourActivity extends Activity implements OnDateSetListener
 *
 * // ...
 *
 * Bundle b = new Bundle();
 * b.putInt(DatePickerDialogFragment.YEAR, 2012);
 * b.putInt(DatePickerDialogFragment.MONTH, 6);
 * b.putInt(DatePickerDialogFragment.DATE, 17);
 * DialogFragment picker = new DatePickerDialogFragment();
 * picker.setArguments(b);
 * picker.show(getActivity().getSupportFragmentManager(), "fragment_date_picker");</pre>
 *
 * @author davidcesarino@gmail.com
 * @version 2012.0828
 * @see <a href="http://code.google.com/p/android/issues/detail?id=34833">Android Issue 34833</a>
 * @see <a href="http://stackoverflow.com/q/11444238/489607"
 * >Jelly Bean DatePickerDialog is there a way to cancel?</a>
 *
 */
public class DatePickerDialogFragment extends DialogFragment {

    private static final String YEAR = "Year";
    private static final String MONTH = "Month";
    private static final String DAY = "Day";

    private OnDateSetListener mListener;
    private Activity mActivity;

    private int year;
    private int month;
    private int day;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity) {
            mActivity = (Activity)context;
            try {
                mListener = (OnDateSetListener) mActivity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnDateSetListener");
            }
        }
        this.mListener = (OnDateSetListener) getActivity();
    }

    @Override
    public void onDetach() {
        this.mListener = null;
        super.onDetach();
    }

    public static DatePickerDialogFragment newInstance (int year, int month, int day) {
        DatePickerDialogFragment newDialog = new DatePickerDialogFragment();

        // Supply initial date to show in dialog.
        Bundle args = new Bundle();

        args.putInt(DAY, day);
        args.putInt(MONTH, month);
        args.putInt(YEAR, year);

        newDialog.setArguments(args);

        return newDialog;
    }

    // Added to original version in order to restore bundle saved by Activity
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        day = Objects.requireNonNull(getArguments()).getInt(DAY);
        month = getArguments().getInt(MONTH);
        year = getArguments().getInt(YEAR);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Jelly Bean introduced a bug in DatePickerDialog (and possibly
        // TimePickerDialog as well), and one of the possible solutions is
        // to postpone the creation of both the listener and the BUTTON_* .
        //
        // Passing a null here won't harm because DatePickerDialog checks for a null
        // whenever it reads the listener that was passed here. >>> This seems to be
        // true down to 1.5 / API 3, up to 4.1.1 / API 16. <<< No worries. For now.
        //
        // See my own question and answer, and details I included for the issue:
        //
        // http://stackoverflow.com/a/11493752/489607
        // http://code.google.com/p/android/issues/detail?id=34833
        //
        // Of course, suggestions welcome.

        final DatePickerDialog picker = new DatePickerDialog(mActivity,
                getConstructorListener(), year, month, day);

        if (hasJellyBeanAndAbove()) {
            picker.setButton(DialogInterface.BUTTON_POSITIVE,
                    Objects.requireNonNull(getActivity()).getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatePicker dp = picker.getDatePicker();
                            mListener.onDateSet(dp,
                                    dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                        }
                    });
            picker.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getActivity().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
        }
        return picker;
    }

    @SuppressWarnings("SameReturnValue")
    private static boolean hasJellyBeanAndAbove() {
        return true;
    }

    private OnDateSetListener getConstructorListener() {
        return hasJellyBeanAndAbove() ? null : mListener;
    }
}


