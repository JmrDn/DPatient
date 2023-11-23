package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.dpatient.Adapter.ChatRoomAdapter;
import com.example.dpatient.Firebase.FirebaseUtil;
import com.example.dpatient.model.ChatRoomMessagesModel;
import com.example.dpatient.model.ChatRoomModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.DialogPropertiesPendulum;
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum;

import java.util.Arrays;

public class ChatRoom extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText messageInput;
    ImageButton sendMessageBtn,
                backBtn;
    String chatRoomId, doctorId;
    ChatRoomModel chatRoomModel;

    ChatRoomAdapter myAdapter;
    TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        recyclerView = findViewById(R.id.chatMessages_Recyclerview);
        messageInput = findViewById(R.id.inputMessages_Edittext);
        sendMessageBtn = findViewById(R.id.send_Btn);
        backBtn = findViewById(R.id.back_Btn);
        name = findViewById(R.id.name_TxtView);

        backBtn.setOnClickListener(v ->{
            onBackPressed();
        });

        Intent intent = getIntent();
        doctorId =  intent.getStringExtra("userUID");
        name.setText(intent.getStringExtra("name"));

        chatRoomId = FirebaseUtil.getChatRoomId(FirebaseAuth.getInstance().getUid(), doctorId);
        sendMessageBtn.setOnClickListener(v ->{
            String messageInputString = messageInput.getText().toString();

            if (messageInputString.isEmpty())
                return;
            sendMessageToUser(messageInputString);
        });

        noInternetDialog();
        setUpRecyclerView();
        getAndCreateChatRoomModel();

    }

    private void setUpRecyclerView() {

        Query query = FirebaseFirestore.getInstance().collection("chatRooms")
                .document(chatRoomId)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatRoomMessagesModel> options = new FirestoreRecyclerOptions.Builder<ChatRoomMessagesModel>()
                .setQuery(query, ChatRoomMessagesModel.class).build();

        myAdapter = new ChatRoomAdapter(options, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(myAdapter);
        myAdapter.startListening();

        myAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
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
    private void getAndCreateChatRoomModel() {
        FirebaseUtil.getChatRoomReference(chatRoomId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            chatRoomModel = task.getResult().toObject(ChatRoomModel.class);
                            if (chatRoomModel == null){
                                //first chat
                                chatRoomModel = new ChatRoomModel(chatRoomId,
                                        Arrays.asList(FirebaseAuth.getInstance().getUid(),doctorId),
                                        Timestamp.now(),
                                        " ");

                                FirebaseUtil.getChatRoomReference(chatRoomId).set(chatRoomModel);

                                DocumentSnapshot documentSnapshot = task.getResult();
                                chatRoomModel.setLastMessage(documentSnapshot.getString("lastMessage"));
                            }
                        }
                    }
                });
    }

    private void sendMessageToUser(String message) {
        chatRoomModel.setLastMessageTimeStamp(Timestamp.now());
        chatRoomModel.setLastMessage(message);
        chatRoomModel.setLastMessageSenderId(FirebaseUtil.currentUserUID());

        FirebaseUtil.getChatRoomReference(chatRoomId).set(chatRoomModel);

        ChatRoomMessagesModel chatRoomMessagesModel = new ChatRoomMessagesModel(message, FirebaseUtil.currentUserUID(), Timestamp.now());
        FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomId)
                .collection("chats")
                .add(chatRoomMessagesModel)
                .addOnCompleteListener(task ->{
                   if (task.isSuccessful()){
                       messageInput.setText("");
                   }
                });
    }

    protected void onStart() {
        super.onStart();
        if (myAdapter != null)
            myAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (myAdapter != null)
            myAdapter.stopListening();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        if (myAdapter != null)
            myAdapter.startListening();
    }
}