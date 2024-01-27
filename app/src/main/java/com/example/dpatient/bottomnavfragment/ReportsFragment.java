package com.example.dpatient.bottomnavfragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dpatient.Adapter.CalendarAdapter;
import com.example.dpatient.Adapter.GlucoseHistoryAdapter;
import com.example.dpatient.Calendar.CalendarUtils;
import com.example.dpatient.LineChartRenderer.CustomLineChartRenderer;
import com.example.dpatient.R;
import com.example.dpatient.UserDetails;
import com.example.dpatient.Utils.DateAndTimeUtils;
import com.example.dpatient.model.GlucoseHistoryModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.TargetOrBuilder;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ReportsFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private LineChart mChart;
    private FrameLayout noDataFrameLayout, lineChartFrameLayout;

    private List<String> xValues;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    private TextView monthYearText, dayHighestGlucose, dayLowestGlucose, dayAverageGlucose;
    private RecyclerView calendarRecyclerview;

    AppCompatButton previousWeekActionBtn, nextWeekActionBtn;


    ArrayList<Entry> entry;
    private TextView overAllGlucoseAverage;
    private TextView overAllGlucoseHighest;
    private TextView overAllGlucoseLowest;
    private RecyclerView historyRecyclerview;
    private GlucoseHistoryAdapter myAdapter;
    private ArrayList<GlucoseHistoryModel> list;
    private  UserDetails userDetails;
    private String patientId;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        initWidgets(view);
        noInternetDialog();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String currentFormattedDate = formatter.format(CalendarUtils.selectedDate);
        setUpLineChart(currentFormattedDate);
        setUpHighLowAverageOfGlucose(currentFormattedDate);
        setUpGlucoseHistory(currentFormattedDate);

        previousWeekActionBtn.setOnClickListener(v->{
            previousWeekAction();
        });

        nextWeekActionBtn.setOnClickListener(v->{
            nextWeekAction();
        });

        setUpOverAllData();


        return view;
    }

    private void setUpGlucoseHistory(String date) {
        if(getContext() != null)
            userDetails = new UserDetails(getContext());
        String year = DateAndTimeUtils.getYear(date);
        String month = DateAndTimeUtils.getMonth(date);

        String patientId = userDetails.getPatientId();

        list = new ArrayList<>();
        myAdapter = new GlucoseHistoryAdapter(list, getContext());
        historyRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        historyRecyclerview.setAdapter(myAdapter);

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users").document(patientId)
                .collection("year_"+ year).document(month + "_" + year).collection(date);

        if(collectionReference != null){
            Query query = collectionReference.orderBy("time", Query.Direction.DESCENDING);
            query.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                QuerySnapshot querySnapshot = task.getResult();
                                if (!querySnapshot.isEmpty() && querySnapshot != null){
                                    String previousTime = "";
                                    String previousGlucoseLevel = "";
                                    list.clear();

                                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                        if (documentSnapshot.exists()){
                                            String time = documentSnapshot.getString("time");
                                            String glucoseLevel = documentSnapshot.getString("glucose_level");
                                            String timeFormatted;
                                            if(!previousTime.equals(time)){
                                                if (!previousGlucoseLevel.equals(glucoseLevel)){
                                                    timeFormatted = DateAndTimeUtils.convertTimeToAMAndPM(time);

                                                    list.add(new GlucoseHistoryModel(glucoseLevel, timeFormatted));
                                                }
                                            }
                                            previousTime = time;
                                            previousGlucoseLevel = glucoseLevel;
                                        }
                                    }
                                    if (myAdapter != null){
                                        myAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });
        }

    }

    private void setUpOverAllData(){
        UserDetails userDetails = new UserDetails(getContext());

        String patientId = userDetails.getPatientId();
        String date = DateAndTimeUtils.dateIdFormat();
        String year = DateAndTimeUtils.getYear(date);

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users").document(patientId)
                .collection("year_"+ year);

        if (collectionReference != null){
            collectionReference
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                QuerySnapshot querySnapshot = task.getResult();
                                if (!querySnapshot.isEmpty() && querySnapshot != null){
                                    long glucoseLevelTotal = 0;
                                    long glucoseAverage = 0;
                                    long glucoseHighest = 0;
                                    long glucoseLowest = 1000;
                                    long documentLength = 0;
                                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                        if (documentSnapshot.exists()){
                                            documentLength++;
                                            glucoseLevelTotal += (long) documentSnapshot.get("average_glucose");

                                            if ((long) documentSnapshot.get("highest_glucose") > glucoseHighest)
                                                glucoseHighest = (long) documentSnapshot.get("highest_glucose");
                                            if ((long) documentSnapshot.get("lowest_glucose") < glucoseLowest)
                                                glucoseLowest = (long) documentSnapshot.get("lowest_glucose");

                                        }
                                    }

                                    glucoseAverage = glucoseLevelTotal / documentLength;

                                    overAllGlucoseAverage.setText(String.valueOf(glucoseAverage));
                                    overAllGlucoseHighest.setText(String.valueOf(glucoseHighest));
                                    overAllGlucoseLowest.setText(String.valueOf(glucoseLowest));

                                }
                            }
                        }
                    });
        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initWidgets(View view) {
        mChart = view.findViewById(R.id.lineChart);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();



        previousWeekActionBtn = view.findViewById(R.id.previousWeekAction_Button);
        nextWeekActionBtn = view.findViewById(R.id.nextWeekAction_Button);

        noDataFrameLayout = view.findViewById(R.id.noData_FrameLayout);
        lineChartFrameLayout = view.findViewById(R.id.lineChart_FrameLayout);

        dayAverageGlucose = view.findViewById(R.id.dayAverageGlucose_Textview);
        dayHighestGlucose = view.findViewById(R.id.dayHighestGlucose_Textview);
        dayLowestGlucose = view.findViewById(R.id.dayLowestGlucose_Textview);

        overAllGlucoseAverage = view.findViewById(R.id.glucoseLevelAverage_Textview);
        overAllGlucoseHighest = view.findViewById(R.id.highestGlucoseLevel_Textview);
        overAllGlucoseLowest = view.findViewById(R.id.lowestGlucoseLevel_TxtView);

        historyRecyclerview = view.findViewById(R.id.glucoseHistory_Recyclerview);

        noDataFrameLayout.setVisibility(View.INVISIBLE);


        //Set up calendar
        calendarRecyclerview = view.findViewById(R.id.calendarRecyclerview);
        monthYearText = view.findViewById(R.id.monthYear_Textview);
        CalendarUtils.selectedDate = LocalDate.now();
        setWeekView();

    }

    private void setUpHighLowAverageOfGlucose(String currentFormattedDate) {
        UserDetails userDetails = new UserDetails(getContext());
        String patientId = userDetails.getPatientId();
        String year = DateAndTimeUtils.getYear(currentFormattedDate);
        String month = DateAndTimeUtils.getMonth(currentFormattedDate);

        CollectionReference collectionReference = firebaseFirestore.collection("Users").document(patientId)
                .collection("year_"+ year).document(month + "_" + year).collection(currentFormattedDate);

        if (collectionReference != null){
            collectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                QuerySnapshot querySnapshot = task.getResult();
                                if (!querySnapshot.isEmpty() && querySnapshot != null){
                                    int highestGlucose = 0;
                                    int lowestGlucose = 1000;
                                    int totalGlucose = 0;
                                    int averageGlucose = 0;
                                    int glucoseDataLength = 0;

                                    for (DocumentSnapshot documentSnapshot: task.getResult()){
                                        String glucoseLevelString = documentSnapshot.getString("glucose_level");
                                        if (glucoseLevelString.equals("NONE"))
                                            glucoseLevelString = "0";
                                        if(glucoseLevelString.equals("�"))
                                            glucoseLevelString = "0";
                                        int glucoseLevel = Integer.parseInt(glucoseLevelString);

                                        //Get the data length
                                        glucoseDataLength++;

                                        //Get highest glucose
                                        if(glucoseLevel > highestGlucose)
                                            highestGlucose = glucoseLevel;

                                        //Get the lowest glucose
                                        if(glucoseLevel < lowestGlucose)
                                            lowestGlucose = glucoseLevel;

                                        //Get the total of glucose
                                        totalGlucose += glucoseLevel;

                                    }

                                    //Get average of glucose
                                    averageGlucose = totalGlucose / glucoseDataLength;

                                    dayHighestGlucose.setText(String.valueOf(highestGlucose));
                                    dayLowestGlucose.setText(String.valueOf(lowestGlucose));
                                    dayAverageGlucose.setText(String.valueOf(averageGlucose));
                                }
                            }

                        }
                    });
        }


