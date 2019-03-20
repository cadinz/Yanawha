package com.yanawha.osejin.yanawha;

public class MarkerInfo {
    double x = 0;
    double y = 0;
    String place_name = "";
    String place_url = "";

    public MarkerInfo(double lat, double lng) {
        this.x = lat;
        this.y = lng;
    }

    public MarkerInfo(String place, double lat, double lng) {
        this.x = lat;
        this.y = lng;
        this.place_name = place;
    }

    public MarkerInfo(double lat, double lng, String place, String imageURL) {
        this.x = lat;
        this.y = lng;
        this.place_name = place;
        this.place_url = imageURL;
    }

    public double getLat() {
        return x;
    }

    public double getLng() {
        return y;
    }

    public String getPlace() {
        return place_name;
    }

    public String getimageURL() {
        return place_url;
    }

    @Override
    public String toString() {
        return "MarkerInfo{" +
                "x=" + x +
                ", y=" + y +
                ", place_name='" + place_name + '\'' +
                ", place_url='" + place_url + '\'' +
                '}';
    }
}
