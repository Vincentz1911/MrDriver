package com.vincentz.driver;

import com.google.android.gms.maps.model.LatLng;

public class LocationModel {
    String name;
    String street;
    String area;
    LatLng latLng;
    float distance;
    boolean saved;

    LocationModel(String name, String street, String area, LatLng latLng, float distance, boolean saved) {
        this.name = name;
        this.street = street;
        this.area = area;
        this.latLng = latLng;
        this.distance = distance;
        this.saved = saved;
    }
}
