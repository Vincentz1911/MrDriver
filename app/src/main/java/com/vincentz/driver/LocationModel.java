package com.vincentz.driver;

import com.google.android.gms.maps.model.LatLng;

public class LocationModel {

    String area;
    String name;
    LatLng latLng;
    float distance;
    boolean saved;

    LocationModel(String area, String name, LatLng latLng, float distance, boolean saved) {
        this.area = area;
        this.name = name;
        this.latLng = latLng;
        this.distance = distance;
        this.saved = saved;
    }
}
