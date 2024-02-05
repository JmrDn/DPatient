package com.example.dpatient.ReportsFasting.FastingReports;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.dpatient.Calendar.CalendarUtils;
import com.example.dpatient.R;
import com.example.dpatient.UserDetails;
import com.example.dpatient.Utils.DateAndTimeUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FastingMonthlyReports extends Fragment {

    private AppCompatButton nextYearBtn, previousYearBtn;
    private LineChart mChart;
    private TextView yearTV;
    private ArrayList<Entry> entry;
    private List<String> xValues;
    private UserDetails userDetails;
    private String patientId;
    private FrameLayout noDataFrameLayout;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fasting_monthly_reports, container, false);
        initWidgets(view);

        noDataFrameLayout.setVisibility(View.GONE);

        CalendarUtils.selectedDate = LocalDate.now();
        yearTV.setText(CalendarUtils.yearFromDate(CalendarUtils.selectedDate));
        setUpLineChart(CalendarUtils.yearFromDate(CalendarUtils.selectedDate));

        nextYearBtn.setOnClickListener(v->{
            nextYearAction();
        });

        previousYearBtn.setOnClickListener(v1->{
            previousYearAction();
        });
        return view;
    }

    private void setUpLineChart(String year) {

        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();

            entry = new ArrayList<>();
            mChart.getAxisRight().setEnabled(false);

            xValues = Arrays.asList("","January " + year, "February " + year, "March " + year, "April " + year,
                    "May " + year, "June " + year, "July " + year, "August " + year,
                    "September " + year, "October " + year, "November " + year, "December " + year);


            XAxis xAxis = mChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(xValues.size());
            xAxis.setAxisMinimum(0);
            xAxis.setAxisMaximum(xValues.size());
            xAxis.setLabelRotationAngle(45f);
            xAxis.setGranularity(1);


            YAxis yAxis = mChart.getAxisLeft();
            yAxis.setDrawGridLines(false);
            yAxis.setAxisMinimum(50);
            yAxis.setAxisMaximum(500);
            yAxis.setLabelCount(7);

            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users")
                    .document(patientId).collection("year_" + year);

            if (collectionReference != null){
                collectionReference.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();

                                    if (!querySnapshot.isEmpty() && querySnapshot != null){
                                        noDataFrameLayout.setVisibility(View.GONE);
                                        mChart.setVisibility(View.VISIBLE);

                                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                            if (documentSnapshot.contains("fasting_average")){
                                                String date = documentSnapshot.getString("date");
                                                long glucoseMonthAverage = documentSnapshot.getLong("fasting_average");

                                                int monthNum = DateAndTimeUtils.getMonthNum(date);

                                                entry.add(new Entry(monthNum, glucoseMonthAverage));
                                            }

                                        }

                                        if (entry != null){
                                            LineDataSet set1 = new LineDataSet(entry, "Glucose Level");
                                            set1.setLineWidth(3);
                                            set1.setCircleRadius(8);
                                            set1.setValueTextSize(10);
                                            set1.setCircleHoleRadius(5);
                                            if (getContext() != null){
                                                set1.setColor(ContextCompat.getColor(getContext(), R.color.patient_color_bg));
                                                set1.setCircleColor(ContextCompat.getColor(getContext(), R.color.patient_color_bg));
                                            }

                                            LineData data = new LineData(set1);

                                            mChart.setData(data);
                                            mChart.animateX(1);
                                        }
                                    }
                                    else{
                                        noDataFrameLayout.setVisibility(View.VISIBLE);
                                        mChart.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
            }
            else{
                noDataFrameLayout.setVisibility(View.VISIBLE);
                mChart.setVisibility(View.GONE);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void previousYearAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusYears(1);
        String year = CalendarUtils.yearFromDate(CalendarUtils.selectedDate);
        yearTV.setText(year);
        setUpLineChart(year);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void nextYearAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusYears(1);
        String year = CalendarUtils.yearFromDate(CalendarUtils.selectedDate);
        yearTV.setText(year);
        setUpLineChart(year);
    }

    private void initWidgets(View view) {

        nextYearBtn = view.findViewById(R.id.nextYearAction_Button);
        previousYearBtn = view.findViewById(R.id.previousYearAction_Button);
        yearTV = view.findViewById(R.id.year_Textview);

        mChart = view.findViewById(R.id.linechart);

        noDataFrameLayout = view.findViewById(R.id.noData_FrameLayout);
    }
}