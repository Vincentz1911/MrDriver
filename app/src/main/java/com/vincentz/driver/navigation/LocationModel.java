package com.vincentz.driver.navigation;

import com.google.android.gms.maps.model.LatLng;

public class LocationModel {
    int driver;
    String name;
    String street;
    String city;
    LatLng latLng;
    float distance;
    int stored;

    LocationModel(int driver, String name, String street, String city, LatLng latLng, float distance, int stored) {
        this.driver = driver;
        this.name = name;
        this.street = street;
        this.city = city;
        this.latLng = latLng;
        this.distance = distance;
        this.stored = stored;
    }
}
