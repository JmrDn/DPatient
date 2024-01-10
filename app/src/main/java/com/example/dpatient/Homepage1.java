package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.dpatient.Utils.FirebaseUtil;
import com.example.dpatient.bottomnavfragment.ConnectWithFragment;
import com.example.dpatient.bottomnavfragment.HomeFragment;
import com.example.dpatient.bottomnavfragment.MessagesFragment;
import com.example.dpatient.bottomnavfragment.ReportsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Homepage1 extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{

    BottomNavigationView bottomNavigationView;
    private AppCompatButton connectBtn;

    String value = "Home";
    public boolean isHomeFragmentActive;
    private UserDetails userDetails;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage1);
        initWidgets();
        setUpAccountConnectedToSensor();


        bottomNavigationView.setOnItemSelectedListener(this);
        Fragment selectedFragment = null;

        if (value.equalsIgnoreCase("Home")){
            isHomeFragmentActive = true;
            selectedFragment = new HomeFragment();
        }


        getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();



        connectBtn.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(), ConnectingToSensorPage.class));
        });

    }

    private void initWidgets() {

        connectBtn = findViewById(R.id.connectToSensor_Button);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();


        if (itemId == R.id.HomeNav){
            isHomeFragmentActive = true;
            selectedFragment = new HomeFragment();
        }
        else if (itemId == R.id.ReportsNav){
            isHomeFragmentActive = false;
            selectedFragment = new ReportsFragment();
        }
        else if(itemId == R.id.ConnectedWithNav){
            isHomeFragmentActive = false;
            selectedFragment = new ConnectWithFragment();
        }
        else if (itemId == R.id.MessageNav) {
            isHomeFragmentActive = false;
            selectedFragment = new MessagesFragment();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
        return true;
    }

    @Override
    public void onBackPressed() {
        if(isHomeFragmentActive){
            finishAffinity();
        }
        else{
            super.onBackPressed();
        }

    }

    public void  setUpAccountConnectedToSensor() {
       if (FirebaseAuth.getInstance().getCurrentUser() != null){
           FirebaseUtil.currentUserDetails().get()
                   .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                           if (task.isSuccessful()) {
                               DocumentSnapshot documentSnapshot = task.getResult();

                               if (documentSnapshot.exists()) {
                                   String patientId = documentSnapshot.getString("patientID");

                                   getResult(patientId);
                               }
                           }
                       }
                   });
       }
    }

    private void getResult(String patientId) {

        if(getApplicationContext()!= null){
            userDetails = new UserDetails(getApplicationContext());
            boolean isConnected = userDetails.isAccountConnectedToSensor();

            if(isConnected){
                connectBtn.setVisibility(View.GONE);
            }
            else{
                connectBtn.setVisibility(View.VISIBLE);
            }
        }
    }


}