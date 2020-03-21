package com.vincentz.driver;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.Objects;

import static com.vincentz.driver.Tools.*;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SETS THEME BASED ON TIME OF DAY. TODO: REQUEST SUNRISE AND SUNSET FROM HTTP
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour > 8 && hour < 20) setTheme(R.style.AppTheme_Day);
        else setTheme(R.style.AppTheme_Night);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void fullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //getSupportActionBar().hide();
    }

    void init() {
        ACT = this;
        LOC = new LocationModel();
        RQ = Volley.newRequestQueue(this); //Starts a http queue for Volley
        FM = getSupportFragmentManager();
        setContentView(R.layout.activity_main);
        fullscreen();
        checkPermissions();

        if (getPreferences(Context.MODE_PRIVATE).getBoolean("HaveRun", false)) setupView();
        else FM.beginTransaction().replace(R.id.fl_big_center, new WelcomeFragment(), "").commit();
    }

    void firstRun() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation();
        else checkPermissions();
        getPreferences(Context.MODE_PRIVATE).edit().putBoolean("HaveRun", true).apply();

        FM.beginTransaction().replace(R.id.fl_left_top, new SelectorFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_left_bottom, new SelectorFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_big_center, new SelectorFragment(), "").commit();
    }

    private void setupView() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation();
        else checkPermissions();
        FM.beginTransaction().replace(R.id.fl_left_top, new InfoFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_right_top, new WeatherFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_left_bottom, new SpotifyFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_right_bottom, new OBD2Fragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_big_center, new MapFragment(), "").commit();
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

        if (provider != null) msg(this, "Best Location Provider: " + provider);
        else {
            msg(this, "No Location provider found");
            return;
        }

        LOC.setNow(lm.getLastKnownLocation(provider));
        lm.requestLocationUpdates(provider, GPSUPDATE, 0, LocationListener);
    }

    public LocationListener LocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //if (location == loc.getNow() || location.getBearing() == 0 || location.getSpeed() == 0) return;
            LOC.setLast(LOC.getNow());
            LOC.setNow(location);
            //Log.d("GPS", "onLocationChanged: " + location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            msg("LOC Status changed: " + provider + " status: " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            msg("LOC Provider enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            msg("LOC Provider disabled: " + provider);
        }
    };
    //endregion
}