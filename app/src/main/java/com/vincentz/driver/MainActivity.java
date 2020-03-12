package com.vincentz.driver;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Objects;

public class MainActivity extends FragmentActivity {

    static boolean[] PERMISSIONS;
    LocationModel loc;
    private int gpsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        loc = new LocationModel();

        if (getPreferences(Context.MODE_PRIVATE).getBoolean("HaveRun", false)) setupView();
        else getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_big_center, new WelcomeFragment(), "").commit();
    }

    void firstRun() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation(); else checkPermissions();
        getPreferences(Context.MODE_PRIVATE).edit().putBoolean("HaveRun", true).apply();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fl_left_top, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_left_bottom, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_big_center, new SelectorFragment(), "").commit();
    }

    private void setupView() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation(); else checkPermissions();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fl_left_top, new InfoFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_left_bottom, new WeatherFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_bottom, new SpotifyFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_big_center, new MapFragment(), "").commit();
    }

    //region PERMISSIONS
    void checkPermissions() {
        String[] REQUEST_PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        PERMISSIONS = new boolean[REQUEST_PERMISSIONS.length];
        if (!hasPermissions(REQUEST_PERMISSIONS))
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, 100);
    }

    private boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (int p = 0; p < permissions.length; p++) {
                if (ActivityCompat.checkSelfPermission(this, permissions[p])
                        != PackageManager.PERMISSION_GRANTED) return false;
                else PERMISSIONS[p] = true;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int rc, String[] permissions, int[] results) {
        for (int p = 0; p < permissions.length; p++)
            if (results.length > 0 && results[p] == PackageManager.PERMISSION_GRANTED)
                PERMISSIONS[p] = true;
    }
    //endregion

    //region LOCATION
    void getLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            checkPermissions();

        //Checks if GPS is on, sets a Listener and if there is a last position
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedRequired(true);
        criteria.setBearingRequired(true);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = Objects.requireNonNull(lm).getBestProvider(criteria, true);

        if (provider != null) Tools.msg(this, "Found Location Provider:" + provider);
        else {Tools.msg(this, "No Location provider found"); return; }

        loc.setLast(lm.getLastKnownLocation(provider));
        lm.requestLocationUpdates(provider, 500, 0, LocationListener);
    }

    public LocationListener LocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            loc.setLast(loc.getNow());
            loc.setNow(location);
            //gpsCount++;
            //Log.d("GPS", "onLocationChanged: " + gpsCount);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
    //endregion
}