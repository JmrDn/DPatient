package com.example.dpatient.bottomnavfragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dpatient.ConnectingToSensorPage;
import com.example.dpatient.DoctorInfo;
import com.example.dpatient.Firebase.FirebaseUtil;
import com.example.dpatient.Login;
import com.example.dpatient.R;
import com.example.dpatient.ViewGlucose;
import com.example.dpatient.ViewHistory;
import com.example.dpatient.YourProfile;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;


public class HomeFragment extends Fragment {

    AppCompatButton connectToSensorBtn,
            viewHistoryBtn;
    TextView nameTextview,
            connectToSensorTextview;
    ImageView connectToSensorImageview;

    CardView proceedToDoctorInfoBtn;
    RelativeLayout connectedToSensorLayout;

    public String patientId, email;

    ShapeableImageView profileImageview;

    StorageReference storageReference;

    ShimmerFrameLayout shimmerFrameLayout;
    RelativeLayout layout;




    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        connectToSensorBtn = view.findViewById(R.id.connect_Btn);
        viewHistoryBtn = view.findViewById(R.id.viewHistory_Btn);
        nameTextview = view.findViewById(R.id.patientName_Textview);
        proceedToDoctorInfoBtn = view.findViewById(R.id.header_Cardview);
        connectedToSensorLayout = view.findViewById(R.id.connectedToSensor_Layout);
        connectToSensorTextview = view.findViewById(R.id.connectToSensor_Textview);
        connectToSensorImageview = view.findViewById(R.id.connectToSensor_Imageview);
        profileImageview = view.findViewById(R.id.profile_pic_image_view);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_Layout);
        layout = view.findViewById(R.id.data_Layout);

        //TODO reminder shimmerlayout is off
        shimmerFrameLayout.setVisibility(View.INVISIBLE);

        setUpPatientName();
        setUpProfilePicture();
        //Go to profile
        profileImageview.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), YourProfile.class);
            intent.putExtra("name", nameTextview.getText().toString());
            intent.putExtra("patientId", patientId);
            intent.putExtra("email", email);
            startActivity(intent);
        });

        //If sensor is connected, display this layout
        connectedToSensorLayout.setVisibility(View.GONE);

        setUpConnectedToSensorLayout();

        //This will effect if the account is connected to sensor
        setUpViewingGlucose();

        noInternetDialog();

        proceedToDoctorInfoBtn.setOnClickListener(v->{

            startActivity(new Intent(getContext(), DoctorInfo.class));
        });




        connectToSensorBtn.setOnClickListener(v -> {
            //If the account is connected to sensor proceed to View glucose page
            //If the account is not connected yet proceed to connect to sensor page
            setUpWhereToIntent();

        });

        viewHistoryBtn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ViewHistory.class));
        });


        return view;
    }



    private void setShimmerLayout() {

        layout.setVisibility(View.INVISIBLE);
        shimmerFrameLayout.startShimmerAnimation();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layout.setVisibility(View.VISIBLE);
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.INVISIBLE);

            }
        },3000);
    }


    private void setUpProfilePicture() {

        FirebaseFirestore.getInstance().collection("UsersUID").document(FirebaseUtil.currentUserUID())
                        .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            if (documentSnapshot.exists()){
                                                String patientId = documentSnapshot.getString("patientID");

                                                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                                                        .get()
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()){
                                                                DocumentSnapshot documentSnapshot1 = task1.getResult();
                                                                if (documentSnapshot1.exists()){
                                                                    String fileName = documentSnapshot1.getString("profilePicture");

                                                                    retrieveImage(fileName);
                                                                }
                                                            }
                                                        });
                                            }
                                            else {
                                                Log.d("TAG", "no such document");
                                            }
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
                            profileImageview.setImageBitmap(bitmap);

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

    private void setUpWhereToIntent() {

        FirebaseUtil.currentUserDetails().get()
                .addOnCompleteListener(task ->{
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            String patientId = document.getString("patientID");



                            //Get the value of connectedToSensor
                            //If connected or true, go to View glucose page
                            //If not, go to connect to sensor page
                            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){
                                            DocumentSnapshot document1 = task1.getResult();

                                            if (document1.exists()){

                                              if (document1.contains("isConnectedToSensor")){
                                                  if (document1.getBoolean("isConnectedToSensor").equals(true)){
                                                      String isConnectedTo = document1.getString("isConnectedTo");

                                                      Intent intent = new Intent(getContext(),ViewGlucose.class);
                                                      intent.putExtra("sensorId", isConnectedTo);
                                                      intent.putExtra("patientId", patientId);
                                                      startActivity(intent);
                                                  }
                                                  else {
                                                      startActivity(new Intent(getContext(), ConnectingToSensorPage.class));
                                                  }

                                              }
                                              else {
                                                  Log.d("TAG", "User is not connected to sensor yet");
                                                  startActivity(new Intent(getContext(), ConnectingToSensorPage.class));
                                              }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void setUpViewingGlucose() {

        //Get userPatientId
        FirebaseFirestore.getInstance().collection("UsersUID").document(FirebaseUtil.currentUserUID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            String patientId = documentSnapshot.getString("patientID");

                           setUpViewingGlucose1(patientId);
                        }
                    }
                    else {
                        Log.d("TAG", "no such document");
                    }
                });
    }

    void setUpViewingGlucose1(String patientId){
        FirebaseFirestore.getInstance().collection("Users").document(patientId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){

                            if (documentSnapshot.contains("isConnectedToSensor")){

                                Boolean isConnectedToSensor = documentSnapshot.getBoolean("isConnectedToSensor");

                                setUpViewingGlucose2(isConnectedToSensor);

                            }
                            else {
                                Boolean isConnectedToSensor = false;

                                setUpViewingGlucose2(isConnectedToSensor);

                            }
                        }
                    }

                });
    }

    void setUpViewingGlucose2(Boolean isConnectedToSensor){
        //This method is where we wil change the icon of connect to sensor
        //We will change it to View Glucose
        if (isConnectedToSensor.equals(true)){

            connectToSensorImageview.setImageResource(R.drawable.viewglucose);
            connectToSensorTextview.setText(R.string.view_glucose);

        }
    }
    private void setUpConnectedToSensorLayout() {

        //Get Patient ID
        FirebaseUtil.currentUserDetails().get()
                .addOnCompleteListener(task ->{
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            String patientId = document.getString("patientID");


                            //Get the value of connectedToSensor
                            //If connected or true, show the layout of "connected to sensor"
                            FirebaseFirestore.getInstance().collection("Users").document(patientId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){
                                            DocumentSnapshot document1 = task1.getResult();
                                            if (document1.exists()){

                                                if (document1.contains("isConnectedToSensor")){
                                                    if (document1.getBoolean("isConnectedToSensor").equals(true)){
                                                        connectedToSensorLayout.setVisibility(View.VISIBLE);
                                                    }
                                                    else {
                                                        connectedToSensorLayout.setVisibility(View.GONE);
                                                    }
                                                }
                                                else {
                                                    Log.d("TAG", "field does not exist: isConnectedToSensor");
//                                                    HashMap <String, Object> update = new HashMap<>();
//                                                    update.put("isConnectedToSensor", false);
//
//                                                    FirebaseFirestore.getInstance().collection("Users").document(patientId)
//                                                            .update(update)
//                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                @Override
//                                                                public void onSuccess(Void unused) {
//
//                                                                    Log.d("TAG", "Successfully update");
//
//                                                                }
//                                                            }).addOnFailureListener(new OnFailureListener() {
//                                                                @Override
//                                                                public void onFailure(@NonNull Exception e) {
//                                                                    Log.d("TAG", "Failed to update document" + e.getMessage());
//                                                                }
//                                                            });


                                                }

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void noInternetDialog() {
        NoInternetDialogPendulum.Builder builder = new NoInternetDialogPendulum.Builder(
                getActivity(),
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
     void setUpPatientName() {

         FirebaseUtil.currentUserDetails()
                 .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                         if(task.isSuccessful()){
                             DocumentSnapshot documentSnapshot = task.getResult();

                             if (documentSnapshot.exists()){
                                 nameTextview.setText(documentSnapshot.getString("fullName"));
                                 patientId = documentSnapshot.getString("patientID");
                                 email = documentSnapshot.getString("email");
                             }

                         }
                         else {
                             Log.d("TAG", "no such document");
                         }
                     }
                 });
    }


}