package biz.ajoshi.t1401654727.checkin.ui.frag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import biz.ajoshi.t1401654727.checkin.FlightListElement;
import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;
import biz.ajoshi.t1401654727.checkin.services.SWCheckinService;
import biz.ajoshi.t1401654727.checkin.ui.FlightRecycleViewCursorAdapter;

/**
 * A fragment representing a list of flights.
 */
public class FlightListFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>, FlightRecycleViewCursorAdapter.FlightItemClickListener {

    private final static int LOADER_ID_LOAD_FLIGHT_LIST = 1;
    /**
     * The Adapter which will be used to populate the ListView with
     * Views.
     */
    private FlightRecycleViewCursorAdapter mAdapter;
    RecyclerView mRecyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FlightListFragment() {
    }

    public static FlightListFragment newInstance() {
        FlightListFragment fragment = new FlightListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID_LOAD_FLIGHT_LIST, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flight, container, false);
        mAdapter = new FlightRecycleViewCursorAdapter(null, getActivity(), (TextView) view.findViewById(R.id.no_flights_message), this);
        mAdapter.setHasStableIds(true);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.flight_recycler_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    /**
     * Given a FlightListElement, extracts the String to be shown as title of the onclick dialog
     * @param data FlightListElement containing the data for this element
     * @return String title of click dialog
     */
    private String getTitleFromListItem(FlightListElement data) {
        String title;
        String timeString = data.displayTime; /// dateFormat.format(cal.getTime());
        boolean checkedIn = data.hasCheckedIn;
        if (checkedIn) {
            //time, origin, destination, position, gate
            title = getString(R.string.flight_detail_dialog_title_checked_in, timeString,
                    data.origin,
                    data.destination,
                    data.position,
                    data.gate
            );
        } else {
            // time, origin, destination
            int attempts = data.attempts;
            if (attempts > 0) {
                // we've  tried to check in before, but failed
                String titlePart1 = getString(R.string.flight_detail_dialog_title_not_checked_in, timeString,
                        data.origin,
                        data.destination);
                String titlePart2 = getString(R.string.flight_detail_dialog_title_auto_checkin_failed, attempts);
                title = getString(R.string.flight_detail_dialog_title_not_checked_in_auto_checkin_failed, titlePart1, titlePart2);
            } else {
                String origin = data.origin;
                String dest = data.destination;
                if (origin == null || dest == null) {
                    title = getString(R.string.flight_detail_dialog_title_not_checked_in_no_locations, timeString);
                } else {
                    title = getString(R.string.flight_detail_dialog_title_not_checked_in, timeString,
                            origin, dest);
                }
            }
        }
        return title;
    }

    public void resetList() {
        getLoaderManager().restartLoader(LOADER_ID_LOAD_FLIGHT_LIST, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (id == LOADER_ID_LOAD_FLIGHT_LIST) {
            return new CursorLoader(getActivity(), EventProvider.AUTH_URI,
                    new String[]{MyDBHelper.COL_ID, MyDBHelper.COL_TIME, MyDBHelper.COL_FNAME,
                            MyDBHelper.COL_LNAME, MyDBHelper.COL_CONF_CODE, MyDBHelper.COL_DONE,
                            MyDBHelper.COL_FROM_PLACE, MyDBHelper.COL_DEST_PLACE, MyDBHelper.COL_DISPLAY_TIME,
                            MyDBHelper.COL_ATTEMPTS, MyDBHelper.COL_GATE, MyDBHelper.COL_POSITION}, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == LOADER_ID_LOAD_FLIGHT_LIST) {
            if (cursor != null && cursor.moveToFirst()) {
                mAdapter.swapCursor(cursor);
            }
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        if (loader.getId() == LOADER_ID_LOAD_FLIGHT_LIST) {
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void flightItemClicked(FlightListElement element, final int index) {
        final String title = getTitleFromListItem(element);

        final String flightId = element.id;
        final String fname = element.fName;
        final String lname = element.lName;
        final String ccode = element.confCode;
        // right now use the standard 2 button dialog, use custom layout if anything else can be done

        DialogFragment deleteOrCheckinDialog = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                // Use the Builder class for convenient dialog construction
                final Activity act = getActivity();
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder.setMessage(title)
                        .setPositiveButton(R.string.flight_detail_dialog_delete_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Delete this entry
                                //TODO confirmation dialog. yay, dialog spam
                                act.getContentResolver().delete(EventProvider.AUTH_URI, MyDBHelper.COL_ID + " = " + flightId, null);
                                resetList();
                            }
                        })
                        .setNegativeButton(R.string.flight_detail_dialog_force_checkin_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // force check in for given id
                                Intent checkinViaWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(SWCheckinService.SOUTHWEST_CHECKIN_URL1, ccode, fname, lname)));
                                startActivity(checkinViaWebIntent);
                            }
                        });
                // Create the AlertDialog object and return it
                return builder.create();
            }
        };
        deleteOrCheckinDialog.show(getFragmentManager(), "FlightListFragment.deleteOrCheckinDialog");
    }
}
