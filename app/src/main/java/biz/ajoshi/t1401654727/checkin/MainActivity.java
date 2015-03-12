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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.frags.DatePickerFrag;
import biz.ajoshi.t1401654727.checkin.frags.FlightListFragment;
import biz.ajoshi.t1401654727.checkin.frags.TimePickerFrag;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;
import biz.ajoshi.t1401654727.checkin.services.SWQuerier;


public class MainActivity extends Activity implements ActionBar.TabListener, TimePickerFrag.OnTimeSetListener, DatePickerFrag.OnDateSetListener, FlightListFragment.OnFragmentInteractionListener {

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
     * Holds departure date and time
     */
    Calendar departCal;
    /**
     * Holds return date and time
     */
    Calendar returnCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
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
        departCal = Calendar.getInstance();
        returnCal = Calendar.getInstance();
        resetAlarm();
    }

    /**
     * Deletes old flights
     */
    public void cleanUpDB() {
       long timeNow = System.currentTimeMillis();
        getContentResolver().delete(EventProvider.authUri, MyDBHelper.COL_TIME + " < " + timeNow, null);
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

    public void createReminder(View view) {
        //This is where I set the alarm for the service
        Toast.makeText(this, "Dep: " + departCal.get(Calendar.YEAR) + ":" +
                departCal.get(Calendar.MONTH) + ":" + departCal.get(Calendar.DATE) + " at " +
                departCal.get(Calendar.HOUR_OF_DAY) + ":" + departCal.get(Calendar.MINUTE) ,
                Toast.LENGTH_SHORT).show();

        long departMillis = departCal.getTimeInMillis();
        long returnMillis = returnCal.getTimeInMillis(); // this one is technically allowed to be 0

        Log.e("SWCheckin","departMillis " + departMillis);
        Log.e("SWCheckin","returnMillis " + returnMillis);

        addReminderToDB(departMillis, getTextViewValueById(R.id.firstName),
                getTextViewValueById(R.id.lastName), getTextViewValueById(R.id.confNum));
        addReminderToDB(returnMillis, getTextViewValueById(R.id.firstName),
                getTextViewValueById(R.id.lastName), getTextViewValueById(R.id.confNum));

        //reset the frag
        FlightListFragment currentFragment = (FlightListFragment) findFragmentByPosition(1);
        currentFragment.resetList();
        resetAlarm();
/*
Because of http://developer.android.com/reference/android/content/Intent.html#filterEquals(android.content.Intent)
returnpIntent will override departpIntent.
So either I change their  action, data, type, class, and categories (extras don't count) or I go a
diff direction and store all these things in a db right now. Then I set an alarm for the soonest
event. I then read DB, figure out what's the soonest one is, and check in to that. THEN I set up an
alarm for the next event.
 */
//        PendingIntent departpIntent = PendingIntent.getBroadcast();
//        am.setExact(AlarmManager.RTC_WAKEUP, departMillis, departpIntent);
//        PendingIntent returnpIntent;
//        am.setExact(AlarmManager.RTC_WAKEUP, departMillis, returnpIntent);
//        addReminderToDB();
//        resetAlarm();
    }
    public Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mSectionsPagerAdapter;
        return getFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":"
                        + fragmentPagerAdapter.getItemId(position));
    }
    /**
     * Inserts new data into the DB
     * @param time Time in ms when this flight takes off
     * @param firstName first name
     * @param lastName last name
     * @param confCode confirmation code
     */
    private void addReminderToDB(long time, String firstName, String lastName, String confCode) {
        if (firstName == null || lastName == null || confCode == null) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COL_FNAME, firstName);
        cv.put(MyDBHelper.COL_LNAME, lastName);
        cv.put(MyDBHelper.COL_CONF_CODE, confCode);
        cv.put(MyDBHelper.COL_TIME, time);
        getContentResolver().insert(EventProvider.authUri, cv);
    }

    private void setAlarm(long time, String fName, String lName, String cCode, String id) {
        AlarmManager am =  (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0,
                SWQuerier.IntentForActionFoo(this, fName, lName, cCode, id), 0);
        long alarmTime = time - 86399999; //86400000 is one day before
        am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        Log.e("SWCheckin","alarm time " + alarmTime);
        //TODO set the correct pintent here
    }

    /**
     * Sets the single alarm we have to go off when the earliest event is scheduled for
     */
    private void resetAlarm() {
       Cursor c = getContentResolver().query(Uri.withAppendedPath(EventProvider.authUri,
               EventProvider.FIRST), new String[] {MyDBHelper.COL_TIME, MyDBHelper.COL_FNAME,
               MyDBHelper.COL_LNAME, MyDBHelper.COL_CONF_CODE, MyDBHelper.COL_ID}, MyDBHelper.COL_DONE + "=?", new String [] {"0"}, null);
       if (c != null) {
           if (c.moveToFirst()) {
               Log.w("swcheckin", "setting alarm for " + c.getLong(0));
               setAlarm(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
               c.close();
               return;
           }
       }
       Toast.makeText(this, "Unable to set timer!", Toast.LENGTH_SHORT);
    }

    /**
     * Takes a textview id and returns the text in it (or null)
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
     * @param a
     */
    public void pickDate(View a) {
        TextView b = (TextView) a;
        DialogFragment newFragment = new DatePickerFrag(b);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Starts up a dialogfragment to choose departure/return time and sets the edittext to that
     * @param a
     */
    public void pickTime(View a) {
        TextView b = (TextView) a;
        DialogFragment newFragment = new TimePickerFrag(b);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void OnTimeSet(TextView view, int hour, int minute) {
        view.setText(hour + ":" + minute);
        int viewId = view.getId();
        switch (viewId) {
            case R.id.departureTime:
                departCal.set(Calendar.HOUR_OF_DAY, hour);
                departCal.set(Calendar.MINUTE, minute);
                departCal.set(Calendar.SECOND, 0);
                break;
            case R.id.returnTime:
                returnCal.set(Calendar.HOUR_OF_DAY, hour);
                returnCal.set(Calendar.MINUTE, minute);
                returnCal.set(Calendar.SECOND, 0);
                break;
        }
    }

    @Override
    public void OnDateSet(TextView view, int year, int month, int day) {
        view.setText(day + ":" + month + ":" + year);
        int viewId = view.getId();
        switch (viewId) {
            case R.id.departureDate:
                departCal.set(Calendar.YEAR, year);
                departCal.set(Calendar.MONTH, month);
                departCal.set(Calendar.DAY_OF_MONTH, day);
                view.setText(day + " " +
                        departCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + ", " + year);
                break;
            case R.id.returnDate:
                returnCal.set(Calendar.YEAR, year);
                returnCal.set(Calendar.MONTH, month);
                returnCal.set(Calendar.DAY_OF_MONTH, day);
                view.setText(day + " " +
                        returnCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + ", " + year);
                break;
        }
    }

    @Override
    public void onFragmentInteraction(String id) {
        //?? do what?
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
            switch(position) {
                case 0:
                    return AddNewFlightFragment.newInstance(position + 1);
                case 1:
                    return FlightListFragment.newInstance();
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

    /**
     * A fragment that allows user to add a flight reminder
     */
    public static class AddNewFlightFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static AddNewFlightFragment newInstance(int sectionNumber) {
            //TODO change this when I add the lookup fragment
            AddNewFlightFragment fragment = new AddNewFlightFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public AddNewFlightFragment() {
        }
        private static String makeFragmentName(int viewId, int position)
        {
            return "android:switcher:" + viewId + ":" + position;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
          //  TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //    textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}
