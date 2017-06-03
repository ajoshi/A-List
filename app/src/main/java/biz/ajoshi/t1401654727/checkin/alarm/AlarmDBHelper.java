package biz.ajoshi.t1401654727.checkin.alarm;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * The DB of alarms is technically not the same as the db of flights. It could be very different
 * Created by Aditya on 3/3/2016.
 */
public class AlarmDBHelper extends SQLiteOpenHelper {
        public static final String ALARM_TABLE_NAME = "alarm_times";
        public static final String COL_ID = "id";
        public static final String COL_REQUESTCODE = "requestCode";
        public static final String COL_INTENT = "intent";
        public static final String COL_TIME = "time";
        public static final String COL_ISPRECISE = "isPrecise";

        // Database creation sql statement
        private static final String DATABASE_CREATE = "create table "
                + ALARM_TABLE_NAME +
                "(" + COL_ID + " integer primary key autoincrement, "
                + COL_TIME + " integer not null, "
                + COL_INTENT + " text not null,"
                + COL_REQUESTCODE + " integer not null, "
                + COL_ISPRECISE + " integer default 0);";
        private static final String DATABASE_NAME = "alarms.db";
        private static final int DATABASE_VERSION = 1;

        public AlarmDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(AlarmDBHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion);
                // Can't upgrade yet
        }

        public long insert(ContentValues vals) {
                SQLiteDatabase db = getWritableDatabase();
                return db.insert(ALARM_TABLE_NAME, null, vals);
        }

        public long insert(long time, Intent intent, int requestCode, boolean isPrecise) {
                ContentValues cv = new ContentValues();
                cv.put(COL_INTENT, intent.toUri(0));
                cv.put(COL_TIME, time);
                cv.put(COL_REQUESTCODE, requestCode);
                cv.put(COL_ISPRECISE, isPrecise);
                return insert(cv);
        }


        public long insert(AlarmData alarmData) {
                ContentValues cv = new ContentValues();
                cv.put(COL_INTENT, alarmData.alarmIntent.toUri(0));
                cv.put(COL_TIME, alarmData.time);
                cv.put(COL_REQUESTCODE, alarmData.requestcode);
                cv.put(COL_ISPRECISE, alarmData.isPrecise);
                return insert(cv);
        }

        public int delete(String whereClause, String[] whereArgs) {
                SQLiteDatabase db = getWritableDatabase();
                return db.delete(ALARM_TABLE_NAME, whereClause, whereArgs);
        }

        /**
         * Deletes alarms set to go off over 10 minutes ago because you're probably too late to get
         * a good seat at this point
         */
        public void pruneOldAlarms() {
                long currentTime = System.currentTimeMillis()+AlarmConstants.MS_IN_TEN_MINUTES;
                delete(COL_TIME + " < " + currentTime, null);
        }

        /**
         * Gets a list of all alarms
         * @return List of all persisted alarms. Null if an error occurred
         */
        public List<AlarmData> getListOfAlarms() {
                pruneOldAlarms();
                SQLiteDatabase db = getReadableDatabase();
                Cursor allAlarmCursor = db.query(
                        AlarmDBHelper.ALARM_TABLE_NAME, null, null, null, null, null, AlarmDBHelper.COL_TIME);
                if (!allAlarmCursor.isClosed() && allAlarmCursor.moveToFirst()) {
                        int intentIndex = allAlarmCursor.getColumnIndex(COL_INTENT);
                        int timeIndex = allAlarmCursor.getColumnIndex(COL_TIME);
                        int requestcodeIndex = allAlarmCursor.getColumnIndex(COL_REQUESTCODE);
                        int isPreciseIndex = allAlarmCursor.getColumnIndex(COL_ISPRECISE);
                        if (intentIndex != -1 && timeIndex != -1 && requestcodeIndex != -1 && isPreciseIndex != -1) {
                                List<AlarmData> alarmList = new LinkedList<>();
                                do {
                                        String intentUri = allAlarmCursor.getString(intentIndex);
                                        boolean isPrecise = allAlarmCursor.getInt(isPreciseIndex) == 1;
                                        long time = allAlarmCursor.getLong(timeIndex);
                                        int requestCode = allAlarmCursor.getInt(requestcodeIndex);
                                        try {
                                                AlarmData data = new AlarmData(Intent.parseUri(intentUri, 0), isPrecise, time, requestCode);
                                                alarmList.add(data);
                                        } catch (URISyntaxException e) {
                                                Log.e("AlarmDBHelper", "Unable to parse alarm intent", e);
                                        }
                                } while (allAlarmCursor.moveToNext());
                                allAlarmCursor.close();
                                return alarmList;
                        }
                }
                allAlarmCursor.close();
                return null;
        }
}
