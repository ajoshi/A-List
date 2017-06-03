package biz.ajoshi.t1401654727.checkin;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import biz.ajoshi.t1401654727.checkin.ui.frag.AddNewFlightFragment;
import biz.ajoshi.t1401654727.checkin.ui.frag.FlightListFragment;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.DatePickerFrag;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.TimePickerFrag;

/**
 * Main activity for this app. Shows the list and the add flight fragments
 */
public class LaunchActivity extends FragmentActivity implements TimePickerFrag.OnTimeSetListener,
        DatePickerFrag.OnDateSetListener, AddNewFlightFragment.NewFlightAddedListener, FlightListFragment.NewFlightAdder {

    FlightListFragment listFragment;
    AddNewFlightFragment newFlightFrag;

    private static final String TAG_LIST_FRAG = "flightList";
    private static final String TAG_NEW_FLIGHTS_FRAG = "addNewFlights";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        listFragment = (FlightListFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAG);
        newFlightFrag = (AddNewFlightFragment) getSupportFragmentManager().findFragmentByTag(TAG_NEW_FLIGHTS_FRAG);
        if (listFragment == null) {
            listFragment = FlightListFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.frag, listFragment, TAG_LIST_FRAG).commit();
        }
        AlarmUtils.resetAlarm(this);
    }

    @Override
    public void OnTimeSet(int viewId, int hour, int minute) {
        newFlightFrag.updateTimeView(viewId, hour, minute);
    }

    @Override
    public void OnDateSet(int viewId, int year, int month, int day) {
        newFlightFrag.updateDateView(viewId, year, month, day);
    }

    /**
     * Refreshed the flight list frag so the user can see updates
     */
    private void refreshFlightListFrag() {
        FlightListFragment currentFragment = listFragment;
        currentFragment.resetList();
    }

    @Override
    public void insertToContentResolver(Uri uri, ContentValues cv) {
        getContentResolver().insert(uri, cv);
    }

    @Override
    public void onNewFlightAdded() {
        refreshFlightListFrag();
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void addNewFlight() {
        // if we use the same instance of newFlightFrag, then user can never clear out the entire form
        newFlightFrag = AddNewFlightFragment.newInstance(0);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frag, newFlightFrag, TAG_NEW_FLIGHTS_FRAG)
                .addToBackStack(null).commit();
    }

    /**
     * Creates an intent to launch this activity
     *
     * @param ctx Context to use to create intent
     * @return
     */
    public static Intent getLaunchIntent(Context ctx) {
        Intent listIntent = new Intent(ctx, LaunchActivity.class);
        return listIntent;
    }

}
