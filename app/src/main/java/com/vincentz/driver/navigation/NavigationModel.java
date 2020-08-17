package com.vincentz.driver.navigation;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class NavigationModel {
    ArrayList<StepsModel> stepsList = new ArrayList<>();
    List<LatLng> latLngList = new ArrayList<>();

    public NavigationModel(ArrayList<StepsModel> stepsList, List<LatLng> latLngList) {
        this.stepsList = stepsList;
        this.latLngList = latLngList;
    }

    public NavigationModel() {

    }
}
