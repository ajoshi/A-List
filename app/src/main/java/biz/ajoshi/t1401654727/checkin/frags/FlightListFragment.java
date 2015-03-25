package biz.ajoshi.t1401654727.checkin.frags;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;
import biz.ajoshi.t1401654727.checkin.services.SWCheckinService;
import biz.ajoshi.t1401654727.checkin.ui.FlightCursorAdapter;

/**
 * A fragment representing a list of flights.
 * <p/>
 * Activities containing this fragment MUST implement the @AbsListView.OnItemClickListener
 * interface.
 */
public class FlightListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int LOADER_ID_LOAD_FLIGHT_LIST = 1;
    private final static String ARG_ID_FOR_LOADER = "biz.ajoshi.t1401654727.checkin.frags.FlightListFragment.id";

    /**
     * The fragment's ListView.
     */
    private AbsListView mListView;
    /**
     * The Adapter which will be used to populate the ListView with
     * Views.
     */
    private CursorAdapter mAdapter;

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
        mAdapter = new FlightCursorAdapter(getActivity(), null);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER_ID_LOAD_FLIGHT_LIST, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flight, container, false);
        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        TextView emptyView = (TextView) view.findViewById(android.R.id.empty);
        if (emptyView != null) {
            emptyView.setText(R.string.empty_flight_list);
        }
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Cursor data = (Cursor) l.getItemAtPosition(position);
        // show detail
        // options: force checkin, delete
        if (data.moveToFirst()) {
            final String title = getTitleFromListItem(data);

            final String flightId = data.getString(data.getColumnIndex(MyDBHelper.COL_ID));
            final String fname = data.getString(data.getColumnIndex(MyDBHelper.COL_FNAME));
            final String lname = data.getString(data.getColumnIndex(MyDBHelper.COL_LNAME));
            final String ccode = data.getString(data.getColumnIndex(MyDBHelper.COL_CONF_CODE));
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

    /**
     * @param data Cursor representation of the listitem we are generating the title for
     * @return String title of the listitem
     */
    private String getTitleFromListItem(Cursor data) {
        String title;
        long time = data.getLong(data.getColumnIndex(MyDBHelper.COL_TIME));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        String timeString = dateFormat.format(cal.getTime());
        boolean checkedIn = data.getInt(data.getColumnIndex(MyDBHelper.COL_DONE)) == 1;
        if (checkedIn) {
            //time, origin, destination, position, gate
            title = getString(R.string.flight_detail_dialog_title_checked_in, timeString,
                    data.getString(data.getColumnIndex(MyDBHelper.COL_FROM_PLACE)),
                    data.getString(data.getColumnIndex(MyDBHelper.COL_DEST_PLACE)),
                    data.getString(data.getColumnIndex(MyDBHelper.COL_POSITION)),
                    data.getString(data.getColumnIndex(MyDBHelper.COL_GATE))
            );
        } else {
            // time, origin, destination
            int attempts = data.getInt(data.getColumnIndex(MyDBHelper.COL_ATTEMPTS));
            if (attempts > 0) {
                // we've  tried to check in before, but failed
                String titlePart1 = getString(R.string.flight_detail_dialog_title_not_checked_in, timeString,
                        data.getString(data.getColumnIndex(MyDBHelper.COL_FROM_PLACE)),
                        data.getString(data.getColumnIndex(MyDBHelper.COL_DEST_PLACE)));
                String titlePart2 = getString(R.string.flight_detail_dialog_title_auto_checkin_failed, attempts);
                title = getString(R.string.flight_detail_dialog_title_not_checked_in_auto_checkin_failed, titlePart1, titlePart2);
            } else {
                String origin = data.getString(data.getColumnIndex(MyDBHelper.COL_FROM_PLACE));
                String dest = data.getString(data.getColumnIndex(MyDBHelper.COL_DEST_PLACE));
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_LOAD_FLIGHT_LIST) {
            return new CursorLoader(getActivity(), EventProvider.AUTH_URI,
                    new String[]{MyDBHelper.COL_ID, MyDBHelper.COL_TIME, MyDBHelper.COL_FNAME,
                            MyDBHelper.COL_LNAME, MyDBHelper.COL_CONF_CODE, MyDBHelper.COL_DONE,
                            MyDBHelper.COL_FROM_PLACE, MyDBHelper.COL_DEST_PLACE,
                            MyDBHelper.COL_ATTEMPTS, MyDBHelper.COL_GATE, MyDBHelper.COL_POSITION}, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_ID_LOAD_FLIGHT_LIST) {
            mAdapter.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_ID_LOAD_FLIGHT_LIST) {
            mAdapter.swapCursor(null);
        }
    }
}
