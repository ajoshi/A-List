package biz.ajoshi.t1401654727.checkin.alarm;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
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
        private static final String DATABASE_NAME = "events.db";
        private static final int DATABASE_VERSION = 3;

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

        public int delete(String whereClause, String[] whereArgs) {
                SQLiteDatabase db = getWritableDatabase();
                return db.delete(ALARM_TABLE_NAME, whereClause, whereArgs);
        }
}
