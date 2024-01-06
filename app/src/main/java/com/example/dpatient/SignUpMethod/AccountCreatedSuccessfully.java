package com.example.dpatient.SignUpMethod;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;

import com.example.dpatient.Login;
import com.example.dpatient.R;
import com.google.firebase.auth.FirebaseAuth;

public class AccountCreatedSuccessfully extends AppCompatActivity {

    private AppCompatButton done_Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_created_successfully);

        done_Btn = findViewById(R.id.done_Btn);

        done_Btn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
        });
    }

    @Override
    public void onBackPressed() {
       startActivity(new Intent(getApplicationContext(), Login.class));
    }
}