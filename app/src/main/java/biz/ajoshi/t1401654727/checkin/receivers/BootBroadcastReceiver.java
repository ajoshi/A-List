package biz.ajoshi.t1401654727.checkin.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import biz.ajoshi.t1401654727.checkin.MainActivity;

/**
 * This resets the alarm upon boot, since alarms are reset upon reboot
 * Created by Aditya on 3/15/2015.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.resetAlarm(context);
    }
}