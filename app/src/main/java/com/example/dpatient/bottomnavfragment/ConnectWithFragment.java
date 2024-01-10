package com.example.dpatient.bottomnavfragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.dpatient.Adapter.ConnectedWithAdapter;
import com.example.dpatient.R;
import com.example.dpatient.UserDetails;
import com.example.dpatient.Utils.DateAndTimeUtils;
import com.example.dpatient.model.ConnectWithModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;


public class ConnectWithFragment extends Fragment {
    private RecyclerView recyclerView;
    private FrameLayout noHealthCareLayout;
    private ConnectedWithAdapter adapter;
    private ArrayList<ConnectWithModel> list;
    private UserDetails userDetails;
    private TextView name;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect_with, container, false);
        initWidgets(view);
        setUpName();
        setUpRecyclerView();


        return view;
    }

    private void setUpName() {
        if (getContext()!= null){
            userDetails = new UserDetails(getContext());
            name.setText(userDetails.getName());
        }
    }

    private void setUpRecyclerView() {
        list = new ArrayList<>();
        adapter = new ConnectedWithAdapter(getContext(), list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (getContext()!= null){
            userDetails = new UserDetails(getContext());

            String patientId = userDetails.getPatientId();
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users")
                    .document(patientId).collection("myDoctor");

            if (collectionReference != null){
                collectionReference.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (!querySnapshot.isEmpty() && querySnapshot != null){
                                        list.clear();
                                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                            if (documentSnapshot.exists()){

                                                String name = documentSnapshot.getString("doctorName");
                                                String userId = documentSnapshot.getString("userID");
                                                String dateAndTimeConnected = documentSnapshot.getString("dateAndTimeConnected");

                                                setUpRecyclerView1(adapter, list, name, userId, dateAndTimeConnected);

                                            }
                                        }
                                    }
                                }
                            }
                        });
            }
        }
    }

    private void setUpRecyclerView1(ConnectedWithAdapter adapter, ArrayList<ConnectWithModel> list, String name, String userId, String dateAndTimeConnected) {

        FirebaseFirestore.getInstance().collection("userDoctor").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                String profilePicture = null;
                                if(documentSnapshot.contains("profilePicture")){
                                    profilePicture = documentSnapshot.getString("profilePicture");
                                }

                                LocalDateTime localDateTime = DateAndTimeUtils.parseDateString(dateAndTimeConnected, "MM/dd/yyyy h:mm a");
                                String dateAndTime = DateAndTimeUtils.calculateMinutesAgo(localDateTime);

                                list.add(new ConnectWithModel(name, profilePicture, userId, dateAndTime));

                                if (list != null){
                                    recyclerView.setVisibility(View.VISIBLE);
                                    noHealthCareLayout.setVisibility(View.GONE);
                                }
                                else{
                                    recyclerView.setVisibility(View.GONE);
                                    noHealthCareLayout.setVisibility(View.VISIBLE);
                                }

                                if (getContext() != null)
                                    adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    private void initWidgets(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        noHealthCareLayout = view.findViewById(R.id.noHealthCare_Framelayout);
        name = view.findViewById(R.id.name_Textview);
    }
}