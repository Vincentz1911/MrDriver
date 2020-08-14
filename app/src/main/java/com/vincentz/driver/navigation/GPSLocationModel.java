package com.vincentz.driver.navigation;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Observable;

public class GPSLocationModel extends Observable {

    private Location locNow;
    private Location locLast;

    public void setNow(Location now) {
        synchronized (this) { locNow = now; }
        setChanged();
        notifyObservers(now);
    }

    public Location now() { return locNow; }
    LatLng latlng() { return new LatLng(locNow.getLatitude(), locNow.getLongitude()); }
    float speed() { return locNow.getSpeed(); }
    float bearing() { return locNow.getBearing(); }
    double altitude() { return locNow.getAltitude(); }

    public void setLast(Location last) {
        synchronized (this) { this.locLast = last; }
        setChanged();
        notifyObservers(last);
    }

    synchronized Location last() { return locLast; }

    synchronized LatLng latlnglast() {
        return new LatLng(locLast.getLatitude(), locLast.getLongitude());
    }
}

