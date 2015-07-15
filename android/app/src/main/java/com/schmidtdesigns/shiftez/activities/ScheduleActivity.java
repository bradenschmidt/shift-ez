package com.schmidtdesigns.shiftez.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.schmidtdesigns.shiftez.Constants;
import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class ScheduleActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ScheduleActivity";
    private Schedule mSchedule;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.schedule_toolbar);
        setupToolbar(toolbar);

        String schedule = getIntent().getStringExtra(Constants.SCHEDULE_PARAM);
        // Get the schedule from the intent
        if (schedule == null) {
            Log.e(TAG, "Schedule from intent is null");
        }
        mSchedule = Schedule.deserializeFromJson(schedule);

        final SubsamplingScaleImageView image =
                (SubsamplingScaleImageView) findViewById(R.id.schedule_image_zoom);
        setupScheduleImage(image);

        ImageButton infoButton = (ImageButton) findViewById(R.id.schedule_info_button);
        infoButton.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        //TODO

        ArrayList<String> list = mSchedule.asStringList();

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Schedule Info");
        //builder.setMessage("Lorem ipsum dolor ....");
        builder.setPositiveButton("OK", null);
        builder.setItems(list.toArray(new CharSequence[list.size()]), null);
        builder.show();
    }
}
