package biz.ajoshi.t1401654727.checkin.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;

/**
 * Allows creation, read, and deletion of a flight event
 */
public class EventProvider extends ContentProvider {

    //move to contract?
    public static final String AUTHORITY = "biz.ajoshi.t1401654727.eventprovider";
    public static final Uri AUTH_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_SEGMENT_SELECT_FIRST = "first";
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
        if (initDBIfNeeded()) {
            return db.delete(MyDBHelper.EVENT_TABLE_NAME, selection, selectionArgs);
        }
        return -1;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = 0;
        if (initDBIfNeeded()) {
            rowID = db.insert(MyDBHelper.EVENT_TABLE_NAME, null, values);
        }
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(AUTH_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        //might be time to implement a matcher!!
        if (initDBIfNeeded()) {
            boolean selectFirst = false;
            String lastPathSegment = uri.getLastPathSegment();
            if (PATH_SEGMENT_SELECT_FIRST.equals(lastPathSegment)) {
                selectFirst = true;
            }

            if (selectFirst) {
                return db.query(MyDBHelper.EVENT_TABLE_NAME, projection, selection, selectionArgs, null, null, MyDBHelper.COL_TIME, "1");
            } else if (lastPathSegment != null) {
                return db.query(MyDBHelper.EVENT_TABLE_NAME, projection, MyDBHelper.COL_ID + "=?", new String[]{lastPathSegment}, null, null, MyDBHelper.COL_TIME);
            }
            return db.query(MyDBHelper.EVENT_TABLE_NAME, projection, selection, selectionArgs, null, null, MyDBHelper.COL_TIME);
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (initDBIfNeeded()) {
            return db.update(MyDBHelper.EVENT_TABLE_NAME, values, selection, selectionArgs);
        }
        return -1;
    }
}
