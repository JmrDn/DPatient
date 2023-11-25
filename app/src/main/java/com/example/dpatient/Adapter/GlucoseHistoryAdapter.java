package com.example.dpatient.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GlucoseHistoryAdapter.MyViewHolder holder, int position) {
        GlucoseHistoryModel glucoseHistoryModel = list.get(position);
        holder.glucoseLevelTextview.setText(glucoseHistoryModel.getGlucoseLevel());
        holder.dateTextview.setText(glucoseHistoryModel.getDate());
        holder.timeTextview.setText(glucoseHistoryModel.getTime());
        holder.glucoseLevel = Integer.parseInt(glucoseHistoryModel.getGlucoseLevel());

        if (holder.glucoseLevel < 120){
            int colorResource = ContextCompat.getColor(context,R.color.lowGlucoseColor);
            holder.glucoseStatusCardview.setCardBackgroundColor(colorResource);
            holder.glucoseLevelStatusTextview.setText("Low");
        }
       else if (holder.glucoseLevel >= 120 && holder.glucoseLevel <= 140){
            int colorResource = ContextCompat.getColor(context,R.color.normalGlucoseColor);
            holder.glucoseStatusCardview.setCardBackgroundColor(colorResource);
            holder.glucoseLevelStatusTextview.setText("Normal");
        }
        else if (holder.glucoseLevel >= 140 && holder.glucoseLevel <= 199){
            int colorResource = ContextCompat.getColor(context,R.color.preDiabeticGlucoseColor);
            holder.glucoseStatusCardview.setCardBackgroundColor(colorResource);
            holder.glucoseLevelStatusTextview.setText("Pre-Diabetic");
        }
        else if (holder.glucoseLevel >= 200){
            int colorResource = ContextCompat.getColor(context,R.color.highGlucoseColor);
            holder.glucoseStatusCardview.setCardBackgroundColor(colorResource);
            holder.glucoseLevelStatusTextview.setText("High");
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView glucoseLevelTextview,
                dateTextview,
                timeTextview,
                glucoseLevelStatusTextview;
        CardView glucoseStatusCardview;
        int glucoseLevel;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            glucoseLevelTextview = itemView.findViewById(R.id.glucoseLevel_Textview);
            dateTextview = itemView.findViewById(R.id.date_Textview);
            timeTextview = itemView.findViewById(R.id.time_Textview);
            glucoseStatusCardview = itemView.findViewById(R.id.glucoseLevelStatus_Cardview);
            glucoseLevelStatusTextview = itemView.findViewById(R.id.glucoseLevelStatus_Textview);
        }
    }
}
