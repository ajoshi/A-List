package biz.ajoshi.t1401654727.checkin.alarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import biz.ajoshi.t1401654727.checkin.alarm.AlarmDBHelper;
import biz.ajoshi.t1401654727.checkin.alarm.AlarmData;
import biz.ajoshi.t1401654727.checkin.alarm.PreciseAlarmManager;

/**
 * This resets the alarm upon boot, since alarms are reset upon reboot
 * Created by Aditya on 3/15/2015.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Ask for all alarms that havent' gone off yet, set alarms for those, delete old ones
        // what this SHOULD do is read the alarms db
        Log.e("ajoshi", "Boot receiver called, setting alarms");
        AlarmDBHelper dbHelper = new AlarmDBHelper(context);
        List <AlarmData> alarmDataList = dbHelper.getListOfAlarms();
        if (alarmDataList != null) {
            PreciseAlarmManager am = new PreciseAlarmManager();
            for (AlarmData alarm : alarmDataList) {
                am.setServiceAlarmDontPersist(context, alarm);
            }
        }
    }
}