package biz.ajoshi.t1401654727.checkin.frags;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ajoshi.t1401654727.checkin.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import biz.ajoshi.t1401654727.checkin.db.MyDBHelper;
import biz.ajoshi.t1401654727.checkin.provider.EventProvider;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the  Callbacks
 * interface.
 */
public class FlightListFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int MS_IN_DAY = 86400000;
    private static final int MS_IN_THREE_HOURS = 10800000;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static FlightListFragment newInstance(String param1, String param2) {
        FlightListFragment fragment = new FlightListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static FlightListFragment newInstance() {
        FlightListFragment fragment = new FlightListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FlightListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if (getActivity() != null) {
            final Calendar cal = Calendar.getInstance();
            final Locale locale = Locale.getDefault();
            final long currentTime = System.currentTimeMillis();
            final Resources res =  getActivity().getResources();
            final Cursor c;
            c = getActivity().getContentResolver().query(EventProvider.authUri, new String[]{MyDBHelper.COL_ID, MyDBHelper.COL_TIME, MyDBHelper.COL_FNAME,
                    MyDBHelper.COL_LNAME, MyDBHelper.COL_CONF_CODE, MyDBHelper.COL_DONE}, null, null, null);
            mAdapter = new CursorAdapter(getActivity(), c) {
                class ViewHolder {
                    TextView uName;
                    TextView time;
                    View bg;
                    ImageView checkbox;

                    public ViewHolder(TextView name, TextView time, View bg) {
                        uName = name;
                        this.time = time;
                        this.bg = bg;
                    }

                    public ViewHolder(View v, int name, int time, int bg, int checkbox) {
                        uName =  (TextView) v.findViewById(name);
                        this.time = (TextView) v.findViewById(time);
                        this.bg = v.findViewById(bg);
                        this.checkbox = (ImageView) v.findViewById(checkbox);
                    }
                }

                private void populate(ViewHolder holder, Cursor cursor) {
                    long time = cursor.getLong(1);
                    String fname = cursor.getString(2);
                    String lname = cursor.getString(3);
                    boolean hasCheckedin = cursor.getInt(4) == 1;

                    /**
                     * Next set the name of the entry.
                     */
                    if (holder.uName != null) {
                        holder.uName.setText(fname + " " + lname);
                    }
                    cal.setTimeInMillis(time);
                    if (holder.time != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM, d h:mm a");

                        holder.time.setText(sdf.format(cal.getTime()));
                    }

                    if(hasCheckedin) {
                        holder.checkbox.setVisibility(View.VISIBLE);
                    }

                    if(currentTime > time) {
                        // shouldn't exist... but just in case!
                        holder.bg.setBackgroundColor(res.getColor(android.R.color.black));
                    } else {
                        if (time - currentTime <= MS_IN_THREE_HOURS) {
                            // three hours to go.
                            holder.bg.setBackgroundColor(res.getColor(android.R.color.holo_red_light));
                        } else if(time - currentTime <= MS_IN_DAY ) {
                            // flight is in a day
                            holder.bg.setBackgroundColor(res.getColor(android.R.color.darker_gray));
                        }
                    }
                }

                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    final LayoutInflater inflater = LayoutInflater.from(context);
                    View v = inflater.inflate(R.layout.flight_list_item, parent, false);

                    ViewHolder holder = new ViewHolder(v, R.id.user_name, R.id.flight_time,
                            R.id.flight_info_container, R.id.has_checked_in);

                    populate(holder, cursor);

                    v.setTag(holder);

                    return v;
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {

                   ViewHolder holder = (ViewHolder)view.getTag();

                    populate(holder, cursor);
                }
};
}
        Log.e(" tag", this.getTag());
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flight, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction("");
        }
    }

    /**
     * Sets text to be shown when list is empty
     * @param emptyText Charsequence to be shown
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void resetList() {
        mListView.invalidate();
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
