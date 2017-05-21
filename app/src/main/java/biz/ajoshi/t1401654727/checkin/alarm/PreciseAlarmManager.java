package biz.ajoshi.t1401654727.checkin.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import biz.ajoshi.t1401654727.checkin.alarm.receiver.PreciseAlarmReceiver;

/**
 * Created by Aditya on 2/25/2016.
 */
public class PreciseAlarmManager {

    public static final String EXTRA_TIME_YO = "timeYo";

    /**
     * Sets an alarm to go off at the requested time unless Xperia device's Standby mode is active.
     * If a precise alarm is set, it will set extra
     * alarms and hold a 1 minute wakelock to ensure the alarm fire within a few seconds of the desired time
     * @param ctx Context to use
     * @param alarmTime Time when the alarm should go off
     * @param alarmIntent Broadcast Intent to fire when the alarm goes off
     * @param requestCode requestcode for this alarm. Should be different for each alarm
     * @param isPrecise if false, delays of a few seconds up to a minute are acceptable
     */
    public void setServiceAlarm(Context ctx, long alarmTime, Intent alarmIntent, int requestCode, boolean isPrecise) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (isPrecise) {
            // precision is expensive- we need to set multiple alarms
            setExactAlarm(am, getPIntentForAlarm(ctx, PreciseAlarmReceiver.ACTION_HALF_HOUR_TO_WAKEUP, requestCode), alarmTime - AlarmConstants.MS_IN_THIRTY_MINUTES);
            setExactAlarm(am, getPIntentForAlarm(ctx, PreciseAlarmReceiver.ACTION_TEN_MIN_TO_WAKEUP, requestCode), alarmTime - AlarmConstants.MS_IN_TEN_MINUTES);
            setExactAlarm(am, getPIntentForAlarm(ctx, PreciseAlarmReceiver.ACTION_FIVE_MIN_TO_WAKEUP, requestCode), alarmTime - AlarmConstants.MS_IN_FIVE_MINUTES);
            setExactAlarm(am, getPIntentForAlarm(ctx, PreciseAlarmReceiver.ACTION_ONE_MIN_TO_WAKEUP, requestCode), alarmTime - AlarmConstants.MS_IN_ONE_MINUTE);
            setExactAlarm(am, getPIntentForAlarm(ctx, PreciseAlarmReceiver.ACTION_TURN_OFF_LOCK, requestCode), alarmTime + AlarmConstants.MS_IN_ONE_MINUTE);
        }
        // Set the real alarm as well
        alarmIntent.putExtra(EXTRA_TIME_YO, alarmTime);
        PendingIntent pendingIntent = PendingIntent.getService(ctx, requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        storeAlarmInDB(ctx, alarmTime, alarmIntent, requestCode, isPrecise);
        // TODO store intent in db with the requestcode for the given time
        setExactAlarm(am, pendingIntent, alarmTime);
    }

    /**
     * Create a PendingIntent for an Intent with the given Action, using given requestcode
     * @param ctx
     * @param action
     * @param requestCode
     * @return
     */
    private PendingIntent getPIntentForAlarm(Context ctx, String action, int requestCode) {
        Intent intent = new Intent(action);
        intent.setPackage(ctx.getPackageName());
        return PendingIntent.getBroadcast(ctx, requestCode,
               intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Sets the alarm for exactly the time that is specified
     * @param am AlarmManager
     * @param pendingIntent PendingIntent of the alarm
     * @param alarmTime time when alarm should go off
     */
    private void setExactAlarm(AlarmManager am, PendingIntent pendingIntent, long alarmTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // if less than kitkat, use the old one
            am.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // else use setexact so the alarm is exact
            am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }
        else {
            // else use the newest one so the alarm is exact
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
          //  am.setAlarmClock(new AlarmManager.AlarmClockInfo(alarmTime, pendingIntent), pendingIntent);
        }
    }

    public boolean storeAlarmInDB(Context ctx, long time, Intent intent, int requestCode, boolean isPrecise) {
        AlarmDBHelper dbHelper = new AlarmDBHelper(ctx);
        return dbHelper.insert(time, intent, requestCode, isPrecise) != -1;
    }
}
