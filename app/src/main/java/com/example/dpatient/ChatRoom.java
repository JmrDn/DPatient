package com.example.dpatient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dpatient.Adapter.ChatRoomAdapter;
import com.example.dpatient.Utils.FirebaseUtil;
import com.example.dpatient.model.ChatRoomMessagesModel;
import com.example.dpatient.model.ChatRoomModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private Dialog deleteDialog;
    private AppCompatButton yesBtn, noBtn;
    private  boolean success;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        initWidgets();
        setUpToolbar();
        setChatRoomId();

        backBtn.setOnClickListener(v ->{
            onBackPressed();
        });


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

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setChatRoomId() {
        Intent intent = getIntent();
        doctorId =  intent.getStringExtra("userUID");
        name.setText(intent.getStringExtra("name"));

        chatRoomId = FirebaseUtil.getChatRoomId(FirebaseAuth.getInstance().getUid(), doctorId);
    }

    private void initWidgets() {
        recyclerView = findViewById(R.id.chatMessages_Recyclerview);
        messageInput = findViewById(R.id.inputMessages_Edittext);
        sendMessageBtn = findViewById(R.id.send_Btn);
        backBtn = findViewById(R.id.back_Btn);
        name = findViewById(R.id.name_TxtView);

        toolbar = findViewById(R.id.toolbar);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_chatroom_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.delete){
            showDeleteDialog();
        }
        return  true;
    }

    private void showDeleteDialog() {
        deleteDialog = new Dialog(this);
        deleteDialog.setContentView(R.layout.delete_convo_dialog_layout);
        deleteDialog.getWindow().setLayout(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(false);
        deleteDialog.show();

        yesBtn = deleteDialog.findViewById(R.id.yes_Btn);
        noBtn = deleteDialog.findViewById(R.id.no_Btn);

        yesBtn.setOnClickListener(v->{
            DocumentReference documentReference =  FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomId);

            documentReference.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                CollectionReference collectionReference = documentReference.collection("chats");
                                collectionReference.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()){


                                                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                                        collectionReference.document(documentSnapshot.getId())
                                                                .delete()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task1) {
                                                                        if (task1.isSuccessful()){
                                                                            success = true;
                                                                            Log.d("TAG", "Document deleted");
                                                                        }
                                                                        else{
                                                                            Log.d("TAG", "Document failed to delete " + task1.getException().getMessage().toString());
                                                                            success = false;
                                                                        }
                                                                    }
                                                                });
                                                    }


                                                }
                                            }
                                        });

                                if (!success){
                                    Toast.makeText(getApplicationContext(), "Conversation deleted", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), Homepage1.class);
                                    intent.putExtra("value", "messageFragment");
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Failed to delete", Toast.LENGTH_LONG).show();
                                    deleteDialog.dismiss();
                                }
                            }
                            else{

                            }
                        }
                    });
        });

        noBtn.setOnClickListener(noV->{
            deleteDialog.dismiss();
        });
    }
}