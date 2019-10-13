package com.aki.stepstracker.utils;

import android.util.Log;

import com.aki.stepstracker.model.StepInfo;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;

import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateTimeInstance;

public class StepsDataParser {
    private static final String TAG = "data parser";

    public static StepInfo dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getDateTimeInstance();
        int totalSteps = 0;
//        String date = dateFormat.format(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS));
        for (DataPoint dp : dataSet.getDataPoints()) {
//            Log.i(TAG, "Data point:");
//            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                totalSteps = totalSteps + dp.getValue(field).asInt();
            }
        }
        Log.i(TAG, "\tSteps for day: " + totalSteps);
        return new StepInfo(DateFormat.getDateInstance().format(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS)), totalSteps);
    }

    public static StepInfo getDataFromBucket(Bucket bucket) {
        DateFormat dateFormat = DateFormat.getDateInstance();
        List<DataSet> dataSets = bucket.getDataSets();
        int totalSteps = 0;
        for (DataSet dataSet : dataSets) {
            for (DataPoint dp : dataSet.getDataPoints()) {
                for(Field field : dp.getDataType().getFields()) {
                    totalSteps = totalSteps + dp.getValue(field).asInt();
                }
            }
        }
        return new StepInfo(dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS)),totalSteps);
    }
}
