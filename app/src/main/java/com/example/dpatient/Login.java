package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.dpatient.SplashScreen.SplashScreenAfterOpeningApp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;

public class Login extends AppCompatActivity {

    EditText password, email;
    Button loginBtn;
    ProgressBar loginProgressBar;

    boolean passwordVisible;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password = findViewById(R.id.password_EditText);
        email = findViewById(R.id.username_EditTxt);
        loginBtn = findViewById(R.id.login_Btn);
        loginProgressBar = findViewById(R.id.login_ProgressBar);

        firebaseAuth = FirebaseAuth.getInstance();

        passwordHideMethod();
        noInternetDialog();
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();

                loginBtn.setVisibility(View.GONE);
                loginProgressBar.setVisibility(View.VISIBLE);

                if (emailString.isEmpty()){
                    email.setError("Enter email");
                    loginBtn.setVisibility(View.VISIBLE);
                    loginProgressBar.setVisibility(View.GONE);
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
                    email.setError("Invalid email");
                    loginBtn.setVisibility(View.VISIBLE);
                    loginProgressBar.setVisibility(View.GONE);
                }
                else if(passwordString.isEmpty()){
                    password.setError("Enter password");
                    loginBtn.setVisibility(View.VISIBLE);
                    loginProgressBar.setVisibility(View.GONE);
                }
                else {

                    firebaseAuth.signInWithEmailAndPassword(emailString, passwordString)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        loginBtn.setVisibility(View.GONE);
                                        loginProgressBar.setVisibility(View.VISIBLE);

                                        verifyPatientAccount(FirebaseAuth.getInstance().getUid());
//                                        if(firebaseAuth.getCurrentUser().isEmailVerified()){
//
//                                        }
//                                        else {
//                                            Toast.makeText(getApplicationContext(), "Please verify your Email", Toast.LENGTH_LONG).show();
//                                            loginBtn.setVisibility(View.VISIBLE);
//                                            loginProgressBar.setVisibility(View.GONE);
//                                        }

                                    } else {
                                        Toast.makeText(getApplicationContext(), "SIGN IN FAILED", Toast.LENGTH_SHORT).show();
                                        loginBtn.setVisibility(View.VISIBLE);
                                        loginProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });


                }




            }
        });
    }

    void verifyPatientAccount(String userUID){
        FirebaseFirestore.getInstance().collection("UsersUID").document(userUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                if (documentSnapshot.getString("accountType").equals("patientAccount")){
                                    loginBtn.setVisibility(View.GONE);
                                    loginProgressBar.setVisibility(View.VISIBLE);

                                    //Account type is patient, means it can proceed
                                    Toast.makeText(getApplicationContext(),"Sign in successfully", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(), Homepage1.class));

                                }
                                else {

                                    loginBtn.setVisibility(View.VISIBLE);
                                    loginProgressBar.setVisibility(View.GONE);

                                    //This account type is doctor, means it cannot proceed
                                    email.setText("");
                                    password.setText("");
                                    Toast.makeText(getApplicationContext(), "Failed to log in", Toast.LENGTH_LONG).show();
                                    FirebaseAuth.getInstance().signOut();

                                }
                            }
                        }
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void passwordHideMethod() {

        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                final int Right = 2;

                if (motionEvent.getAction()== MotionEvent.ACTION_UP){
                    if (motionEvent.getRawX()>= password.getRight()-password.getCompoundDrawables()[Right].getBounds().width()){
                        int selection = password.getSelectionEnd();
                        if (passwordVisible){
                            //set drawable image here
                            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, R.drawable.baseline_visibility_off_24, 0);
                            // for hide password
                            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        }
                        else {

                            //set drawable image here
                            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, R.drawable.baseline_visibility_24, 0);
                            // for show password
                            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;

                        }
                        password.setSelection(selection);
                        return true;
                    }
                }
                return false;
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
    protected void onStart(){
        super.onStart();

        if (firebaseAuth.getCurrentUser()!= null){
            finish();
            startActivity(new Intent(getApplicationContext(), SplashScreenAfterOpeningApp.class));
        }
    }
}