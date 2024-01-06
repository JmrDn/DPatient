package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.dpatient.Adapter.GlucoseHistoryAdapter;
import com.example.dpatient.Utils.FirebaseUtil;
import com.example.dpatient.model.GlucoseHistoryModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;

import java.util.ArrayList;

public class ViewHistory extends AppCompatActivity {

    RecyclerView recyclerView;

    GlucoseHistoryAdapter myAdapter;

    ArrayList<GlucoseHistoryModel> list;

    ImageButton backBtn;
     String patientId;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);

        recyclerView = findViewById(R.id.glucoseLevelHistory_Recyclerview);
        backBtn = findViewById(R.id.back_Btn);

        backBtn.setOnClickListener(v ->{
            onBackPressed();
        });

//        setUpGlucoseLevelHistoryRecyclerview();
        noInternetDialog();


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
//     void setUpGlucoseLevelHistoryRecyclerview() {
//        list = new ArrayList<>();
//        myAdapter = new GlucoseHistoryAdapter(getApplicationContext(), list);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
//        recyclerView.setLayoutManager(linearLayoutManager);
//
//
//
//         FirebaseUtil.currentUserDetails()
//                 .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                     @Override
//                     public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                         if (task.isSuccessful()){
//                             DocumentSnapshot document = task.getResult();
//                             if (document.exists()){
//                                 patientId = document.getString("patientID");
//
//                                 FirebaseFirestore.getInstance().collection("Users").document(patientId)
//                                         .collection("glucose_Level_History")
//                                         .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                             @SuppressLint("NotifyDataSetChanged")
//                                             @Override
//                                             public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                                                 if (error != null){
//                                                     Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
//                                                 }
//                                                 else {
//                                                     list.clear();
//
//                                                     for (QueryDocumentSnapshot documentSnapshot: value){
//                                                         if (documentSnapshot.exists()){
//                                                             String glucoseLevel = documentSnapshot.getString("glucose_level");
//                                                             if (!glucoseLevel.equals("0")){
//                                                                 list.add(new GlucoseHistoryModel(documentSnapshot.getString("glucose_level"),
//                                                                         documentSnapshot.getString("date"),
//                                                                         documentSnapshot.getString("time")));
//
//                                                                 recyclerView.setAdapter(myAdapter);
//                                                                 myAdapter.notifyDataSetChanged();
//                                                             }
//
//                                                         }
//                                                     }
//                                                 }
//                                             }
//                                         });
//
//                             }
//                         }
//                         else {
//                             Log.d("TAG", "no such document");
//                         }
//
//
//                     }
//                 });
//
//
//
//
//
//
//
//    }


}