package com.example.dpatient.SignUpMethod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.dpatient.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Random;

public class SignUpStep2 extends AppCompatActivity {
    private AppCompatEditText emailET;
    private AppCompatEditText passwordET;
    private AppCompatEditText confirmPasswordET;
    private AppCompatButton createAccBtn;
    private ProgressBar createAccPB;
    private Toolbar toolbar;
    private boolean patientIdExist = false;
    private String patientId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step2);

        initWidgets();
        setUpToolbar();

        createAccPB.setVisibility(View.GONE);

        createAccBtn.setOnClickListener(v->{
            createAccBtn.setVisibility(View.GONE);
            createAccPB.setVisibility(View.VISIBLE);
            String email = emailET.getText().toString();
            String password = passwordET.getText().toString();
            String confirmPassword = confirmPasswordET.getText().toString();

            if(email.isEmpty()){
                createAccBtn.setVisibility(View.VISIBLE);
                createAccPB.setVisibility(View.GONE);
                emailET.setError("Enter email");
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                createAccBtn.setVisibility(View.VISIBLE);
                createAccPB.setVisibility(View.GONE);
                emailET.setError("Enter valid email");
            }
            else if(password.isEmpty()){
                createAccBtn.setVisibility(View.VISIBLE);
                createAccPB.setVisibility(View.GONE);
                passwordET.setError("Enter password");
            }
            else if(password.length() < 8){
                createAccBtn.setVisibility(View.VISIBLE);
                createAccPB.setVisibility(View.GONE);
                passwordET.setError("Password consist of 8 characters");
            }
            else if(confirmPassword.isEmpty()){
                createAccBtn.setVisibility(View.VISIBLE);
                createAccPB.setVisibility(View.GONE);
                confirmPasswordET.setError("Enter password");
            }
            else if(!password.equals(confirmPassword)){
                createAccBtn.setVisibility(View.VISIBLE);
                createAccPB.setVisibility(View.GONE);
                passwordET.setError("Password not match");
                confirmPasswordET.setError("Password not match");
            }
            else{
                createAccBtn.setVisibility(View.GONE);
                createAccPB.setVisibility(View.VISIBLE);

                createAccount(email,confirmPassword);
            }
        });


    }

    private void setUpToolbar() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeActionContentDescription("Back");
    }

    @SuppressLint("WrongViewCast")
    private void initWidgets() {
        emailET = findViewById(R.id.email_EditTxt);
        passwordET = findViewById(R.id.password_Edittext);
        confirmPasswordET = findViewById(R.id.confirmPassword_EditText);

        createAccBtn = findViewById(R.id.create_Btn);
        createAccPB = findViewById(R.id.createAcc_ProgressBar);

        toolbar = findViewById(R.id.toolbar);


    }

    private void createAccount(String email, String password) {
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String gender = intent.getStringExtra("gender");
        String age = intent.getStringExtra("age");
        String address = intent.getStringExtra("address");
        String fullName = firstName + " " + lastName;


        String charSet = "1234567890";
        patientId = "";
        Random random = new Random();

        for(int i = 0; i < 6; i++){
            patientId += charSet.charAt(random.nextInt(charSet.length()));
        }

        while (patientIdExist(patientId)){

            for(int i = 0; i < 6; i++){
                patientId += charSet.charAt(random.nextInt(charSet.length()));
            }
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            String userId = FirebaseAuth.getInstance().getUid();


                            HashMap<String, Object> savedUsers = new HashMap<>();
                            savedUsers.put("patientID", patientId);
                            savedUsers.put("fullName", fullName);
                            savedUsers.put("search", fullName.toLowerCase());
                            savedUsers.put("email", email);
                            savedUsers.put("gender", gender);
                            savedUsers.put("age", age);
                            savedUsers.put("userUID", userId);
                            savedUsers.put("address", address);
                            savedUsers.put("recent_glucose_level", "0");
                            savedUsers.put("accountType", "patientAccount");


                            FirebaseFirestore.getInstance().collection("UsersUID").document(userId)
                                    .set(savedUsers)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.d("TAG", "Log in credentials saved [UserUID]");
                                            }
                                            else{
                                                Log.d("TAG", "Log in credentials not saved [UserUID]");
                                            }
                                        }
                                    });
                            HashMap<String, Object> savedUsersPatient = new HashMap<>();
                            savedUsersPatient.put("patientID", patientId);
                            savedUsersPatient.put("fullName", fullName);
                            savedUsersPatient.put("search", fullName.toLowerCase());
                            savedUsersPatient.put("email", email);
                            savedUsersPatient.put("gender", gender);
                            savedUsersPatient.put("age", age);
                            savedUsersPatient.put("userUID", userId);
                            savedUsersPatient.put("address", address);
                            savedUsersPatient.put("recent_glucose_level", "0");
                            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                                    .set(savedUsersPatient)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.d("TAG", "Log in credentials saved [Users]");
                                            }
                                            else{
                                                Log.d("TAG", "Log in credentials not saved [Users]");
                                            }
                                        }
                                    });

                            Toast.makeText(getApplicationContext(), "Account created Successfully", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), AccountCreatedSuccessfully.class));

                        }
                        else{
                            createAccBtn.setVisibility(View.VISIBLE);
                            createAccPB.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Sign up failed " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public boolean patientIdExist(String patientId){

        FirebaseFirestore.getInstance().collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            QuerySnapshot querySnapshot = task.getResult();

                            if(querySnapshot !=null){


                                for(DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()){
                                    String patientIdFromDB = documentSnapshot.getString("patientId");

                                    if(patientId.equals(patientIdFromDB)){
                                       patientIdExist = true;
                                    }
                                    else {
                                        patientIdExist = false;
                                    }
                                }


                            }
                        }
                    }
                });

        return  patientIdExist;
    }
}