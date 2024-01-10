package com.example.dpatient.model;

public class ConnectWithModel {
    String name;
    String profilePicture;
    String userId;
    String dateAndTime;

    public ConnectWithModel(String name, String profilePicture, String userId, String dateAndTime){
        this.name = name;
        this.profilePicture = profilePicture;
        this.userId = userId;
        this.dateAndTime = dateAndTime;
    }

    public String getName() {
        return name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
    public String getUserId(){
        return userId;
    }
    public String getDateAndTime(){
        return dateAndTime;
    }

}

