package biz.ajoshi.t1401654727.checkin.ui;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import biz.ajoshi.t1401654727.checkin.Constants;
import biz.ajoshi.t1401654727.checkin.FlightListElement;
import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;

/**
 * Created by Aditya on 4/24/2015.
 */
public class FlightRecycleViewCursorAdapter extends CursorRecyclerViewAdapter<FlightListViewHolder> implements FlightListViewHolder.FlightItemClickListener  {
    private long mCurrentTime;
    private Resources mResources;
    private Context mContext;
    private LayoutInflater mInflater;
    private TextView mEmptyView;
    private FlightItemClickListener mFlightClickListener;
    int DB_INDEX_TIME;
    int DB_INDEX_DISPLAY_TIME;
    int DB_INDEX_FNAME;
    int DB_INDEX_LNAME;
    int DB_INDEX_CCODE;
    int DB_INDEX_DONE;
    int DB_INDEX_DEST;
    int DB_INDEX_ORIGIN;
    int DB_INDEX_ID;
    int DB_INDEX_GATE;
    int DB_INDEX_POSITION;
    int DB_INDEX_ATTEMPTS;

    private static final int FLIGHT_IMMINENT_RES_ID = R.color.flight_list_item_bg_imminent;
    private static final int FLIGHT_SOON_RES_ID = R.color.flight_list_item_bg_soon;
    private static final int FLIGHT_GONE_RES_ID = R.color.flight_list_item_bg_gone;
    private static final int FLIGHT_DEFAULT_RES_ID = R.color.flight_list_item_bg_normal;

    /**
     *
     * @param flightList Cursor containing flight list data
     * @param c Context
     * @param emptyView TextView to be shown if the list is empty
     * @param clickListener Listener to be invoked on click
     */
    public FlightRecycleViewCursorAdapter(Cursor flightList, Context c,
                                    TextView emptyView, FlightItemClickListener clickListener) {
        super(c, flightList);

        mContext = c;
        mFlightClickListener = clickListener;
        mInflater = LayoutInflater.from(mContext);
        if (mContext != null) {
            mCurrentTime = System.currentTimeMillis();
            mResources = mContext.getResources();
        }
        this.mEmptyView = emptyView;
    }

    /**
     * Swaps the cursor (does not close), fetches the column Ids and shows or hides the empty view
     *
     * @param c
     * @return
     */
    public Cursor swapCursor(Cursor c) {
        if (c != null) {
            // Store the indices so I can change my query without breaking UI
            DB_INDEX_TIME = c.getColumnIndex(MyDBHelper.COL_TIME);
            DB_INDEX_FNAME = c.getColumnIndex(MyDBHelper.COL_FNAME);
            DB_INDEX_LNAME = c.getColumnIndex(MyDBHelper.COL_LNAME);
            DB_INDEX_CCODE = c.getColumnIndex(MyDBHelper.COL_CONF_CODE);
            DB_INDEX_DONE = c.getColumnIndex(MyDBHelper.COL_DONE);
            DB_INDEX_DEST = c.getColumnIndex(MyDBHelper.COL_DEST_PLACE);
            DB_INDEX_ORIGIN = c.getColumnIndex(MyDBHelper.COL_FROM_PLACE);
            DB_INDEX_DISPLAY_TIME = c.getColumnIndex(MyDBHelper.COL_DISPLAY_TIME);
            DB_INDEX_ID = c.getColumnIndex(MyDBHelper.COL_ID);
            DB_INDEX_GATE = c.getColumnIndex(MyDBHelper.COL_GATE);
            DB_INDEX_POSITION = c.getColumnIndex(MyDBHelper.COL_POSITION);
            DB_INDEX_ATTEMPTS = c.getColumnIndex(MyDBHelper.COL_ATTEMPTS);
            if (c.getCount() > 0 && mEmptyView != null) {
                mEmptyView.setVisibility(View.GONE);
            }
        } else if (mEmptyView != null) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
        return super.swapCursor(c);
    }

    @Override
    public FlightListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = mInflater.inflate(R.layout.flight_list_item, viewGroup, false);
        return new FlightListViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(FlightListViewHolder flightListViewHolder, Cursor cursor) {
        populate(flightListViewHolder, cursor, mContext);
    }

    /**
     * Given a ViewHolder and a cursor, populates the viewholder
     *
     * @param holder  ViewHolder containing references to the desired views
     * @param cursor cursor containing data
     * @param ctx     Context
     */
    private void populate(FlightListViewHolder holder, Cursor cursor, Context ctx) {
        FlightListElement element = new FlightListElement(cursor, DB_INDEX_ID, DB_INDEX_TIME,
                DB_INDEX_FNAME, DB_INDEX_LNAME, DB_INDEX_DONE, DB_INDEX_ORIGIN,
                DB_INDEX_DEST, DB_INDEX_DISPLAY_TIME, -1, -1, -1, -1);
        String places = element.origin;
        if (places != null) {
            places = ctx.getString(R.string.flight_list_origin_to_dest, places, element.destination);
        }
        /**
         * Next set the name of the entry.
         */
        if (holder.passengerNameView != null) {
            holder.passengerNameView.setText(ctx.getString(R.string.flight_list_name, element.fName, element.lName));
        }
        if (holder.placesView != null) {
            if (places != null) {
                holder.placesView.setText(places);
                holder.placesView.setVisibility(View.VISIBLE);
            } else {
                holder.placesView.setVisibility(View.GONE);
            }
        }
        long time = element.timeStamp;
        if (holder.timeView != null) {
            holder.timeView.setText(element.displayTime);
        }
        holder.checkbox.setVisibility(element.hasCheckedIn ? View.VISIBLE : View.GONE);

        if (mCurrentTime > time) {
            // shouldn't exist- should have been cleared by the cleanup, but just in case!
            holder.backgroundView.setCardBackgroundColor(mResources.getColor(FLIGHT_GONE_RES_ID));
        } else {
            if (time - mCurrentTime <= Constants.MS_IN_THREE_HOURS) {
                // three hours to go.
                holder.backgroundView.setCardBackgroundColor(mResources.getColor(FLIGHT_IMMINENT_RES_ID));
            } else if (time - mCurrentTime <= Constants.MS_IN_DAY) {
                // flight is in a day
                holder.backgroundView.setCardBackgroundColor(mResources.getColor(FLIGHT_SOON_RES_ID));
            } else {
                // in case row reuse changed the color of this row
                holder.backgroundView.setCardBackgroundColor(mResources.getColor(FLIGHT_DEFAULT_RES_ID));
            }
        }
    }

    @Override
    public void flightItemClicked(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        // I'd do a save pos, move to new pos, fetch data, move back to old pos, but I see no benefit
        mFlightClickListener.flightItemClicked(new FlightListElement(c, DB_INDEX_ID, -1,
                DB_INDEX_FNAME, DB_INDEX_LNAME, DB_INDEX_DONE, DB_INDEX_ORIGIN,
                DB_INDEX_DEST, DB_INDEX_DISPLAY_TIME, DB_INDEX_GATE, DB_INDEX_POSITION,
                DB_INDEX_CCODE, DB_INDEX_ATTEMPTS), position);

    }

    public interface FlightItemClickListener {
        /**
         * Defines the behavior when a list item is clicked
         *
         * @param element  FlightListElement containing data
         * @param position position of the FlightListElement in the backing array
         */
        public void flightItemClicked(FlightListElement element, int position);
    }
}
