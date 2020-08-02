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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.Objects;

import static com.vincentz.driver.Tools.*;

public class MainActivity extends FragmentActivity {

    private FrameLayout fl_leftWindow, fl_spotify, fl_weather, fl_navigation, fl_camera, fl_obd2;
    private ImageView btn_fullscreen;
    private FrameLayout activeFrame;
    boolean isFullscreen;

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

    private void init() {
        ACT = this;
        LOC = new GPSLocationModel();
        RQ = Volley.newRequestQueue(this);
        IO = getPreferences(MODE_PRIVATE);

        setContentView(R.layout.activity_main);
        fullscreen();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> fullscreen());
        checkPermissions();

        //IF APP NEVER RUN BEFORE, MAKE WELCOMESCREEN SO PERMISSIONS DOESNT F.UP
        if (IO.getBoolean("HaveRun", false)) setupView();
        else getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_map_overlay, new WelcomeFragment(), "").commit();

        initButtons();
    }

    private void initButtons() {
        fl_navigation = findViewById(R.id.fl_navigation);
        fl_spotify = findViewById(R.id.fl_spotify);
        fl_weather = findViewById(R.id.fl_weather);
        fl_camera = findViewById(R.id.fl_camera);
        fl_obd2 = findViewById(R.id.fl_obd2);
        fl_leftWindow = findViewById(R.id.fl_left_window);

        btn_fullscreen = findViewById(R.id.img_fullscreen);
        btn_fullscreen.setOnClickListener(view -> {
            if (!isFullscreen) hideAllFrameLayouts();
            else showFrameLayout(activeFrame);
        });

        findViewById(R.id.btn_navigation).setOnClickListener(v -> showFrameLayout(fl_navigation));
        findViewById(R.id.btn_spotify).setOnClickListener(v -> showFrameLayout(fl_spotify));
        findViewById(R.id.btn_weather).setOnClickListener(v -> showFrameLayout(fl_weather));
        findViewById(R.id.btn_obd2).setOnClickListener(v -> showFrameLayout(fl_obd2));
        findViewById(R.id.btn_camera).setOnClickListener(v -> showFrameLayout(fl_camera));
    }

    private void showFrameLayout(FrameLayout fl) {
        hideAllFrameLayouts();
        isFullscreen = false;
        activeFrame = fl;
        fl.setVisibility(View.VISIBLE);
        fl_leftWindow.setVisibility(View.VISIBLE);
        btn_fullscreen.setImageResource(R.drawable.mic_fullscreen_100dp);
    }

    private void hideAllFrameLayouts() {
        isFullscreen = true;
        fl_leftWindow.setVisibility(View.GONE);
        fl_spotify.setVisibility(View.GONE);
        fl_weather.setVisibility(View.GONE);
        fl_navigation.setVisibility(View.GONE);
        fl_obd2.setVisibility(View.GONE);
        fl_camera.setVisibility(View.GONE);
        btn_fullscreen.setImageResource(R.drawable.mic_fullscreen_exit_100dp);
    }

    void setupView() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation();
        else checkPermissions();
        IO.edit().putBoolean("HaveRun", true).apply();
        FragmentManager FM = getSupportFragmentManager();
        FM.beginTransaction().replace(R.id.fl_map_overlay, new MapFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_navigation, new NavigationFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_dateAndTime, new InfoFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_spotify, new SpotifyFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_weather, new WeatherFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_obd2, new OBD2Fragment(), "").commit();
        //FM.beginTransaction().replace(R.id.fl_camera, new CameraFragment(), "").commit();
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
        public void onStatusChanged(String provider, int status, Bundle extras) { }

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