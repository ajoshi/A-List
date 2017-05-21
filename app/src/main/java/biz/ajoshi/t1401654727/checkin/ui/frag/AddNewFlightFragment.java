package biz.ajoshi.t1401654727.checkin.ui.frag;

/**
 * Created by Aditya on 3/8/2015.
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import biz.ajoshi.t1401654727.checkin.AlarmUtils;
import biz.ajoshi.t1401654727.checkin.CityTimezoneTuple;
import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.DatePickerFrag;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.TimePickerFrag;

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
    /*
     * For saving and retrieving state on rotation
     */
    private static final String STATE_DEPARTURE_TIME = "depTime";
    private static final String STATE_RETURN_TIME = "retTime";
    private static final String STATE_DEPARTURE_TZ = "depTZ";
    private static final String STATE_RETURN_TZ = "retTZ";
    private static final String CITY_TIMEZONE_SEPARATOR = "\\|";
    public static final int TIMEZONE_INDEX = 0;
    public static final int CITY_INDEX = 1;
    public static final int MAX_INDEX = CITY_INDEX;

    public interface NewFlightAddedListener {
        void insertToContentResolver(Uri uri, ContentValues cv);
        void onNewFlightAdded();
    }
    NewFlightAddedListener newFlightCallbacks;

    /**
     * Holds departure date and time
     */
    Calendar departCal;
    /**
     * Holds return date and time
     */
    Calendar returnCal;
    /**
     * I can't store the tz in the calendar because if I do store it in the cal, then getting a
     * timeformat with default locale will change the displayed time
     */
    TimeZone departTz;
    TimeZone returnTz;
    /**
     * Array of timezones so I can use them in the AutocompleteView's clickhandler
     */
    final TimeZone[] timeZones = new TimeZone[2];
    static final int TZ_DEPART_INDEX = 0;
    static final int TZ_RETURN_INDEX = 1;

    /**
     * Holds City and time zone tuples for all possible destinations. Use loader if list gets too long
     */
    ArrayList<CityTimezoneTuple> tupleList;

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
        View rootView = inflater.inflate(R.layout.fragment_add_flight, container, false);
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
        View submitButton = rootView.findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createReminder(view);
            }
        });

        departCal = Calendar.getInstance();
        returnCal = Calendar.getInstance();
        timeZones[TZ_DEPART_INDEX] = departCal.getTimeZone();
        timeZones[TZ_RETURN_INDEX] = returnCal.getTimeZone();
        departTz = departCal.getTimeZone();
        returnTz = returnCal.getTimeZone();

        setupCityAndTZView(rootView, R.id.departLoc, R.id.departureTZ, timeZones, TZ_DEPART_INDEX);
        setupCityAndTZView(rootView, R.id.arriveLoc, R.id.returnTZ, timeZones, TZ_RETURN_INDEX);
        setDateClickListener(rootView, R.id.returnDate);
        setDateClickListener(rootView, R.id.departureDate);
        setTimeClickListener(rootView, R.id.returnTime);
        setTimeClickListener(rootView, R.id.departureTime);
        return rootView;
    }

    private void setDateClickListener(View rootView, int id) {
        rootView.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDate(view);
            }
        });
    }

    private void setTimeClickListener(View rootView, int id) {
        rootView.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickTime(view);
            }
        });
    }

    /**
     * Attaches the city adapter and click listener to the given AutoCompleteTextView view id.
     * The click listener populates the given timezone view id and the timezone is stored in the
     * tz array at the provided index
     *
     * @param rootView               View containing the AutocompleteTextView and the timezone TextView
     * @param autoCompleteTextViewId id of the AutocompleteTextView
     * @param tzTextViewId           id of the TextView showing the timezone
     * @param tz                     Array of TimeZones to store the timezone for the chosen city in
     * @param tzIndex                location of the Timezone in the tz array
     */
    private void setupCityAndTZView(final View rootView, final int autoCompleteTextViewId, final int tzTextViewId, final TimeZone[] tz, final int tzIndex) {
        AutoCompleteTextView a = (AutoCompleteTextView) rootView.findViewById(autoCompleteTextViewId);
        ArrayAdapter <CityTimezoneTuple> cityListAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                getTupleList(R.array.city_name_list));
        a.setAdapter(cityListAdapter);
        a.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityTimezoneTuple selection = (CityTimezoneTuple) parent.getItemAtPosition(position);
                final TextView tv = (TextView) rootView.findViewById(tzTextViewId);
                tz[tzIndex] = TimeZone.getTimeZone(selection.tz);
                tv.setText(tz[tzIndex].getDisplayName(false, TimeZone.SHORT));
