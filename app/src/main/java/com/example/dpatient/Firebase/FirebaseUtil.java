package com.example.dpatient.Firebase;


import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

public class FirebaseUtil {


    public static String currentUserUID (){
        return FirebaseAuth.getInstance().getUid();
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("UsersUID");
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("UsersUID").document(currentUserUID());
    }

    public static DocumentReference lastMessageReference(List<String> patientID){

        if (patientID.get(0).equals(FirebaseAuth.getInstance().getUid())){
            return FirebaseUtil.allUserCollectionReference().document(patientID.get(1));
        }
        else {
            return FirebaseUtil.allUserCollectionReference().document(patientID.get(0));
        }
    }

    public static String timeStamp(Timestamp timestamp){
        return new SimpleDateFormat("hh:mm a").format(timestamp.toDate());
    }

    public static CollectionReference chatRoomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatRooms");
    }

    public static String getChatRoomId(String patientId, String doctorId){

        if (patientId == null || doctorId == null){
            throw new IllegalArgumentException("Patient ID and Doctor ID must not be null");
        }
        if (patientId.hashCode() < doctorId.hashCode()){
            return patientId + "_" + doctorId;
        }

        else{
            return doctorId + "_" + patientId;
        }
    }

    public static DocumentReference getChatRoomReference (String chatRoomId){
        return chatRoomCollectionReference().document(chatRoomId);
    }
    @SuppressLint("SimpleDateFormat")
    public static String timeStampToString(Timestamp timestamp){
        return new SimpleDateFormat("hh:mm a").format(timestamp.toDate());
    }

}
