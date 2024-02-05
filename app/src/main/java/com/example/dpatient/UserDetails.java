package com.example.dpatient;

import android.content.Context;
import android.content.SharedPreferences;

public class UserDetails {
    Context context;
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public static final String PREF_NAME = "user_Details";

    public UserDetails(Context context){
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public void  setPatientId(String patientId){
        editor.putString("patientId", patientId);
        editor.apply();

    }
    public void setFastingStatus(String fasting){
        editor.putString("fasting", fasting);
        editor.apply();
    }
    public String getFastingStatus(){
        return sharedPreferences.getString("fasting", null);
    }

    public  void logout(){
        editor.clear();
        editor.apply();
        editor.commit();

    }
    public String getPatientId(){

        return sharedPreferences.getString("patientId", null);

    }

    public void setSensorId(String sensorId){
        editor.putString("sensorId", sensorId);
        editor.apply();
    }

    public String getSensorId(){
        return sharedPreferences.getString("sensorId", null);
    }

    public void setAccountIsConnectedToSensor(boolean isConnected){
        editor.putBoolean("isConnected", isConnected);
        editor.apply();

    }

    public boolean isAccountConnectedToSensor(){
        return sharedPreferences.getBoolean("isConnected", false);
    }

    public void setName(String name){
        editor.putString("name", name);
        editor.apply();

    }
    public String getName(){
        return sharedPreferences.getString("name", null);
    }

    public void setEmail(String email){
        editor.putString("email", email);
        editor.apply();
    }
    public String getEmail(){
        return sharedPreferences.getString("email", null);
    }


}
