package biz.ajoshi.t1401654727.checkin.alarm.receiver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.alarm.AlarmConstants;

/**
 * Created by Aditya on 2/15/2016.
 */
public class PreciseAlarmReceiver extends WakefulBroadcastReceiver {

    public static final String ACTION_HALF_HOUR_TO_WAKEUP = "biz.ajoshi.t1401654727.receivers.WAKE_UP_IN_THIRTY";
    public static final String ACTION_TEN_MIN_TO_WAKEUP = "biz.ajoshi.t1401654727.receivers.WAKE_UP_IN_TEN";
    public static final String ACTION_FIVE_MIN_TO_WAKEUP = "biz.ajoshi.t1401654727.receivers.WAKE_UP_IN_FIVE";
    public static final String ACTION_ONE_MIN_TO_WAKEUP = "biz.ajoshi.t1401654727.receivers.WAKE_UP_IN_ONE";
    public static final String ACTION_TURN_OFF_LOCK = "biz.ajoshi.t1401654727.receivers.DONE";
    protected PowerManager.WakeLock mWakeLock;

    @Override
    public void onReceive(Context context, Intent intent) {
       // makeNot(context, intent.getAction());
            if (ACTION_ONE_MIN_TO_WAKEUP.equals(intent.getAction())) {
                PowerManager pm = (PowerManager)context.getSystemService(
                        Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        "PreciseAlarmReceiver");
                mWakeLock.acquire(AlarmConstants.MS_IN_FIVE_MINUTES); // Should never take longer
            } else if (ACTION_TURN_OFF_LOCK.equals(intent.getAction())) {
                if (mWakeLock != null) {
                    mWakeLock.release();
                }
            }
            PreciseAlarmReceiver.completeWakefulIntent(intent);
    }
    private void makeNot(Context ctx, String huh) {
        long time = System.currentTimeMillis();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setContentText(time + huh.substring(33))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true);
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify((int)(time % 10), mBuilder.build());
    }

}
