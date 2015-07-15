package com.schmidtdesigns.shiftez.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.activities.ScheduleActivity;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
    private ViewHolder mViewHolder;

    public ScheduleAdapter(Context context, ArrayList<Schedule> schedules) {
        mSchedules = schedules;
        mContext = context;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_view, container, false);

        mViewHolder = new ViewHolder(view);

        final Schedule s = mSchedules.get(position);

        Picasso.with(mContext).load(s.getImageUrl()).into(mViewHolder.scheduleImage);

        mViewHolder.scheduleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ScheduleActivity.class);
                intent.putExtra(Constants.SCHEDULE_PARAM, Schedule.serializeToJson(s));

                // Setup Transition Animation
                // TODO
                String transitionName = mContext.getString(R.string.schedule_image_transition);
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext,
                                mViewHolder.scheduleImage,   // The view which starts the transition
                                transitionName    // The transitionName of the view we’re transitioning
                                // to
                        );
                ActivityCompat.startActivity((Activity) mContext, intent, options.toBundle());
            }
        });

        setupScheduleWeekCurrent(s);

        mViewHolder.scheduleYear.setText(String.valueOf(s.getYear()));
        mViewHolder.scheduleWeek.setText(" Week " + s.getWeek());

        setupScheduleWeekDays(s);

        container.addView(view);

        return view;
    }

    private void setupScheduleWeekCurrent(Schedule s) {
        int weeks = Weeks.weeksBetween(new DateTime().withYear(s.getYear())
                        .withWeekOfWeekyear(s.getWeek()).plusWeeks(s.getWeekOffset()),
                new DateTime()).getWeeks();

        if (weeks == 0) {
            mViewHolder.scheduleWeekCurrent.setText("Current Week");
        } else if (weeks == -1) {
            mViewHolder.scheduleWeekCurrent.setText("In " + abs(weeks) + " Week");
        } else if (weeks == 1) {
            mViewHolder.scheduleWeekCurrent.setText(weeks + " Week Ago");
        } else if (weeks < 0) {
            mViewHolder.scheduleWeekCurrent.setText("In " + abs(weeks) + " Weeks");
        } else {
            mViewHolder.scheduleWeekCurrent.setText(abs(weeks) + " Weeks Ago");
        }
    }

    /**
     * Setup the schedule start and end dates based on the schedules information.
     *
     * @param s Schedule to use for info
     */
    private void setupScheduleWeekDays(Schedule s) {
        // Get start and end dates of the current schedules week
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.WEEK_OF_YEAR, s.getWeek() + s.getWeekOffset());
        cal.set(Calendar.YEAR, s.getYear());

        DateFormat df = new SimpleDateFormat("EEE MMM dd", Locale.CANADA);
        String startDate = df.format(cal.getTime());

        cal.add(Calendar.DATE, 6);
        String endDate = df.format(cal.getTime());

        mViewHolder.scheduleWeekDays.setText(startDate + " - " + endDate);
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

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'schedule_view.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @InjectView(R.id.schedule_year)
        TextView scheduleYear;
        @InjectView(R.id.schedule_week)
        TextView scheduleWeek;
        @InjectView(R.id.schedule_week_current)
        TextView scheduleWeekCurrent;
        @InjectView(R.id.schedule_week_days)
        TextView scheduleWeekDays;
        @InjectView(R.id.schedule_image)
        ImageView scheduleImage;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
