package com.example.test.sms;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Extract Year, Month and Day of calender to get date.
        Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);

        // Set onTimeSetListener and pass to Sms schedule activity
        return new DatePickerDialog(getActivity(), getTheme(), (DatePickerDialog.OnDateSetListener) getActivity(), yy, mm, dd);
    }
}
