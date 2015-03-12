package com.ajoshi.t1401654727.checkin.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ajoshi.t1401654727.checkin.R;
import com.ajoshi.t1401654727.checkin.db.MyDBHelper;
import com.ajoshi.t1401654727.checkin.provider.EventProvider;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class SWQuerier extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CHECK_IN = "com.ajoshi.t1401654727.checkin.services.action.CHECKIN";

    // TODO: Rename parameters
    private static final String EXTRA_FIRST_NAME = "com.ajoshi.t1401654727.checkin.services.extra.FNMME";
    private static final String EXTRA_LAST_NAME = "com.ajoshi.t1401654727.checkin.services.extra.LNAME";
    private static final String EXTRA_CONF_CODE = "com.ajoshi.t1401654727.checkin.services.extra.CCODE";
    private static final String EXTRA_ID = "com.ajoshi.t1401654727.checkin.services.extra.ID";
    public static final String SOUTHWEST_CHECKIN_URL1 = "http://www.southwest.com/flight/retrieveCheckinDoc.html";
    public static final String SOUTHWEST_CHECKIN_URL2 = "http://www.southwest.com/flight/selectPrintDocument.html";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String fname, String lname, String confCode, String id) {
        Intent intent = new Intent(context, SWQuerier.class);
        intent.setAction(ACTION_CHECK_IN);
        intent.putExtra(EXTRA_FIRST_NAME, fname);
        intent.putExtra(EXTRA_LAST_NAME, lname);
        intent.putExtra(EXTRA_CONF_CODE, confCode);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    }

    public static Intent IntentForActionFoo(Context context, String fname, String lname, String confCode, String id) {
        Intent intent = new Intent(context, SWQuerier.class);
        intent.setAction(ACTION_CHECK_IN);
        intent.putExtra(EXTRA_FIRST_NAME, fname);
        intent.putExtra(EXTRA_LAST_NAME, lname);
        intent.putExtra(EXTRA_CONF_CODE, confCode);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    public SWQuerier() {
        super("SWQuerier");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_IN.equals(action)) {
                Log.d("SWCheckin", "checkin time!");
                final String fName = intent.getStringExtra(EXTRA_FIRST_NAME);
                final String Lname = intent.getStringExtra(EXTRA_LAST_NAME);
                final String cCode = intent.getStringExtra(EXTRA_CONF_CODE);
                final String id = intent.getStringExtra(EXTRA_ID);
                Log.d("SWCheckin", "checkin time!");
                Log.d("SWCheckin", fName);
                Log.d("SWCheckin", Lname);
                Log.d("SWCheckin", cCode);
                Log.d("SWCheckin", id);
                handleActionCheckin(fName, Lname, cCode, id);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckin(String fName, String lName, String cCode, String id) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            try {
                checkin(fName, lName, cCode);
//                onSuccess(id, fName, lName, cCode);
            } catch (IOException e) {
                //TODO retry
                e.printStackTrace();
            }
        } else {
            //TODO retry?
            // display error
        }
    }

    private void onSuccess(String id, String fName, String lName, String cCode) throws IOException {
        makeSuccessNotification(fName, lName, cCode);
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COL_DONE,1);
        getContentResolver().update(EventProvider.authUri, cv, MyDBHelper.COL_ID +"=?", new String [] {id});
    }

    private void makeSuccessNotification(String fName, String lName, String cCode) throws IOException {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Checked in to SW")
                        .setContentText(fName + " " + lName + " checked in to flight in 24 hours with confirmation code " + cCode);
        // Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void checkin(String fName, String lName, String cCode) throws IOException {
        //set up the httpurlconnectin
        URL url = new URL(SOUTHWEST_CHECKIN_URL1);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        //add the checkin values
        String checkinParams= String.format("confirmationNumber=%s&firstName=%s&lastName=%s", cCode, fName, lName);
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(checkinParams);
        writer.flush();
        writer.close();
        os.close();

        conn.connect();

        InputStream is = null;
        is = conn.getInputStream();
            if (is != null) {
                is.close();
            }
        conn.disconnect();

        //http://www.southwest.com//flight/selectPrintDocument.html
        // the index below might be important when you have multiple flights you can check in for!
        //checkinPassengers[0].selected

        //set up the httpurlconnectin
        url = new URL(SOUTHWEST_CHECKIN_URL2);
        conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        Log.d("SWCheckin",
                "Checkin done, getting ticket");
        //add the checkin values
        checkinParams= String.format("checkinPassengers[0].selected=true", cCode, fName, lName);
        os = conn.getOutputStream();
        writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(checkinParams);
        writer.flush();
        writer.close();
        os.close();

        conn.connect();

        is = conn.getInputStream();
        String tag = readTag(is);
        String group =  null;
        String position =  null;
        while (tag != null) {
            //look for boarding_group and boarding_info eg  <td class="boarding_group"><h2 class="boardingInfo">B</h2></td>
            if(tag.contains("boarding_group")) {
                Log.d("SWCheckin:tag", tag);
                Log.d("SWCheckin:nextline", readTag(is));
                Log.d("SWCheckin:location", ""+len);
                group =  readTag(is);
                Log.d("SWCheckin", group);
            }
            if(tag.contains("boarding_position")) {
                Log.d("SWCheckin:tag", tag);
                Log.d("SWCheckin:nectLine", readTag(is));
                Log.d("SWCheckin:location", ""+len);
                position =  readTag(is);
                Log.d("SWCheckin", position);
                break;
            }
            Log.d("SWCheckin:location", "tag: "+tag);

            tag = readTag(is);
        }

        Log.d("SWCheckin:location", "apparently done: "+len);

        if (is != null) {
            is.close();
        }
        conn.disconnect();

        if (position !=null && group !=null)
        {
            //success!!
            String pos = position.substring(0,2);
            if (pos.charAt(1) == '<') {
                pos = pos.substring(0,1);
            }
                        makeSuccessNotification(fName, lName, group.charAt(0)+""+pos);
        }
    }
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
int len = 0;
    public String readTag(InputStream stream) throws IOException {
        boolean isInTag = true;
        StringBuilder sb = new StringBuilder();
        do {
            int thisChar = stream.read();
            System.out.println(thisChar);
            // len++;
            if (thisChar == -1) {
                return null;
            }
            if (thisChar == '<') {
                isInTag = true;
            } else if (thisChar == '>') {
                isInTag = false;
            }
            sb.append((char)thisChar);
        } while (isInTag);
        return sb.toString();
    }

}
