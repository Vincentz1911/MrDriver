package com.vincentz.driver;

import androidx.fragment.app.Fragment;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
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
    private LatLng pos, lastPos, camPos;
    private float speed = 0, bearing = 0, tilt;
    private int zoom = 20;
    private TextView txt_Speed, txt_Bearing1, txt_Bearing2, txt_Zoom;
    private ImageView img_compass;
    private boolean isMapReady = false;
    private SeekBar slider_zoom;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View root = li.inflate(R.layout.fragment_map, vg, false);
        txt_Bearing1 = root.findViewById(R.id.txt_bearing1);
        txt_Bearing2 = root.findViewById(R.id.txt_bearing2);
        txt_Speed = root.findViewById(R.id.txt_speed);
        img_compass = root.findViewById(R.id.img_compass);
        txt_Zoom = root.findViewById(R.id.txt_zoom);
        //endregion
        LOC.addObserver(this);
        Objects.requireNonNull((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(this);
        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        isMapReady = true;
        map = googleMap;
        map.setMapStyle(new MapStyleOptions(getString(R.string.mapstyle2)));
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);

        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(true);

        if (location != null)
            pos = new LatLng(location.getLatitude(), location.getLongitude());
        else if (lastLocation != null)
            pos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        else return;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        new Timer("Mark").schedule(new TimerTask() {
            @Override
            public void run() { updateMarker();
            }
        }, 0, timerUpdate);

        new Timer("Cam").schedule(new TimerTask() {
            @Override
            public void run() { updateCamera();
            }
        }, 0, timerUpdate);
    }

    private void updateMarker() {
    }

    @Override
    public void update(Observable locModel, Object loc) {
        location = ((LocationModel) locModel).getNow();
        lastLocation = ((LocationModel) locModel).getLast();

        //if (isMapReady && location != null && lastLocation != null) updateCamera();
    }
int count =1;


    private void updateCamera() {
        // Location x = 10
        // LastLocation = 5

        // 0.2 sec later
        // x = 10 + (10-5)/5 = 11

        // 1 second later
        // Location x = 15
        // LastLocation = 10

        LatLng posP =new LatLng(location.getLatitude(), location.getLongitude());
        LatLng posPLast =new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        if (pos.equals(posP)) {
            count ++;
        } else count=1;

        camPos =  new LatLng(
                posP.latitude + (posP.latitude - posPLast.latitude) * 5,
                posP.longitude + (posP.longitude - posPLast.longitude) * 5);


        //if pos = location : pos = Location + location-lastlocation/5
        //lastPos = pos;
//        if (location != null)
            pos = new LatLng(location.getLatitude(), location.getLongitude());
//        else if (lastLocation != null)
            lastPos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//        else return;

//        if (pos == lastPos) {
//
//        }

        //if (pos != null) lastPos = pos; location.getSpeed()* location.getSpeed()
//        if (lastPos == null) camPos = pos;
//        else
//            camPos = new LatLng(
//                    (pos.latitude + (pos.latitude - lastPos.latitude) * 5),
//                    (pos.longitude + (pos.longitude - lastPos.longitude) * 5));

        if (location.hasBearing()) bearing = location.getBearing();
        if (location.hasSpeed()) {
            speed = location.getSpeed();
            tilt = 20 + speed;
        }
        float speedzoom = zoom - speed / 10;

        int timeBetween = (int) (location.getElapsedRealtimeNanos() - lastLocation.getElapsedRealtimeNanos()) / 1000000;
        Log.d(TAG, "updateCamera: " + camPos);
        ACT.runOnUiThread(() -> {
            txt_Speed.setText(getString(R.string.mapspeed, (int) (speed * 3.6)));
            txt_Bearing1.setText(Tools.getDirection(bearing));
            txt_Bearing2.setText(getString(R.string.bearing, (int) bearing));
            img_compass.setRotation(bearing);
            txt_Zoom.setText("c/z/sz/t" + count + " / "  +zoom + " / " + speedzoom + " / "+ tilt);

            // 10 kmt (18 - 3 /6 = 16.5 * (1 + location.getSpeed()/100))
            // 110 kmt (18 - 30 /6 = 12 * (1 + location.getSpeed()/100)
            //bearing = (location.getBearing() != 0.0 && speed > 1) ? location.getBearing() : bearing;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(camPos)
                    .zoom(speedzoom)
                    .bearing(bearing)
                    .tilt(tilt)
                    .build();

//            if (timeBetween > 0)
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                    Tools.cameraUpdate, null);
        });
    }
}


//    private void initUI(View view) {
//
//        slider_zoom = view.findViewById(R.id.slider_zoom);
//        slider_zoom.setProgress(zoom);
//        slider_zoom.setMax(20);
//        slider_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { zoom = progress; }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) { }
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) { }
//        });
//    }


//        double d = 0;//location.getSpeed()/1000;
//        double tc = location.getBearing();
//        double lat1=location.getLatitude();
//        double lon1=location.getLongitude();
//
//        double lat= Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d) *  Math.cos(tc));
//        double dlon = Math.atan2(Math.sin(tc) * Math.sin(d) * Math.cos(lat1), Math.cos(d) - Math.sin(lat1) * Math.sin(lat));
//        double lon = Math.floorMod((long) (lon1 - dlon + Math.PI), (long) (2 *Math.PI)) - Math.PI;
//        camPos = new LatLng(lat1, lon1);

//        lat =asin( sin(lat1)*cos(d) + cos(lat1)*sin(d)*cos(tc))
//        dlon=atan2(sin(tc)*sin(d)*cos(lat1),cos(d)-sin(lat1)*sin(lat))
//        lon=mod( lon1-dlon +pi,2*pi )-pi