package com.vincentz.driver;

import androidx.annotation.NonNull;
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

public class MainActivity extends FragmentActivity {

    static boolean[] PERMISSIONS;
    static Location LOCATION, LASTLOCATION;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        fm = getSupportFragmentManager();

        boolean alreadyRun = false;
        alreadyRun = getPreferences(Context.MODE_PRIVATE).getBoolean("HaveRun", alreadyRun);
        if (!alreadyRun) getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_big_center, new WelcomeFragment(), "").commit();
        else setupView();
    }

    void firstRun() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation();
        getPreferences(Context.MODE_PRIVATE).edit().putBoolean("HaveRun", true).apply();
        fm.beginTransaction().replace(R.id.fl_left_top, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_left_bottom, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_big_center, new SelectorFragment(), "").commit();
    }

    private void setupView() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation();
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
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int p = 0; p < permissions.length; p++)
            if (grantResults.length > 0 && grantResults[p] == PackageManager.PERMISSION_GRANTED)
                PERMISSIONS[p] = true;
    }
    //endregion

    //region LOCATION
    void getLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return;

        //Checks if GPS is on, sets a Listener and if there is a last position
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return;

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedRequired(true);
        criteria.setBearingRequired(true);
        String provider = lm.getBestProvider(criteria, true);

        if (provider == null) return;
        LASTLOCATION = lm.getLastKnownLocation(provider);
        lm.requestLocationUpdates(provider, 1000, 3, LocationListener);
    }

    public LocationListener LocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location onChanged) {
            LOCATION = onChanged;
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