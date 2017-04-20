package biz.ajoshi.t1401654727.checkin.alarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This resets the alarm upon boot, since alarms are reset upon reboot
 * Created by Aditya on 3/15/2015.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Ask for all alarms that havent' gone off yet, set alarms for those, delete old ones
    }
}