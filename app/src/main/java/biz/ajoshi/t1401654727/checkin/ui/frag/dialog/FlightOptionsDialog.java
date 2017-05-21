package biz.ajoshi.t1401654727.checkin.ui.frag.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import biz.ajoshi.t1401654727.checkin.FlightListElement;
import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.network.Network;

/**
 * Created by ajoshi on 5/20/2017.
 */

public class FlightOptionsDialog  extends DialogFragment {
    public static final String ARG_TITLE= "biz.ajoshi.t1401654727.checkin.ui.frag.dialog.title";
    public static final String ARG_ELEMENT= "biz.ajoshi.t1401654727.checkin.ui.frag.dialog.element";

    /**
     * Allows this dialog to delete/modify the flight list
     */
    public interface ClickListener {
        void deleteFlightWithId(String flightId);
    }

    public static FlightOptionsDialog getInstance(String title, FlightListElement element) {
        FlightOptionsDialog dialog = new FlightOptionsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        bundle.putParcelable(ARG_ELEMENT, element);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final String title = args.getString(ARG_TITLE);
        FlightListElement element = args.getParcelable(ARG_ELEMENT);

        final String flightId = element.id;
        final String fname = element.fName;
        final String lname = element.lName;
        final String ccode = element.confCode;
        // Use the Builder class for convenient dialog construction
        final Activity act = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setMessage(title)
                .setPositiveButton(R.string.flight_detail_dialog_delete_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Delete this entry
                        //TODO confirmation dialog. yay, dialog spam
                        ((ClickListener)getTargetFragment()).deleteFlightWithId(flightId);
                    }
                })
                .setNegativeButton(R.string.flight_detail_dialog_force_checkin_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // force check in for given id
                        Intent checkinViaWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Network
                                .SOUTHWEST_CHECKIN_URL1, ccode, fname, lname)));
                        startActivity(checkinViaWebIntent);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
