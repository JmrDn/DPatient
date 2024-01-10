package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.dpatient.Utils.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;

import java.util.HashMap;
import java.util.Objects;

public class ConnectingToSensorPage extends AppCompatActivity {

    ImageButton backBtn;
    Button pairBtn;
    EditText sensorIdInput;
    Dialog dialog;
    ProgressDialog progressDialog;
    private UserDetails userDetails;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting_to_sensor_page);


        backBtn = findViewById(R.id.back_Btn);
        pairBtn = findViewById(R.id.pair_Btn);
        sensorIdInput = findViewById(R.id.input_Edittext);



        if (getApplicationContext() != null){


            progressDialog = new ProgressDialog(ConnectingToSensorPage.this);
            progressDialog.setTitle("Connecting...");

            userDetails = new UserDetails(getApplicationContext());

            boolean isConnected = userDetails.isAccountConnectedToSensor();

            if (!isConnected){
                pairBtn.setOnClickListener(v ->{
                    startProgressDialog(true);
                    String sensorIdInputString = sensorIdInput.getText().toString();
                    if (sensorIdInputString.isEmpty()){
                        sensorIdInput.setError("Enter sensor Id");
                        startProgressDialog(false);
                    }
                    else {
                        startProgressDialog(true);
                        connectingToSensorMethod(sensorIdInputString);

                    }
                });
            }
        }

        noInternetDialog();



        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });

    }

    public void startProgressDialog(Boolean visible){

        if(visible){
            progressDialog.show();
        }
        else {

            progressDialog.dismiss();

            if(progressDialog.isShowing() && progressDialog != null){
                progressDialog.dismiss();
            }

        }



    }
    void connectingToSensorMethod(String sensorIdInputString){
        if (getApplicationContext() != null){
            userDetails = new UserDetails(ConnectingToSensorPage.this);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(sensorIdInputString);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        if (Objects.requireNonNull(snapshot.child("isConnected").getValue()).equals(true)){
                            startProgressDialog(false);
                            sensorIdInput.setError("This sensor is already connected to other account");
                            sensorIdInput.setText("");

                        }
                        else {

                            startProgressDialog(false);
                            //You are now connected to sensor
                            connectedToSensorDialog();


                            //Get the sensorID
                            String sensorId = snapshot.child("sensorId").getValue().toString();
                            userDetails.setAccountIsConnectedToSensor(true);
                            userDetails.setSensorId(sensorId);

                            //Calling patientId to update the patient connection status to sensor
                            FirebaseFirestore.getInstance().collection("UsersUID").document(FirebaseUtil.currentUserUID())
                                    .get()
                                    .addOnCompleteListener(task ->{
                                        if (task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()){
                                                String patientId = document.getString("patientID");


                                                updatePatientConnectionStatusToSensor(patientId, sensorId);
                                                HashMap hashMap = new HashMap();
                                                //Updating the connection status of Sensor and where is it connected
                                                hashMap.put("isConnected", true);
                                                hashMap.put("isConnectedTo", patientId);


                                                databaseReference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                                    @Override
                                                    public void onSuccess(Object o) {
                                                        Log.d("TAG", "sensor is successfully connected");
                                                    }
                                                });

                                            }
                                        }
                                        else {
                                            Log.d("TAG", "task is unsuccessful");
                                        }
                                    });

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startProgressDialog(false);

                                    startActivity(new Intent(getApplicationContext(), Homepage1.class));
                                    finish();
                                }
                            },5000);


                        }
                    }
                    else {
                        startProgressDialog(false);
                        //Set up error when the user entered a wrong sensor ID
                        sensorIdInput.setError("Enter valid sensor ID");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("TAG", error.getMessage());
                }
            });

        }

    }
    void connectedToSensorDialog(){
        dialog = new Dialog(ConnectingToSensorPage.this);
        dialog.setContentView(R.layout.you_are_now_connected_to_sensor_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        dialog.show();




    }
    void updatePatientConnectionStatusToSensor(String patientId, String sensorId){


        HashMap <String, Object>hashMap = new HashMap();
        hashMap.put("isConnectedToSensor", true);
        hashMap.put("isConnectedTo", sensorId);
        FirebaseFirestore.getInstance().collection("Users").document(patientId)
                .update(hashMap)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d("TAG", "Updating patientId value is success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", Objects.requireNonNull(e.getMessage()));
                    }
                });
    }
    private void noInternetDialog() {
        NoInternetDialogPendulum.Builder builder = new NoInternetDialogPendulum.Builder(
                this,
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