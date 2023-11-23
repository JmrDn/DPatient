package com.example.dpatient.model;

public class GlucoseHistoryModel {

    String glucoseLevel,
            date,
            time;

    public GlucoseHistoryModel(String glucoseLevel, String date, String time) {
        this.glucoseLevel = glucoseLevel;
        this.date = date;
        this.time = time;
    }

    public void setGlucoseLevel(String glucoseLevel) {
        this.glucoseLevel = glucoseLevel;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGlucoseLevel() {
        return glucoseLevel;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
