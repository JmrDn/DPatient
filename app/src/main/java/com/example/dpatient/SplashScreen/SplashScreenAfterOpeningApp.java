package com.example.dpatient.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.dpatient.Homepage1;
import com.example.dpatient.R;

@SuppressLint("CustomSplashScreen")
public class SplashScreenAfterOpeningApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_after_opening_app);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), Homepage1.class));
            }
        },1500);
    }
}