package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class EditProfile extends AppCompatActivity {

    ShapeableImageView profilePicImageview;
    ImageButton selectImageBtn, backBtn;
    Button cancelBtn, uploadBtn;
    Uri uri;

    StorageReference storageReference;
    ProgressDialog progressDialog;
    LinearLayout buttonLayout;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePicImageview = findViewById(R.id.profilePicture_Imageview);
        selectImageBtn = findViewById(R.id.selectImage_Btn);
        uploadBtn = findViewById(R.id.save_Btn);
        cancelBtn = findViewById(R.id.cancel_Btn);
        backBtn = findViewById(R.id.back_Btn);
        buttonLayout = findViewById(R.id.button_Layout);

        buttonLayout.setVisibility(View.INVISIBLE);


        setProfilePicture();


        backBtn.setOnClickListener(v->{
            onBackPressed();
        });

        cancelBtn.setOnClickListener(v->{
            onBackPressed();
        });

        uploadBtn.setOnClickListener(v->{
            uploadImage();
        });

        selectImageBtn.setOnClickListener(v->{
            ImagePicker.with(EditProfile.this)
                    .crop(1,1)	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start();

        });
    }

    private void setProfilePicture() {
        Intent intent = getIntent();
        String patientId = intent.getStringExtra("patientId");

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

    private void retrieveImage(String fileName){
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
    private void uploadImage() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading new profile picture...");
        progressDialog.show();

        Intent intent = getIntent();
        String patientId = intent.getStringExtra("patientId");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM_dd_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        String currentDate = simpleDateFormat.format(now);
        String fileName = patientId + "_" + currentDate;





        storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);
        storageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profilePicImageview.setImageURI(null);

                        if (progressDialog.isShowing()){

                            HashMap <String, Object> updateProfilePic = new HashMap<>();
                            updateProfilePic.put("profilePicture", fileName);

                            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                                    .update(updateProfilePic)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Successfully uploaded", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), YourProfile.class));
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (progressDialog.isShowing()){
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed to upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }


                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){
            buttonLayout.setVisibility(View.VISIBLE);
            uri = data.getData();
            profilePicImageview.setImageURI(uri);
        }
        else if(resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(getApplicationContext(), ImagePicker.getError(data), Toast.LENGTH_LONG).show();

        }
        else {
            Toast.makeText(getApplicationContext(), "Task cancelled", Toast.LENGTH_LONG).show();
        }
    }
}