package com.example.dpatient.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpatient.Homepage1;
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
        String userIdString = model.getUserId();
        String doctorName = model.getName();

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

        holder.disconnectBtn.setOnClickListener(v->{
            showDisconnectDialog(userIdString, doctorName);
        });








    }

    private void showDisconnectDialog(String userIdString, String doctorName) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.disconnect_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        AppCompatButton yesBtn = dialog.findViewById(R.id.yes_Btn);
        AppCompatButton noBtn = dialog.findViewById(R.id.no_Btn);
        TextView description = dialog.findViewById(R.id.description_Textview);

        String message = "Do you want to disconnect with Dr. " + doctorName + "?";
        description.setText(message);

        if (context.getApplicationContext() != null){
            UserDetails userDetails = new UserDetails(context.getApplicationContext());

            String patientId = userDetails.getPatientId();


            yesBtn.setOnClickListener(v->{
                FirebaseFirestore.getInstance().collection("Users").document(patientId)
                        .collection("myDoctor").document(userIdString)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(context.getApplicationContext(), "You are now disconnect to Dr. " + doctorName, Toast.LENGTH_LONG).show();
                                    context.startActivity(new Intent(context.getApplicationContext(), Homepage1.class));
                                    dialog.dismiss();
                                }
                                else{
                                    Toast.makeText(context.getApplicationContext(), "Failed to disconnect", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            }
                        });

                FirebaseFirestore.getInstance().collection("userDoctor").document(userIdString)
                        .collection("myPatient").document(patientId)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d("TAG", "Disconnect doctor to patient");
                                    dialog.dismiss();
                                }
                                else{
                                    Log.d("TAG", "Failed to disconnect doctor to patient");
                                    dialog.dismiss();
                                }
                            }
                        });

            });

            noBtn.setOnClickListener(v->{
                dialog.dismiss();
            });
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
        ImageButton disconnectBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name_Textview);
            imageview = itemView.findViewById(R.id.profile_pic_image_view);
            dateAndTime = itemView.findViewById(R.id.dateAndTimeConnected_Textview);
            disconnectBtn = itemView.findViewById(R.id.disconnect_Button);
        }
    }
}
