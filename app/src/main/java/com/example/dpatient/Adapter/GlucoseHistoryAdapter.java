package com.example.dpatient.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpatient.R;
import com.example.dpatient.model.GlucoseHistoryModel;

import java.util.ArrayList;

public class GlucoseHistoryAdapter extends RecyclerView.Adapter<GlucoseHistoryAdapter.MyViewHolder> {
    ArrayList<GlucoseHistoryModel> list;
    Context context;

    public GlucoseHistoryAdapter(ArrayList<GlucoseHistoryModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public GlucoseHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.glucose_history_list, parent ,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GlucoseHistoryAdapter.MyViewHolder holder, int position) {
        GlucoseHistoryModel model = list.get(position);
        holder.glucoseLevel.setText(model.getGlucoseLevel());
        holder.time.setText(model.getTime());

        //Low glucose
        if (Integer.parseInt(model.getGlucoseLevel()) < 60) {

            holder.glucoseStatusBGColor.setBackgroundResource(R.drawable.low_glucose_status_color);


        }

        //Normal glucose
        else if (Integer.parseInt(model.getGlucoseLevel()) >= 60 && Integer.parseInt(model.getGlucoseLevel()) <= 140) {

            holder.glucoseStatusBGColor.setBackgroundResource(R.drawable.normal_glucose_status_color);

        }

        // Pre-Diabetic
        else if (Integer.parseInt(model.getGlucoseLevel()) >= 140 && Integer.parseInt(model.getGlucoseLevel()) <= 199) {

            holder.glucoseStatusBGColor.setBackgroundResource(R.drawable.pre_diabetic_glucose_status_color);
        }


        //High glucose
        else if (Integer.parseInt(model.getGlucoseLevel()) >= 200) {


            holder.glucoseStatusBGColor.setBackgroundResource(R.drawable.high_glucose_status_color);


        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView glucoseLevel, time;
        RelativeLayout glucoseStatusBGColor;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            glucoseLevel = itemView.findViewById(R.id.glucoseLevel_Textview);
            time = itemView.findViewById(R.id.time_Textview);
            glucoseStatusBGColor = itemView.findViewById(R.id.glucoseStatus_RelativeLayout);
        }
    }
}
