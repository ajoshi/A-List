package biz.ajoshi.swcheckin;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;

public class EventProviderTest extends ProviderTestCase2<EventProvider> {

    private static final String TAG = EventProviderTest.class.getSimpleName();
    private static final int NUMBER_OF_TEST_ROWS = 3;
    private static final String FIRST_NAME_BASE = "firstName";
    private static final String LAST_NAME_BASE = "lastName";
    private static final String CONF_CODE_BASE = "confcode";
    private static final long TIME_BASE = System.currentTimeMillis();
    private static final long BASE_TIME_DELTA = 1000;

    private MockContentResolver mMockResolver;

    /**
     * Constructor.
     *
     * @param providerClass     The class name of the provider under test
     * @param providerAuthority The provider's authority string
     */
    public EventProviderTest(Class<EventProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    public EventProviderTest() {
        super(EventProvider.class, EventProvider.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.d(TAG, "setUp: ");
        mMockResolver = getMockContentResolver();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Log.d(TAG, "tearDown:");
    }

    /**
     * Util to make contentvalues using the given params
     */
    private ContentValues makeFakeCV(String fname, String lname, String conf, long time) {
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COL_FNAME, fname);
        cv.put(MyDBHelper.COL_LNAME, lname);
        cv.put(MyDBHelper.COL_CONF_CODE, conf);
        cv.put(MyDBHelper.COL_TIME, time);
        return cv;
    }

    /**
     * Util to insert default rows
     *
     * @return Uri of the last insert
     */
    private Uri insertTestRows() {
        for (int i = 0; i < NUMBER_OF_TEST_ROWS - 1; i++) {
            mMockResolver.insert(EventProvider.AUTH_URI, makeFakeCV(FIRST_NAME_BASE + i,
                    LAST_NAME_BASE + i, CONF_CODE_BASE + i, TIME_BASE + (i * BASE_TIME_DELTA)));
        }
        return mMockResolver.insert(EventProvider.AUTH_URI, makeFakeCV(FIRST_NAME_BASE, LAST_NAME_BASE,
                CONF_CODE_BASE, TIME_BASE - BASE_TIME_DELTA));
    }

    /**
     * Test that the correct uri is returned by an insert
     */
    public void testInsertReturnsRightUri() {
        Uri uri = mMockResolver.insert(EventProvider.AUTH_URI, makeFakeCV("fname", "lname", "conf", System.currentTimeMillis()));
        assertEquals(1L, ContentUris.parseId(uri));
    }

    /**
     * Test that multiple inserts give us an expected uri (ids autoincrement)
     */
    public void testMultipleInserts() {
        Uri uri = insertTestRows();
        assertEquals(NUMBER_OF_TEST_ROWS, ContentUris.parseId(uri));
    }

    /**
     * Test that the Uri we get from an insert does select the inserted entry
     */
    public void testUriFromInsertWorks() {
        Uri uri = insertTestRows();
        Cursor c = mMockResolver.query(uri,
                new String[]{MyDBHelper.COL_ID, MyDBHelper.COL_FNAME},
                null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(FIRST_NAME_BASE, c.getString(1));
    }

    /**
     * Tests that the 'select first' path segment gives us the next upcoming flight
     */
    public void testGetUpcoming() {
        insertTestRows();
        Cursor c = mMockResolver.query(Uri.withAppendedPath(EventProvider.AUTH_URI,
                        EventProvider.PATH_SEGMENT_SELECT_FIRST),
                new String[]{MyDBHelper.COL_ID, MyDBHelper.COL_FNAME},
                null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(FIRST_NAME_BASE, c.getString(1));
    }

    /**
     * Tests that the default query gives us all the rows ordered by time
     */
    public void testNormalQuery() {
        insertTestRows();
        Cursor c = mMockResolver.query(EventProvider.AUTH_URI,
                new String[]{MyDBHelper.COL_ID, MyDBHelper.COL_FNAME},
                null, null, null);
        assertEquals(NUMBER_OF_TEST_ROWS, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(FIRST_NAME_BASE, c.getString(1));
        assertTrue(c.moveToNext());
        assertEquals(FIRST_NAME_BASE + 0, c.getString(1));
    }

    /**
     * Tests that delete does, in fact, delete
     */
    public void testDelete() {
        insertTestRows();
        mMockResolver.delete(EventProvider.AUTH_URI, MyDBHelper.COL_ID + "=?", new String[]{String.valueOf(NUMBER_OF_TEST_ROWS)});
        Cursor c = mMockResolver.query(EventProvider.AUTH_URI,
                new String[]{MyDBHelper.COL_ID, MyDBHelper.COL_FNAME},
                null, null, null);
        assertEquals(NUMBER_OF_TEST_ROWS - 1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(FIRST_NAME_BASE + 0, c.getString(1));
    }
}
