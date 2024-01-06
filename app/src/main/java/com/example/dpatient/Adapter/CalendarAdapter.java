package com.example.dpatient.Adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpatient.Adapter.CalendarViewHolder;
import com.example.dpatient.Calendar.CalendarUtils;
import com.example.dpatient.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener) {
        this.days = days;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = parent.getHeight(); //week view

        return new CalendarViewHolder(view, onItemListener, days);
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        final LocalDate date = days.get(position);
        if (date == null)
            holder.dayOfMonth.setText("");
        else{
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            if(date.equals(CalendarUtils.selectedDate)){

                holder.parentView.setBackgroundColor(ContextCompat.getColor(holder.parentView.getContext(), R.color.selectedColor));
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(holder.parentView.getContext(), R.color.patient_color_bg));

            }


        }

    }

    @Override
    public int getItemCount() {
        return days.size();
    }
    public interface OnItemListener{
        void onItemClick(int position, LocalDate date);
    }
}
