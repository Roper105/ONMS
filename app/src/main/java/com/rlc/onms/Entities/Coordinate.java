package com.rlc.onms.Entities;

import androidx.annotation.NonNull;

public class Coordinate {
    private final String latitude;
    private final String longitude;

    public Coordinate(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return latitude + ", " + longitude;
    }
}