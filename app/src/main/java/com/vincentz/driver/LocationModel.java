package com.vincentz.driver;

import android.location.Location;
import java.util.Observable;

class LocationModel extends Observable {

    private Location now;
    private Location last;

    void setNow(Location now) {
        synchronized (this) { this.now = now; }
        setChanged();
        notifyObservers(now);
    }
    synchronized Location getNow() { return now; }

    void setLast(Location last) {
        synchronized (this) { this.last = last; }
        setChanged();
        notifyObservers(last);
    }
    synchronized Location getLast() { return last; }
}

