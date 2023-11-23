package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.dpatient.bottomnavfragment.HomeFragment;
import com.example.dpatient.bottomnavfragment.MessagesFragment;
import com.example.dpatient.bottomnavfragment.ReportsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class Homepage1 extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{

    BottomNavigationView bottomNavigationView;

    String value = "Home";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage1);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(this);
        Fragment selectedFragment = null;

        if (value.equalsIgnoreCase("Home")){
            selectedFragment = new HomeFragment();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();

    }


    private NavigationBarView.OnItemSelectedListener navListener = item -> {

        Fragment selectedFragment = null;

        int itemId = item.getItemId();


        if (itemId == R.id.HomeNav){
            selectedFragment = new HomeFragment();
        }
        else if (itemId == R.id.ReportsNav){
            selectedFragment = new ReportsFragment();
        }
        else if (itemId == R.id.MessageNav){
            selectedFragment = new MessagesFragment();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
        return true;
    };


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();


        if (itemId == R.id.HomeNav){
            selectedFragment = new HomeFragment();
        }
        else if (itemId == R.id.ReportsNav){
            selectedFragment = new ReportsFragment();
        }
        else if (itemId == R.id.MessageNav){
            selectedFragment = new MessagesFragment();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
        return true;
    }
}