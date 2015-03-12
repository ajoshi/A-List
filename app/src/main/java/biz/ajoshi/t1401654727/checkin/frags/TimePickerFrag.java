package biz.ajoshi.t1401654727.checkin.frags;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by aditya on 6/1/2014.
 */
public class TimePickerFrag extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

    TextView callingView;

    public TimePickerFrag() {
        super();
    }

    public TimePickerFrag(TextView view) {
        callingView = view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    /**
     * Interface used to get data back from this frag
     */
    public interface OnTimeSetListener {
        public void OnTimeSet(TextView view, int hour, int minute);
    }


    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        if(callingView != null) {
            callingView.setText("");
        }
        Activity callingActivity = getActivity();
        if (callingActivity != null) {
            ((OnTimeSetListener) callingActivity).OnTimeSet(callingView, hourOfDay, minute);
        } else {
            Log.e("SWCheckin", "Activity was null!");
        }
    }
}

