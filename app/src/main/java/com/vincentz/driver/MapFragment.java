package com.vincentz.driver;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.maps.android.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.Tools.ACT;
import static com.vincentz.driver.Tools.LOC;
import static com.vincentz.driver.Tools.RQ;
import static com.vincentz.driver.Tools.msg;
import static com.vincentz.driver.Tools.timerUpdate;

public class MapFragment extends Fragment implements Observer, OnMapReadyCallback {

    private String TAG = "Maps";
    private Location location, lastLocation;
    private GoogleMap map;
    private LatLng pos;
    private float speed = 0, bearing = 0, tilt = 4, zoom = 20, speedZoom, speedTilt;
    private TextView txt_Speed, txt_Bearing, txt_Zoom, txt_Street;
    private ImageView img_compass, img_fullscreen;
    private String street;
    private NumberPicker np_Tilt, np_Zoom;
    private boolean isTraffic = false, isSatellite = false, isFullscreen = false;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View root = li.inflate(R.layout.fragment_map, vg, false);
        Objects.requireNonNull((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(this);

        txt_Bearing = root.findViewById(R.id.txt_bearing1);
        txt_Speed = root.findViewById(R.id.txt_speed);
        txt_Street = root.findViewById(R.id.txt_street);
        txt_Speed.setOnClickListener(view -> {
            geoLocationReversed();
        });

        txt_Speed.setOnLongClickListener(view -> {
            isTraffic = isTraffic ? false : true;
            map.setTrafficEnabled(isTraffic);
            msg("Show Traffic: " + isTraffic);
            return true;
        });

        img_compass = root.findViewById(R.id.img_compass);
        img_fullscreen = root.findViewById(R.id.img_fullscreen);
        img_fullscreen.setOnClickListener(view -> {
            isFullscreen = isFullscreen ? false : true;
            if (isFullscreen){
                getActivity().findViewById(R.id.left_side).setVisibility(View.GONE);
                getActivity().findViewById(R.id.right_side).setVisibility(View.GONE);
                img_fullscreen.setImageResource(R.drawable.ic_fullscreen_exit_black_68dp);
            } else {
                getActivity().findViewById(R.id.left_side).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.right_side).setVisibility(View.VISIBLE);
                img_fullscreen.setImageResource(R.drawable.ic_fullscreen_black_68dp);
            }
        });

        img_compass.setOnLongClickListener(view -> {
            isSatellite = isSatellite ? false : true;
            if (isSatellite) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            else map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        });
        img_compass.setOnClickListener(view -> { routing(); });

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

    void routing() {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car" +
                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62" +
                "&start=" + LOC.getNow().getLongitude() + ",%20" + LOC.getNow().getLatitude() +
                "&end=" + "12.4818872,%20" + "55.6805428";

        //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    GeoJsonLayer layer = new GeoJsonLayer(map, response);
                    layer.getDefaultLineStringStyle().setColor(R.color.colorRoute);
                    layer.getDefaultPointStyle().setVisible(true);
                    layer.addLayerToMap();
                },
                error -> msg(ACT, "Volley Routing Error")));
    }

    void routingResponse(JSONObject response) {
        try {
            street = response.getJSONArray("features").getJSONObject(0)
                    .getJSONObject("properties").getString("street");
            msg(street);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void geoLocationReversed() {
        String url = "https://api.openrouteservice.org/geocode/reverse" +
                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62" +
                "&point.lat=" + LOC.getNow().getLatitude() +
                "&point.lon=" + LOC.getNow().getLongitude() +
                "&size=1&layers=address";

        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        street = response.getJSONArray("features").getJSONObject(0)
                                .getJSONObject("properties").getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> msg(ACT, "Volley Location Error")));
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

    int count = 0;

    private void updateCamera() {
        pos = new LatLng(location.getLatitude(), location.getLongitude());

        if (location.hasBearing()) bearing = location.getBearing();
        if (location.hasSpeed()) speed = location.getSpeed();

        count++;
        if (count > 3) {
            //geoLocationReversed();
            count = 0;
        }


        zoom = np_Zoom.getValue();
        tilt = np_Tilt.getValue() * 10;
        speedTilt = (tilt + speed / 2 < 70) ? tilt + speed / 2 : 70;
        speedZoom = (zoom - speed / 10 < 20) ? zoom - speed / 10 : 20;

        //int timeBetween = (int) (location.getElapsedRealtimeNanos() - lastLocation.getElapsedRealtimeNanos()) / 1000000;
        //bearing = (location.getBearing() != 0.0 && speed > 1) ? location.getBearing() : bearing;

        ACT.runOnUiThread(() -> {
            txt_Street.setText(street);
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