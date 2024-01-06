package com.example.dpatient.model;

public class GlucoseHistoryModel {
    String glucoseLevel;
    String time;

    public GlucoseHistoryModel(String glucoseLevel, String time){
        this.glucoseLevel = glucoseLevel;
        this.time = time;
    }

    public String getGlucoseLevel() {
        return glucoseLevel;
    }

    public void setGlucoseLevel(String glucoseLevel) {
        this.glucoseLevel = glucoseLevel;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

