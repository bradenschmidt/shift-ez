package com.schmidtdesigns.shiftez.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.activities.MainActivity;
import com.schmidtdesigns.shiftez.fragments.ScheduleFragment;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static java.lang.Math.abs;

/**
 * Pager Adapter used to show each given schedule on their own page. Handles view injection and
 * setup.
 * <p/>
 * Created by braden on 15-06-09.
 */
public class ScheduleAdapter extends PagerAdapter {
    private ArrayList<Schedule> mSchedules;
    private Context mContext;

    public ScheduleAdapter(Context context, ArrayList<Schedule> schedules) {
        mSchedules = schedules;
        mContext = context;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_view, container, false);

        final Schedule s = mSchedules.get(position);

        ImageView scheduleImage = (ImageView) view.findViewById(R.id.schedule_image);
        Picasso.with(mContext).load(s.getImage()).into(scheduleImage);

        scheduleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Clicked image", Toast.LENGTH_SHORT).show();

                ((MainActivity) mContext).displayView(ScheduleFragment.newInstance(s), true);

            }
        });

        TextView scheduleWeekCurrent = (TextView) view.findViewById(R.id.schedule_week_current);
        setupScheduleWeekCurrent(scheduleWeekCurrent, s);

        TextView scheduleWeekNum = (TextView) view.findViewById(R.id.schedule_week_num);
        scheduleWeekNum.setText("Year: " + s.getYear() + " - Week: " + s.getWeek());

        TextView scheduleWeekDays = (TextView) view.findViewById(R.id.schedule_week_days);
        setupScheduleWeekDays(scheduleWeekDays, s);

        container.addView(view);

        return view;
    }

    private void setupScheduleWeekCurrent(TextView scheduleWeekCurrent, Schedule s) {
        int weeks = Weeks.weeksBetween(new DateTime().withYear(s.getYear()).withWeekOfWeekyear(s.getWeek()).plusWeeks(s.getWeekOffset()), new DateTime()).getWeeks();

        if (weeks == 0) {
            scheduleWeekCurrent.setText("Current Week");
        } else if (weeks == -1) {
            scheduleWeekCurrent.setText("In " + abs(weeks) + " Week");
        } else if (weeks == 1) {
            scheduleWeekCurrent.setText(weeks + " Week Ago");
        } else if (weeks < 0) {
            scheduleWeekCurrent.setText("In " + weeks + " Weeks");
        } else {
            scheduleWeekCurrent.setText(abs(weeks) + " Weeks Ago");
        }
    }

    /**
     * Setup the schedule start and end dates based on the schedules information.
     *
     * @param scheduleWeekDays TextView to put days
     * @param s                Schedule to use for info
     */
    private void setupScheduleWeekDays(TextView scheduleWeekDays, Schedule s) {
        // Get start and end dates of the current schedules week
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.WEEK_OF_YEAR, s.getWeek() + s.getWeekOffset());
        cal.set(Calendar.YEAR, s.getYear());

        DateFormat df = new SimpleDateFormat("EEE MMM dd", Locale.CANADA);
        String startDate = df.format(cal.getTime());

        cal.add(Calendar.DATE, 6);
        String endDate = df.format(cal.getTime());

        scheduleWeekDays.setText(startDate + " - " + endDate);
    }

    /**
     * Return the position in the adapter that is the current weeks schedule. Not necessarily the
     * last item since we can have schedules in the future.
     *
     * @return position of current week, if the current week does not exist then return last item
     */
    public int getCurrentWeekPosition() {
        Calendar cal = Calendar.getInstance();

        // Look through schedules to find one equivalent to the current week
        for (int i = 0; i < mSchedules.size(); i++) {
            Schedule s = mSchedules.get(i);

            int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);

            if (s.getWeek() + s.getWeekOffset() == currentWeek) {
                return i;
            }
        }

        return mSchedules.size();
    }

    @Override
    public int getCount() {
        return mSchedules.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
