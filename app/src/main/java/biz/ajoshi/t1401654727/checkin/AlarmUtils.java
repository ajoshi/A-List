package biz.ajoshi.t1401654727.checkin;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import biz.ajoshi.t1401654727.checkin.alarm.AlarmData;
import biz.ajoshi.t1401654727.checkin.alarm.PreciseAlarmManager;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;
import biz.ajoshi.t1401654727.checkin.services.SWCheckinService;

/**
 * Created by ajoshi on 8/15/2016.
 */
public class AlarmUtils {
    /**
     * Sets the single alarm we have to go off when the earliest event is scheduled for
     */
    public static void resetAlarm(Context ctx) {
        resetAlarm(ctx, false);
    }

    /**
     * Sets the single alarm we have to go off when the earliest event is scheduled for
     * @param isPrecise true if we want to be sure that the device is awake when this alarm goes off
     */
    public static void resetAlarm(Context ctx, boolean isPrecise) {
        Cursor c = ctx.getContentResolver().query(Uri.withAppendedPath(EventProvider.AUTH_URI,
                        EventProvider.PATH_SEGMENT_SELECT_FIRST),
                new String[]{MyDBHelper.COL_TIME, MyDBHelper.COL_FNAME,
                        MyDBHelper.COL_LNAME, MyDBHelper.COL_CONF_CODE, MyDBHelper.COL_ID},
                MyDBHelper.COL_DONE + "=? AND " + MyDBHelper.COL_ATTEMPTS + "<?",
                new String[]{"0", String.valueOf(Constants.MAX_TRIES_FOR_CHECKIN)}, null);
        if (c != null) {
            if (c.moveToFirst()) {
                setAlarm(ctx, c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), isPrecise);
                c.close();
                return;
            }
        }
        Toast.makeText(ctx, R.string.unable_to_set_timer_toast, Toast.LENGTH_SHORT);
    }

    /**
     * Sets an exact alarm for a checkin at the given time. If 'isprecise' is set to true,
     * additional alarms are set to make sure the device is awake when the alarm goes off
     *
     * @param ctx
     * @param time  when the checkin should occur
     * @param fName first name of the passenger
     * @param lName last name
     * @param cCode confirmation code
     * @param id    id of this entry in the db
     * @param isPrecise true if we want to be sure that the device is awake when this alarm goes off
     */
    private static void setAlarm(Context ctx, long time, String fName, String lName, String cCode, String id, boolean isPrecise) {
       /*
        * Because of http://developer.android.com/reference/android/content/Intent.html#filterEquals(android.content.Intent)
        * returnpIntent will override departpIntent.
        * So either I change their  action, data, type, class, and categories (extras don't count) or I go a
        * diff direction and store all these things in a db right now. Then I set an alarm for the soonest
        * event. I then read DB, figure out what the soonest one is, and check in to that. THEN I set up an
        * alarm for the next event.
        */
        long alarmTime = time - Constants.MS_IN_DAY;
        PreciseAlarmManager am = new PreciseAlarmManager();
        AlarmData alarmData = new AlarmData(SWCheckinService.IntentForCheckingIn(ctx, fName, lName, cCode, id), true, alarmTime, 0);
        am.setServiceAlarm(ctx, alarmData);
    }
}