//        DocumentReference documentReference = firebaseFirestore.collection("Users").document(patientId)
//                .collection("year_"+ year).document(month + "_" + year);
//
//        if (documentReference != null){
//            documentReference.get()
//                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if (task.isSuccessful()){
//                                DocumentSnapshot documentSnapshot = task.getResult();
//                                if (documentSnapshot.exists()){
//                                    long averageGlucose = documentSnapshot.getLong("average_glucose");
//                                    long highestGlucose = documentSnapshot.getLong("highest_glucose");
//                                    long lowestGlucose = documentSnapshot.getLong("lowest_glucose");
//
//
//                                }
//
//                            }
//                        }
//                    });
//        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setWeekView() {
        monthYearText.setText(CalendarUtils.monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = CalendarUtils.daysInWeekArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, (CalendarAdapter.OnItemListener) this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        calendarRecyclerview.setLayoutManager(layoutManager);
        calendarRecyclerview.setAdapter(calendarAdapter);

    }



    @SuppressLint("NewApi")
    private void setUpLineChart(String date) {

        String year = DateAndTimeUtils.getYear(date);
        String month = DateAndTimeUtils.getMonth(date);

         if (getContext()!= null){
             userDetails = new UserDetails(getContext());
             patientId = userDetails.getPatientId();
         }

        entry = new ArrayList<>();
        mChart.getAxisRight().setEnabled(false);


        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(7);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(23);
        xAxis.setGranularity(1);
        xValues = Arrays.asList("12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM"
                , "8AM", "9AM", "10AM", "11AM", "12NN", "1PM", "2PM"
                , "3PM", "4PM", "5PM", "6PM", "7PM", "9PM", "10PM"
                , "11PM", "12MN");
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setAxisMinimum(50);
        yAxis.setAxisMaximum(500);
        yAxis.setLabelCount(7);


        CollectionReference collectionReference = firebaseFirestore.collection("Users").document(patientId)
                .collection("year_"+ year).document(month + "_" + year).collection(date);


        if(collectionReference != null){
            collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        //Check if the collection is not empty
                        if (querySnapshot.isEmpty()) {

                            //If empty, there's no data
                            lineChartFrameLayout.setVisibility(View.GONE);
                            noDataFrameLayout.setVisibility(View.VISIBLE);

                        } else {
                            //If not, show the line chart
                            lineChartFrameLayout.setVisibility(View.VISIBLE);
                            noDataFrameLayout.setVisibility(View.GONE);

                            int [] timeArray = new int[24];
                            int [] timeLengthArray = new int[24];
                            int [] timeAverageArray = new int[24];


                            //Fetching documents
                            for (QueryDocumentSnapshot documentSnapshot1 : task.getResult()) {
                                if (documentSnapshot1.exists()) {

                                    String time = documentSnapshot1.getString("time");
                                    String glucoseLevel = documentSnapshot1.getString("glucose_level");
                                    int glucoseLevelInt = Integer.parseInt(glucoseLevel);

                                    String TIME = DateAndTimeUtils.getTimeForLineChart(time);
                                    int timeInt = Integer.parseInt(TIME);

                                    // Updating arrays
                                    timeArray[timeInt] += glucoseLevelInt;
                                    timeLengthArray[timeInt] = timeLengthArray[timeInt] + 1;

                                }


                            }


                            //Compute the average glucose level for each time
                            for (int i = 0; i < 24; i++){

                                if(timeLengthArray[i] != 0)
                                    timeAverageArray[i] = timeArray[i] / timeLengthArray[i];

                            }

                            //Input the data to line chart
                            for (int i = 0; i <24; i++){
                                if(timeAverageArray[i] != 0)
                                    entry.add(new Entry(i,timeAverageArray[i]));

                            }

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
                }
            });
        }
    }


    private void noInternetDialog() {
        NoInternetDialogPendulum.Builder builder = new NoInternetDialogPendulum.Builder(
                getActivity(),
                getLifecycle()
        );

        DialogPropertiesPendulum properties = builder.getDialogProperties();

        properties.setConnectionCallback(new ConnectionCallback() { // Optional
            @Override
            public void hasActiveConnection(boolean hasActiveConnection) {
                // ...
            }
        });

        properties.setCancelable(false); // Optional
        properties.setNoInternetConnectionTitle("No Internet"); // Optional
        properties.setNoInternetConnectionMessage("Check your Internet connection and try again"); // Optional
        properties.setShowInternetOnButtons(true); // Optional
        properties.setPleaseTurnOnText("Please turn on"); // Optional
        properties.setWifiOnButtonText("Wifi"); // Optional
        properties.setMobileDataOnButtonText("Mobile data"); // Optional


        builder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void previousWeekAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void nextWeekAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemClick(int position, LocalDate date) {

        if(date != null)
        {
            CalendarUtils.selectedDate = date;
            String dateString = String.valueOf(date);

            DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                Date date1 = inputDateFormat.parse(dateString);
                DateFormat outputDateFormat = new SimpleDateFormat("ddMMyyyy");

                String newDate = outputDateFormat.format(date1);
                setWeekView();
                setUpLineChart(newDate);
                setUpHighLowAverageOfGlucose(newDate);
                setUpGlucoseHistory(newDate);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }


        }

    }



}