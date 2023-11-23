package com.example.dpatient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpatient.R;
import com.example.dpatient.model.GlucoseHistoryModel;

import java.util.ArrayList;

public class GlucoseHistoryAdapter extends RecyclerView.Adapter<GlucoseHistoryAdapter.MyViewHolder> {

    Context context;
    ArrayList <GlucoseHistoryModel> list;

    public GlucoseHistoryAdapter(Context context, ArrayList<GlucoseHistoryModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public GlucoseHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.glucose_history_recyclerview_list, parent, false);

        return new GlucoseHistoryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GlucoseHistoryAdapter.MyViewHolder holder, int position) {
        GlucoseHistoryModel glucoseHistoryModel = list.get(position);
        holder.glucoseLevelTextview.setText(glucoseHistoryModel.getGlucoseLevel());
        holder.dateTextview.setText(glucoseHistoryModel.getDate());
        holder.timeTextview.setText(glucoseHistoryModel.getTime());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView glucoseLevelTextview,
                dateTextview,
                timeTextview;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            glucoseLevelTextview = itemView.findViewById(R.id.glucoseLevel_Textview);
            dateTextview = itemView.findViewById(R.id.date_Textview);
            timeTextview = itemView.findViewById(R.id.time_Textview);
        }
    }
}
