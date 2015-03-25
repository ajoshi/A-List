package biz.ajoshi.t1401654727.checkin.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import biz.ajoshi.t1401654727.checkin.Constants;
import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;

import static biz.ajoshi.t1401654727.checkin.MainActivity.resetAlarm;

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
    public static final String SOUTHWEST_CHECKIN_URL1 = "https://www.southwest.com/flight/retrieveCheckinDoc.html?confirmationNumber=%1s&firstName=%2s&lastName=%3s";
    private static final String SOUTHWEST_CHECKIN_URL2 = "https://www.southwest.com/flight/selectPrintDocument.html?int=";
    private static final String SOUTHWEST_CHECKIN_URL3 = "https://www.southwest.com/flight/selectPrintDocument.html";
    private static final String BOARDING_GROUP_TAG_CLASS = "boarding_group";
    private static final String BOARDING_POSITION_TAG_CLASS = "boarding_position";
    private static final String GATE_TAG_CLASS = "\"gate\"";
    private static final int BYTES_TO_SKIP = 27000;
    private static final int MS_IN_MINUTE = 60000;
    private final static int SUCCESS_NOTIFICATION_ID = 1;
    private final static int FAILURE_NOTIFICATION_ID = 2;


    public SWCheckinService() {
        super("SWQuerier");
    }

    /**
     * Starts this service to Check in with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startCheckingIn(Context context, String fname, String lname,
                                       String confCode, String id) {
        Intent intent = new Intent(context, SWCheckinService.class);
        intent.setAction(ACTION_CHECK_IN);
        intent.putExtra(EXTRA_FIRST_NAME, fname);
        intent.putExtra(EXTRA_LAST_NAME, lname);
        intent.putExtra(EXTRA_CONF_CODE, confCode);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    }

    /**
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

    /**
     * Checks in to southwest, and returns the httpresponse
     *
     * @param fName String first name of passenger
     * @param lName String last name of passenger
     * @param cCode String confirmation code for flight
     * @return HttpReponse containing checkin info
     * @throws IOException
     */
    private static HttpResponse getCheckInPageResponse(String fName, String lName, String cCode) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        // make first call which will redirect me
        HttpGet httpget = new HttpGet(String.format(SOUTHWEST_CHECKIN_URL1, cCode, fName, lName));
        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httpget);
        String firstCookies = getCookiesFromHeaders(response.getHeaders("Set-Cookie"));
        response.getEntity().consumeContent();

        // take redirect
        HttpGet httpget2 = new HttpGet(SOUTHWEST_CHECKIN_URL2);
        httpget2.setHeader("Cookie", firstCookies);
        response = httpclient.execute(httpget);
        String secondCookies = getCookiesFromHeaders(response.getHeaders("Set-Cookie"));
        response.getEntity().consumeContent();

        // submit checkin confirmation
        HttpPost httpPost = new HttpPost(SOUTHWEST_CHECKIN_URL3);
        httpPost.setHeader("Cookie", secondCookies);
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("checkinPassengers[0].selected", "true"));
        postParameters.add(new BasicNameValuePair("printDocuments", "Check In"));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        response = httpclient.execute(httpPost);
        return response;
    }

    /**
     * Get the content for the next HTML tag in the stream
     * eg, if stream is at "&lt;a&gt;hi&lt;b&gt;>Bob&lt;/b&gt;&lt;/a&gt;", returns hi
     *
     * @param is InputStream to get data from
     * @return Document content for the next tag
     * @throws IOException
     */
    public static String getDocumentContentForNextTag(InputStream is) throws IOException {
        // discard next tag
        readTag(is);
        // we are at the content + next tag now
        String content = readTag(is);

        //discard the closing tag
        int indexOfNextTag = content.indexOf('<');
        if (indexOfNextTag != -1) {
            content = content.substring(0, indexOfNextTag);
        }
        return content;
    }

    private static String getCookiesFromHeaders(Header[] cookieHeaders) {
        StringBuilder cookieBuilder = new StringBuilder();
        for (Header cook : cookieHeaders) {
            // JSESSIONID=F145AF252BD9F751826BF0; Path=/flight/; HttpOnly
            String cookString = cook.getValue();
            String cookieSubstring = cookString.substring(0, cookString.indexOf(';') + 2);
            // we could pull just the tuple and append the semicolon later, or just keep it in
            // I do the latter
            cookieBuilder.append(cookieSubstring);
        }
        return cookieBuilder.toString();
    }

    public static String readTag(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        boolean isInTag = true;
        StringBuilder sb = new StringBuilder();
        do {
            int thisChar = stream.read();
            if (thisChar == -1) {
                return sb.toString();
            }
            if (thisChar == '<') {
                isInTag = true;
            } else if (thisChar == '>') {
                isInTag = false;
            }
            sb.append((char) thisChar);
        } while (isInTag);
        return sb.toString();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_IN.equals(action)) {
                final String fName = intent.getStringExtra(EXTRA_FIRST_NAME);
                final String lName = intent.getStringExtra(EXTRA_LAST_NAME);
                final String cCode = intent.getStringExtra(EXTRA_CONF_CODE);
                final String id = intent.getStringExtra(EXTRA_ID);
                handleActionCheckin(fName, lName, cCode, id);
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
                checkin(fName, lName, cCode, id);
            } catch (IOException e) {
                registerRetry(id);
                e.printStackTrace();
            }
        } else {
            registerRetry(id);
        }
    }

    private void registerRetry(String id) {
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
                resetAlarm(this);
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
                           @Nullable String gate, @Nullable String origin, @Nullable String destination) throws IOException {
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
        getContentResolver().update(EventProvider.AUTH_URI, cv, MyDBHelper.COL_ID + "=?",
                new String[]{id});
        resetAlarm(this);
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
    private void makeSuccessNotification(String fName, String lName, String boardingPosition,
                                         @Nullable String gate, String origin, String destination) throws IOException {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text_small, boardingPosition)
                                + (gate == null ? "" : getString(R.string.notification_has_gate, gate)));
        //TODO add destination name, etc so it's easier to figure out what we checked in to
        // probably easier to do when I add a share feature- nobody  wants to type all that out
        if (origin != null && destination != null) {
            mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notification_big_text, fName, lName, origin, destination, boardingPosition, gate)));
        }
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
                        .setContentText(getString(R.string.notification_failure_text));
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

        HttpResponse response = getCheckInPageResponse(fName, lName, cCode);

        InputStream is = response.getEntity().getContent();
        long readPosition = 0;
        // Skip the first 27kb since it's all JS, CSS and unnecessary data
        while (readPosition++ < BYTES_TO_SKIP) {
            // could skip a bit more, but I don't want to cut it too close in case they change the site again
            is.read();
        }
        String tag = readTag(is);
        while (tag != null) {
            //look for boarding_group and boarding_info eg  <td class="boarding_group"><h2 class="boardingInfo">B</h2></td>
            if (tag.contains(BOARDING_GROUP_TAG_CLASS)) {
                group = getDocumentContentForNextTag(is);
            }
            if (tag.contains(BOARDING_POSITION_TAG_CLASS)) {
                position = getDocumentContentForNextTag(is);
            }
            if (tag.contains(GATE_TAG_CLASS)) {
                gate = getDocumentContentForNextTag(is);
                break;
            }
            tag = readTag(is);
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
