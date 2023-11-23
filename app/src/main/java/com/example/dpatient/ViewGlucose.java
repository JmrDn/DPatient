package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dpatient.bottomnavfragment.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class ViewGlucose extends AppCompatActivity {
    Button viewHistoryBtn;
    ImageButton backBtn;
    TextView dateTextview,
            timeTextview,
            glucoseLevelTextview,
            glucoseLevelStatusTextview,
            isConnectToDeviceId;

    ImageView trendArrowImageview;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_glucose);
        viewHistoryBtn = findViewById(R.id.viewHistory_Btn);
        backBtn = findViewById(R.id.back_Btn);
        dateTextview = findViewById(R.id.date_Textview);
        timeTextview = findViewById(R.id.time_Textview);
        glucoseLevelTextview = findViewById(R.id.glucoseLevel_Textview);
        glucoseLevelStatusTextview = findViewById(R.id.glucoseLevelStatus_Textview);
        trendArrowImageview = findViewById(R.id.trendArrow_Imageview);
        isConnectToDeviceId = findViewById(R.id.connectedTo_Textview);

        Intent intent = getIntent();
        String sensorId = intent.getStringExtra("sensorId");
        String patientId = intent.getStringExtra("patientId");

        setUpTrendArrow(sensorId, patientId);
        setDetectedGlucoseFromSensorMethod(sensorId, patientId);
        getDetectedGlucoseFromSensorMethod(patientId);
        showDeviceIdConnected();


        backBtn.setOnClickListener(view -> {
            onBackPressed();
        });

        viewHistoryBtn.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ViewHistory.class));
        });
    }

    private void showDeviceIdConnected() {
        Intent intent = getIntent();
        String deviceId = intent.getStringExtra("sensorId");
        isConnectToDeviceId.setText("Connected to Device ID: " + deviceId);
    }

    private void setUpTrendArrow(String sensorId, String patientId){
        FirebaseFirestore.getInstance().collection("Users").document(patientId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                           String recentGlucoseLevel = documentSnapshot.getString("recent_glucose_level");

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(sensorId);
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        String newGlucoseLevel = snapshot.child("glucose_Level").getValue().toString();
                                        int recentGlucoseLevelInt = Integer.parseInt(recentGlucoseLevel);
                                        int newGlucoseLevelInt = Integer.parseInt(newGlucoseLevel);

                                        if (newGlucoseLevelInt > recentGlucoseLevelInt){
                                            trendArrowImageview.setImageResource(R.drawable.uptrendarrow);
                                        }
                                        else if (newGlucoseLevelInt < recentGlucoseLevelInt){
                                            trendArrowImageview.setImageResource(R.drawable.downtrendarrow);
                                        }
                                        else if (newGlucoseLevelInt == recentGlucoseLevelInt){
                                            trendArrowImageview.setImageResource(R.drawable.nochangestrendarrow);
                                        }
                                    }
                                    else {
                                        Log.d("TAG", "snapshot does not exist");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }
                });
        refreshTrendArrow(5000);

    }

    private void setDetectedGlucoseFromSensorMethod(String sensorId,String patientId) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(sensorId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String time = snapshot.child("time").getValue().toString();
                    String date = snapshot.child("date").getValue().toString();
                    String glucoseLevel = snapshot.child("glucose_Level").getValue().toString();

                    HashMap<String, Object> recentGlucoseData = new HashMap<>();
                    recentGlucoseData.put("time", time);
                    recentGlucoseData.put("date", date);
                    recentGlucoseData.put("recent_glucose_level", glucoseLevel);

                    FirebaseFirestore.getInstance().collection("Users").document(patientId)
                            .update(recentGlucoseData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("TAG", "Recent glucose data successfully updated");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", Objects.requireNonNull(e.getMessage()));

                                }
                            });



                }
                else {
                    Log.d("TAG", "no such value");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        refreshSetDetectedGlucoseFromSensorMethod(5000);

    }

    private void getDetectedGlucoseFromSensorMethod(String patientId) {
        FirebaseFirestore.getInstance().collection("Users").document(patientId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            String glucoseLevelString = documentSnapshot.getString("recent_glucose_level");
                            String time = documentSnapshot.getString("time");
                            String date = documentSnapshot.getString("date");
                            String name = documentSnapshot.getString("fullName");
                            int glucoseLevel = Integer.parseInt(glucoseLevelString);

                            if (glucoseLevel >= 200) {
                                HashMap<String, Object> notificationUpdate = new HashMap<>();
                                notificationUpdate.put("glucose_level", glucoseLevel);
                                notificationUpdate.put("time", time);
                                notificationUpdate.put("date", date);
                                notificationUpdate.put("name", name);
                                notificationUpdate.put("patientID", patientId);

                                FirebaseFirestore.getInstance().collection("high_glucose_list").document(patientId)
                                        .set(notificationUpdate)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d("Firebase Database", "Added to high glucose list");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Firebase Database", "Failed to add high glucose list");
                                            }
                                        });
                            }
                            timeTextview.setText(time);
                            dateTextview.setText(date);
                            glucoseLevelTextview.setText(glucoseLevelString);

                            setUpGlucoseHistory(timeTextview.getText().toString(), dateTextview.getText().toString(), glucoseLevelTextview.getText().toString(), patientId);
                        } else {
                            Log.d("TAG", "no such document");
                        }
                    } else {
                        Log.d("TAG", "task is unsuccessful");
                    }

                });
        refreshGetDetectedGlucoseFromSensorMethod(5000);
    }

    private void setUpGlucoseHistory(String time, String date, String glucoseLevel, String patientId) {
       final String dateAndTimeDocumentName = date + "_" + time;
       String glucoseHistoryDocumentName = dateAndTimeDocumentName.replace(' ', '_');
       String glucoseHistoryDocumentName1 = glucoseHistoryDocumentName.replace('/', '_');;



        HashMap <String, Object> glucoseHistory = new HashMap<>();
        glucoseHistory.put("time", time);
        glucoseHistory.put("date", date);
        glucoseHistory.put("glucose_level", glucoseLevel);
        glucoseHistory.put("timeStamp", Timestamp.now());

        FirebaseFirestore.getInstance().collection("Users").document(patientId)
                .collection("glucose_Level_History").document(glucoseHistoryDocumentName1)
                .set(glucoseHistory)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("TAG", "New data of glucose history added");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG","Failed to add data of glucose history");

                    }
                });


    }

    private void refreshGetDetectedGlucoseFromSensorMethod(int milliseconds) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                String patientId = intent.getStringExtra("patientId");
                String sensorId = intent.getStringExtra("sensorId");
                getDetectedGlucoseFromSensorMethod(patientId);

            }
        };

        handler.postDelayed(runnable,milliseconds);
    }

    private void refreshSetDetectedGlucoseFromSensorMethod(int milliseconds) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                String patientId = intent.getStringExtra("patientId");
                String sensorId = intent.getStringExtra("sensorId");
                setDetectedGlucoseFromSensorMethod(sensorId, patientId);


            }
        };

        handler.postDelayed(runnable,milliseconds);
    }

    private void refreshTrendArrow(int milliseconds) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                String patientId = intent.getStringExtra("patientId");
                String sensorId = intent.getStringExtra("sensorId");

                setUpTrendArrow(sensorId,patientId);


            }
        };

        handler.postDelayed(runnable,milliseconds);
    }
}