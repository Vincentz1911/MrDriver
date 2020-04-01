package com.vincentz.driver;

import com.google.android.gms.maps.model.LatLng;

class LocationModel {

    String localadmin;
    String name;
    LatLng latLng;
    float distance;

    LocationModel(String localadmin, String name, LatLng latLng, float distance) {
        this.localadmin = localadmin;
        this.name = name;
        this.latLng = latLng;
        this.distance = distance;
    }
}
