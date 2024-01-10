package com.example.dpatient.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpatient.R;
import com.example.dpatient.UserDetails;
import com.example.dpatient.model.ConnectWithModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ConnectedWithAdapter extends RecyclerView.Adapter<ConnectedWithAdapter.MyViewHolder>{
    Context context;
    ArrayList<ConnectWithModel> list;
    public ConnectedWithAdapter(Context context, ArrayList<ConnectWithModel> list){
        this.list = list;
        this.context = context;
    }
    @NonNull
    @Override
    public ConnectedWithAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.connected_doctor_list, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectedWithAdapter.MyViewHolder holder, int position) {
        ConnectWithModel model = list.get(position);
        holder.name.setText("Dr. " + model.getName());
        holder.userId = model.getUserId();
        holder.profilePic = model.getProfilePicture();
        holder.dateAndTime.setText(model.getDateAndTime());

        if (holder.profilePic != null){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("images/"+ holder.profilePic);

            try {
                File localFile = File.createTempFile("tempfile", "jpg");
                storageReference.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                holder.imageview.setImageBitmap(bitmap);

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








    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ShapeableImageView imageview;
        String userId;
        String profilePic;
        TextView dateAndTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name_Textview);
            imageview = itemView.findViewById(R.id.profile_pic_image_view);
            dateAndTime = itemView.findViewById(R.id.dateAndTimeConnected_Textview);
        }
    }
}
