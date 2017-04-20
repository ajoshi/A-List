package biz.ajoshi.t1401654727.checkin;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        Fragment currentFrag = getSupportFragmentManager().findFragmentById(R.id.frag);
        if (currentFrag == null || listFragment == null) {
            listFragment = FlightListFragment.newInstance();
            newFlightFrag = AddNewFlightFragment.newInstance(0);
            getSupportFragmentManager().beginTransaction().add(R.id.frag, listFragment).commit();
        }
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frag, newFlightFrag)
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
