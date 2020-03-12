package com.vincentz.driver;

import androidx.fragment.app.Fragment;

import android.location.Location;
import android.os.Bundle;
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

public class MapFragment extends Fragment implements Observer, OnMapReadyCallback {

    private MainActivity main;
    private Location location, lastLocation;
    private GoogleMap map;
    private LatLng pos, lastPos, camPos;
    //private float speed = 0, bearing = 0;
    private int zoom = 18;
    private TextView txt_Speed, txt_Bearing1, txt_Bearing2;
    private ImageView img_compass;
    private SeekBar slider_zoom;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View view = li.inflate(R.layout.fragment_map, vg, false);
        txt_Bearing1 = view.findViewById(R.id.txt_bearing1);
        txt_Bearing2 = view.findViewById(R.id.txt_bearing2);
        txt_Speed = view.findViewById(R.id.txt_speed);
        img_compass = view.findViewById(R.id.img_compass);
        //endregion
        if (getActivity() != null) main = (MainActivity) getActivity();
        main.loc.addObserver(this);
        Objects.requireNonNull((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(this);
        return view;
    }

    @Override
    public void update(Observable locModel, Object loc) {
        location = ((LocationModel)locModel).getNow();
        lastLocation = ((LocationModel)locModel).getLast();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));

        new Timer("Cam").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { updateCamera(); }}, 2000, 200);
    }

    private void updateCamera() {
        if (pos != null) lastPos = pos;
        if (lastPos == null) camPos = pos;
        else camPos = new LatLng(
                    (pos.latitude + (pos.latitude - lastPos.latitude)) ,
                    (pos.longitude + (pos.longitude - lastPos.longitude) ));


        main.runOnUiThread(() -> {
            txt_Speed.setText(getString(R.string.mapspeed, (int) (location.getSpeed() * 3.6)));
            txt_Bearing1.setText(Tools.getDirection(location.getBearing()));
            txt_Bearing2.setText(getString(R.string.bearing, (int) location.getBearing()));
            img_compass.setRotation(location.getBearing());

            // 10 kmt (18 - 3 /6 = 16.5 * (1 + location.getSpeed()/100))
            // 110 kmt (18 - 30 /6 = 12 * (1 + location.getSpeed()/100)
            //bearing = (location.getBearing() != 0.0 && speed > 1) ? location.getBearing() : bearing;

            float speedzoom = zoom - location.getSpeed() / 20;

            ((TextView) getView().findViewById(R.id.txt_zoom)).setText("zoom : " + zoom + "/" + speedzoom);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(camPos)
                    .zoom(20)
                    .bearing(location.getBearing())
                    .tilt(50 + location.getSpeed()/2)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });
    }
}
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