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







    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initWidgets(view);
        setUpConnectedToSensorLayout();




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

                                            setMonthDocumentData(highestGlucose, lowestGlucose, averageGlucose);
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

    private void setMonthDocumentData(int highestGlucose, int lowestGlucose, int averageGlucose){



        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            String patientId = userDetails.getPatientId();
            String dateId = DateAndTimeUtils.dateIdFormat();
            String month = DateAndTimeUtils.getMonth(dateId);
            String year = DateAndTimeUtils.getYear(dateId);
            String time = DateAndTimeUtils.time24HrsFormat();
            String date = DateAndTimeUtils.getDate();

            if (patientId != null){
                DocumentReference documentReferenceReferenceMonth = FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .collection("year_" + year).document(month + "_" + year);

                HashMap<String, Object> saveData = new HashMap<>();
                saveData.put("highest_glucose", highestGlucose);
                saveData.put("lowest_glucose", lowestGlucose);
                saveData.put("average_glucose", averageGlucose);
                saveData.put("date", date);
                saveData.put("time", time);

                if(documentReferenceReferenceMonth != null){
                    documentReferenceReferenceMonth.set(saveData)
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
    }

    private void fastingStatus(int glucoseLevel){

        if (glucoseLevel < 80) {

            glucoseStatusColor.setBackgroundResource(R.drawable.low_glucose_status_color);
            glucoseStatusTV.setText("Low");


        }

        //Normal glucose
        else if (glucoseLevel > 80 && glucoseLevel <= 100) {

            glucoseStatusColor.setBackgroundResource(R.drawable.normal_glucose_status_color);
            glucoseStatusTV.setText("Normal");
        }

        // Pre-Diabetic
        else if (glucoseLevel >= 130 && glucoseLevel <= 199) {

            glucoseStatusColor.setBackgroundResource(R.drawable.pre_diabetic_glucose_status_color);
            glucoseStatusTV.setText("Pre-Diabetic");
        }
    }

    private void setUpGlucoseStatusColor(String glucoseLevelString) {

        if (getContext()!= null) {
            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();

            int glucoseLevel = Integer.parseInt(glucoseLevelString);

            //Low glucose
            if (glucoseLevel < 60) {

                glucoseStatusColor.setBackgroundResource(R.drawable.low_glucose_status_color);
                glucoseStatusTV.setText("Low");


            }

            //Normal glucose
            else if (glucoseLevel >= 60 && glucoseLevel <= 130) {

                glucoseStatusColor.setBackgroundResource(R.drawable.normal_glucose_status_color);
                glucoseStatusTV.setText("Normal");
            }

            // Pre-Diabetic
            else if (glucoseLevel >= 130 && glucoseLevel <= 199) {

                glucoseStatusColor.setBackgroundResource(R.drawable.pre_diabetic_glucose_status_color);
                glucoseStatusTV.setText("Pre-Diabetic");
            }


            //High glucose
            else if (glucoseLevel >= 200) {

                DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();

                glucoseStatusColor.setBackgroundResource(R.drawable.high_glucose_status_color);
                glucoseStatusTV.setText("High");

                HashMap<String, Object> notificationUpdate = new HashMap<>();
                notificationUpdate.put("glucose_level", glucoseLevel);
                notificationUpdate.put("time", dateAndTimeUtils.getTimeWithAMAndPM());
                notificationUpdate.put("date", dateAndTimeUtils.getDate());
                notificationUpdate.put("name", name);
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


                                            setUpHighestLowestAndAverage(glucoseAverage, glucoseHighest, glucoseLowest);
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

    private void setUpHighestLowestAndAverage(long glucoseAverage, long glucoseHighest, long glucoseLowest) {

        if (getContext() != null){
            userDetails = new UserDetails(getContext());
            patientId = userDetails.getPatientId();

            if (patientId != null){
                HashMap<String, Object> update = new HashMap<>();
                update.put("highest_glucose", glucoseHighest);
                update.put("lowest_glucose", glucoseLowest);
                update.put("average_glucose", glucoseAverage);

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