package com.vincentz.driver;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Observable;

class GPSLocationModel extends Observable {

    private Location locNow;
    private Location locLast;

    void setNow(Location now) {
        synchronized (this) { locNow = now; }
        setChanged();
        notifyObservers(now);
    }

    Location now() { return locNow; }
    LatLng latlng() { return new LatLng(locNow.getLatitude(), locNow.getLongitude()); }
    float speed() { return locNow.getSpeed(); }
    float bearing() { return locNow.getBearing(); }
    double altitude() { return locNow.getAltitude(); }

    void setLast(Location last) {
        synchronized (this) { this.locLast = last; }
        setChanged();
        notifyObservers(last);
    }

    synchronized Location last() { return locLast; }

    synchronized LatLng latlnglast() {
        return new LatLng(locLast.getLatitude(), locLast.getLongitude());
    }
}

