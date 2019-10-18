package com.aki.stepstracker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.aki.stepstracker.R;
import com.aki.stepstracker.adapter.StepCountAdapter;
import com.aki.stepstracker.model.StepInfo;
import com.aki.stepstracker.utils.StepsDataParser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class UserDataActivity extends AppCompatActivity {

    public static final String TAG = "UserDataActivity";

    RecyclerView recyclerView;
    List<StepInfo> stepsList = new ArrayList<>();
    LinearLayoutManager layoutManager;
    ExtendedFloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{Color.parseColor("#FF8489"), Color.parseColor("#D5ADC8")});
        findViewById(R.id.root).setBackground(drawable);
        fab = findViewById(R.id.toggle);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    fab.shrink();
                } else {
                    fab.extend();
                }
            }
        });
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        new ReadData().execute();
    }

    public void toggleListOrder(View view) {
        boolean flag = !layoutManager.getReverseLayout();
        layoutManager.setReverseLayout(flag);
        layoutManager.setStackFromEnd(flag);
    }

    public void onCustomBackClicked(View view) {
        onBackPressed();
    }

    private class ReadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Setting a start and end date using a range of 2 week before this moment.

            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(new Date());
            calEnd.set(Calendar.DAY_OF_YEAR, calEnd.get(Calendar.DAY_OF_YEAR));
            calEnd.set(Calendar.HOUR_OF_DAY, 0);
            calEnd.set(Calendar.MINUTE, 0);
            calEnd.set(Calendar.SECOND, 0);
            calEnd.set(Calendar.MILLISECOND, 0);
            long endTime = calEnd.getTimeInMillis();
            calEnd.set(Calendar.DAY_OF_YEAR, calEnd.get(Calendar.DAY_OF_YEAR)-14);
            long startTime = calEnd.getTimeInMillis();


            java.text.DateFormat dateFormat = DateFormat.getDateTimeInstance();
            Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
            Log.i(TAG, "Range End: " + dateFormat.format(endTime));

            DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                    .setAppPackageName("com.google.android.gms")
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setType(DataSource.TYPE_DERIVED)
                    .setStreamName("estimated_steps")
                    .build();

            DataReadRequest readRequest =
                    new DataReadRequest.Builder()
                            .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                            .bucketByTime(1, TimeUnit.DAYS)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .enableServerQueries()
                            .build();
            Task<DataReadResponse> response = Fitness.getHistoryClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext())).readData(readRequest);
            DataReadResponse result = null;
            try {
                result = Tasks.await(response);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            if (result != null) {
                List<Bucket> buckets = result.getBuckets();
                for (int i = 0; i < buckets.size(); i++) {
                    stepsList.add(StepsDataParser.getDataFromBucket(buckets.get(i)));
                }
            } else {
                Log.i(TAG, "result was empty");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerView.setAdapter(new StepCountAdapter(getApplicationContext(), stepsList));
        }
    }

}
