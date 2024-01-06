package com.example.dpatient.SignUpMethod;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.example.dpatient.R;

public class SignUpStep1 extends AppCompatActivity {
    private AppCompatEditText firstNameET;
    private AppCompatEditText lastNameET;
    private AppCompatEditText ageET;
    private AppCompatEditText addressET;

    private RadioButton maleRB;
    private RadioButton femaleRB;
    private RadioButton preferNotToSayRB;
    private AppCompatButton nextBtn;
    private Toolbar toolbar;
    private RelativeLayout genderError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step1);

        initWidgets();
        setUpToolbar();
        setUpGenderError();

        nextBtn.setOnClickListener(v->{
            String firstName = firstNameET.getText().toString();
            String lastName = lastNameET.getText().toString();
            String age = ageET.getText().toString();
            String address = addressET.getText().toString();
            String gender = "Prefer not to say";

            if(firstName.isEmpty()){
                firstNameET.setError("Enter first name");
            }
            else if (lastName.isEmpty()){
                lastNameET.setError("Enter last name");
            }
            else if (!maleRB.isChecked() && !femaleRB.isChecked() && !preferNotToSayRB.isChecked()){
                genderError.setVisibility(View.VISIBLE);
            }
            else if (age.isEmpty()){
                ageET.setError("Enter age");
            }
            else if (address.isEmpty()){
                addressET.setError("Enter Error");
            }
            else {

                 if (femaleRB.isChecked())
                    gender = "Female";
                else if (maleRB.isChecked())
                    gender = "Male";
                else if (preferNotToSayRB.isChecked())
                    gender = "Prefer not to say";

                Intent intent = new Intent(getApplicationContext(), SignUpStep2.class);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("age", age);
                intent.putExtra("address", address);
                intent.putExtra("gender", gender);
                startActivity(intent);
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setUpGenderError() {

        genderError.setVisibility(View.GONE);

        maleRB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view,  MotionEvent motionEvent) {
                genderError.setVisibility(View.GONE);
                return false;
            }
        });

        femaleRB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                genderError.setVisibility(View.GONE);
                return false;
            }
        });

        preferNotToSayRB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                genderError.setVisibility(View.GONE);
                return false;
            }
        });
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeActionContentDescription("Back");
    }

    private void proceedToStep2(String firstName, String lastName,String age, String address, String gender) {



    }

    @SuppressLint("WrongViewCast")
    private void initWidgets() {
        firstNameET = findViewById(R.id.firstName_Edittext);
        lastNameET = findViewById(R.id.lastName_Edittext);
        ageET = findViewById(R.id.age_EditTxt);
        addressET = findViewById(R.id.address_EditTxt);

        maleRB = findViewById(R.id.radioBtn_Male);
        femaleRB = findViewById(R.id.radioBtn_Female);
        preferNotToSayRB = findViewById(R.id.preferNotToSay_RadioButton);

        nextBtn = findViewById(R.id.next_Button);

        toolbar = findViewById(R.id.toolbar);

        genderError = findViewById(R.id.genderError_Layout);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}