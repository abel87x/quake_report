package com.example.android.quakereport;

public class Quake {

    private double mMagnitude;
    private String mLocation;
    private long mDateTimeMilliseconds;
    private String mQuakeWebSite;

    public Quake(double magnitude, String location, long date, String website) {
        this.mMagnitude = magnitude;
        this.mLocation = location;
        this.mDateTimeMilliseconds = date;
        this.mQuakeWebSite = website;
    }

    public double getMagnitude() {
        return mMagnitude;
    }

    public String getLocation() {
        return mLocation;
    }

    public long getDateTime() {
        return mDateTimeMilliseconds;
    }

    public String getQuakeWebSite() {
        return mQuakeWebSite;
    }
}
