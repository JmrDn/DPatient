package com.example.dpatient.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpatient.ChatRoom;
import com.example.dpatient.Utils.FirebaseUtil;
import com.example.dpatient.R;
import com.example.dpatient.model.ChatRoomModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ChatListAdapter extends FirestoreRecyclerAdapter<ChatRoomModel, ChatListAdapter.ViewHolder> {

    Context context;

    public ChatListAdapter(@NonNull FirestoreRecyclerOptions<ChatRoomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ChatRoomModel model) {

        FirebaseUtil.lastMessageReference(model.getPatientID())
                .get().addOnCompleteListener(task ->{
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                            holder.name.setText(document.getString("fullName"));
                            holder.userUID = document.getString("userUID");

                            getFileName(holder.userUID, holder.chatImageview);
                        if (model.getLastMessageSenderId().equals(FirebaseUtil.currentUserUID()))
                            holder.lastMessage.setText("You:" + model.getLastMessage());
                        else
                            holder.lastMessage.setText(model.getLastMessage());

                        holder.lastMessageTime.setText(FirebaseUtil.timeStamp(model.getLastMessageTimeStamp()));
                    }
                    else {
                        Log.d("TAG", "no such document");
                    }
                });

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatRoom.class);
            intent.putExtra("name", holder.name.getText().toString());
            intent.putExtra("userUID", holder.userUID);
            context.startActivity(intent);
        });

    }

    private void getFileName(String userUID, ShapeableImageView imageView) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("userDoctor").document(userUID);
        if (documentReference!= null){
            documentReference.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()){
                                    if(documentSnapshot.contains("profilePicture")){
                                        String fileName = documentSnapshot.getString("profilePicture");
                                        retrieveImage(fileName, imageView);
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private void retrieveImage(String fileName, ShapeableImageView imageView) {
       StorageReference storageReference = FirebaseStorage.getInstance().getReference("images/"+ fileName);

        try {
            File localFile = File.createTempFile("tempfile", "jpg");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_recyclerview, parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,
                lastMessage,
                lastMessageTime;

        String userUID;
        ShapeableImageView chatImageview;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name_TxtView);
            lastMessage = itemView.findViewById(R.id.lastMessage_Textview);
            lastMessageTime = itemView.findViewById(R.id.lastMessage_time_Textview);
            chatImageview = itemView.findViewById(R.id.profilePicture_Imageview);
        }
    }
}