//                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
            }
        });
    }



    /**
     * Starts up a dialogfragment to choose departure/return date and sets the edittext to that
     *
     * @param dateView
     */
    public void pickDate(View dateView) {
        hideKeyboard(dateView);
        DialogFragment newFragment = new DatePickerFrag();
        Bundle args = new Bundle();
        args.putInt(DatePickerFrag.ARG_VIEW_ID, dateView.getId());
        newFragment.setArguments(args);
        newFragment.show(getChildFragmentManager(), "datePicker");
    }

    private void hideKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Starts up a dialogfragment to choose departure/return time and sets the edittext to that
     *
     * @param timeView
     */
    public void pickTime(View timeView) {
        hideKeyboard(timeView);
        Bundle args = new Bundle();
        args.putInt(TimePickerFrag.ARG_VIEW_ID, timeView.getId());
        DialogFragment timePickerFragment = new TimePickerFrag();
        timePickerFragment.setArguments(args);
        timePickerFragment.show(getFragmentManager(), "timePicker");
    }


    /**
     * Gets ArrayList of city names for use in Origin/Destination list
     *
     * @param arrayResId Resource Id of the array that holds the data in TimeZone|City format
     * @return ArrayList of city names
     */
    public ArrayList<CityTimezoneTuple> getTupleList(int arrayResId) {
        if (tupleList != null) {
            return tupleList;
        }
        String[] cityTZList = getResources().getStringArray(arrayResId);
        if (cityTZList == null) {
            return null;
        }
        getCityAndtimeZoneList(cityTZList);
        return tupleList;
    }

    /**
     * Given an array of Strings in Timezone|City format, populates the timezone and city arraylists
     */
    private void getCityAndtimeZoneList(String[] cityTZList) {
        String[] tempArray;
        int arraySize = cityTZList.length;
        tupleList = new ArrayList<>(arraySize);
        for (String tzAndCity : cityTZList) {
            tempArray = tzAndCity.split(CITY_TIMEZONE_SEPARATOR);
            if (tempArray.length > MAX_INDEX) {
                tupleList.add(new CityTimezoneTuple((tempArray[CITY_INDEX]), (tempArray[TIMEZONE_INDEX])));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_DEPARTURE_TIME, departCal);
        outState.putSerializable(STATE_RETURN_TIME, returnCal);
        outState.putSerializable(STATE_DEPARTURE_TZ, timeZones[TZ_DEPART_INDEX]);
        outState.putSerializable(STATE_RETURN_TZ, timeZones[TZ_RETURN_INDEX]);
    }

    @Override
    public void onAttach(Activity ctx) {
        if (!(ctx instanceof NewFlightAddedListener)) {
            throw new ClassCastException("Parent must implement NewFlightAdder");
        }
        newFlightCallbacks = (NewFlightAddedListener) ctx;
        super.onAttach(ctx);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            departCal = (Calendar) savedInstanceState.getSerializable(STATE_DEPARTURE_TIME);
            returnCal = (Calendar) savedInstanceState.getSerializable(STATE_RETURN_TIME);
            timeZones[TZ_DEPART_INDEX] = (TimeZone) savedInstanceState.getSerializable(STATE_DEPARTURE_TZ);
            timeZones[TZ_RETURN_INDEX] = (TimeZone) savedInstanceState.getSerializable(STATE_RETURN_TZ);
            Activity act = getActivity();
            if (act != null) {
                // can hosting act even be null here? maybe if user quickly exits or if app crashes?
                updateDateTimeAndTZViews(departCal, returnCal, timeZones[TZ_DEPART_INDEX], timeZones[TZ_RETURN_INDEX]);
            }
        }
    }

    /**
     * Updates dates and times for the entire fragment if we have all the calendar data
     *
     * @param departTime
     * @param returnTime
     */
    public void updateDateTimeAndTZViews(Calendar departTime, Calendar returnTime, TimeZone depTz, TimeZone retTz) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        setDateTimeViews(dateFormat, timeFormat, departTime, depTz, R.id.departureTime, R.id.departureDate, R.id.departureTZ);
        if (returnTime.getTimeInMillis() > departTime.getTimeInMillis()) {
            setDateTimeViews(dateFormat, timeFormat, returnTime, retTz, R.id.returnTime, R.id.returnDate, R.id.returnTZ);
        }
    }

    /**
     * Sets the date and time for the given date and time views
     *
     * @param dateFormat
     * @param timeFormat
     * @param tempCal
     * @param timeViewId
     * @param dateViewId
     */
    private void setDateTimeViews(DateFormat dateFormat, DateFormat timeFormat, Calendar tempCal,
                                  TimeZone tz, int timeViewId, int dateViewId, int tzViewId) {
        Activity currentActivity = getActivity();
        TextView view = (TextView) currentActivity.findViewById(timeViewId);
        if (view != null) {
            view.setText(timeFormat.format(tempCal.getTime()));
        }
        view = (TextView) currentActivity.findViewById(dateViewId);
        if (view != null) {
            view.setText(dateFormat.format(tempCal.getTime()));
        }
        view = (TextView) currentActivity.findViewById(tzViewId);
        if (view != null) {
            view.setText(tz.getDisplayName(false, TimeZone.SHORT));
        }
    }

    /**
     * Updates a date view with the given date
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
                departCal.set(Calendar.HOUR_OF_DAY, hour);
                departCal.set(Calendar.MINUTE, minute);
                view.setText(dateFormat.format(departCal.getTime()));
                break;
            case R.id.returnTime:
                returnCal.set(Calendar.HOUR_OF_DAY, hour);
                returnCal.set(Calendar.MINUTE, minute);
                view.setText(dateFormat.format(returnCal.getTime()));
                break;
        }
    }

    /**
     * Gets the flight departure time in milliseconds after performing timezone correction
     *
     * @return departure time in milliseconds
     */
    public long getDepartCalMillis() {
        long timeInMS = departCal.getTimeInMillis();
        // depart time needs to have its timezone offset removed, otherwise we end up setting the
        // alarm for the wrong time
        int localOffset = Calendar.getInstance().getTimeZone().getOffset(timeInMS);
        int targetOffset = TimeZone.getTimeZone(timeZones[TZ_DEPART_INDEX].getID()).getOffset(timeInMS);
        int difference = localOffset - targetOffset;
        return timeInMS + (difference);
    }

    /**
     * Gets the flight return time in milliseconds after performing timezone correction
     *
     * @return departure time in milliseconds
     */
    public long getReturnCalMillis() {
        long timeInMS = returnCal.getTimeInMillis();
        // return time needs to have its timezone offset removed, otherwise we end up setting the
        // alarm for the wrong time
        int localOffset = Calendar.getInstance().getTimeZone().getOffset(timeInMS);
        int targetOffset = TimeZone.getTimeZone(timeZones[TZ_RETURN_INDEX].getID()).getOffset(timeInMS);
        int difference = localOffset - targetOffset;
        return timeInMS + (difference);
    }

    /**
     * Returns the display string of the flight departure time
     *
     * @param dateFormat DateFormat to use to display the departure time
     * @return Date, Time, and Timezone of the departing flight
     */
    public String getDepartTimeString(DateFormat dateFormat) {
        return getString(R.string.flight_list_timestamp_format,
                dateFormat.format(departCal.getTime()),
                timeZones[TZ_DEPART_INDEX].getDisplayName(false, TimeZone.SHORT));
    }

    /**
     * Returns the display string of the flight return time
     *
     * @param dateFormat DateFormat to use to display the return time
     * @return Date, Time, and Timezone of the returning flight
     */
    public String getReturnTimeString(DateFormat dateFormat) {
        return getString(R.string.flight_list_timestamp_format,
                dateFormat.format(returnCal.getTime()),
                timeZones[TZ_RETURN_INDEX].getDisplayName(false, TimeZone.SHORT));
    }

    public void createReminder(View view) {
        Activity currentActivity = getActivity();
        if (currentActivity == null) {
            return;
        }
        view = (View)view.getParent();
        String firstName = getTextViewValueById(view, R.id.firstName);
        String lastName = getTextViewValueById(view, R.id.lastName);
        // Store the names in prefs so next time hopefully the user won't have to type in their name
        SharedPreferences.Editor editor = currentActivity.getSharedPreferences(AddNewFlightFragment
                .PREF_NAMES_FILENAME, Context.MODE_PRIVATE).edit();
        editor.putString(AddNewFlightFragment.PREF_FIRST_NAME, firstName);
        editor.putString(AddNewFlightFragment.PREF_LAST_NAME, lastName);
        editor.apply();

        //This is where I set the alarm for the service
        long departMillis = getDepartCalMillis();
        long returnMillis = getReturnCalMillis();

        String confirmationCode = getTextViewValueById(view, R.id.confNum);
        String origin = getTextViewValueById(view, R.id.departLoc);
        String destination = getTextViewValueById(view, R.id.arriveLoc);
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        if (addReminderToDB(departMillis, firstName, lastName, confirmationCode, origin, destination, getDepartTimeString(dateTimeFormat))) {
            if (returnMillis > departMillis) {
                // Only add a return flight if it's after the departure
                // This lets us make sure that we don't set a reminder if there is no return
                addReminderToDB(returnMillis, firstName, lastName, confirmationCode, destination, origin, getReturnTimeString(dateTimeFormat));
                // Check in alarm set for flight at %s on %s and return at %3$s on %4$s
                Toast.makeText(currentActivity, currentActivity.getString(R.string.entry_added_multiple_toast,
                        getTextViewValueById(view, R.id.departureTime),
                        getTextViewValueById(view, R.id.departureDate),
                        getTextViewValueById(view, R.id.returnTime),
                        getTextViewValueById(view, R.id.returnDate)),
                        Toast.LENGTH_LONG).show();
            } else {
                // Check in alarm set for flight at %s on %s
                Toast.makeText(currentActivity, currentActivity.getString(R.string.entry_added_toast,
                        getTextViewValueById(view, R.id.departureTime),
                        getTextViewValueById(view, R.id.departureDate)),
                        Toast.LENGTH_LONG).show();
            }

            //reset the frag
            newFlightCallbacks.onNewFlightAdded();
            AlarmUtils.resetAlarm(currentActivity);
        } else {
            Toast.makeText(currentActivity, R.string.entry_not_added_toast, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Inserts new data into the DB
     *
     * @param time      Time in ms when this flight takes off
     * @param dTime     String form of time to be displayed
     * @param firstName first name
     * @param lastName  last name
     * @param confCode  confirmation code
     * @return true if succeeded, false otherwise
     */
    private boolean addReminderToDB(long time, String dTime, String firstName, String lastName, String confCode) {
        if (firstName == null || lastName == null || confCode == null) {
            return false;
        }

        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COL_FNAME, firstName);
        cv.put(MyDBHelper.COL_LNAME, lastName);
        cv.put(MyDBHelper.COL_CONF_CODE, confCode);
        cv.put(MyDBHelper.COL_TIME, time);
        cv.put(MyDBHelper.COL_DISPLAY_TIME, dTime);
        newFlightCallbacks.insertToContentResolver(EventProvider.AUTH_URI, cv);
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
            return addReminderToDB(time, dTime, firstName, lastName, confCode);
        }
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COL_FNAME, firstName);
        cv.put(MyDBHelper.COL_LNAME, lastName);
        cv.put(MyDBHelper.COL_CONF_CODE, confCode);
        cv.put(MyDBHelper.COL_TIME, time);
        cv.put(MyDBHelper.COL_DISPLAY_TIME, dTime);
        cv.put(MyDBHelper.COL_FROM_PLACE, flightSource);
        cv.put(MyDBHelper.COL_DEST_PLACE, flightDest);
        newFlightCallbacks.insertToContentResolver(EventProvider.AUTH_URI, cv);
        return true;
    }

    /**
     * Takes a textview id and returns the text in it (or null)
     *
     * @param id Id of textview
     * @return text in view or null
     */
    private String getTextViewValueById(View root, int id) {
        String returnValue = null;
        View v = root.findViewById(id);
        if (v != null && v instanceof TextView) {
            CharSequence tempVal = ((TextView) v).getText();
            if (tempVal != null && tempVal.length() > 1) {
                returnValue = tempVal.toString();
            }
        }
        return returnValue;
    }
}