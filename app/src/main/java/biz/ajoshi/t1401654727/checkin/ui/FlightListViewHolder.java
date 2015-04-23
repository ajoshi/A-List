package biz.ajoshi.t1401654727.checkin.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import biz.ajoshi.t1401654727.checkin.R;

/**
 * Created by Aditya on 4/21/2015.
 */
public class FlightListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView passengerNameView;
    TextView timeView;
    TextView placesView;
    View backgroundView;
    ImageView checkbox;
    FlightItemClickListener mClickListener;

    public FlightListViewHolder(View itemView, FlightItemClickListener clickListener) {
        super(itemView);
        passengerNameView = (TextView) itemView.findViewById(R.id.user_name);
        this.timeView = (TextView) itemView.findViewById(R.id.flight_time);
        this.placesView = (TextView) itemView.findViewById(R.id.places);
        this.backgroundView = itemView.findViewById(R.id.flight_info_container);
        this.checkbox = (ImageView) itemView.findViewById(R.id.has_checked_in);
        mClickListener = clickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        mClickListener.flightItemClicked(getAdapterPosition());
    }

    public interface FlightItemClickListener {
        /**
         * Called when a view is clicked. The implementing class should have access to the backing
         * data and can retrieve data and then react accordingly
         *
         * @param index index of clicked row in the backing data structure
         */
        public void flightItemClicked(int index);
    }
}
