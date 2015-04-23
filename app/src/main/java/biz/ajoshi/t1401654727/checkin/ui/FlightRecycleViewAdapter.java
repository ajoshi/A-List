package biz.ajoshi.t1401654727.checkin.ui;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import biz.ajoshi.t1401654727.checkin.Constants;
import biz.ajoshi.t1401654727.checkin.FlightListElement;
import biz.ajoshi.t1401654727.checkin.R;

/**
 * Adapter for RecyclerView to show flight info
 */
public class FlightRecycleViewAdapter extends RecyclerView.Adapter<FlightListViewHolder> implements FlightListViewHolder.FlightItemClickListener {
    private List<FlightListElement> mDataList;
    private Calendar mCalendar;
    private long mmCurrentTime;
    private Resources mResources;
    private Context mContext;
    private LayoutInflater mInflater;
    private TextView mEmptyView;
    private FlightItemClickListener mFlightClickListener;

    private static final int FLIGHT_IMMINENT_RES_ID = android.R.color.holo_red_light;
    private static final int FLIGHT_SOON_RES_ID = android.R.color.darker_gray;
    private static final int FLIGHT_GONE_RES_ID = android.R.color.black;
    private static final int FLIGHT_DEFAULT_RES_ID = android.R.color.transparent;

    public FlightRecycleViewAdapter(List<FlightListElement> flightList, Context c,
                                    TextView emptyView, FlightItemClickListener clickListener) {
        this.mDataList = flightList;
        mContext = c;
        mFlightClickListener = clickListener;
        mInflater = LayoutInflater.from(mContext);
        if (mContext != null) {
            mCalendar = Calendar.getInstance();
            mmCurrentTime = System.currentTimeMillis();
            mResources = mContext.getResources();
        }
        this.mEmptyView = emptyView;
    }

    @Override
    public FlightListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = mInflater.inflate(R.layout.flight_list_item, viewGroup, false);
        mEmptyView = (TextView) itemView.findViewById(R.id.no_flights_message);
        return new FlightListViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(FlightListViewHolder flightListViewHolder, int index) {
        if (mDataList != null) {
            populate(flightListViewHolder, mDataList.get(index), mContext);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList != null) {
            return mDataList.size();
        }
        return 0;
    }

    /**
     * Removes the item at the given position from the backing list and notifies the adapter so
     * view can be updated/animated
     *
     * @param position
     */
    public void removeItem(int position) {
        super.notifyItemRemoved(position);
        if (mDataList != null) {
            mDataList.remove(position);
        }
    }

    /**
     * Given a ViewHolder and a cursor, populates the viewholder
     *
     * @param holder  ViewHolder containing references to the desired views
     * @param element FlightListElement containing data
     * @param ctx     Context
     */
    private void populate(FlightListViewHolder holder, FlightListElement element, Context ctx) {
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
        mCalendar.setTimeInMillis(time);
        if (holder.timeView != null) {
            holder.timeView.setText(element.displayTime);
        }
        holder.checkbox.setVisibility(element.hasCheckedIn ? View.VISIBLE : View.GONE);

        if (mmCurrentTime > time) {
            // shouldn't exist- should have been cleared by the cleanup, but just in case!
            holder.backgroundView.setBackgroundColor(mResources.getColor(FLIGHT_GONE_RES_ID));
        } else {
            if (time - mmCurrentTime <= Constants.MS_IN_THREE_HOURS) {
                // three hours to go.
                holder.backgroundView.setBackgroundColor(mResources.getColor(FLIGHT_IMMINENT_RES_ID));
            } else if (time - mmCurrentTime <= Constants.MS_IN_DAY) {
                // flight is in a day
                holder.backgroundView.setBackgroundColor(mResources.getColor(FLIGHT_SOON_RES_ID));
            } else {
                // in case row reuse changed the color of this row
                holder.backgroundView.setBackgroundColor(mResources.getColor(FLIGHT_DEFAULT_RES_ID));
            }
        }
    }

    public void setList(ArrayList<FlightListElement> resultSet) {
        boolean isListempty = true;
        if (resultSet == null) {
            if (mDataList != null) {
                mDataList.clear();
            }
        } else {
            if (mDataList == null) {
                mDataList = new ArrayList<FlightListElement>(resultSet.size());
            }
            mDataList.clear();
            mDataList.addAll(resultSet);
            if (mDataList.size() > 0) {
                isListempty = false;
            }
        }
        mEmptyView.setVisibility(isListempty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void flightItemClicked(int index) {
        mFlightClickListener.flightItemClicked(mDataList.get(index), index);
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
