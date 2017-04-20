package biz.ajoshi.t1401654727.checkin;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;
import biz.ajoshi.t1401654727.checkin.ui.frag.AddNewFlightFragment;
import biz.ajoshi.t1401654727.checkin.ui.frag.FlightListFragment;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.DatePickerFrag;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.TimePickerFrag;

/**
 * Main activity for this app. Holds viewpager for the various fragments
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener,
        TimePickerFrag.OnTimeSetListener,
        DatePickerFrag.OnDateSetListener {

    private final static String EXTRA_SHOW_LIST = "biz.ajoshi.t1401654727.checkin.MainActivity";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cleanUpDB();

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        // I use a Holo theme, so NPE shouldn't happen
        // Also, I know this is deprecated; it wasn't when I put it in.
        //TODO try to replace
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

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
        AlarmUtils.resetAlarm(this, true);
        // Now see if we were launched by notification
        Intent i = getIntent();
        if (i != null && i.getBooleanExtra(EXTRA_SHOW_LIST, false)) {
            mViewPager.setCurrentItem(SectionsPagerAdapter.INDEX_OF_FLIGHT_LIST);
        }
//        getWindow().setBackgroundDrawable(null);
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
     * Deletes old flights
     */
    public void cleanUpDB() {
        long timeNow = Calendar.getInstance().getTimeInMillis();
        getContentResolver().delete(EventProvider.AUTH_URI, MyDBHelper.COL_TIME + " < " + timeNow, null);
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
 // TODO deletable with this class     * finds a frag in the given position
     *
     * @param position
     * @return
     */
    public Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mSectionsPagerAdapter;
        return getSupportFragmentManager().findFragmentByTag(
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
     * Creates an inte // TODO deletable with this classnt that shows the flight list upon launch
     *
     * @param ctx Context to use to create intent
     * @return
     */
    public static Intent getListIntent(Context ctx) {
        Intent listIntent = new Intent(ctx, MainActivity.class);
        listIntent.putExtra(EXTRA_SHOW_LIST, true);
        return listIntent;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final static int PAGE_COUNT = 2;
        public final static int INDEX_OF_FLIGHT_LIST = 1;
        public final static int INDEX_OF_FLIGHT_ENTRY = 0;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a AddNewFlightFragment (defined as a static inner class below).
            Fragment frag;
            switch (position) {
                case INDEX_OF_FLIGHT_ENTRY:
                    frag = findFragmentByPosition(INDEX_OF_FLIGHT_ENTRY);
                    if (frag == null) {
                        return AddNewFlightFragment.newInstance(position + 1);
                    } else {
                        return frag;
                    }
                case INDEX_OF_FLIGHT_LIST:
                    frag = findFragmentByPosition(INDEX_OF_FLIGHT_LIST);
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

