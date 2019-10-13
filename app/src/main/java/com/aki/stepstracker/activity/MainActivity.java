package com.aki.stepstracker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aki.stepstracker.R;
import com.aki.stepstracker.model.StepInfo;
import com.aki.stepstracker.utils.StepsDataParser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    TextView date;
    TextView stepsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{Color.parseColor("#3EADCF"), Color.parseColor("#ABE9CD")});
        findViewById(R.id.root).setBackground(drawable);
        stepsCount = findViewById(R.id.stepsCountToday);
        date = findViewById(R.id.dateToday);
        date.setText(DateFormat.getDateInstance().format(new Date()));
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
            }
        }
    }

    /**
     * Records step data by requesting a subscription to background step data.
     */
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                    getTodaySteps();
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    private void getTodaySteps() {
        new TodayStepCountTask().execute();
    }

    public void nextScreen(View view) {
        startActivity(new Intent(MainActivity.this, UserDataActivity.class));
    }

    private class TodayStepCountTask extends AsyncTask<Void, Void, StepInfo> {
        private StepInfo stepInfo;

        protected StepInfo doInBackground(Void... params) {
            Task<DataSet> dataSetTask = Fitness.getHistoryClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext())).readDailyTotal(DataType.TYPE_STEP_COUNT_CUMULATIVE);
            try {
                DataSet await = Tasks.await(dataSetTask);
                stepInfo = StepsDataParser.dumpDataSet(await);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return stepInfo;
        }

        @Override
        protected void onPostExecute(StepInfo stepInfo) {
            super.onPostExecute(stepInfo);
            if (stepInfo == null) {
                stepsCount.setText("0");
            }
            else {
                date.setText(stepInfo.getDate());
                stepsCount.setText(stepInfo.getSteps());
            }
        }
    }
}
