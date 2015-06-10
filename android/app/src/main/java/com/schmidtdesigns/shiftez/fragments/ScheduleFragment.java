package com.schmidtdesigns.shiftez.fragments;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.adapters.ScheduleAdapter;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;
import com.schmidtdesigns.shiftez.network.ScheduleRetrofitRequest;

/**
 * A placeholder fragment containing a simple view.
 */
public class ScheduleFragment extends BaseFragment {

    private static final String TAG = "ScheduleFragment";
    private ViewPager mPager;
    private ScheduleAdapter mPagerAdapter;

    public ScheduleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_schedule, container, false);

        /*
        * Request object for schedules
	    */
        int year = 2015;
        ScheduleRetrofitRequest scheduleRequest = new ScheduleRetrofitRequest(year);
        getSpiceManager().execute(scheduleRequest, "schedules", DurationInMillis.ONE_MINUTE, new ListScheduleRequestListener());

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) rootView.findViewById(R.id.schedulePager);


        return rootView;
    }

    /**
     * Called on completion of the api request to get the Schedules.
     * Deals with api call failure -
     * On Success - use the schedules.
     */
    public final class ListScheduleRequestListener implements RequestListener<ScheduleResponse> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //TODO
            Toast.makeText(getActivity(), "Schedule Request Failure", Toast.LENGTH_SHORT).show();
            Log.d(TAG, spiceException.getCause().toString());

            //TextView mainText = (TextView) findViewById(R.id.mainText);
            //mainText.setText("Failed");
        }

        /**
         * Use the schedules
         *
         * @param result - Schedules in the Datastore
         */
        @Override
        public void onRequestSuccess(final ScheduleResponse result) {
            Toast.makeText(getActivity(), "Schedule Request Success", Toast.LENGTH_SHORT).show();
            Log.d(TAG, result.toString());

            /**
            Schedule s = result.getSchedules().get(0);

            ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView);
            Picasso.with(getActivity()).load(s.getImage()).into(imageView);

            TextView textView = (TextView) getActivity().findViewById(R.id.textView);
            textView.setText("Year: " + s.getYear() + " Week: " + s.getWeek());
            **/

            mPagerAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), result.getSchedules());
            mPager.setAdapter(mPagerAdapter);

        }
    }
}
