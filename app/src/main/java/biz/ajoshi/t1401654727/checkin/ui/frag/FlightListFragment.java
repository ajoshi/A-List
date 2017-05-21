package biz.ajoshi.t1401654727.checkin.ui.frag;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import biz.ajoshi.t1401654727.checkin.ui.FlightRecycleViewCursorAdapter;
import biz.ajoshi.t1401654727.checkin.ui.frag.dialog.FlightOptionsDialog;

/**
 * A fragment representing a list of flights.
 */
public class FlightListFragment extends Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>,
        FlightRecycleViewCursorAdapter.FlightItemClickListener, FlightOptionsDialog.ClickListener {

    /**
     * Interface that will somehow add a new flight when asked to do so
     */
    public interface NewFlightAdder {
        void addNewFlight();
    }

    private final static int LOADER_ID_LOAD_FLIGHT_LIST = 1;
    /**
     * The Adapter which will be used to populate the ListView with
     * Views.
     */
    private FlightRecycleViewCursorAdapter mAdapter;
    RecyclerView mRecyclerView;
    private NewFlightAdder newFlightAdder;
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
    public void onAttach(Activity activity) {
        if (!(activity instanceof AddNewFlightFragment.NewFlightAddedListener)) {
            throw new ClassCastException("Activity must implement NewFlightAdder");
        }
        newFlightAdder = (NewFlightAdder) activity;
        super.onAttach(activity);
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
        mRecyclerView.addItemDecoration(new SpaceItemDecoration((int)getResources().getDimension(R.dimen.flight_card_vertical_space)));
        final FloatingActionButton myFab = (FloatingActionButton) view.findViewById(R.id.add_flight_fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newFlightAdder.addNewFlight();
            }
        });
        return view;
    }

    /**
     * Given a FlightListElement, extracts the String to be shown as title of the onclick dialog
     *
     * @param data FlightListElement containing the data for this element
     * @return String title of click dialog
     */
    private String getTitleFromListItem(FlightListElement data, Context ctx) {
        String title;
        String timeString = data.displayTime; /// dateFormat.format(cal.getTime());
        boolean checkedIn = data.hasCheckedIn;
        if (checkedIn) {
            //time, origin, destination, position, gate
            title = ctx.getString(R.string.flight_detail_dialog_title_checked_in, timeString,
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
                String titlePart1 = ctx.getString(R.string.flight_detail_dialog_title_not_checked_in, timeString,
                        data.origin,
                        data.destination);
                String titlePart2 = ctx.getString(R.string.flight_detail_dialog_title_auto_checkin_failed, attempts);
                title = ctx.getString(R.string.flight_detail_dialog_title_not_checked_in_auto_checkin_failed, titlePart1, titlePart2);
            } else {
                String origin = data.origin;
                String dest = data.destination;
                if (origin == null || dest == null) {
                    title = ctx.getString(R.string.flight_detail_dialog_title_not_checked_in_no_locations, timeString);
                } else {
                    title = ctx.getString(R.string.flight_detail_dialog_title_not_checked_in, timeString,
                            origin, dest);
                }
            }
        }
        return title;
    }

    /**
     * Restarts the query for data which reloads the list
     */
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
        // right now use the standard 2 button dialog, use custom layout if anything else can be done
        DialogFragment deleteOrCheckinDialog =
                FlightOptionsDialog.getInstance(getTitleFromListItem(element, getContext()), element);
        /*
         * This is a hack to keep from going from DialogFrag -> Activity -> Frag
         */
        deleteOrCheckinDialog.setTargetFragment(this, 1);
        deleteOrCheckinDialog.show(getFragmentManager(), "FlightListFragment.deleteOrCheckinDialog");
    }

    @Override
    public void deleteFlightWithId(String flightId) {
        getActivity().getContentResolver().delete(EventProvider.AUTH_URI, MyDBHelper.COL_ID + " = " + flightId, null);
        resetList();
    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int mVerticalSpaceHeight;

        public SpaceItemDecoration(int mVerticalSpaceHeight) {
            this.mVerticalSpaceHeight = mVerticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = mVerticalSpaceHeight;
            outRect.left = mVerticalSpaceHeight/2;
            outRect.right = mVerticalSpaceHeight/2;
        }
    }

}
