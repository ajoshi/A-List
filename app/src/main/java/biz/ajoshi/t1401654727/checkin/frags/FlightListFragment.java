package biz.ajoshi.t1401654727.checkin.frags;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;
import biz.ajoshi.t1401654727.checkin.ui.FlightCursorAdapter;

/**
 * A fragment representing a list of flights.
 * <p/>
 * Activities containing this fragment MUST implement the @AbsListView.OnItemClickListener
 * interface.
 */
public class FlightListFragment extends ListFragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final static int LOADER_ID_LOAD_FLIGHT_LIST = 1;
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
        mListView.setAdapter(mAdapter);
        setEmptyText(getString(R.string.empty_flight_list));
        // Set OnItemClickListener so we can be notified on item clicks so we can display more info
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO give more detailed info about flight
        // toast? alertdialog?
    }

    /**
     * Sets text to be shown when list is empty
     *
     * @param emptyText Charsequence to be shown
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
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
                            MyDBHelper.COL_FROM_PLACE, MyDBHelper.COL_DEST_PLACE}, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
