package biz.ajoshi.t1401654727.checkin.ui.frag.dialog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by aditya on 6/1/2014.
 */
public class DatePickerFrag extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public static final String ARG_VIEW_ID = "biz.ajoshi.t1401654727.checkin.ui.frag.dialog.DatePickerFrag.viewId";

    public DatePickerFrag() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Activity callingActivity = getActivity();
        if (callingActivity != null) {
            ((OnDateSetListener) callingActivity).OnDateSet(getArguments().getInt(ARG_VIEW_ID), year, month, day);
        } else {
            Log.e("SWCheckin", "Activity was null!");
        }
    }

    /**
     * Interface used to get data back from this frag
     */
    public interface OnDateSetListener {
        public void OnDateSet(int viewId, int year, int month, int day);
    }
}

