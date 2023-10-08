package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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

                                        if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                            loginBtn.setVisibility(View.GONE);
                                            loginProgressBar.setVisibility(View.VISIBLE);
                                            Toast.makeText(getApplicationContext(),"Sign in successfully", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(getApplicationContext(), HomePage.class));
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "Please verify your Email", Toast.LENGTH_LONG).show();
                                            loginBtn.setVisibility(View.VISIBLE);
                                            loginProgressBar.setVisibility(View.GONE);
                                        }

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

    protected void onStart(){
        super.onStart();

        if (firebaseAuth.getCurrentUser()!= null){
            finish();
            startActivity(new Intent(getApplicationContext(), HomePage.class));
        }
    }
}