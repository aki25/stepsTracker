package com.aki.stepstracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aki.stepstracker.R;
import com.aki.stepstracker.model.StepInfo;
import com.google.android.gms.fitness.data.DataSet;

import java.util.List;

public class StepCountAdapter extends RecyclerView.Adapter<StepCountAdapter.ViewHolder> {

    private List<StepInfo> mData;
    private LayoutInflater mInflater;
//    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public StepCountAdapter(Context context, List<StepInfo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @NonNull
    @Override
    public StepCountAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.steps_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepCountAdapter.ViewHolder holder, int position) {
        int steps = mData.get(position).getSteps();
        String date = mData.get(position).getDate();
        holder.stepsCount.setText(String.valueOf(steps));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView stepsCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            stepsCount = itemView.findViewById(R.id.stepsCount);
        }
    }
}
