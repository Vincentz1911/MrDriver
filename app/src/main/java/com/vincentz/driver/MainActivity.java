package com.vincentz.driver;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.Objects;

import static com.vincentz.driver.Tools.*;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setThemeBasedOnTimeOfDay();
        super.onCreate(savedInstanceState);
        init();
    }

//    @Override
//    protected void onResume() {
//        setThemeBasedOnTimeOfDay();
//        super.onResume();
//        init();
//    }

    //SETS THEME BASED ON SUNRISE AND SUNSET.
    private void setThemeBasedOnTimeOfDay() {
        Calendar now = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        Calendar sunDown = Calendar.getInstance();

        sunUp.setTimeInMillis(getPreferences(Context.MODE_PRIVATE).getLong("Sunrise", 0));
        sunUp.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        sunDown.setTimeInMillis(getPreferences(Context.MODE_PRIVATE).getLong("Sunset", 0));
        sunDown.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        if (now.after(sunUp) && now.before(sunDown)) setTheme(R.style.AppTheme_Day);
        else setTheme(R.style.AppTheme_Night);
    }

    void init() {
        ACT = this;
        LOC = new GPSLocationModel();
        RQ = Volley.newRequestQueue(this);
        FM = getSupportFragmentManager();
        IO = getPreferences(MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        fullscreen();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> fullscreen());
        checkPermissions();

        if (IO.getBoolean("HaveRun", false)) setupView();
        else FM.beginTransaction().replace(R.id.fl_map_overlay, new WelcomeFragment(), "").commit();

        initButtons();
    }

    private void initButtons() {
        ImageView btn_navigation = findViewById(R.id.btn_navigation);
        ImageView btn_weather = findViewById(R.id.btn_weather);
        ImageView btn_obd2 = findViewById(R.id.btn_obd2);
        ImageView btn_spotify = findViewById(R.id.btn_spotify);
        ImageView btn_phone = findViewById(R.id.btn_phone);
        ImageView btn_camera = findViewById(R.id.btn_camera);

        btn_spotify.setOnClickListener(v -> {
            hideAllFrameLayouts();
            FM.beginTransaction().replace(R.id.fl_left_window, new SpotifyFragment(), "").commit();
        });
        btn_weather.setOnClickListener(v -> {
            FM.beginTransaction().replace(R.id.fl_left_window, new WeatherFragment(), "").commit();
        });
    }

    private void hideAllFrameLayouts() {



    }


    void setupView() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation();
        else checkPermissions();
        IO.edit().putBoolean("HaveRun", true).apply();
        FM.beginTransaction().replace(R.id.fl_right_top, new InfoFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_left_window, new SpotifyFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_map_overlay, new MapFragment(), "").commit();
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
    public void onRequestPermissionsResult(int rc, String[] permissions, @NonNull int[] results) {
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
        if (provider != null) msg("Best Location Provider: " + provider);
        else {
            msg("No Location provider found");
            return;
        }
        lm.requestLocationUpdates(provider, 0, 0, LocationListener);
        LOC.setNow(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        if (LOC.now() == null)
            LOC.setNow(lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
    }

    public LocationListener LocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (!location.hasSpeed() || !location.hasBearing()) return;
            LOC.setLast(LOC.now());
            LOC.setNow(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            msg("LOC enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            msg("LOC disabled: " + provider);
        }
    };
    //endregion
}