package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dpatient.databinding.ActivityMainBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class YourProfile extends AppCompatActivity {


    ShapeableImageView profilePicImageview;
    ImageButton editProfilePicBtn;
    ImageView backBtn;
    TextView nameTextview,
            emailTextview;
    public String patientId;
    Button logOutBtn;

    StorageReference storageReference;
    private  UserDetails userDetails;
    private TextView patientIdTV;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_profile);

        profilePicImageview = findViewById(R.id.profilePicture_Imageview);
        editProfilePicBtn = findViewById(R.id.editImage_Btn);
        nameTextview = findViewById(R.id.name_TxtView);
        logOutBtn = findViewById(R.id.logout_Btn);
        backBtn = findViewById(R.id.back_Btn);
        emailTextview = findViewById(R.id.email_Textview);
        patientIdTV = findViewById(R.id.patientId_Textview);

        backBtn.setOnClickListener(v->{
            onBackPressed();

        });

        logOutBtn.setOnClickListener(view -> {
            if (getApplicationContext() != null)
                userDetails = new UserDetails(getApplicationContext());

            userDetails.logout();
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getApplicationContext(),"Log out successfully", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), Login.class));

        });

        setProfilePicture();

        getNameAndEmail();

        editProfilePicBtn.setOnClickListener(v->{
          Intent intent = new Intent(getApplicationContext(), EditProfile.class);
          intent.putExtra("name", nameTextview.getText().toString());
          intent.putExtra("patientId", patientId);
          startActivity(intent);
        });
    }


    private void setProfilePicture() {
        Intent intent = getIntent();
        String patientId = intent.getStringExtra("patientId");

        patientIdTV.setText(patientId);

        if (patientId != null){
            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                String fileName = documentSnapshot.getString("profilePicture");

                                retrieveImage(fileName);
                            }
                        }
                    });
        }
    }
    private  void retrieveImage(String fileName){
        storageReference = FirebaseStorage.getInstance().getReference("images/"+ fileName);

        try {
            File localFile = File.createTempFile("tempfile", "jpg");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            profilePicImageview.setImageBitmap(bitmap);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "Failed to retrieve Profile Picture: " + e.getCause());

                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    private void getNameAndEmail(){
        Intent intent = getIntent();
         patientId = intent.getStringExtra("patientId");
        nameTextview.setText(intent.getStringExtra("name"));
        emailTextview.setText(intent.getStringExtra("email"));
    }
    
}