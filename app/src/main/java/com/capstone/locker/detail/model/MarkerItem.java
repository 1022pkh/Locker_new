package com.capstone.locker.detail.model;

/**
 * Created by Isoft on 2016-07-04.
 */
public class MarkerItem {
    double lat;
    double lon;
    int img;

    public MarkerItem(double lat, double lon, int img) {
        this.lat = lat;
        this.lon = lon;
        this.img = img;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int price) {
        this.img = price;
    }
}
