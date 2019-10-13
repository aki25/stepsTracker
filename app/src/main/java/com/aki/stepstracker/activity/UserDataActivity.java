package com.aki.stepstracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.aki.stepstracker.R;
import com.aki.stepstracker.adapter.StepCountAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;

public class UserDataActivity extends AppCompatActivity {

    public static final String TAG = "UserDataActivity";
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        new ReadData().execute();
    }

    private class ReadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Setting a start and end date using a range of 1 week before this moment.
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -2);
            long startTime = cal.getTimeInMillis();

            java.text.DateFormat dateFormat = DateFormat.getDateInstance();
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
                            // The data request can specify multiple data types to return, effectively
                            // combining multiple data queries into one call.
                            // In this example, it's very unlikely that the request is for several hundred
                            // datapoints each consisting of a few steps and a timestamp.  The more likely
                            // scenario is wanting to see how many steps were walked per day, for 7 days.
                            .aggregate(ESTIMATED_STEP_DELTAS,DataType.AGGREGATE_STEP_COUNT_DELTA)
                            // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                            // bucketByTime allows for a time span, whereas bucketBySession would allow
                            // bucketing by "sessions", which would need to be defined in code.
                            .bucketByTime(1, TimeUnit.DAYS)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .enableServerQueries()
                            .build();
            Task<DataReadResponse> response = Fitness.getHistoryClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext())).readData(readRequest);
//            DataReadResponse result = response.getResult();
            DataReadResponse result = null;
            try {
                result = Tasks.await(response);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            if (result != null) {
                List<Bucket> buckets = result.getBuckets();
                for (int i = 0; i<buckets.size();i++) {
                    dumpDataSet(buckets.get(i).getDataSets().get(0));
                }
            } else {
                Log.i(TAG, "result was empty");
            }
            return null;
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getDateTimeInstance();
        int totalSteps = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                totalSteps = totalSteps + dp.getValue(field).asInt();
            }
        }
        Log.i(TAG, "\tSteps for day: " + totalSteps);
    }

}
