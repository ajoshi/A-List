package biz.ajoshi.t1401654727.checkin.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import biz.ajoshi.t1401654727.checkin.AlarmUtils;
import biz.ajoshi.t1401654727.checkin.Constants;
import biz.ajoshi.t1401654727.checkin.LaunchActivity;
import biz.ajoshi.t1401654727.checkin.MainActivity;
import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.network.HtmlReader;
import biz.ajoshi.t1401654727.checkin.network.Network;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;

import static biz.ajoshi.t1401654727.checkin.alarm.PreciseAlarmManager.EXTRA_ALARM_TIME;

/**
 * An {@link IntentService} subclass for handling asynchronous network requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class SWCheckinService extends IntentService {
    private static final String ACTION_CHECK_IN = "biz.ajoshi.t1401654727.checkin.services.action.CHECKIN";
    private static final String EXTRA_FIRST_NAME = "biz.ajoshi.t1401654727.checkin.services.extra.FNAME";
    private static final String EXTRA_LAST_NAME = "biz.ajoshi.t1401654727.checkin.services.extra.LNAME";
    private static final String EXTRA_CONF_CODE = "biz.ajoshi.t1401654727.checkin.services.extra.CCODE";
    private static final String EXTRA_ID = "biz.ajoshi.t1401654727.checkin.services.extra.ID";
    private static final String BOARDING_GROUP_TAG_CLASS = "boarding_group";
    private static final String BOARDING_POSITION_TAG_CLASS = "boarding_position";
    private static final String GATE_TAG_CLASS = "\"gate\"";
    // This isn't an exact number of bytes to skip, just the minimum
    private static final int BYTES_TO_SKIP = 27000;
    private static final int MS_IN_MINUTE = 60000;
    private final static int SUCCESS_NOTIFICATION_ID = 1;
    private final static int FAILURE_NOTIFICATION_ID = 2;
    private static final String TAG = "SWCheckinService";

    /*
     * When developing the app, we don't want the Southwest server to be queried.
     * Set to true to disable checkin.
     */
    public static final boolean DEBUG_DISABLE_CHECKIN = false;

    public SWCheckinService() {
        super("SWQuerier");
    }

    /**
     * Gets the intent to start the check in service. If the service is already performing a task
     * (checking in), this action will be queued
     * @param context  Context used for creating the intent
     * @param fname    first name of passenger
     * @param lname    last name of passenger
     * @param confCode confirmation code for flight
     * @param id       ID of this entry in the database
     * @return Intent to launch the checkin service
     */
    public static Intent IntentForCheckingIn(Context context, String fname, String lname,
                                             String confCode, String id) {
        Intent intent = new Intent(context, SWCheckinService.class);
        intent.setAction(ACTION_CHECK_IN);
        intent.putExtra(EXTRA_FIRST_NAME, fname);
        intent.putExtra(EXTRA_LAST_NAME, lname);
        intent.putExtra(EXTRA_CONF_CODE, confCode);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager pm = (PowerManager)getSystemService(
                Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "SWCheckinService");
        wl.acquire(Constants.MS_IN_THREE_HOURS/60); // Should never take longer than 3 minutes
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_IN.equals(action)) {
                final String fName = intent.getStringExtra(EXTRA_FIRST_NAME);
                final String lName = intent.getStringExtra(EXTRA_LAST_NAME);
                final String cCode = intent.getStringExtra(EXTRA_CONF_CODE);
                final String id = intent.getStringExtra(EXTRA_ID);
                final long time = intent.getLongExtra(EXTRA_ALARM_TIME, 0);
                handleActionCheckin(fName, lName, cCode, id, time);
            }
        }
        wl.release();
    }



    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckin(String fName, String lName, String cCode, String id, long time) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (DEBUG_DISABLE_CHECKIN) {
                if (time != 0) {
                    long currentTime = System.currentTimeMillis();
                    long delay = currentTime - time;
                    onSuccess(id, fName, lName, "delay " + delay, null, null, null);
                } else {
                    // put up a fake notification
                    onSuccess(id, fName, lName, "b5", null, null, null);

                }
            } else {
                try {
                    // fetch data
                    checkin(fName, lName, cCode, id);
                } catch (IOException e) {
                    registerRetry(id);
                    e.printStackTrace();
                }
            }
        } else {
            registerRetry(id);
        }
    }

    private void registerRetry(String id) {
        Log.w(TAG, "checkin failed, setting retry alarm");
        // see how many times we've tried for this flight
        Cursor entryForThisFlight = getContentResolver().query(EventProvider.AUTH_URI,
                new String[]{MyDBHelper.COL_ATTEMPTS}, MyDBHelper.COL_ID + "=?", new String[]{id},
                null);
        if (entryForThisFlight != null && entryForThisFlight.getCount() == 1 && entryForThisFlight.moveToFirst()) {
            int attempts = entryForThisFlight.getInt(0);
            entryForThisFlight.close();
            if (attempts++ < Constants.MAX_TRIES_FOR_CHECKIN) {
                long nextAttemptTime = Calendar.getInstance().getTimeInMillis() +
                        Constants.MS_IN_DAY + (attempts * MS_IN_MINUTE);

                // if we haven't tried too much, try again. else we let it drop eventually.
                ContentValues cv = new ContentValues();
                // Reregister for n minutes later- linear backoff
                cv.put(MyDBHelper.COL_TIME, nextAttemptTime);
                cv.put(MyDBHelper.COL_ATTEMPTS, attempts);
                getContentResolver().update(EventProvider.AUTH_URI, cv, MyDBHelper.COL_ID + "=?",
                        new String[]{id});
                AlarmUtils.resetAlarm(this);
            } else {
                /// if we didn't succeed in 15 minutes, I don't see any reason to keep trying
                makeFailureNotification();
            }

        }
    }

    /**
     * Make success notification, update DB and reset alarm
     *
     * @param id               id of flight we checked in to
     * @param fName
     * @param lName
     * @param boardingPosition
     * @param gate
     * @param origin
     * @param destination
     * @throws IOException
     */
    private void onSuccess(String id, String fName, String lName, String boardingPosition,
                           @Nullable String gate, @Nullable String origin, @Nullable String destination) {
        makeSuccessNotification(fName, lName, boardingPosition, gate, origin, destination);
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COL_DONE, 1);
        if (gate != null) {
            cv.put(MyDBHelper.COL_GATE, gate);
        }
        if (origin != null) {
            cv.put(MyDBHelper.COL_FROM_PLACE, origin);
        }
        if (destination != null) {
            cv.put(MyDBHelper.COL_DEST_PLACE, destination);
        }
        cv.put(MyDBHelper.COL_POSITION, boardingPosition);
        cv.put(MyDBHelper.COL_FNAME, fName);

        getContentResolver().update(EventProvider.AUTH_URI, cv, MyDBHelper.COL_ID + "=?",
                new String[]{id});
        AlarmUtils.resetAlarm(this);
    }

    /**
     * Creates the success notification. Clicking the notification does nothing.
     *
     * @param fName            first name
     * @param lName            last name
     * @param boardingPosition boarding position, eg "A01"
     * @param gate             name of gate, eg "C6"
     * @param origin           where the flight originates from
     * @param destination      where the flight is headed
     * @throws IOException
     */
    private void
    makeSuccessNotification(String fName, String lName, String boardingPosition,
                                         @Nullable String gate, String origin, String destination) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(lName)//getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text_small, boardingPosition)
                                + (gate == null ? "" : getString(R.string.notification_has_gate, gate)))
                        .setAutoCancel(true);
        if (origin != null && destination != null) {
            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notification_big_text, fName, lName, origin, destination, boardingPosition, gate)));
        }
        mBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                LaunchActivity.getLaunchIntent(getApplicationContext()), PendingIntent.FLAG_ONE_SHOT));
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(SUCCESS_NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Creates a failure notification to inform user of failure to check in
     */
    private void makeFailureNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.notification_failure_title))
                        .setContentText(getString(R.string.notification_failure_text))
                        .setAutoCancel(true);
        mBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                MainActivity.getListIntent(getApplicationContext()), PendingIntent.FLAG_ONE_SHOT));
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(FAILURE_NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * This performs the actual checkin and creates a notification on success
     *
     * @param fName String first name of passenger
     * @param lName String last name of passenger
     * @param cCode String confirmation code for flight
     * @param id    String id of this entry in db
     * @throws IOException
     */
    private void checkin(String fName, String lName, String cCode, String id) throws IOException {
        String group = null;
        String position = null;
        String gate = null;
        Network network = new Network();
        HttpResponse response = network.getCheckInPageResponse(fName, lName, cCode);

        HtmlReader reader = new HtmlReader();
        InputStream is = response.getEntity().getContent();
        long readPosition = 0;
        // Skip the first 27kb since it's all JS, CSS and unnecessary data
        //            is.skip(BYTES_TO_SKIP);
        while (readPosition++ < BYTES_TO_SKIP) {
            // could skip a bit more, but I don't want to cut it too close in case they change the site again
            is.read();
        }
        String tag = reader.readTag(is);
        while (tag != null) {
            //look for boarding_group and boarding_info eg  <td class="boarding_group"><h2 class="boardingInfo">B</h2></td>
            if (tag.contains(BOARDING_GROUP_TAG_CLASS)) {
                group = reader.getDocumentContentForNextTag(is);
            }
            if (tag.contains(BOARDING_POSITION_TAG_CLASS)) {
                position = reader.getDocumentContentForNextTag(is);
            }
            if (tag.contains(GATE_TAG_CLASS)) {
                gate = reader.getDocumentContentForNextTag(is);
                if(gate != null && gate.length() > Constants.MAX_GATE_NAME_LENGTH) {
                    // This would happen if the gate data wasn't correctly populated in the response
                    gate = gate.substring(0, Constants.MAX_GATE_NAME_LENGTH);
                }
                break;
            }
            tag = reader.readTag(is);
        }
        if (is != null) {
            is.close();
        }
        response.getEntity().consumeContent();
        if (position != null && group != null) {
            //success!!
            String newPosition = new StringBuilder(group).append(" ").append(position).toString();
            //TODO extract origin and destination from response
            onSuccess(id, fName, lName, newPosition, gate, null, null);
        } else {
            // something bad happened. Now we just try again
            registerRetry(id);
        }
    }

}
