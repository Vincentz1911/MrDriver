package com.vincentz.driver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private LatLng pos;
    private float bearing = 0;
    private int zoom = 18;
    private TextView txt_Speed, txt_Bearing1, txt_Bearing2;
    private ImageView img_compass;
    private SeekBar slider_zoom;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_map, vg, false);

        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);

        txt_Bearing1 = view.findViewById(R.id.txt_bearing1);
        txt_Bearing2 = view.findViewById(R.id.txt_bearing2);
        txt_Speed = view.findViewById(R.id.txt_speed);
        img_compass = view.findViewById(R.id.img_compass);
        slider_zoom = view.findViewById(R.id.slider_zoom);
        slider_zoom.setProgress(zoom);
        slider_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                zoom = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(new MapStyleOptions(getString(R.string.json_mapstyle)));
        map.setBuildingsEnabled(true);
        map.setTrafficEnabled(true);

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Tools.checkPermissions(getActivity());
            return;
        }

        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);

        LocationManager lm =
                (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng pos = new LatLng(last.getLatitude(), last.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
        } catch (Exception e) {
            Tools.msg(getContext(), "No Last Known Location");
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, GPSlistener);
    }



    private LocationListener GPSlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) return;
            LatLng oldPos = pos;
            float speed = location.getSpeed();
            pos = new LatLng(location.getLatitude(), location.getLongitude());
            bearing = (location.getBearing() != 0.0 && speed > 1) ? location.getBearing() : bearing;

            txt_Speed.setText(getString(R.string.mapspeed, (int) (speed * 3.6)));
            txt_Bearing1.setText(getDirection(bearing));
            txt_Bearing2.setText(getString(R.string.bearing, (int) bearing));
            ((TextView)getView().findViewById(R.id.txt_zoom)).setText("zoom : " + zoom);

            img_compass.setRotation(bearing);
            LatLng camPos;
            if (oldPos == null) camPos = pos;
            else {
                camPos = new LatLng(
                        (pos.latitude + (pos.latitude - oldPos.latitude) * speed),
                        (pos.longitude + (pos.longitude - oldPos.longitude) * speed));
            }

            // 10 kmt (18 - 3 /6 = 16.5
            // 110 kmt (18 - 30 /6 = 12

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(camPos)
                    .zoom(zoom - speed / 6)
                    .bearing(bearing)
                    .tilt(45)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) { }
        @Override public void onProviderDisabled(String provider) { }
    };

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
