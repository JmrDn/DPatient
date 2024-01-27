package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SensorPage extends AppCompatActivity {
    private AppCompatButton disconnectBtn;
    private TextView sensorId;
    private UserDetails userDetails;
    private String sensorIdString;
    private Dialog disconnectDialog;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private  AppCompatButton yesBtn;
    private  AppCompatButton noBtn;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_page);
        initWidgets();

        setSensorId();
        setUpToolbar();



        disconnectBtn.setOnClickListener(v->{
            showDisconnectDialog();
        });
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeActionContentDescription("Back");
    }
    public void startProgressDialog(Boolean visible){
        if (getApplicationContext()!= null){
            progressDialog = new ProgressDialog(SensorPage.this);
            progressDialog.setTitle("Disconnecting...");
        }

        if(visible){
            progressDialog.show();
        }
        else {
            if (progressDialog != null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }

        }

    }

    private void showDisconnectDialog() {

        disconnectDialog = new Dialog(SensorPage.this);
        disconnectDialog.setContentView(R.layout.disconnect_option_dialog);
        disconnectDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        disconnectDialog.setCancelable(false);
        disconnectDialog.show();

        yesBtn = disconnectDialog.findViewById(R.id.yes_Button);
        noBtn = disconnectDialog.findViewById(R.id.no_Button);

        if (getApplicationContext() != null){

            userDetails = new UserDetails(SensorPage.this);


            String patientId = userDetails.getPatientId();
            String sensorId = userDetails.getSensorId();

            yesBtn.setOnClickListener(v-> {
                startProgressDialog(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (patientId != null && sensorId != null){

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(sensorId);

                            userDetails.setSensorId(null);
                            userDetails.setAccountIsConnectedToSensor(false);
                            //Set up sensor case to false
                            databaseReference.child("isConnected").setValue(false);
                            databaseReference.child("isConnectedTo").setValue(null);
                            databaseReference.child("glucose_Level").setValue(0);

                            //Set up patient case to false
                            HashMap<String, Object> updateConnectedToSensor = new HashMap<>();
                            updateConnectedToSensor.put("isConnectedTo", null);
                            updateConnectedToSensor.put("isConnectedToSensor", false);

                            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                                    .update(updateConnectedToSensor)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Log.d("TAG", "Connected to sensor Updated to false");


                                            } else {
                                                Log.d("TAG", "Connected to sensor is not updated to false");
                                            }
                                        }
                                    });

                        }

                        disconnectDialog.dismiss();


                        Toast.makeText(getApplicationContext(),"Successfully Disconnected", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), Homepage1.class));
                        finish();
                    }
                },3000);


            });

            noBtn.setOnClickListener(v -> {
                disconnectDialog.cancel();
            });

        }

    }




    private void setSensorId() {

        if (getApplicationContext() != null){
            userDetails = new UserDetails(getApplicationContext());
            String patientId = userDetails.getPatientId();
            boolean isConnectFromUserDetails = userDetails.isAccountConnectedToSensor();

            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()){
                                    if(documentSnapshot.contains("isConnectedToSensor") && documentSnapshot.contains("isConnectedTo")){
                                        boolean isConnected = documentSnapshot.getBoolean("isConnectedToSensor");
                                        if (isConnected && isConnectFromUserDetails){
                                            if (documentSnapshot.contains("isConnectedTo")){
                                                sensorId.setText(documentSnapshot.getString("isConnectedTo"));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });

        }


    }

    private void initWidgets() {
        disconnectBtn = findViewById(R.id.disconnect_Button);
        sensorId = findViewById(R.id.sensorId_Textview);

        toolbar = findViewById(R.id.toolbar);
        userDetails = new UserDetails(getApplicationContext());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}