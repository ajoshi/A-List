package biz.ajoshi.t1401654727.checkin.frags;

/**
 * Created by Aditya on 3/8/2015.
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import biz.ajoshi.t1401654727.checkin.R;

/**
 * A fragment that allows user to add a flight reminder
 */
public class AddNewFlightFragment extends Fragment {
    public static final String PREF_FIRST_NAME = "biz.ajoshi.t1401654727.checkin.pref.firstName";
    public static final String PREF_LAST_NAME = "biz.ajoshi.t1401654727.checkin.pref.lastName";
    public static final String PREF_NAMES_FILENAME = "biz.ajoshi.t1401654727.checkin";
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String STATE_DEPARTURE_TIME = "depTime";
    private static final String STATE_RETURN_TIME = "retTime";

    /**
     * Holds departure date and time
     */
    Calendar departCal;
    /**
     * Holds return date and time
     */
    Calendar returnCal;

    public AddNewFlightFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AddNewFlightFragment newInstance(int sectionNumber) {
        AddNewFlightFragment fragment = new AddNewFlightFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // autopopulate name based on last entry
        // could use acctmgr, but that's just more permissions
        SharedPreferences prefs = getActivity().getSharedPreferences(
                PREF_NAMES_FILENAME, Context.MODE_PRIVATE);
        String fName = prefs.getString(PREF_FIRST_NAME, null);
        String lName = prefs.getString(PREF_LAST_NAME, null);
        if (fName != null && lName != null) {
            TextView fNameView = (TextView) rootView.findViewById(R.id.firstName);
            TextView lNameView = (TextView) rootView.findViewById(R.id.lastName);
            if (fNameView != null && lNameView != null) {
                fNameView.setText(fName);
                lNameView.setText(lName);
            }
        }
        departCal = Calendar.getInstance();
        returnCal = Calendar.getInstance();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_DEPARTURE_TIME, departCal.getTimeInMillis());
        outState.putLong(STATE_RETURN_TIME, returnCal.getTimeInMillis());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            departCal.setTimeInMillis(savedInstanceState.getLong(STATE_DEPARTURE_TIME));
            returnCal.setTimeInMillis(savedInstanceState.getLong(STATE_RETURN_TIME));
            Activity act = getActivity();
            if (act != null) {
                // can hosting act even be null here? maybe if user quickly exits or if app crashes?
                updateDateViewAndTimeView(departCal.getTimeInMillis(), returnCal.getTimeInMillis());
            }
        }
    }

    /**
     * Updates dates and times for the entire fragment if we have all the calendar data
     *
     * @param departTime
     * @param returnTime
     */
    public void updateDateViewAndTimeView(long departTime, long returnTime) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTimeInMillis(departTime);
        setDateTimeViews(dateFormat, timeFormat, tempCal, R.id.departureTime, R.id.departureDate);
        if (returnTime > departTime) {
            tempCal.setTimeInMillis(returnTime);
            setDateTimeViews(dateFormat, timeFormat, tempCal, R.id.returnTime, R.id.returnDate);
        }
    }

    private void setDateTimeViews(DateFormat dateFormat, DateFormat timeFormat, Calendar tempCal, int timeId, int dateId) {
        TextView view = (TextView) getActivity().findViewById(timeId);
        if (view != null) {
            view.setText(timeFormat.format(tempCal.getTime()));
        }
        view = (TextView) getActivity().findViewById(dateId);
        if (view != null) {
            view.setText(dateFormat.format(tempCal.getTime()));
        }
    }

    /**
     * Updates a a date view with the given date
     *
     * @param viewId
     * @param year
     * @param month
     * @param day
     */
    public void updateDateView(int viewId, int year, int month, int day) {
        TextView view = (TextView) getActivity().findViewById(viewId);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        switch (viewId) {
            case R.id.departureDate:
                departCal.set(Calendar.YEAR, year);
                departCal.set(Calendar.MONTH, month);
                departCal.set(Calendar.DAY_OF_MONTH, day);
                view.setText(dateFormat.format(departCal.getTime()));
                break;
            case R.id.returnDate:
                returnCal.set(Calendar.YEAR, year);
                returnCal.set(Calendar.MONTH, month);
                returnCal.set(Calendar.DAY_OF_MONTH, day);
                view.setText(dateFormat.format(returnCal.getTime()));
                break;
        }
    }

    /**
     * Updates a time view with the given time
     *
     * @param viewId
     * @param hour
     * @param minute
     */
    public void updateTimeView(int viewId, int hour, int minute) {
        TextView view = (TextView) getActivity().findViewById(viewId);
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        switch (viewId) {
            case R.id.departureTime:
                departCal.set(Calendar.HOUR, hour);
                departCal.set(Calendar.MINUTE, minute);
                view.setText(dateFormat.format(departCal.getTime()));
                break;
            case R.id.returnTime:
                returnCal.set(Calendar.HOUR, hour);
                returnCal.set(Calendar.MINUTE, minute);
                view.setText(dateFormat.format(returnCal.getTime()));
                break;
        }
    }

    public Calendar getDepartCal() {
        return departCal;
    }

    public Calendar getReturnCal() {
        return returnCal;
    }

}