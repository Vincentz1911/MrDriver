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
    private boolean haveLocationPermission;
    private TextView txt_Speed, txt_Bearing;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_map, vg, false);

        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);;

        txt_Bearing = view.findViewById(R.id.txt_bearing);
        txt_Speed = view.findViewById(R.id.txt_speed);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(new MapStyleOptions(getString(R.string.json_mapstyle)));
        map.setBuildingsEnabled(true);
        map.setTrafficEnabled(true);

        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Tools.msg(getContext(), "Permission is required for location");
            Tools.checkPermissions(getActivity());
        } else haveLocationPermission = true;

        do { Tools.checkPermissions(getActivity());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!haveLocationPermission);

        map.setMyLocationEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);

        LocationManager lm =
                (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng pos = new LatLng(last.getLatitude(), last.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
        } catch (Exception e) {
            Tools.msg(getContext(), "No Last Known Location");
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 1, GPSlistener);
    }

    private LocationListener GPSlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) return;
            LatLng oldPos = pos;
            float speed = location.getSpeed();
            pos = new LatLng(location.getLatitude(), location.getLongitude());
            bearing = location.getBearing() != 0.0 ? location.getBearing() : bearing;

            txt_Speed.setText(getString(R.string.speed, String.valueOf(speed)));
            txt_Bearing.setText(getString(R.string.bearing, bearing));

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
                    .zoom(18 - speed / 6)
                    .bearing(bearing)
                    .tilt(45)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) { }
        @Override
        public void onProviderDisabled(String provider) { }
    };
}
