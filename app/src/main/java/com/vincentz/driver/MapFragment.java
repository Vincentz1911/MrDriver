package com.vincentz.driver;

import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private Activity activity;
    private GoogleMap map;
    private LatLng pos, lastPos, camPos;
    private float speed = 0, bearing = 0;
    private int zoom = 18;
    private TextView txt_Speed, txt_Bearing1, txt_Bearing2;
    private ImageView img_compass;
    private SeekBar slider_zoom;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_map, vg, false);
        activity = getActivity();
        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager().
                findFragmentById(R.id.map))).getMapAsync(this);
        txt_Bearing1 = view.findViewById(R.id.txt_bearing1);
        txt_Bearing2 = view.findViewById(R.id.txt_bearing2);
        txt_Speed = view.findViewById(R.id.txt_speed);
        img_compass = view.findViewById(R.id.img_compass);
        //initUI(view);
        return view;
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
        map.setMyLocationEnabled(true);
        map.setMapStyle(new MapStyleOptions(getString(R.string.mapstyle2)));
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);

        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(true);

        if (MainActivity.LASTLOCATION != null) {
            lastPos = new LatLng(
                    MainActivity.LASTLOCATION.getLatitude(),
                    MainActivity.LASTLOCATION.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos, 15));
        } else Tools.msg(activity, "No Last Known Location");

        new Timer("UpdateCamera").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { updateCamera(); }}, 1000, 1000);
    }

    private void updateCamera() {
        // 10 kmt (18 - 3 /6 = 16.5
        // 110 kmt (18 - 30 /6 = 12
        if (MainActivity.LOCATION == null) return;

        lastPos = pos;
        pos = new LatLng(MainActivity.LOCATION.getLatitude(), MainActivity.LOCATION.getLongitude());
        speed = MainActivity.LOCATION.getSpeed();
        bearing = MainActivity.LOCATION.getBearing();
        //bearing = (location.getBearing() != 0.0 && speed > 1) ? location.getBearing() : bearing;

        if (lastPos == null) camPos = pos;
        else { camPos = new LatLng(
                    (pos.latitude + (pos.latitude - lastPos.latitude) * speed/10),
                    (pos.longitude + (pos.longitude - lastPos.longitude) * speed/10));
        }

        activity.runOnUiThread(() -> {
            txt_Speed.setText(getString(R.string.mapspeed, (int) (speed * 3.6)));
            txt_Bearing1.setText(Tools.getDirection(bearing));
            txt_Bearing2.setText(getString(R.string.bearing, (int) bearing));
            img_compass.setRotation(bearing);

            float speedzoom = zoom - speed / 20;

            ((TextView) getView().findViewById(R.id.txt_zoom)).setText("zoom : " + zoom + "/" + speedzoom);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(camPos)
                    .zoom(speedzoom)
                    .bearing(bearing)
                    .tilt(45)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });
    }
}
