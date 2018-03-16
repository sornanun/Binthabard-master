package com.example.sornanun.binthabard;

/**
 * Created by SORNANUN on 5/6/2560.
 */

public class LocationData {

    public static String myLat;
    public static String myLong;
    public static String myAddress;
    public static String myUpdateTime;

    public String getMyLat() {
        return myLat;
    }

    public void setMyLat(String myLat) {
        this.myLat = myLat;
    }

    public String getMyLong() {
        return myLong;
    }

    public void setMyLong(String myLong) {
        this.myLong = myLong;
    }

    public String getMyAddress() {
        return myAddress;
    }

    public void setMyAddress(String myAddress) {
        this.myAddress = myAddress;
    }

    public static String getMyUpdateTime() {
        return myUpdateTime;
    }

    public static void setMyUpdateTime(String myUpdateTime) {
        LocationData.myUpdateTime = myUpdateTime;
    }
}
