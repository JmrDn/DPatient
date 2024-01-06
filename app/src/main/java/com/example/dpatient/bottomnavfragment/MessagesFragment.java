package com.example.dpatient.bottomnavfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dpatient.Adapter.ChatListAdapter;
import com.example.dpatient.Utils.FirebaseUtil;
import com.example.dpatient.R;
import com.example.dpatient.model.ChatRoomModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;


public class MessagesFragment extends Fragment {

    ChatListAdapter myAdapter;
    RecyclerView recyclerView;
    TextView lastMessage,
            lastMessageTimeStamp,
            doctorName;

    String doctorId,
            chatRoomId;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        recyclerView = view.findViewById(R.id.chatList_Recyclerview);
        lastMessage = view.findViewById(R.id.lastMessage_Textview);
        lastMessageTimeStamp = view.findViewById(R.id.lastMessage_time_Textview);
        doctorName = view.findViewById(R.id.name_TxtView);



       noInternetDialog();



        setUpRecyclerview();

        return view;
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


    private void setUpRecyclerview() {

        Query query = FirebaseUtil.chatRoomCollectionReference()
                .whereArrayContains("patientID", FirebaseUtil.currentUserUID())
                .orderBy("lastMessageTimeStamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatRoomModel> options = new FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                .setQuery(query, ChatRoomModel.class).build();

        myAdapter = new ChatListAdapter(options, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(myAdapter);
        myAdapter.startListening();



    }

    @Override
    public void onStart() {
        super.onStart();
        if (myAdapter != null){
            myAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (myAdapter != null){
            myAdapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (myAdapter != null){
            myAdapter.notifyDataSetChanged();
        }
    }
}