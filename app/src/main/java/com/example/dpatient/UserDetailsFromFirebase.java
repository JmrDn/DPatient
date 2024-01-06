package com.example.dpatient;

public class UserDetailsFromFirebase {
    String patientId;

    public UserDetailsFromFirebase(String patientId){
        this.patientId = patientId;
    }

    public String getPatientId() {
        return patientId;
    }
}
