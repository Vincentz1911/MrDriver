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
import android.widget.FrameLayout;

import java.util.List;

public class MainActivity extends FragmentActivity {

    public static boolean[] HAVE_PERMISSIONS;
    static Location location, lastLocation;
    private static int countGPS;
    static FrameLayout centerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        checkPermissions();
        setContentView(R.layout.activity_main);
        centerLayout = findViewById(R.id.fl_big_center);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fl_left_top, new InfoFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_left_bottom, new WeatherFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_bottom, new SpotifyFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_big_center, new MapFragment(), "").commit();
    }

    //region PERMISSIONS
    void checkPermissions() {
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        HAVE_PERMISSIONS = new boolean[PERMISSIONS.length];
        if (!hasPermissions(PERMISSIONS))
            ActivityCompat.requestPermissions(this, PERMISSIONS, 100);
        if (HAVE_PERMISSIONS[0] || HAVE_PERMISSIONS[1]) getLocation();
    }

    private boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (int p = 0; p < permissions.length; p++) {
                if (ActivityCompat.checkSelfPermission(this, permissions[p])
                        != PackageManager.PERMISSION_GRANTED) return false;
                else HAVE_PERMISSIONS[p] = true;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int p = 0; p < permissions.length; p++)
            if (grantResults.length > 0 && grantResults[p] == PackageManager.PERMISSION_GRANTED)
                HAVE_PERMISSIONS[p] = true;
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

        List<String> listLM = lm.getAllProviders();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedRequired(true);
        criteria.setBearingRequired(true);
        //criteria.setPowerRequirement(Criteria.ACCURACY_HIGH);
        String provider = lm.getBestProvider(criteria, false);
        lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (provider == null) return;
        lm.requestLocationUpdates(provider, 0, 0, GPSlistener);
    }

    public LocationListener GPSlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location onChanged) {
            location = onChanged;
            countGPS++;
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