package com.schmidtdesigns.shiftez.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ScheduleActivity extends BaseActivity {

    private static final String TAG = "ScheduleActivity";
    @InjectView(R.id.schedule_toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.schedule_image_zoom)
    SubsamplingScaleImageView mScheduleImage;
    private Schedule mSchedule;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        ButterKnife.inject(this);

        setupToolbar(mToolbar);

        // Get the schedule from the intent
        String schedule = getIntent().getStringExtra(Constants.SCHEDULE_PARAM);
        if (schedule == null) {
            Log.e(TAG, "Schedule from intent is null");
        }
        mSchedule = Schedule.deserializeFromJson(schedule);

        setupScheduleImage(mScheduleImage);
    }

    private void setupScheduleImage(final SubsamplingScaleImageView image) {
        image.setMaxScale(10F);

        Picasso.with(getApplicationContext())
                .load(mSchedule.getImageUrl())
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        image.setImage(ImageSource.bitmap(bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        //TODO
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        //TODO
                    }
                });
    }

    private void setupToolbar(Toolbar toolbar) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        } else {
            Log.e(TAG, "Toolbar is null");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.schedule_info_button)
    public void showScheduleInfo() {
        ArrayList<String> list = mSchedule.asStringList();

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Schedule Info");
        builder.setPositiveButton("OK", null);
        builder.setItems(list.toArray(new CharSequence[list.size()]), null);
        builder.show();
    }

    @OnClick(R.id.schedule_share_button)
    public void shareSchedule() {
        String body = "Schedule Info:\n"
                + "Store Name: " + mSchedule.getStoreName() + "\n"
                + "Department Name: " + mSchedule.getDepName() + "\n"
                + "Schedule Year: " + mSchedule.getYear() + "\n"
                + "Schedule Year: " + mSchedule.getYear() + "\n"
                + "Schedule Week: " + mSchedule.getWeek() + "\n"
                + "Schedule Week Offset: " + mSchedule.getWeekOffset() + "\n"
                + "Schedule Image: " + mSchedule.getImageUrl();

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Shared Schedule");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_schedule_using)));
    }

    @OnClick(R.id.schedule_update_button)
    public void updateSchedule() {
        Intent intent = new Intent(this, UploadActivity.class);
        intent.putExtra(Constants.SCHEDULE_PARAM, Schedule.serializeToJson(mSchedule));

        startActivity(intent);
    }
}
