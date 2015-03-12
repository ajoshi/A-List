package biz.ajoshi.t1401654727.checkin.frags;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by aditya on 6/1/2014.
 */
public class DatePickerFrag extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    TextView callingView;

    public DatePickerFrag() {
        super();
    }

    public DatePickerFrag(TextView view) {
        callingView = view;
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

    /**
     * Interface used to get data back from this frag
     */
    public interface OnDateSetListener {
        public void OnDateSet(TextView view, int year, int month, int day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Activity callingActivity = getActivity();
        if (callingActivity != null) {
            ((OnDateSetListener) callingActivity).OnDateSet(callingView, year, month, day);
        } else {
            Log.e("SWCheckin","Activity was null!");
        }
    }
}

