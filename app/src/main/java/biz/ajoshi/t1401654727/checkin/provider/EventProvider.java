package biz.ajoshi.t1401654727.checkin.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;

/**
 * Allows creation, read, and deletion of an event
 */
public class EventProvider extends ContentProvider {

    //move to contract?
    public static final Uri authUri = Uri.parse("content://com.ajoshi.t1401654727.eventprovider");
    MyDBHelper dbHelper;
    SQLiteDatabase db;

    public EventProvider() {
        super();
        initDBIfNeeded();
    }

    private boolean initDBIfNeeded() {
        if (dbHelper != null && db != null) {
            return true;
        }
        Context ctx = getContext();
        if (ctx != null) {
            dbHelper = new MyDBHelper(ctx);
            db = dbHelper.getWritableDatabase();
            return true;
        }
        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(initDBIfNeeded()) {
            return db.delete(MyDBHelper.EVENT_TABLE_NAME, selection, selectionArgs);
        }
        return -1;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if(initDBIfNeeded()) {
            db.insert(MyDBHelper.EVENT_TABLE_NAME, null, values);
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }
    public static final String FIRST = "first";
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        //might be time to implement a matcher!!
        if(initDBIfNeeded()) {
            boolean selectFirst = false;
            if (FIRST.equals(uri.getLastPathSegment())) {
                selectFirst = true;
            }

            if (selectFirst) {
                return db.query(MyDBHelper.EVENT_TABLE_NAME, projection, selection, selectionArgs, null, null, MyDBHelper.COL_TIME, "1");
            }
            return db.query(MyDBHelper.EVENT_TABLE_NAME, projection, selection, selectionArgs, null, null, MyDBHelper.COL_TIME);
        }
        return null;
        // TODO: Implement this to handle query requests from clients.
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        if(initDBIfNeeded()) {
            return db.update(MyDBHelper.EVENT_TABLE_NAME, values, selection, selectionArgs);
        }
        return -1;
    }
}
