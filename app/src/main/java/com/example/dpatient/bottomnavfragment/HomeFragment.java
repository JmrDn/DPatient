package com.example.dpatient.bottomnavfragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dpatient.ConnectingToSensorPage;
import com.example.dpatient.SensorPage;
import com.example.dpatient.UserDetails;
import com.example.dpatient.Utils.DateAndTimeUtils;
import com.example.dpatient.Utils.FirebaseUtil;
import com.example.dpatient.R;
import com.example.dpatient.ViewGlucose;
import com.example.dpatient.YourProfile;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.type.DateTime;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;


public class HomeFragment extends Fragment {


    private TextView nameTextview;

    private TextView glucoseLevelTV;
    private TextView timeTV;
    private TextView glucoseStatusTV;
    private RelativeLayout connectedToSensorLayout;

    public String patientId, email, name;

    private ShapeableImageView profileImageview;

    public StorageReference storageReference;
    private RelativeLayout glucoseStatusColor;

     RelativeLayout layout;
     private LineChart lineChart;
     private ArrayList<Entry> entry;

     private UserDetails userDetails;
     private TextView fastingTV;
     private String fastingString = "2-3hrs After Eating";







    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initWidgets(view);
        setUpConnectedToSensorLayout();

        setUpFastingContent();
        setUpPatientName();
        setUpProfilePicture();
        setUpGlucoseLevel();
        
        getData();
        setUpLineChart();
        setUpOverAllData();

        noInternetDialog();

        userDetails = new UserDetails(getContext());

