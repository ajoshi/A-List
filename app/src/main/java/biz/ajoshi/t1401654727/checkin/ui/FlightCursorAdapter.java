package biz.ajoshi.t1401654727.checkin.ui;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import biz.ajoshi.t1401654727.checkin.Constants;
import biz.ajoshi.t1401654727.checkin.R;
import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;

/**
 * CursorAdapter to show flight info
 */
public class FlightCursorAdapter extends CursorAdapter {

    LayoutInflater mInflater;
    int DB_INDEX_TIME;
    int DB_INDEX_DISPLAY_TIME;
    int DB_INDEX_FNAME;
    int DB_INDEX_LNAME;
    int DB_INDEX_CCODE;
    int DB_INDEX_DONE;
    int DB_INDEX_DEST;
    int DB_INDEX_ORIGIN;
    Calendar cal;
    Locale locale;
    long currentTime;
    Resources res;

    public FlightCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mInflater = LayoutInflater.from(context);
        if (context != null) {
            cal = Calendar.getInstance();
            locale = Locale.getDefault();
            currentTime = System.currentTimeMillis();
            res = context.getResources();
        }
    }

    @Override
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
        }
        return super.swapCursor(c);
    }

    /**
     * Given a ViewHolder and a cursor, populates the viewholder
     *
     * @param holder ViewHolder containing references to the desired views
     * @param cursor Cursor containing data
     * @param ctx    Context
     */
    private void populate(ViewHolder holder, Cursor cursor, Context ctx) {
        long time = cursor.getLong(DB_INDEX_TIME);
        String fname = cursor.getString(DB_INDEX_FNAME);
        String lname = cursor.getString(DB_INDEX_LNAME);
        boolean hasCheckedin = cursor.getInt(DB_INDEX_DONE) == 1;
        String places = cursor.getString(DB_INDEX_ORIGIN);
        if (places != null) {
            places = ctx.getString(R.string.flight_list_origin_to_dest, places, cursor.getString(DB_INDEX_DEST));
        }
        /**
         * Next set the name of the entry.
         */
        if (holder.uName != null) {
            holder.uName.setText(ctx.getString(R.string.flight_list_name, fname, lname));
        }
        if (holder.places != null) {
            if (places != null) {
                holder.places.setText(places);
                holder.places.setVisibility(View.VISIBLE);
            } else {
                holder.places.setVisibility(View.GONE);
            }
        }
        cal.setTimeInMillis(time);
        if (holder.time != null) {
            holder.time.setText(cursor.getString(DB_INDEX_DISPLAY_TIME));
        }
        holder.checkbox.setVisibility(hasCheckedin ? View.VISIBLE : View.GONE);

        if (currentTime > time) {
            // shouldn't exist- should have been cleared by the cleanup, but just in case!
            holder.bg.setBackgroundColor(res.getColor(android.R.color.black));
        } else {
            if (time - currentTime <= Constants.MS_IN_THREE_HOURS) {
                // three hours to go.
                holder.bg.setBackgroundColor(res.getColor(android.R.color.holo_red_light));
            } else if (time - currentTime <= Constants.MS_IN_DAY) {
                // flight is in a day
                holder.bg.setBackgroundColor(res.getColor(android.R.color.darker_gray));
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.flight_list_item, parent, false);

        ViewHolder holder = new ViewHolder(v, R.id.user_name, R.id.flight_time, R.id.places,
                R.id.flight_info_container, R.id.has_checked_in);
        populate(holder, cursor, context);
        v.setTag(holder);

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        populate(holder, cursor, context);
    }

    class ViewHolder {
        TextView uName;
        TextView time;
        TextView places;
        View bg;
        ImageView checkbox;

        public ViewHolder(View v, int name, int time, int places, int bg, int checkbox) {
            uName = (TextView) v.findViewById(name);
            this.time = (TextView) v.findViewById(time);
            this.places = (TextView) v.findViewById(places);
            this.bg = v.findViewById(bg);
            this.checkbox = (ImageView) v.findViewById(checkbox);
        }
    }
}