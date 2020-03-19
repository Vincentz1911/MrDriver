package com.vincentz.driver;

import androidx.fragment.app.Fragment;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.Tools.*;

public class MapFragment extends Fragment implements Observer, OnMapReadyCallback {

    private String TAG = "Maps";
    private Location location, lastLocation;
    private GoogleMap map;
    private LatLng pos;
    private float speed = 0, bearing = 0, tilt = 4, zoom = 20, speedZoom, speedTilt;
    private TextView txt_Speed, txt_Bearing, txt_Zoom;
    private ImageView img_compass;
    private NumberPicker np_Tilt, np_Zoom;
    private boolean isTraffic = false;
    private boolean isSatellite = false;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View root = li.inflate(R.layout.fragment_map, vg, false);
        Objects.requireNonNull((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(this);

        txt_Bearing = root.findViewById(R.id.txt_bearing1);
        txt_Speed = root.findViewById(R.id.txt_speed);
        txt_Speed.setOnClickListener(view -> {
            isTraffic = isTraffic ? false : true;
            map.setTrafficEnabled(isTraffic);
            msg("Show Traffic: "+ isTraffic);
        });

        img_compass = root.findViewById(R.id.img_compass);
        img_compass.setOnClickListener(view -> {
            isSatellite = isSatellite ? false : true;
            if (isSatellite) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            else map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        });

        txt_Zoom = root.findViewById(R.id.txt_zoom);

        np_Tilt = root.findViewById(R.id.np_tilt);
        np_Tilt.setMinValue(0);
        np_Tilt.setMaxValue(6);
        np_Tilt.setValue((int) tilt);

        np_Zoom = root.findViewById(R.id.np_zoom);
        np_Zoom.setMinValue(15);
        np_Zoom.setMaxValue(25);
        np_Zoom.setValue((int) zoom);
        //endregion
        LOC.addObserver(this);

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(new MapStyleOptions(getString(R.string.bluemapday)));
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(true);
        map.setTrafficEnabled(isTraffic);
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //Projection projection = map.getProjection();
        //projection.getVisibleRegion();


        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(false);

        if (location != null)
            pos = new LatLng(location.getLatitude(), location.getLongitude());
        else if (lastLocation != null)
            pos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        else return;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));

        new Timer("Cam").schedule(new TimerTask() {
            @Override
            public void run() {
                updateCamera();
            }
        }, 0, timerUpdate);
    }

    @Override
    public void update(Observable locModel, Object loc) {
        location = ((LocationModel) locModel).getNow();
        lastLocation = ((LocationModel) locModel).getLast();
        //if (isMapReady && location != null && lastLocation != null) updateCamera();
    }

    int count = 1;

    private void updateCamera() {
        pos = new LatLng(location.getLatitude(), location.getLongitude());

        if (location.hasBearing()) bearing = location.getBearing();
        if (location.hasSpeed()) speed = location.getSpeed();

        zoom = np_Zoom.getValue();
        tilt = np_Tilt.getValue() * 10;
        speedTilt = (tilt + speed / 2 < 70) ? tilt + speed / 2 : 70;
        speedZoom = (zoom - speed / 10 < 20) ? zoom - speed / 10 : 20;

        //int timeBetween = (int) (location.getElapsedRealtimeNanos() - lastLocation.getElapsedRealtimeNanos()) / 1000000;
        //bearing = (location.getBearing() != 0.0 && speed > 1) ? location.getBearing() : bearing;

        ACT.runOnUiThread(() -> {
            txt_Speed.setText(getString(R.string.mapspeed, (int) (speed * 3.6)));
            txt_Bearing.setText(getString(R.string.bearing, Tools.getDirection(bearing), (int) bearing));
            //txt_Bearing2.setText(getString(R.string.bearing, Tools.getDirection(bearing), (int) bearing));
            img_compass.setRotation(bearing);
            txt_Zoom.setText("s" + (int) speed + " /z-sz " + (int) zoom + "/" + (int) speedZoom
                    + " t/st " + (int) tilt + "/" + (int) speedTilt);

            // 10 kmt (18 - 3 /6 = 16.5 * (1 + location.getSpeed()/100))
            // 110 kmt (18 - 30 /6 = 12 * (1 + location.getSpeed()/100)
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pos)
                    .zoom(speedZoom)
                    .bearing(bearing)
                    .tilt(speedTilt)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Tools.cameraUpdate, null);
            map.setPadding(0, 350, 0, 0);
        });
    }
}