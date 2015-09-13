package biz.ajoshi.t1401654727.checkin.receivers;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import biz.ajoshi.t1401654727.checkin.services.SWCheckinService;

public class WakefulReceiver extends WakefulBroadcastReceiver {

    public static final String ACTION_WAKE_UP = "biz.ajoshi.t1401654727.receivers.WAKE_UP";

    private static final String EXTRA_FIRST_NAME = "biz.ajoshi.t1401654727.checkin.receivers.extra.FNAME";
    private static final String EXTRA_LAST_NAME = "biz.ajoshi.t1401654727.checkin.receivers.extra.LNAME";
    private static final String EXTRA_CONF_CODE = "biz.ajoshi.t1401654727.checkin.receivers.extra.CCODE";
    private static final String EXTRA_ID = "biz.ajoshi.t1401654727.checkin.receivers.extra.ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        // This is the Intent to deliver to our service.
        String fname = intent.getStringExtra(EXTRA_FIRST_NAME);
        String lname = intent.getStringExtra(EXTRA_LAST_NAME);
        String ccode = intent.getStringExtra(EXTRA_CONF_CODE);
        String id = intent.getStringExtra(EXTRA_ID);
        Intent serviceIntent = SWCheckinService.IntentForCheckingIn(context, fname, lname, ccode, id);

        // Start the service, keeping the device awake while it is launching.
        Log.i("WakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
        startWakefulService(context, serviceIntent);
    }

    /**
     * @param fname    first name of passenger
     * @param lname    last name of passenger
     * @param confCode confirmation code for flight
     * @param id       ID of this entry in the database
     * @return Intent to launch the checkin service
     */
    public static Intent IntentForCheckingIn(String fname, String lname,
                                             String confCode, String id) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FIRST_NAME, fname);
        intent.putExtra(EXTRA_LAST_NAME, lname);
        intent.putExtra(EXTRA_CONF_CODE, confCode);
        intent.putExtra(EXTRA_ID, id);
        intent.setAction(ACTION_WAKE_UP);
        return intent;
    }
}