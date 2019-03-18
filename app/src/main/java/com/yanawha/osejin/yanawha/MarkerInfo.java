package com.yanawha.osejin.yanawha;

import org.jetbrains.annotations.NotNull;

public class MarkerInfo {
    double lat = 0;
    double lng = 0;
    String place = "";
    String imageURL = "";

    public MarkerInfo(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
    public MarkerInfo(String place, double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.place = place;
    }

    public MarkerInfo(double lat, double lng, String place, String imageURL) {
        this.lat = lat;
        this.lng = lng;
        this.place = place;
        this.imageURL = imageURL;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getPlace() {
        return place;
    }

    public String getimageURL() {
        return imageURL;
    }
}
