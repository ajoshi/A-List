package biz.ajoshi.t1401654727.checkin;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;
import biz.ajoshi.t1401654727.checkin.services.SWCheckinService;
import biz.ajoshi.t1401654727.checkin.ui.frag.AddNewFlightFragment;
import biz.ajoshi.t1401654727.checkin.ui.frag.FlightListFragment;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.DatePickerFrag;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.TimePickerFrag;

/**
 * Main activity for this app. Holds viewpager for the various fragments
 */
public class MainActivity extends Activity implements ActionBar.TabListener, TimePickerFrag.OnTimeSetListener, DatePickerFrag.OnDateSetListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * Sets the single alarm we have to go off when the earliest event is scheduled for
     */
    public static void resetAlarm(Context ctx) {
        Cursor c = ctx.getContentResolver().query(Uri.withAppendedPath(EventProvider.AUTH_URI,
                        EventProvider.PATH_SEGMENT_SELECT_FIRST),
                new String[]{MyDBHelper.COL_TIME, MyDBHelper.COL_FNAME,
                        MyDBHelper.COL_LNAME, MyDBHelper.COL_CONF_CODE, MyDBHelper.COL_ID},
                MyDBHelper.COL_DONE + "=? AND " + MyDBHelper.COL_ATTEMPTS + "<?",
                new String[]{"0", String.valueOf(Constants.MAX_TRIES_FOR_CHECKIN)}, null);
        if (c != null) {
            if (c.moveToFirst()) {
                setAlarm(ctx, c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                c.close();
                return;
            }
        }
        Toast.makeText(ctx, R.string.unable_to_set_timer_toast, Toast.LENGTH_SHORT);
    }

    /**
     * Sets an exact alarm for a checkin at the given time
     *
     * @param ctx
     * @param time  when the checkin should occur
     * @param fName first name of the passenger
     * @param lName last name
     * @param cCode confirmation code
     * @param id    id of this entry in the db
     */
    private static void setAlarm(Context ctx, long time, String fName, String lName, String cCode, String id) {
       /*
        * Because of http://developer.android.com/reference/android/content/Intent.html#filterEquals(android.content.Intent)
        * returnpIntent will override departpIntent.
        * So either I change their  action, data, type, class, and categories (extras don't count) or I go a
        * diff direction and store all these things in a db right now. Then I set an alarm for the soonest
        * event. I then read DB, figure out what the soonest one is, and check in to that. THEN I set up an
        * alarm for the next event.
        */
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(ctx, 0,
                SWCheckinService.IntentForCheckingIn(ctx, fName, lName, cCode, id), PendingIntent.FLAG_UPDATE_CURRENT);
        long alarmTime = time - Constants.MS_IN_DAY;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // if less than kitkat, use the old one
            am.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        } else {
            // else use the new one so the alarm is exact
            am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        // I use a Holo theme, so NPE shouldn't happen
        // Also, I know this is deprecated; it wasn't when I put it in.
        //TODO try to replace
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        cleanUpDB();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        // on launch, reset alarm
        resetAlarm(this);
    }

    /**
     * Deletes old flights
     */
    public void cleanUpDB() {
        long timeNow = Calendar.getInstance().getTimeInMillis();
        getContentResolver().delete(EventProvider.AUTH_URI, MyDBHelper.COL_TIME + " < " + timeNow, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            refreshFlightListFrag();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * Create a reminder for the given flight. Also update the stored user name
     *
     * @param view
     */
    public void createReminder(View view) {
        String firstName = getTextViewValueById(R.id.firstName);
        String lastName = getTextViewValueById(R.id.lastName);
        // Store the names in prefs so next time hopefully the user won't have to type in their name
        SharedPreferences.Editor editor = getSharedPreferences(AddNewFlightFragment
                .PREF_NAMES_FILENAME, MODE_PRIVATE).edit();
        editor.putString(AddNewFlightFragment.PREF_FIRST_NAME, firstName);
        editor.putString(AddNewFlightFragment.PREF_LAST_NAME, lastName);
        editor.apply();

        AddNewFlightFragment addNewFlightFrag = getFlightFrag();
        //This is where I set the alarm for the service
        long departMillis = addNewFlightFrag.getDepartCalMillis();
        long returnMillis = addNewFlightFrag.getReturnCalMillis();

        String confirmationCode = getTextViewValueById(R.id.confNum);
        String origin = getTextViewValueById(R.id.departLoc);
        String destination = getTextViewValueById(R.id.arriveLoc);
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        if (addReminderToDB(departMillis, firstName, lastName, confirmationCode, origin, destination, addNewFlightFrag.getDepartTimeString(dateTimeFormat))) {
            if (returnMillis > departMillis) {
                // Only add a return flight if it's after the departure
                // This lets us make sure that we don't set a reminder if there is no return
                addReminderToDB(returnMillis, firstName, lastName, confirmationCode, destination, origin, addNewFlightFrag.getReturnTimeString(dateTimeFormat));
                // Check in alarm set for flight at %s on %s and return at %3$s on %4$s
                Toast.makeText(this, getString(R.string.entry_added_multiple_toast,
                                getTextViewValueById(R.id.departureTime),
                                getTextViewValueById(R.id.departureDate),
                                getTextViewValueById(R.id.returnTime),
                                getTextViewValueById(R.id.returnDate)),
                        Toast.LENGTH_LONG).show();
            } else {
                // Check in alarm set for flight at %s on %s
                Toast.makeText(this, getString(R.string.entry_added_toast,
                                getTextViewValueById(R.id.departureTime),
                                getTextViewValueById(R.id.departureDate)),
                        Toast.LENGTH_LONG).show();
            }

            //reset the frag
            refreshFlightListFrag();
            resetAlarm(this);
        } else {
            Toast.makeText(this, R.string.entry_not_added_toast, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Inserts new data into the DB
     *
     * @param time      Time in ms when this flight takes off
     * @param firstName first name
     * @param lastName  last name
     * @param confCode  confirmation code
     * @return true if succeeded, false otherwise
     */
    private boolean addReminderToDB(long time, String firstName, String lastName, String confCode) {
        if (firstName == null || lastName == null || confCode == null) {
            return false;
        }

        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COL_FNAME, firstName);
        cv.put(MyDBHelper.COL_LNAME, lastName);
        cv.put(MyDBHelper.COL_CONF_CODE, confCode);
        cv.put(MyDBHelper.COL_TIME, time);
        getContentResolver().insert(EventProvider.AUTH_URI, cv);
        return true;
    }

    /**
     * Inserts new data into the DB
     *
     * @param time         Time in ms when this flight takes off
     * @param firstName    first name
     * @param lastName     last name
     * @param confCode     confirmation code
     * @param flightSource City from which the flight originates
     * @param flightDest   City where the flight lands
     * @param dTime        Localized time string for display purposes
     * @return true if succeeded, false otherwise
     */
    private boolean addReminderToDB(long time, String firstName, String lastName, String confCode,
                                    String flightSource, String flightDest, String dTime) {
        if (firstName == null || lastName == null || confCode == null) {
            return false;
        }
        if (flightSource == null || flightDest == null) {
            return addReminderToDB(time, firstName, lastName, confCode);
        }
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COL_FNAME, firstName);
        cv.put(MyDBHelper.COL_LNAME, lastName);
        cv.put(MyDBHelper.COL_CONF_CODE, confCode);
        cv.put(MyDBHelper.COL_TIME, time);
        cv.put(MyDBHelper.COL_DISPLAY_TIME, dTime);
        cv.put(MyDBHelper.COL_FROM_PLACE, flightSource);
        cv.put(MyDBHelper.COL_DEST_PLACE, flightDest);
        getContentResolver().insert(EventProvider.AUTH_URI, cv);
        return true;
    }

    /**
     * Takes a textview id and returns the text in it (or null)
     *
     * @param id Id of textview
     * @return text in view or null
     */
    private String getTextViewValueById(int id) {
        String returnValue = null;
        View v = findViewById(id);
        if (v != null && v instanceof TextView) {
            CharSequence tempVal = ((TextView) v).getText();
            if (tempVal != null && tempVal.length() > 1) {
                returnValue = tempVal.toString();
            }
        }
        return returnValue;
    }

    /**
     * Starts up a dialogfragment to choose departure/return date and sets the edittext to that
     *
     * @param dateView
     */
    public void pickDate(View dateView) {
        DialogFragment newFragment = new DatePickerFrag();
        Bundle args = new Bundle();
        args.putInt(DatePickerFrag.ARG_VIEW_ID, dateView.getId());
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Starts up a dialogfragment to choose departure/return time and sets the edittext to that
     *
     * @param timeView
     */
    public void pickTime(View timeView) {
        Bundle args = new Bundle();
        args.putInt(TimePickerFrag.ARG_VIEW_ID, timeView.getId());
        DialogFragment timePickerFragment = new TimePickerFrag();
        timePickerFragment.setArguments(args);
        timePickerFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void OnTimeSet(int viewId, int hour, int minute) {
        getFlightFrag().updateTimeView(viewId, hour, minute);
    }

    @Override
    public void OnDateSet(int viewId, int year, int month, int day) {
        getFlightFrag().updateDateView(viewId, year, month, day);
    }

    /**
     * Refreshed the flight list frag so the user can see updates
     */
    private void refreshFlightListFrag() {
        FlightListFragment currentFragment = (FlightListFragment) findFragmentByPosition(1);
        currentFragment.resetList();
    }

    /**
     * finds a frag in the given position
     *
     * @param position
     * @return
     */
    public Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mSectionsPagerAdapter;
        return getFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":"
                        + fragmentPagerAdapter.getItemId(position));
    }

    /**
     * gets the current AddNewFlightFragment
     *
     * @return
     */
    private AddNewFlightFragment getFlightFrag() {
        return (AddNewFlightFragment) findFragmentByPosition(0);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final static int PAGE_COUNT = 2;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a AddNewFlightFragment (defined as a static inner class below).
            Fragment frag;
            switch (position) {
                case 0:
                    frag = findFragmentByPosition(0);
                    if (frag == null) {
                        return AddNewFlightFragment.newInstance(position + 1);
                    } else {
                        return frag;
                    }
                case 1:
                    frag = findFragmentByPosition(1);
                    if (frag == null) {
                        return FlightListFragment.newInstance();
                    } else {
                        return frag;
                    }
            }
            return AddNewFlightFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
            }
            return null;
        }
    }
}

