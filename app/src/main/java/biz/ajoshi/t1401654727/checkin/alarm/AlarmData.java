package biz.ajoshi.t1401654727.checkin.alarm;

import android.content.Intent;

/**
 * Created by ajoshi on 5/21/2017.
 */

public class AlarmData {
    Intent alarmIntent;
    boolean isPrecise;
    long time;
    int requestcode;

    /**
     *
     * @param intent Broadcast Intent to fire when the alarm goes off
     * @param isPrecise if false, delays of a few seconds up to a minute are acceptable
     * @param timestamp Time when the alarm should go off
     * @param requestcode requestcode for this alarm. Should be different for each alarm
     */
    public AlarmData(Intent intent, boolean isPrecise, long timestamp, int requestcode) {
        alarmIntent = intent;
        this.isPrecise = isPrecise;
        time = timestamp;
        this.requestcode = requestcode;
    }
}
