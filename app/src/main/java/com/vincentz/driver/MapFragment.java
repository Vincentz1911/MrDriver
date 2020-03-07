package com.vincentz.driver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

    private GoogleMap map;
    private LatLng pos, lastPos, camPos;
    private float speed = 0, bearing = 0;
    private int zoom = 18;
    private TextView txt_Speed, txt_Bearing1, txt_Bearing2;
    private ImageView img_compass;
    private SeekBar slider_zoom;

    void msg(final String text) { getActivity().runOnUiThread(() ->
            Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show());
    }
    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_map, vg, false);

        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager().
                findFragmentById(R.id.map))).getMapAsync(this);

        initUI(view);
        return view;
    }

    private void initUI(View view) {
        txt_Bearing1 = view.findViewById(R.id.txt_bearing1);
        txt_Bearing2 = view.findViewById(R.id.txt_bearing2);
        txt_Speed = view.findViewById(R.id.txt_speed);
        img_compass = view.findViewById(R.id.img_compass);
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(new MapStyleOptions(getString(R.string.json_mapstyle)));
        map.setBuildingsEnabled(true);
        map.setTrafficEnabled(true);

        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(false);

        //new Thread(this::updateCamera);
        mUiSettings.setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);

        if (MainActivity.lastLocation != null) {
            lastPos = new LatLng(
                    MainActivity.lastLocation.getLatitude(),
                    MainActivity.lastLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos, 15));
        } else msg("No Last Known Location");

        new Timer("UpdateCamera").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { updateCamera(); }}, 1000, 1000);
    }

    private void updateCamera() {
        // 10 kmt (18 - 3 /6 = 16.5
        // 110 kmt (18 - 30 /6 = 12
        if (MainActivity.location == null) return;

        lastPos = pos;
        pos = new LatLng(MainActivity.location.getLatitude(), MainActivity.location.getLongitude());
        speed = MainActivity.location.getSpeed();
        bearing = MainActivity.location.getBearing();
        //bearing = (location.getBearing() != 0.0 && speed > 1) ? location.getBearing() : bearing;

        if (lastPos == null) camPos = pos;
        else { camPos = new LatLng(
                    (pos.latitude + (pos.latitude - lastPos.latitude) * speed),
                    (pos.longitude + (pos.longitude - lastPos.longitude) * speed));
        }

        getActivity().runOnUiThread(() -> {
            txt_Speed.setText(getString(R.string.mapspeed, (int) (speed * 3.6)));
            txt_Bearing1.setText(getDirection(bearing));
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

//    private LocationListener GPSlistener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            if (location == null) return;
//            lastPos = pos;
//
//            speed = location.getSpeed();
//            pos = new LatLng(location.getLatitude(), location.getLongitude());
//            bearing = location.getBearing();
//            //bearing = (location.getBearing() != 0.0 && speed > 1) ? location.getBearing() : bearing;
//
//            txt_Speed.setText(getString(R.string.mapspeed, (int) (speed * 3.6)));
//            txt_Bearing1.setText(getDirection(bearing));
//            txt_Bearing2.setText(getString(R.string.bearing, (int) bearing));
//            img_compass.setRotation(bearing);
//
//            ((TextView) getView().findViewById(R.id.txt_zoom)).setText("zoom : " + zoom);
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//        }
//    };

    private String getDirection(float b) {
        b -= 22.5;
        if (b <= 0 || b > 315) return "N";
        else if (b <= 45) return "NE";
        else if (b <= 90) return "E";
        else if (b <= 135) return "SE";
        else if (b <= 180) return "S";
        else if (b <= 225) return "SW";
        else if (b <= 270) return "W";
        else if (b <= 315) return "NW";
        else return "";
    }
}