        profileImageview.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), YourProfile.class);
            intent.putExtra("name", nameTextview.getText().toString());
            intent.putExtra("patientId", patientId);
            intent.putExtra("email", email);
            startActivity(intent);
        });

        connectedToSensorLayout.setOnClickListener(v->{
            startActivity(new Intent(getContext(), SensorPage.class));
        });

        return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private void setUpFastingContent() {

        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            if (userDetails.getFastingStatus() != null){
                fastingTV.setText(userDetails.getFastingStatus());
                fastingString = userDetails.getFastingStatus();
            }
            else{
                userDetails.setFastingStatus(fastingString);
                fastingTV.setText(userDetails.getFastingStatus());
            }




            fastingTV.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    showFastingOptionDialog();
                    return false;
                }
            });
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private void showFastingOptionDialog() {
       if (getContext() != null){

           userDetails = new UserDetails(getContext());

           Dialog dialog = new Dialog(getContext());
           dialog.setContentView(R.layout.fasting_choose_dialog_layout);
           dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
           dialog.setCancelable(true);
           dialog.show();

           TextView dialogFastingTV, dialogNonFastingTV, dialogAfterEatingTV;
           dialogFastingTV = dialog.findViewById(R.id.fasting_Textview);
           dialogNonFastingTV = dialog.findViewById(R.id.nonFasting_Textview);
           dialogAfterEatingTV = dialog.findViewById(R.id.afterEating_Textview);

           dialogFastingTV.setOnTouchListener(new View.OnTouchListener() {

               @Override
               public boolean onTouch(View v, MotionEvent event) {
                   fastingString = "Fasting";
                   userDetails.setFastingStatus(fastingString);
                   fastingTV.setText(userDetails.getFastingStatus());
                   setUpGlucoseLevel();
                   dialog.dismiss();
                   return false;
               }
           });

           dialogNonFastingTV.setOnTouchListener(new View.OnTouchListener() {

               @Override
               public boolean onTouch(View v, MotionEvent event) {
                   fastingString = "2-3hrs After Eating";
                   userDetails.setFastingStatus(fastingString);
                   fastingTV.setText(userDetails.getFastingStatus());
                   setUpGlucoseLevel();
                   dialog.dismiss();
                   return false;
               }
           });

           dialogAfterEatingTV.setOnTouchListener(new View.OnTouchListener() {


               @Override
               public boolean onTouch(View v, MotionEvent event) {
                   fastingString = "After Eating";
                   userDetails.setFastingStatus(fastingString);
                   fastingTV.setText(userDetails.getFastingStatus());
                   setUpGlucoseLevel();
                   dialog.dismiss();
                   return false;
               }
           });
       }

    }


    private void updateUserDetails() {
        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            String patientId = userDetails.getPatientId();

            if(patientId != null){
                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot documentSnapshot = task.getResult();

                                    if (documentSnapshot.exists()){
                                        if (documentSnapshot.contains("isConnectedTo") && documentSnapshot.contains("isConnectedToSensor")){
                                            String sensorId = documentSnapshot.getString("isConnectedTo");
                                            boolean isConnected = documentSnapshot.getBoolean("isConnectedToSensor");
                                            userDetails.setAccountIsConnectedToSensor(isConnected);
                                            userDetails.setSensorId(sensorId);
                                        }
                                    }
                                }
                            }
                        });
            }
        }
    }


    private void setUpLineChart() {
        entry = new ArrayList<>();

        lineChart.getAxisRight().setEnabled(false);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(-150f);
        xAxis.setGranularityEnabled(true);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setGranularity(1f);

        String dateId = DateAndTimeUtils.dateIdFormat();
        String month = DateAndTimeUtils.getMonth(dateId);
        String year = DateAndTimeUtils.getYear(dateId);


       if (patientId != null){
           CollectionReference collectionReferenceDay = FirebaseFirestore.getInstance().collection("Users").document(patientId)
                   .collection("year_" + year).document(month + "_" + year)
                   .collection(dateId);

           if(collectionReferenceDay != null){
               Query query = collectionReferenceDay.orderBy("time", Query.Direction.ASCENDING);
               query.get()
                       .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                               if (task.isSuccessful()){
                                   int num = 0;
                                   QuerySnapshot querySnapshot = task.getResult();
                                   if (!querySnapshot.isEmpty() && querySnapshot != null){
                                       for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                           String glucoseLevelString = documentSnapshot.getString("glucose_level");
                                           int glucoseLevel = 0;
                                           if(!glucoseLevelString.equals("�"))
                                               glucoseLevel  = Integer.parseInt(glucoseLevelString);

                                           num++;

                                           if(glucoseLevel != 0)
                                               entry.add(new Entry(num, glucoseLevel));

                                       }

                                       LineDataSet lineDataSet = new LineDataSet(entry, "Glucose level");
                                       lineDataSet.setDrawCircleHole(false);
                                       lineDataSet.setDrawCircles(false);
                                       if(getContext() != null)
                                           lineDataSet.setColor(ContextCompat.getColor(getContext(),R.color.patient_color_bg));

                                       lineDataSet.setDrawValues(false);
                                       lineDataSet.setLineWidth(3);

                                       LineData data = new LineData(lineDataSet);
                                       lineChart.setData(data);
                                       lineChart.animateX(1);

                                   }
                               }
                           }
                       });
           }
       }
        refreshLineChart(1000);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpGlucoseLevel() {
        if (getContext() != null){
            userDetails = new UserDetails(getContext());

            boolean isConnected = userDetails.isAccountConnectedToSensor();
            String sensorId = userDetails.getSensorId();

            if(isConnected){
                timeTV.setVisibility(View.VISIBLE);

                if (sensorId != null){
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(sensorId);

                    databaseReference.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String glucoseLevel = snapshot.child("glucose_Level").getValue().toString();
                                String time = snapshot.child("time").getValue().toString();
                                String date = snapshot.child("date").getValue().toString();


                                String dateString = date +" " + time;

                                LocalDateTime yourDate = DateAndTimeUtils.parseDateString(dateString, "dd/MM/yyyy H:m:s");
                                String minAgo = DateAndTimeUtils.calculateMinutesAgo(yourDate);

                                timeTV.setText(minAgo);

                                setValue(glucoseLevel, true);


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("TAG", error.getMessage());
                        }
                    });
                }
            }
            else{
                timeTV.setVisibility(View.GONE);
            }


        }

    }

    private void setValue(String glucoseLevel, boolean isConnected) {

        if (isConnected){
            if(glucoseLevel.equals("NONE"))
                glucoseLevel = "0";
            if(glucoseLevel.equals("�"))
                glucoseLevel = "0";

            glucoseLevelTV.setText(glucoseLevel);
        }

        else{
            glucoseLevelTV.setText("0");
            glucoseLevel = "0";
        }

        setUpRecentGlucose(glucoseLevel);
        saveData(glucoseLevel);
        setUpGlucoseStatusColor(glucoseLevel);
    }

    private void setUpRecentGlucose(String glucoseLevel) {

        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            String patientId = userDetails.getPatientId();
            String time = DateAndTimeUtils.getTimeWithAMAndPM();
            String date = DateAndTimeUtils.getDate();

            if (patientId!= null){
                HashMap<String, Object> recentData = new HashMap<>();
                recentData.put("recent_glucose_level", glucoseLevel);
                recentData.put("date", date);
                recentData.put("time", time);
                recentData.put("fasting", fastingTV.getText().toString());

                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .update(recentData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d("TAG", "Recent glucose updated");
                                }
                                else {
                                    Log.d("TAG", "Recent glucose failed to update");
                                }
                            }
                        });
            }
        }


    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void refresh(int i) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpGlucoseLevel();
            }
        }, i);
    }

    private void saveData(String glucoseLevel) {

        if (getContext()!= null){
            userDetails = new UserDetails(getContext());
            String patientId = userDetails.getPatientId();

            String dateId = DateAndTimeUtils.dateIdFormat();
            String month = DateAndTimeUtils.getMonth(dateId);
            String year = DateAndTimeUtils.getYear(dateId);
            String time = DateAndTimeUtils.time24HrsFormat();
            String date = DateAndTimeUtils.getDate();
            String timeId = DateAndTimeUtils.timeId();

            if (patientId != null){
                if (!glucoseLevel.equals("0")){

                    DocumentReference documentReferenceDay = FirebaseFirestore.getInstance().collection("Users").document(patientId)
                            .collection("year_" + year).document(month + "_" + year)
                            .collection(dateId).document(timeId);

                    HashMap<String, Object> glucoseDataDay = new HashMap<>();
                    glucoseDataDay.put("glucose_level", glucoseLevel);
                    glucoseDataDay.put("time", time);
                    glucoseDataDay.put("date", date);
                    glucoseDataDay.put("fasting", fastingTV.getText().toString());

                    documentReferenceDay.set(glucoseDataDay)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.d("TAG", "Data saved");
                                    }
                                    else{
                                        Log.d("TAG", "Data not saved HomeFragment: 179");
                                    }
                                }
                            });
                }
            }
        }









    }

    private void getData(){
        if (getContext()!= null){
            userDetails = new UserDetails(getContext());


            String dateId = DateAndTimeUtils.dateIdFormat();
            String month = DateAndTimeUtils.getMonth(dateId);
            String year = DateAndTimeUtils.getYear(dateId);

            if(patientId != null){
                CollectionReference collectionReferenceDay = FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .collection("year_" + year).document(month + "_" + year)
                        .collection(dateId);

                if (collectionReferenceDay != null){
                    Query query = collectionReferenceDay.orderBy("time", Query.Direction.DESCENDING);

                    query.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){
                                        QuerySnapshot querySnapshot =task.getResult();
                                        if (!querySnapshot.isEmpty()){

                                            int highestGlucose = 0;
                                            int lowestGlucose = 1000;
                                            int totalGlucose = 0;
                                            int averageGlucose = 0;
                                            int glucoseDataLength = 0;

                                            int fastingHighestGlucose = 0;
                                            int fastingLowestGlucose = 1000;
                                            int fastingTotalGlucose = 0;
                                            int fastingAverageGlucose = 0;
                                            int fastingGlucoseDataLength = 0;

                                            int afterEatingHighestGlucose = 0;
                                            int afterEatingLowestGlucose = 1000;
                                            int afterEatingTotalGlucose = 0;
                                            int afterEatingAverageGlucose = 0;
                                            int afterEatingGlucoseDataLength = 0;

                                            int after2_3HrsEatingHighestGlucose = 0;
                                            int after2_3HrsEatingLowestGlucose = 1000;
                                            int after2_3HrsEatingTotalGlucose = 0;
                                            int after2_3HrsEatingAverageGlucose = 0;
                                            int after2_3HrsEatingGlucoseDataLength = 0;

                                            for (DocumentSnapshot documentSnapshot: task.getResult()){
                                                String glucoseLevelString = documentSnapshot.getString("glucose_level");
                                                if (glucoseLevelString.equals("NONE"))
                                                    glucoseLevelString = "0";
                                                if(glucoseLevelString.equals("�"))
                                                    glucoseLevelString = "0";
                                                int glucoseLevel = Integer.parseInt(glucoseLevelString);

                                                if (documentSnapshot.contains("fasting")){
                                                    String fasting = documentSnapshot.getString("fasting");

                                                    if (fasting != null && !fasting.isEmpty()){
                                                        if (fasting.equals("Fasting")){
                                                            //Fasting Data Result
                                                            //Get the data length
                                                            fastingGlucoseDataLength++;

                                                            //Get highest glucose
                                                            if(glucoseLevel > fastingHighestGlucose)
                                                                fastingHighestGlucose = glucoseLevel;

                                                            //Get the lowest glucose
                                                            if(glucoseLevel < fastingLowestGlucose)
                                                                fastingLowestGlucose = glucoseLevel;

                                                            //Get the total of glucose
                                                            fastingTotalGlucose += glucoseLevel;
                                                        }
                                                        else if (fasting.equals("After Eating")){
                                                            //After Eating Data Result
                                                            //Get the data length
                                                            afterEatingGlucoseDataLength++;

                                                            //Get highest glucose
                                                            if(glucoseLevel > afterEatingHighestGlucose)
                                                                afterEatingHighestGlucose = glucoseLevel;

                                                            //Get the lowest glucose
                                                            if(glucoseLevel < afterEatingLowestGlucose)
                                                                afterEatingLowestGlucose = glucoseLevel;

                                                            //Get the total of glucose
                                                            afterEatingTotalGlucose += glucoseLevel;
                                                        }
                                                        else if (fasting.equals("2-3hrs After Eating")){
                                                            //After 2-3 Hrs Eating data result
                                                            //Get the data length
                                                            after2_3HrsEatingGlucoseDataLength++;

                                                            //Get highest glucose
                                                            if(glucoseLevel > after2_3HrsEatingHighestGlucose)
                                                                after2_3HrsEatingHighestGlucose = glucoseLevel;

                                                            //Get the lowest glucose
                                                            if(glucoseLevel < after2_3HrsEatingLowestGlucose)
                                                                after2_3HrsEatingLowestGlucose = glucoseLevel;

                                                            //Get the total of glucose
                                                            after2_3HrsEatingTotalGlucose += glucoseLevel;
                                                        }
                                                    }
                                                }


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

                                            if (fastingGlucoseDataLength != 0)
                                                //Get average of fasting
                                                fastingAverageGlucose = fastingTotalGlucose / fastingGlucoseDataLength;

                                            if (afterEatingGlucoseDataLength != 0)
                                                //Get average of After eating
                                                afterEatingAverageGlucose = afterEatingTotalGlucose / afterEatingGlucoseDataLength;

                                            if (after2_3HrsEatingGlucoseDataLength != 0)
                                                //Get average of After 2-3 Hrs Eating
                                                after2_3HrsEatingAverageGlucose = after2_3HrsEatingTotalGlucose / after2_3HrsEatingGlucoseDataLength;

                                            setMonthDocumentData(highestGlucose, lowestGlucose, averageGlucose,
                                                    fastingHighestGlucose, fastingLowestGlucose, fastingAverageGlucose,
                                                    afterEatingHighestGlucose, afterEatingLowestGlucose, afterEatingAverageGlucose,
                                                    after2_3HrsEatingHighestGlucose, after2_3HrsEatingLowestGlucose, after2_3HrsEatingAverageGlucose);
                                        }
                                    }
                                }
                            });
                }
            }
            refreshGetData(2000);
        }


    }

    private void refreshGetData(int milliseconds) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getData();
            }
        }, milliseconds);
    }

    private void refreshLineChart(int milliseconds) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpLineChart();
            }
        }, milliseconds);
    }

    private void setMonthDocumentData(int highestGlucose, int lowestGlucose, int averageGlucose,
                                      int fastingHighestGlucose, int fastingLowestGlucose, int fastingAverageGlucose,
                                      int afterEatingHighestGlucose, int afterEatingLowestGlucose, int afterEatingAverageGlucose,
                                      int after2_3HrsEatingHighestGlucose, int after2_3HrsEatingLowestGlucose, int after2_3HrsEatingAverageGlucose){



        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            String patientId = userDetails.getPatientId();
            String dateId = DateAndTimeUtils.dateIdFormat();
            String month = DateAndTimeUtils.getMonth(dateId);
            String year = DateAndTimeUtils.getYear(dateId);
            String time = DateAndTimeUtils.time24HrsFormat();
            String date = DateAndTimeUtils.getDate();

            if (fastingLowestGlucose == 1000)
                fastingLowestGlucose = 0;
            if (afterEatingLowestGlucose == 1000)
                afterEatingLowestGlucose = 0;
            if (after2_3HrsEatingLowestGlucose == 1000)
                after2_3HrsEatingLowestGlucose = 0;



            if (patientId != null){
                DocumentReference documentReferenceReferenceMonth = FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .collection("year_" + year).document(month + "_" + year);

                HashMap<String, Object> saveData = new HashMap<>();

                long finalFastingLowestGlucose = fastingLowestGlucose;
                long finalAfterEatingLowestGlucose = afterEatingLowestGlucose;
                long finalAfter2_3HrsEatingLowestGlucose = after2_3HrsEatingLowestGlucose;



                if (documentReferenceReferenceMonth != null){

                    documentReferenceReferenceMonth.get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot = task.getResult();

                                        if (documentSnapshot.exists()){

                                            if(documentSnapshot.contains("fasting_highest") &&
                                                    documentSnapshot.contains("fasting_lowest")&&
                                                    documentSnapshot.contains("fasting_average")){

                                                long oldFastingHighest = documentSnapshot.getLong("fasting_highest");
                                                long oldFastingLowest = documentSnapshot.getLong("fasting_lowest");
                                                long oldFastingAverage = documentSnapshot.getLong("fasting_average");

                                                if(oldFastingLowest == 0)
                                                    oldFastingLowest = 1000;

                                                if (fastingHighestGlucose > oldFastingHighest)
                                                    saveData.put("fasting_highest", fastingHighestGlucose);
                                                if (oldFastingLowest == 0){
                                                    if (finalFastingLowestGlucose < oldFastingLowest){
                                                        saveData.put("fasting_lowest", finalFastingLowestGlucose);


                                                    }
                                                }
                                                else{
                                                    if (finalFastingLowestGlucose < oldFastingLowest){

                                                        if (finalFastingLowestGlucose != 1000)
                                                            saveData.put("fasting_lowest", finalFastingLowestGlucose);
                                                        else{
                                                            saveData.put("fasting_lowest", 0);
                                                        }

                                                    }
                                                }

                                                if (fastingAverageGlucose != 0){
                                                    saveData.put("fasting_average", (fastingAverageGlucose + oldFastingAverage) / 2);
                                                }

                                            }
                                            else{
                                                saveData.put("fasting_highest", fastingHighestGlucose);
                                                saveData.put("fasting_lowest", finalFastingLowestGlucose);
                                                saveData.put("fasting_average", fastingAverageGlucose);
                                            }

                                            if(documentSnapshot.contains("after_eating_highest") &&
                                                    documentSnapshot.contains("after_eating_lowest")&&
                                                    documentSnapshot.contains("after_eating_average")){

                                                long oldAfterEatingHighest = documentSnapshot.getLong("after_eating_highest");
                                                long oldAfterEatingLowest = documentSnapshot.getLong("after_eating_lowest");
                                                long oldAfterEatingAverage = documentSnapshot.getLong("after_eating_average");

                                                if(oldAfterEatingLowest == 0)
                                                    oldAfterEatingLowest = 1000;
                                                if (afterEatingHighestGlucose > oldAfterEatingHighest)
                                                    saveData.put("after_eating_highest", fastingHighestGlucose);
                                                if (finalAfterEatingLowestGlucose < oldAfterEatingLowest){

                                                    if (finalAfterEatingLowestGlucose != 1000)
                                                        saveData.put("after_eating_lowest", finalAfterEatingLowestGlucose);
                                                    else{
                                                        saveData.put("after_eating_lowest", 0);
                                                    }
                                                }
                                                if (afterEatingAverageGlucose != 0){
                                                    saveData.put("after_eating_average", (afterEatingAverageGlucose + oldAfterEatingAverage) / 2);
                                                }

                                            }
                                            else{
                                                saveData.put("after_eating_highest", afterEatingHighestGlucose);
                                                saveData.put("after_eating_lowest", finalAfterEatingLowestGlucose);
                                                saveData.put("after_eating_average", afterEatingAverageGlucose);
                                            }

                                            if(documentSnapshot.contains("after_2_3_hours_highest") &&
                                                    documentSnapshot.contains("after_2_3_hours_lowest")&&
                                                    documentSnapshot.contains("after_2_3_hours_average")){

                                                long oldAfter2_3hrsEatingHighest = documentSnapshot.getLong("after_2_3_hours_highest");
                                                long oldAfter2_3hrsEatingLowest = documentSnapshot.getLong("after_2_3_hours_lowest");
                                                long oldAfter2_3hrsEatingAverage = documentSnapshot.getLong("after_2_3_hours_average");

                                                if(oldAfter2_3hrsEatingLowest == 0)
                                                    oldAfter2_3hrsEatingLowest = 1000;

                                                if (after2_3HrsEatingHighestGlucose > oldAfter2_3hrsEatingHighest)
                                                    saveData.put("after_2_3_hours_highest", after2_3HrsEatingHighestGlucose);
                                                if (finalAfter2_3HrsEatingLowestGlucose < oldAfter2_3hrsEatingLowest){

                                                    if(finalAfter2_3HrsEatingLowestGlucose != 1000){
                                                        saveData.put("after_2_3_hours_lowest", finalAfterEatingLowestGlucose);
                                                    }
                                                    else{
                                                        saveData.put("after_2_3_hours_lowest", 0);
                                                    }
                                                }

                                                if (after2_3HrsEatingAverageGlucose != 0){
                                                    saveData.put("after_2_3_hours_average", (after2_3HrsEatingAverageGlucose + oldAfter2_3hrsEatingAverage) / 2);
                                                }

                                            }
                                            else{
                                                saveData.put("after_2_3_hours_highest", after2_3HrsEatingHighestGlucose);
                                                saveData.put("after_2_3_hours_lowest", finalAfter2_3HrsEatingLowestGlucose);
                                                saveData.put("after_2_3_hours_average", after2_3HrsEatingAverageGlucose);
                                            }

                                            saveData.put("highest_glucose", highestGlucose);
                                            saveData.put("lowest_glucose", lowestGlucose);
                                            saveData.put("average_glucose", averageGlucose);
                                            saveData.put("date", date);
                                            saveData.put("time", time);


                                            documentReferenceReferenceMonth.update(saveData)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Log.d("TAG", "Date saved");
                                                            }
                                                            else {
                                                                Log.d("TAG", "Date not saved, HomeFragment: 283");
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                }

            }
        }
    }


    private void setUpGlucoseStatusColor(String glucoseLevelString) {

        int glucoseLevelInt = Integer.parseInt(glucoseLevelString);

        if (fastingString.equals("Fasting")){
            fastingResult(glucoseLevelInt);
        }
        else if (fastingString.equals("2-3hrs After Eating")){
            afterEating2_3hrsResult(glucoseLevelInt);
        }
        else if (fastingString.equals("After Eating")){
            afterEatingResult(glucoseLevelInt);
        }

    }

    private void afterEatingResult(int glucoseLevel) {

        if (getContext() != null){

            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();
            HashMap<String, Object> update = new HashMap<>();
            //Low glucose
            if (glucoseLevel < 170) {

                glucoseStatusColor.setBackgroundResource(R.drawable.low_glucose_status_color);
                glucoseStatusTV.setText("Low");
                update.put("recent_glucose_status", "Low");


            }

            //Normal glucose
            else if (glucoseLevel >= 170 && glucoseLevel <= 200) {

                glucoseStatusColor.setBackgroundResource(R.drawable.normal_glucose_status_color);
                glucoseStatusTV.setText("Normal");
                update.put("recent_glucose_status", "Normal");
            }

            // Pre-Diabetic
            else if (glucoseLevel >= 201 && glucoseLevel <= 230) {

                glucoseStatusColor.setBackgroundResource(R.drawable.pre_diabetic_glucose_status_color);
                glucoseStatusTV.setText("Pre-Diabetic");
                update.put("recent_glucose_status", "Pre-Diabetic");
            }


            //High glucose
            else if (glucoseLevel > 230) {

                DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();

                glucoseStatusColor.setBackgroundResource(R.drawable.high_glucose_status_color);
                glucoseStatusTV.setText("High");
                update.put("recent_glucose_status", "High");

                HashMap<String, Object> notificationUpdate = new HashMap<>();
                notificationUpdate.put("glucose_level", glucoseLevel);
                notificationUpdate.put("time", dateAndTimeUtils.getTimeWithAMAndPM());
                notificationUpdate.put("date", dateAndTimeUtils.getDate());
                notificationUpdate.put("name", name);
                notificationUpdate.put("fasting", fastingString);
                notificationUpdate.put("patientID", patientId);
                notificationUpdate.put("userUID", FirebaseAuth.getInstance().getUid());




                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .collection("myDoctor")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (!querySnapshot.isEmpty() && querySnapshot != null){
                                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                            if (documentSnapshot.exists()){
                                                String doctorUserId = documentSnapshot.getString("userID");

                                                FirebaseFirestore.getInstance().collection("userDoctor").document(doctorUserId)
                                                        .collection("myPatientWithHighGlucose")
                                                        .document(patientId).set(notificationUpdate)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Log.d("TAG", "High glucose notified to doctor");
                                                                }
                                                                else{
                                                                    Log.d("TAG", "Failed to notify doctor on high glucose");
                                                                }
                                                            }
                                                        });

                                                FirebaseFirestore.getInstance().collection("userDoctor").document(doctorUserId)
                                                        .collection("highGlucosePatientNotification")
                                                        .document(patientId).set(notificationUpdate)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Log.d("TAG", "High glucose notified to doctor");
                                                                }
                                                                else{
                                                                    Log.d("TAG", "Failed to notify doctor on high glucose");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                }
                            }
                        });
            }
            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                    .update(update)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d("TAG", "recent glucose status successfully updated");
                            }
                            else{
                                Log.d("TAG", "recent glucose status failed to update");
                            }
                        }
                    });
        }


    }

    private void afterEating2_3hrsResult(int glucoseLevel) {

        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();

            HashMap<String, Object> update = new HashMap<>();
            //Low glucose
            if (glucoseLevel < 60) {

                glucoseStatusColor.setBackgroundResource(R.drawable.low_glucose_status_color);
                glucoseStatusTV.setText("Low");
                update.put("recent_glucose_status", "Low");


            }

            //Normal glucose
            else if (glucoseLevel >= 120 && glucoseLevel <= 140) {

                glucoseStatusColor.setBackgroundResource(R.drawable.normal_glucose_status_color);
                glucoseStatusTV.setText("Normal");
                update.put("recent_glucose_status", "Normal");
            }

            // Pre-Diabetic
            else if (glucoseLevel > 141 && glucoseLevel <= 199) {

                glucoseStatusColor.setBackgroundResource(R.drawable.pre_diabetic_glucose_status_color);
                glucoseStatusTV.setText("Pre-Diabetic");
                update.put("recent_glucose_status", "Pre-Diabetic");
            }


            //High glucose
            else if (glucoseLevel >= 200) {

                DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();

                glucoseStatusColor.setBackgroundResource(R.drawable.high_glucose_status_color);
                glucoseStatusTV.setText("High");
                update.put("recent_glucose_status", "High");

                HashMap<String, Object> notificationUpdate = new HashMap<>();
                notificationUpdate.put("glucose_level", glucoseLevel);
                notificationUpdate.put("time", dateAndTimeUtils.getTimeWithAMAndPM());
                notificationUpdate.put("date", dateAndTimeUtils.getDate());
                notificationUpdate.put("name", name);
                notificationUpdate.put("fasting", fastingString);
                notificationUpdate.put("patientID", patientId);
                notificationUpdate.put("userUID", FirebaseAuth.getInstance().getUid());




                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .collection("myDoctor")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (!querySnapshot.isEmpty() && querySnapshot != null){
                                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                            if (documentSnapshot.exists()){
                                                String doctorUserId = documentSnapshot.getString("userID");

                                                FirebaseFirestore.getInstance().collection("userDoctor").document(doctorUserId)
                                                        .collection("myPatientWithHighGlucose")
                                                        .document(patientId).set(notificationUpdate)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Log.d("TAG", "High glucose notified to doctor");
                                                                }
                                                                else{
                                                                    Log.d("TAG", "Failed to notify doctor on high glucose");
                                                                }
                                                            }
                                                        });

                                                FirebaseFirestore.getInstance().collection("userDoctor").document(doctorUserId)
                                                        .collection("highGlucosePatientNotification")
                                                        .document(patientId).set(notificationUpdate)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Log.d("TAG", "High glucose notified to doctor");
                                                                }
                                                                else{
                                                                    Log.d("TAG", "Failed to notify doctor on high glucose");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                }
                            }
                        });
            }

            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                    .update(update)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d("TAG", "recent glucose status successfully updated");
                            }
                            else{
                                Log.d("TAG", "recent glucose status failed to update");
                            }
                        }
                    });
        }



    }

    @SuppressLint("SetTextI18n")
    private void fastingResult(int glucoseLevel) {


        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();

            HashMap<String, Object> update = new HashMap<>();


            //Low glucose
            if (glucoseLevel < 80) {

                glucoseStatusColor.setBackgroundResource(R.drawable.low_glucose_status_color);
                glucoseStatusTV.setText("Low");
                update.put("recent_glucose_status", "Low");

            }

            //Normal glucose
            else if (glucoseLevel >= 80 && glucoseLevel <= 100) {

                glucoseStatusColor.setBackgroundResource(R.drawable.normal_glucose_status_color);
                glucoseStatusTV.setText("Normal");
                update.put("recent_glucose_status", "Normal");
            }

            // Pre-Diabetic
            else if (glucoseLevel >= 101 && glucoseLevel <= 125) {

                glucoseStatusColor.setBackgroundResource(R.drawable.pre_diabetic_glucose_status_color);
                glucoseStatusTV.setText("Pre-Diabetic");
                update.put("recent_glucose_status", "Pre-Diabetic");
            }


            //High glucose
            else if (glucoseLevel > 125) {

                DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();

                glucoseStatusColor.setBackgroundResource(R.drawable.high_glucose_status_color);
                glucoseStatusTV.setText("High");
                update.put("recent_glucose_status", "High");

                HashMap<String, Object> notificationUpdate = new HashMap<>();
                notificationUpdate.put("glucose_level", glucoseLevel);
                notificationUpdate.put("time", dateAndTimeUtils.getTimeWithAMAndPM());
                notificationUpdate.put("date", dateAndTimeUtils.getDate());
                notificationUpdate.put("name", name);
                notificationUpdate.put("fasting", fastingString);
                notificationUpdate.put("patientID", patientId);
                notificationUpdate.put("userUID", FirebaseAuth.getInstance().getUid());




                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .collection("myDoctor")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (!querySnapshot.isEmpty() && querySnapshot != null){
                                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                            if (documentSnapshot.exists()){
                                                String doctorUserId = documentSnapshot.getString("userID");

                                                FirebaseFirestore.getInstance().collection("userDoctor").document(doctorUserId)
                                                        .collection("myPatientWithHighGlucose")
                                                        .document(patientId).set(notificationUpdate)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Log.d("TAG", "High glucose notified to doctor");
                                                                }
                                                                else{
                                                                    Log.d("TAG", "Failed to notify doctor on high glucose");
                                                                }
                                                            }
                                                        });

                                                FirebaseFirestore.getInstance().collection("userDoctor").document(doctorUserId)
                                                        .collection("highGlucosePatientNotification")
                                                        .document(patientId).set(notificationUpdate)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Log.d("TAG", "High glucose notified to doctor");
                                                                }
                                                                else{
                                                                    Log.d("TAG", "Failed to notify doctor on high glucose");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                }
                            }
                        });
            }

            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                    .update(update)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d("TAG", "recent glucose status successfully updated");
                            }
                            else{
                                Log.d("TAG", "recent glucose status failed to update");
                            }
                        }
                    });
        }


    }

    private void setUpHighGlucoseToDoctor(HashMap<String, Object> notificationUpdate, String patientId) {

    }


    private void initWidgets(View view) {
        nameTextview = view.findViewById(R.id.patientName_Textview);
        connectedToSensorLayout = view.findViewById(R.id.connectedToSensor_Layout);
        profileImageview = view.findViewById(R.id.profile_pic_image_view);
        layout = view.findViewById(R.id.data_Layout);

        glucoseLevelTV = view.findViewById(R.id.glucoseLevel_Textview);
        timeTV = view.findViewById(R.id.time_Textview);
        glucoseStatusColor = view.findViewById(R.id.glucoseStatusColor_RelativeLayout);
        glucoseStatusTV = view.findViewById(R.id.glucoseStatus_Textview);

        lineChart = view.findViewById(R.id.lineChart);

        fastingTV = view.findViewById(R.id.fasting_Textview);


    }


    private void setUpProfilePicture() {

        if (getContext() != null){
            userDetails = new UserDetails(getContext());

            patientId = userDetails.getPatientId();

            if (patientId != null){
                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .get()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                DocumentSnapshot documentSnapshot1 = task1.getResult();
                                if (documentSnapshot1.exists()){
                                    if(documentSnapshot1.contains("profilePicture")){
                                        String fileName = documentSnapshot1.getString("profilePicture");
                                        retrieveImage(fileName);
                                    }


                                }
                            }
                        });
            }
        }

    }

    private void retrieveImage(String fileName){
        storageReference = FirebaseStorage.getInstance().getReference("images/"+ fileName);

        try {
            File localFile = File.createTempFile("tempfile", "jpg");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            profileImageview.setImageBitmap(bitmap);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "Failed to retrieve Profile Picture: " + e.getCause());

                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setUpOverAllData(){

        if(getContext() != null){
            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();

            String date = DateAndTimeUtils.dateIdFormat();
            String year = DateAndTimeUtils.getYear(date);

            if (patientId != null){
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

                                            long fastingHighestGlucose = 0;
                                            long fastingLowestGlucose = 1000;
                                            long fastingTotalGlucose = 0;
                                            long fastingAverageGlucose = 0;
                                            long fastingGlucoseDataLength = 0;

                                            long afterEatingHighestGlucose = 0;
                                            long afterEatingLowestGlucose = 1000;
                                            long afterEatingTotalGlucose = 0;
                                            long afterEatingAverageGlucose = 0;
                                            long afterEatingGlucoseDataLength = 0;

                                            long after2_3HrsEatingHighestGlucose = 0;
                                            long after2_3HrsEatingLowestGlucose = 1000;
                                            long after2_3HrsEatingTotalGlucose = 0;
                                            long after2_3HrsEatingAverageGlucose = 0;
                                            long after2_3HrsEatingGlucoseDataLength = 0;
                                            for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                                if (documentSnapshot.exists()){
                                                    documentLength++;
                                                    glucoseLevelTotal += (long) documentSnapshot.get("average_glucose");

                                                    if ((long) documentSnapshot.get("highest_glucose") > glucoseHighest)
                                                        glucoseHighest = (long) documentSnapshot.get("highest_glucose");
                                                    if ((long) documentSnapshot.get("lowest_glucose") < glucoseLowest)
                                                        glucoseLowest = (long) documentSnapshot.get("lowest_glucose");

                                                    if (documentSnapshot.contains("fasting_average") &&
                                                            documentSnapshot.contains("fasting_highest") &&
                                                            documentSnapshot.contains("fasting_lowest")){

                                                        if (documentSnapshot.getLong("fasting_highest") > fastingHighestGlucose)
                                                            fastingHighestGlucose = documentSnapshot.getLong("fasting_highest");
                                                        if (documentSnapshot.getLong("fasting_lowest") < fastingLowestGlucose)
                                                            fastingLowestGlucose = documentSnapshot.getLong("fasting_lowest");
                                                        if (documentSnapshot.getLong("fasting_average") != 0){
                                                            fastingGlucoseDataLength++;
                                                            fastingTotalGlucose += documentSnapshot.getLong("fasting_average");
                                                        }


                                                    }

                                                    if(documentSnapshot.contains("after_eating_average") &&
                                                            documentSnapshot.contains("after_eating_highest") &&
                                                            documentSnapshot.contains("after_eating_lowest")){

                                                        if (documentSnapshot.getLong("after_eating_highest") > afterEatingHighestGlucose)
                                                            afterEatingHighestGlucose = documentSnapshot.getLong("after_eating_highest");
                                                        if (documentSnapshot.getLong("after_eating_lowest") < afterEatingLowestGlucose)
                                                            afterEatingLowestGlucose = documentSnapshot.getLong("after_eating_lowest");
                                                        if (documentSnapshot.getLong("after_eating_average") != 0){
                                                            afterEatingGlucoseDataLength++;
                                                            afterEatingTotalGlucose += documentSnapshot.getLong("after_eating_average");
                                                        }

                                                    }

                                                    if (documentSnapshot.contains("after_2_3_hours_average") &&
                                                            documentSnapshot.contains("after_2_3_hours_highest")&&
                                                            documentSnapshot.contains("after_2_3_hours_lowest")){

                                                        if (documentSnapshot.getLong("after_2_3_hours_highest") > after2_3HrsEatingHighestGlucose)
                                                            after2_3HrsEatingHighestGlucose = documentSnapshot.getLong("after_2_3_hours_highest");
                                                        if (documentSnapshot.getLong("after_2_3_hours_lowest") < after2_3HrsEatingLowestGlucose)
                                                            after2_3HrsEatingLowestGlucose = documentSnapshot.getLong("after_2_3_hours_lowest");
                                                        if (documentSnapshot.getLong("after_2_3_hours_average") != 0){
                                                            after2_3HrsEatingGlucoseDataLength++;
                                                            after2_3HrsEatingTotalGlucose += documentSnapshot.getLong("after_2_3_hours_average");
                                                        }

                                                    }

                                                }
                                            }

                                            glucoseAverage = glucoseLevelTotal / documentLength;

                                            if (fastingGlucoseDataLength != 0)
                                                fastingAverageGlucose = fastingTotalGlucose / fastingGlucoseDataLength;

                                            if (afterEatingGlucoseDataLength != 0)
                                                afterEatingAverageGlucose = afterEatingTotalGlucose / afterEatingGlucoseDataLength;

                                            if (after2_3HrsEatingGlucoseDataLength != 0)
                                                after2_3HrsEatingAverageGlucose = after2_3HrsEatingTotalGlucose / after2_3HrsEatingGlucoseDataLength;



                                            setUpHighestLowestAndAverage(glucoseAverage, glucoseHighest, glucoseLowest,
                                                    fastingHighestGlucose, fastingLowestGlucose, fastingAverageGlucose,
                                                    afterEatingHighestGlucose, afterEatingLowestGlucose, afterEatingAverageGlucose,
                                                    after2_3HrsEatingHighestGlucose, after2_3HrsEatingLowestGlucose, after2_3HrsEatingAverageGlucose);
                                        }
                                    }
                                }
                            });
                }
            }
            refreshOverAllData(3000);
        }



    }

    private void refreshOverAllData(int i) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpOverAllData();
            }
        }, i);
    }

    private void setUpHighestLowestAndAverage(long glucoseAverage, long glucoseHighest, long glucoseLowest,
                                              long fastingHighestGlucose, long fastingLowestGlucose, long fastingAverageGlucose,
                                              long afterEatingHighestGlucose, long afterEatingLowestGlucose, long afterEatingAverageGlucose,
                                              long after2_3HrsEatingHighestGlucose, long after2_3HrsEatingLowestGlucose, long after2_3HrsEatingAverageGlucose) {

        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();

            if (patientId != null){
                HashMap<String, Object> update = new HashMap<>();
                update.put("highest_glucose", glucoseHighest);
                update.put("lowest_glucose", glucoseLowest);
                update.put("average_glucose", glucoseAverage);
                update.put("fasting_highest", fastingHighestGlucose);
                update.put("fasting_lowest", fastingLowestGlucose);
                update.put("fasting_average", fastingAverageGlucose);
                update.put("after_eating_highest", afterEatingHighestGlucose);
                update.put("after_eating_lowest", afterEatingLowestGlucose);
                update.put("after_eating_average", afterEatingAverageGlucose);
                update.put("after_2_3_hours_highest", after2_3HrsEatingHighestGlucose);
                update.put("after_2_3_hours_lowest", after2_3HrsEatingLowestGlucose);
                update.put("after_2_3_hours_average", after2_3HrsEatingAverageGlucose);

                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .update(update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Log.d("TAG", "Highest, Lowest and Average glucose of patient updated");
                                else
                                    Log.d("TAG", "Highest, Lowest and Average glucose of patient failed to update");
                            }
                        });
            }
        }



    }

    private void setUpConnectedToSensorLayout() {

       if (getContext()!= null){
           userDetails = new UserDetails(getContext());
           boolean isConnected = userDetails.isAccountConnectedToSensor();

           if (isConnected)
               connectedToSensorLayout.setVisibility(View.VISIBLE);
           else {
               connectedToSensorLayout.setVisibility(View.GONE);
               glucoseLevelTV.setText("0");
           }
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
     void setUpPatientName() {
         if (getContext()!= null){
            userDetails = new UserDetails(getContext());
             email = userDetails.getEmail();
             patientId = userDetails.getPatientId();
             name = userDetails.getName();
             nameTextview.setText(name);
         }

     }

}