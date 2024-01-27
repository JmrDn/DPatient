package com.example.dpatient.Utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateAndTimeUtils {

    public static String getTimeWithAMAndPM(){
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
    }

    public static String convertTimeToAMAndPM(String time){
        String newDate = "";
        DateFormat outputDateFormat  = new SimpleDateFormat("HH:mm");

        try {
            Date date1 = outputDateFormat.parse(time);
            DateFormat inputDateFormat = new SimpleDateFormat("h:mm a");
            newDate = inputDateFormat.format(date1);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return  newDate;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String calculateMinutesAgo(LocalDateTime yourDate) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(yourDate, now);

        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        long day = hours / 24;
        long week = day / 7;


        if (minutes == 0) {
            return "just now";
        } else if (minutes == 1) {
            return "1 minute ago";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (minutes < 120) {
            return "1 hour ago";
        } else if (hours > 1 && hours < 25) {
            return hours + " hours ago";
        }
        else if (day == 1){
            return "1 day ago";
        }
        else if (day > 1 && day <= 7){
            return day + " days ago";
        }
        else if (week == 1){
            return "1 week ago";
        }
        else{
            return  week + "weeks ago";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static LocalDateTime parseDateString(String dateString, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(dateString, formatter);
    }

    public static String getDate(){
        return new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
    }

    public static String dateIdFormat(){
        return new SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(new Date());
    }
    public static String time24HrsFormat(){
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
    public static String timeId(){
        return new SimpleDateFormat("HHmmss", Locale.getDefault()).format(new Date());
    }

    public static String getMonth(String date){
        String month = "";
        if(date.charAt(2) == '0' && date.charAt(3) == '1')
            month = "january";
        else if (date.charAt(2) == '0' && date.charAt(3) == '2')
            month = "february";
        else if (date.charAt(2) == '0' && date.charAt(3) == '3')
            month = "march";
        else if (date.charAt(2) == '0' && date.charAt(3) == '4')
            month = "april";
        else if (date.charAt(2) == '0' && date.charAt(3) == '5')
            month = "may";
        else if (date.charAt(2) == '0' && date.charAt(3) == '6')
            month = "june";
        else if (date.charAt(2) == '0' && date.charAt(3) == '7')
            month = "july";
        else if (date.charAt(2) == '0' && date.charAt(3) == '8')
            month = "august";
        else if (date.charAt(2) == '0' && date.charAt(3) == '9')
            month = "september";
        else if (date.charAt(2) == '1' && date.charAt(3) == '0')
            month = "october";
        else if (date.charAt(2) == '1' && date.charAt(3) == '1')
            month = "november";
        else if (date.charAt(2) == '1' && date.charAt(3) == '2')
            month = "december";

        return  month;
    }

    public static String getYear(String date){

        String year = String.valueOf(date.charAt(4)) +
                String.valueOf(date.charAt(5))  +
                String.valueOf(date.charAt(6))  +
                String.valueOf(date.charAt(7));

        return year;

    }

    public static String getTimeForLineChart(String time){
        String TIME = "";

        if (time.charAt(0) == '0' && time.charAt(1) == '0')
            TIME = "0";
        else if (time.charAt(0) == '0' && time.charAt(1) == '1')
            TIME = "1";
        else if (time.charAt(0) == '0' && time.charAt(1) == '2')
            TIME = "2";
        else if (time.charAt(0) == '0' && time.charAt(1) == '3')
            TIME = "3";
        else if (time.charAt(0) == '0' && time.charAt(1) == '4')
            TIME = "4";
        else if (time.charAt(0) == '0' && time.charAt(1) == '5')
            TIME = "5";
        else if (time.charAt(0) == '0' && time.charAt(1) == '6')
            TIME = "6";
        else if (time.charAt(0) == '0' && time.charAt(1) == '7')
            TIME = "7";
        else if (time.charAt(0) == '0' && time.charAt(1) == '8')
            TIME = "8";
        else if (time.charAt(0) == '0' && time.charAt(1) == '9')
            TIME = "9";
        else if (time.charAt(0) == '1' && time.charAt(1) == '0')
            TIME = "10";
        else if (time.charAt(0) == '1' && time.charAt(1) == '1')
            TIME = "11";
        else if (time.charAt(0) == '1' && time.charAt(1) == '2')
            TIME = "12";
        else if (time.charAt(0) == '1' && time.charAt(1) == '3')
            TIME = "13";
        else if (time.charAt(0) == '1' && time.charAt(1) == '4')
            TIME = "14";
        else if (time.charAt(0) == '1' && time.charAt(1) == '5')
            TIME = "15";
        else if (time.charAt(0) == '1' && time.charAt(1) == '6')
            TIME = "16";
        else if (time.charAt(0) == '1' && time.charAt(1) == '7')
            TIME = "17";
        else if (time.charAt(0) == '1' && time.charAt(1) == '8')
            TIME = "18";
        else if (time.charAt(0) == '1' && time.charAt(1) == '9')
            TIME = "19";
        else if (time.charAt(0) == '2' && time.charAt(1) == '0')
            TIME = "20";
        else if (time.charAt(0) == '2' && time.charAt(1) == '1')
            TIME = "21";
        else if (time.charAt(0) == '2' && time.charAt(1) == '2')
            TIME = "22";
        else if (time.charAt(0) == '2' && time.charAt(1) == '3')
            TIME = "23";
        else if (time.charAt(0) == '2' && time.charAt(1) == '4')
            TIME = "24";

        return  TIME;

    }

}