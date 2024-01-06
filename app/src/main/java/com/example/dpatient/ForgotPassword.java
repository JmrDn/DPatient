package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    AppCompatEditText emailET;
    AppCompatButton submitBtn;
    FirebaseAuth firebaseAuth;
    Toolbar toolbar;
    ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailET = findViewById(R.id.email_Edittext);
        submitBtn = findViewById(R.id.submit_Button);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        setupToolbar();

        submitBtn.setOnClickListener(v->{
            progressBar.setVisibility(View.VISIBLE);
            submitBtn.setVisibility(View.GONE);
            String email = emailET.getText().toString();
            if (email.isEmpty()){
                progressBar.setVisibility(View.GONE);
                submitBtn.setVisibility(View.VISIBLE);
                emailET.setError("Enter email");
            }
            else {
                forgotPassword(email);
            }

        });
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeActionContentDescription("Back");

    }

    private void forgotPassword(String email) {
        progressBar = findViewById(R.id.progressBar);
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.VISIBLE);
                    submitBtn.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Successfully send, Please check your email to reset password", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    submitBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Enter valid email", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                submitBtn.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();

    }
}