package biz.ajoshi.t1401654727.checkin.ui.frag.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


import biz.ajoshi.t1401654727.checkin.R;

/**
 * Created by Aditya on 3/30/2015.
 */
public class TimezonePicker extends DialogFragment {
    public static final String ARG_VIEW_ID = "biz.ajoshi.t1401654727.checkin.ui.frag.dialog.TimezonePicker.viewId";
    public TimezonePicker() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.timezone_picker_dialog_title)
        builder.setTitle(R.string.notification_title)
                .setItems(R.array.time_zone_name_list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Activity act = getActivity();
                        if (act != null) {
                            ((OnTimezoneSelected) act).onTimeZoneSelected(getArguments().getInt(ARG_VIEW_ID), which);
                        }
                    }
                });
        return builder.create();
    }

    public interface OnTimezoneSelected {
        void onTimeZoneSelected(int viewId, int timeZoneIndex);
    }
}
