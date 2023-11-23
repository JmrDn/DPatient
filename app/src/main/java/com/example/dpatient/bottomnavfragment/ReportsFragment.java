package com.example.dpatient.bottomnavfragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dpatient.R;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReportsFragment extends Fragment {

    private LineChart mChart;

    private List<String> xValues;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;


    ArrayList<Entry> entry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        mChart = view.findViewById(R.id.lineChart);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        noInternetDialog();


        setUpLineChart();


        return view;
    }

    private void refresh (int milliseconds){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpLineChart();
            }
        }, milliseconds);
    }

    private void setUpLineChart() {
        entry = new ArrayList<>();
        mChart.getAxisRight().setEnabled(false);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(7);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(23);
        xAxis.setGranularity(1);
        xValues = Arrays.asList("12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM"
                , "8AM", "9AM", "10AM", "11AM", "12NN", "1PM" ,"2PM"
                , "3PM", "4PM" ,"5PM" ,"6PM" ,"7PM" ,"9PM" ,"10PM"
                ,"11PM", "12MN");
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setAxisMinimum(50);
        yAxis.setAxisMaximum(400);
        yAxis.setLabelCount(7);

        firebaseFirestore.collection("UsersUID").document(firebaseAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String patientId = documentSnapshot.getString("patientID");

                            CollectionReference collectionReference = firebaseFirestore.collection("Users").document(patientId).collection("glucose_Level_History");

                            Query query = collectionReference.orderBy("timeStamp", Query.Direction.ASCENDING);



                                    query
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if (error != null){
                                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                            else {
                                                entry.clear();
                                                for (QueryDocumentSnapshot documentSnapshot1: value){
                                                    if (documentSnapshot1.exists()){

                                                        String time = documentSnapshot1.getString("time");
                                                        String date = documentSnapshot1.getString("date");
                                                        String glucoseLevel = documentSnapshot1.getString("glucose_level");

                                                        setUpLineChart1(time, glucoseLevel, entry);




                                                    }
                                                }

                                                LineDataSet set1 = new LineDataSet(entry, "Glucose Level");
                                                set1.setLineWidth(3);
                                                set1.setCircleRadius(8);
                                                set1.setValueTextSize(10);

                                                LineData data = new LineData(set1);
                                                mChart.setData(data);
                                                mChart.animateX(1);
                                            }

                                        }
                                    });
                        }
                    }
                });

    refresh(1000);
    }

    private void setUpLineChart1 (String time, String glucoseLevel, ArrayList<Entry> entry){

        //Convert time to date format
        DateFormat dateFormatInput = new SimpleDateFormat("hh:mm a");

        try {
            Date date = dateFormatInput.parse(time);
            DateFormat dateFormatOutput = new SimpleDateFormat("kk:mm");
            String timeFormatted = dateFormatOutput.format(date);

            setUpLineChart2(timeFormatted, glucoseLevel, entry);

        } catch (ParseException e) {
            Log.d("Time Conversion", "Time Conversion: " + e.getMessage());
        }


    }

    private void setUpLineChart2(String timeFormatted, String glucoseLevel, ArrayList<Entry> entry) {

        Log.d("TAG", timeFormatted);

        if (timeFormatted.charAt(0) == '0'){
            char timeChar = timeFormatted.charAt(1);
            int timeInt = Integer.parseInt(String.valueOf(timeChar));
            int glucoseLevelInt = Integer.parseInt(glucoseLevel);
            entry.add(new Entry(timeInt,glucoseLevelInt));

        }
        else if (timeFormatted.charAt(0) == '1'){


            String timeString = '1' + String.valueOf(timeFormatted.charAt(1));
            int timeInt = Integer.parseInt(timeString);
            int glucoseLevelInt = Integer.parseInt(glucoseLevel);
            entry.add(new Entry(timeInt,glucoseLevelInt));

        }
        else if (timeFormatted.charAt(0) == '2'){


            String timeString = '2' + String.valueOf(timeFormatted.charAt(1));
            int timeInt = Integer.parseInt(timeString);
            int glucoseLevelInt = Integer.parseInt(glucoseLevel);
            entry.add(new Entry(timeInt,glucoseLevelInt));

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

}