package biz.ajoshi.t1401654727.checkin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by aditya on 6/7/2014.
 */
public class MyDBHelper extends SQLiteOpenHelper {

    public static final String EVENT_TABLE_NAME = "event_table";
    public static final String COL_ID = "_id";
    public static final String COL_FNAME = "fname";
    public static final String COL_LNAME = "lname";
    public static final String COL_CONF_CODE = "confcode";
    public static final String COL_TIME = "time";
    public static final String COL_DONE = "done";
    public static final String COL_GATE = "gate";
    public static final String COL_POSITION = "position";
    public static final String COL_FROM_PLACE = "from_place";
    public static final String COL_DEST_PLACE = "dest_place";
    public static final String COL_ATTEMPTS = "attempts";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + EVENT_TABLE_NAME +
            "(" + COL_ID + " integer primary key autoincrement, "
            + COL_FNAME + " text not null, "
            + COL_LNAME + " text not null, "
            + COL_TIME + " integer not null, "
            + COL_CONF_CODE + " text not null,"
            + COL_FROM_PLACE + " text,"
            + COL_DEST_PLACE + " text,"
            + COL_GATE + " text,"
            + COL_POSITION + " text,"
            + COL_DONE + " integer default 0,"
            + COL_ATTEMPTS + " integer default 0);";
    private static final String DATABASE_NAME = "events.db";
    private static final int DATABASE_VERSION = 2;

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MyDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion);
        if (oldVersion == 1 && newVersion == DATABASE_VERSION) {
            db.execSQL("ALTER TABLE " + EVENT_TABLE_NAME + " ADD " + COL_POSITION + " text");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE_NAME);
            onCreate(db);
        }

    }


}
