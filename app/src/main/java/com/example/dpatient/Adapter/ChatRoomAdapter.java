package com.example.dpatient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpatient.R;
import com.example.dpatient.model.ChatRoomMessagesModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class ChatRoomAdapter extends FirestoreRecyclerAdapter<ChatRoomMessagesModel, ChatRoomAdapter.ViewHolder> {

    Context context;

    public ChatRoomAdapter(@NonNull FirestoreRecyclerOptions<ChatRoomMessagesModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRoomAdapter.ViewHolder holder, int position, @NonNull ChatRoomMessagesModel model) {

        if (model.getSenderId().equals(FirebaseAuth.getInstance().getUid())){
            holder.senderView.setVisibility(View.VISIBLE);
            holder.receiverView.setVisibility(View.GONE);
            holder.senderMessage.setText(model.getMessage());
        }
        else {
            holder.receiverView.setVisibility(View.VISIBLE);
            holder.senderView.setVisibility(View.GONE);
            holder.receiverMessage.setText(model.getMessage());
        }

    }

    @NonNull
    @Override
    public ChatRoomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.messages_list_recyclerview, parent, false);

        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderMessage, receiverMessage;
        CardView senderView, receiverView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.sender_Textview);
            receiverMessage = itemView.findViewById(R.id.receiver_Textview);
            senderView = itemView.findViewById(R.id.sender_Cardview);
            receiverView = itemView.findViewById(R.id.receiver_Cardview);
        }
    }
}
