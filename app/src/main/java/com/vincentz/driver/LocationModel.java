package com.vincentz.driver;

import com.google.android.gms.maps.model.LatLng;

public class LocationModel {
    String name;
    String street;
    String city;
    LatLng latLng;
    float distance;
    boolean saved;

    LocationModel(String name, String street, String city, LatLng latLng, float distance, boolean saved) {
        this.name = name;
        this.street = street;
        this.city = city;
        this.latLng = latLng;
        this.distance = distance;
        this.saved = saved;
    }

//    public LocationModel(String name, String street, String city, double latitude, double longitude,
//                         float distance, boolean saved) {
//        this.name = name;
//        this.street = street;
//        this.city = city;
//        this.saved = saved;
//        this.distance = distance;
//        this.latLng = new LatLng(latitude, longitude);
//    }
}
