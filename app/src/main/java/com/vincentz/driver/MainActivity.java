package com.vincentz.driver;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.toolbox.Volley;
import com.vincentz.driver.navigation.GPSLocationModel;
import com.vincentz.driver.navigation.MapFragment;
import com.vincentz.driver.navigation.NavigationFragment;
import com.vincentz.driver.weather.WeatherFragment;

import java.util.Calendar;
import java.util.Objects;
import java.util.Set;

import static com.vincentz.driver.Tools.*;

public class MainActivity extends FragmentActivity {

    private FrameLayout[] frames;
    private FrameLayout activeFrame;
    private ImageView fullscreenButton;
    private boolean isFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setThemeBasedOnTimeOfDay();
        super.onCreate(savedInstanceState);
        IO = getPreferences(MODE_PRIVATE);
        LOC = new GPSLocationModel();
        RQ = Volley.newRequestQueue(this);
        theme();

        fullscreen(this);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> fullscreen(this));
        checkPermissions();
        initButtons();

        //CREATES A NEW TEXT TO SPEECH INSTANTIATION
        TTS = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                TTS.setLanguage(LANG.get(IO.getInt("Language", 0)));
                Set<Voice> voices = TTS.getVoices();
                msg(this, "Voices : " + voices.size());
            }
        });

        //Driver d = ;
        new Driver().checkDriver(this);

        //IF APP NEVER RUN BEFORE, MAKE WELCOMESCREEN SO PERMISSIONS DOESNT F.UP
        if (IO.getBoolean("HaveRun", false)) setupView();
        else getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_map_overlay, new WelcomeFragment(), "").commit();

    }

    private void theme() {
        switch (IO.getInt("Theme", 0)) {
            case 0:
                setTheme(R.style.AppTheme_Air);
                break;
            case 1:
                setTheme(R.style.AppTheme_Blue);
                break;
            case 2:
                setTheme(R.style.AppTheme_Day);
                break;
            case 3:
                setTheme(R.style.AppTheme_Night);
                break;
            default:
                setTheme(R.style.AppTheme_Day);
        }
        setContentView(R.layout.activity_main);
    }

    //SETS THEME BASED ON SUNRISE AND SUNSET.
    private void setThemeBasedOnTimeOfDay() {
        Calendar now = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        Calendar sunDown = Calendar.getInstance();

        sunUp.setTimeInMillis(IO.getLong("Sunrise", 0));
        sunUp.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        sunDown.setTimeInMillis(IO.getLong("Sunset", 0));
        sunDown.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        if (now.after(sunDown) && now.before(sunUp)) setTheme(R.style.AppTheme_Night);
        else setTheme(R.style.AppTheme_Day);
    }

    private void initButtons() {
        FrameLayout sidebar = findViewById(R.id.sidebar);
        FrameLayout fl_navigation = findViewById(R.id.fl_navigation);
        FrameLayout fl_spotify = findViewById(R.id.fl_spotify);
        FrameLayout fl_weather = findViewById(R.id.fl_weather);
        FrameLayout fl_obd2 = findViewById(R.id.fl_obd2);
        FrameLayout fl_settings = findViewById(R.id.fl_settings);

        findViewById(R.id.btn_navigation).setOnClickListener(v -> showFrame(fl_navigation));
        findViewById(R.id.btn_spotify).setOnClickListener(v -> showFrame(fl_spotify));
        findViewById(R.id.btn_weather).setOnClickListener(v -> showFrame(fl_weather));
        findViewById(R.id.btn_obd2).setOnClickListener(v -> showFrame(fl_obd2));
        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.ankai.cardvr");
            if (launchIntent != null) startActivity(launchIntent);
        });

        findViewById(R.id.btn_settings).setOnClickListener(v -> showFrame(fl_settings));
        findViewById(R.id.btn_phone).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL)));

        fullscreenButton = findViewById(R.id.img_fullscreen);
        fullscreenButton.setOnClickListener(view -> {
            if (!isFullscreen) isFullscreen = hideAllFrames();
            else isFullscreen = showFrame(activeFrame);
        });

        frames = new FrameLayout[]{sidebar, fl_navigation, fl_spotify, fl_weather, fl_obd2, fl_settings};
        showFrame(frames[1]);
    }

    private boolean showFrame(FrameLayout fl) {
        hideAllFrames();
        activeFrame = fl;
        activeFrame.setVisibility(View.VISIBLE);
        frames[0].setVisibility(View.VISIBLE);
        fullscreenButton.setImageResource(R.drawable.fic_fullscreen_100dp);
        return false;
    }

    private boolean hideAllFrames() {
        for (FrameLayout fl : frames) fl.setVisibility(View.GONE);
        fullscreenButton.setImageResource(R.drawable.fic_fullscreen_exit_100dp);
        return true;
    }

    void setupView() {
        if (PERMISSIONS[0] || PERMISSIONS[1]) getLocation();
        else checkPermissions();
        IO.edit().putBoolean("HaveRun", true).apply();
        FragmentManager FM = getSupportFragmentManager();
        FM.beginTransaction().replace(R.id.fl_map_overlay, new MapFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_navigation, new NavigationFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_spotify, new SpotifyFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_weather, new WeatherFragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_obd2, new OBD2Fragment(), "").commit();
        FM.beginTransaction().replace(R.id.fl_settings, new SettingsFragment(), "").commit();
    }

    //region PERMISSIONS
    void checkPermissions() {
        String[] REQUEST_PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
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
        super.onRequestPermissionsResult(rc, permissions, results);
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
        if (lm == null) return;
        String provider = lm.getBestProvider(criteria, true);
        if (provider != null) msg(this, "Best Location Provider: " + provider);
        else {
            msg(this, "No GPS found");
            return;
        }
        lm.requestLocationUpdates(provider, 0, 0, LocationListener);
        LOC.setNow(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        if (LOC.now() == null)
            LOC.setNow(lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
    }

    LocationListener LocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (!location.hasSpeed() || !location.hasBearing()) return;
            LOC.setLast(LOC.now());
            LOC.setNow(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };
    //endregion
}