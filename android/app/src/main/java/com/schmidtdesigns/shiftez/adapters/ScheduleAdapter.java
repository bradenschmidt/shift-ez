package com.schmidtdesigns.shiftez.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
 * sequence.
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

        Schedule s = mSchedules.get(position);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        Picasso.with(mContext).load(s.getImage()).into(imageView);

        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText("Year: " + s.getYear() + " Week: " + s.getWeek());

        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return mSchedules.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
