package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;

public class DoctorInfo extends AppCompatActivity {

    Button chatDoctorBtn;
    String doctorId, doctorName;
    TextView viewLocationBtn, drName;
    ImageView backBtn;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_info);

        chatDoctorBtn = findViewById(R.id.chatDoctor_Btn);
        viewLocationBtn = findViewById(R.id.viewLocation_Textview);
        backBtn = findViewById(R.id.back_Btn);
        drName = findViewById(R.id.drName_Textview);



        Intent intent = getIntent();
        doctorId = intent.getStringExtra("doctorUID");
        doctorName = intent.getStringExtra("doctorName");

        setDoctorName();
        noInternetDialog();

        backBtn.setOnClickListener(v->{
            onBackPressed();
        });

        chatDoctorBtn.setOnClickListener(v->{
            Intent intent1 = new Intent(getApplicationContext(), DoctorChatRoom.class);
            intent1.putExtra("doctorId", doctorId);
            intent1.putExtra("doctorName", doctorName);
            startActivity(intent1);
        });

        viewLocationBtn.setOnClickListener(v->{
            goLink("https://maps.app.goo.gl/YdFKPbv6XzcjcJfU7");
        });


    }

    private void setDoctorName() {

       drName.setText(doctorName);
    }

    private void goLink(String linkLocation) {
        Uri uri = Uri.parse(linkLocation);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
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