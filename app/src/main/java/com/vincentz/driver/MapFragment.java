package com.vincentz.driver;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private GeoJsonLayer route;
    private LatLng pos;
    private float speed = 0, bearing = 0, tilt = 4, zoom = 20, speedZoom, speedTilt;
    private TextView txt_Speed, txt_Bearing, txt_Street, txt_Zoom;
    private ImageView img_compass, img_fullscreen, img_directions;
    private ListView lv_searchResults;
    private LinearLayout layout_search;
    private EditText input_search;
    private String street;
    private NumberPicker np_Tilt, np_Zoom;
    private boolean isTraffic = false, isSatellite = false, isFullscreen = false, isCamLock = true;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View root = li.inflate(R.layout.fragment_map, vg, false);
        Objects.requireNonNull((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(this);

        layout_search = root.findViewById(R.id.layout_search);
        lv_searchResults = root.findViewById(R.id.listview_locations);

        input_search = root.findViewById(R.id.input_search);
        input_search.addTextChangedListener(new TextWatcher() {
            Timer requestAutoComplete;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (requestAutoComplete != null) {
                    requestAutoComplete.cancel();
                    requestAutoComplete.purge();
                }
                requestAutoComplete = new Timer("AutoComplete");
                requestAutoComplete.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String url = "https://api.openrouteservice.org/geocode/autocomplete" +
                                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62"
                                + "&size=5&text=" + editable.toString()
                                + "&boundary.circle.lon=" + LOC.getNow().getLongitude()
                                + "&boundary.circle.lat=" + LOC.getNow().getLatitude()
                                + "&boundary.circle.radius=500&boundary.country=DK";

                        //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
                        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                                response -> searchAutoComplete(response),
                                error -> msg(ACT, "Volley Routing Error")));
                    }
                }, 2000);
            }
        });


        txt_Bearing = root.findViewById(R.id.txt_bearing1);
        txt_Street = root.findViewById(R.id.txt_street);

        np_Tilt = root.findViewById(R.id.np_tilt);
        np_Tilt.setMinValue(0);
        np_Tilt.setMaxValue(6);
        np_Tilt.setValue((int) tilt);

        np_Zoom = root.findViewById(R.id.np_zoom);
        np_Zoom.setMinValue(15);
        np_Zoom.setMaxValue(25);
        np_Zoom.setValue((int) zoom);

        txt_Speed = root.findViewById(R.id.txt_speed);
        txt_Speed.setOnClickListener(view -> {
            geoLocationReversed(pos);
        });
        txt_Speed.setOnLongClickListener(view -> {
            isTraffic = isTraffic ? false : true;
            map.setTrafficEnabled(isTraffic);
            msg("Show Traffic: " + isTraffic);
            return true;
        });

        img_directions = root.findViewById(R.id.img_directions);
        img_directions.setOnClickListener(view -> {
                    if (layout_search.getVisibility() == View.GONE)
                        layout_search.setVisibility(View.VISIBLE);
                    else layout_search.setVisibility(View.GONE);
                }
        );

        img_fullscreen = root.findViewById(R.id.img_fullscreen);
        img_fullscreen.setOnClickListener(view -> {
            isFullscreen = isFullscreen ? false : true;
            if (isFullscreen) {
                getActivity().findViewById(R.id.left_side).setVisibility(View.GONE);
                getActivity().findViewById(R.id.right_side).setVisibility(View.GONE);
                img_fullscreen.setImageResource(R.drawable.ic_fullscreen_exit_black_68dp);
            } else {
                getActivity().findViewById(R.id.left_side).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.right_side).setVisibility(View.VISIBLE);
                img_fullscreen.setImageResource(R.drawable.ic_fullscreen_black_68dp);
            }
        });

        img_compass = root.findViewById(R.id.img_compass);
        img_compass.setOnClickListener(view -> {
            isCamLock = true;
            zoom = 20;
            np_Zoom.setValue((int) zoom);
            msg("Camera Locked:" + isCamLock);
        });
        img_compass.setOnLongClickListener(view -> {
            isSatellite = isSatellite ? false : true;
            if (isSatellite) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            else map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        });


        txt_Zoom = root.findViewById(R.id.txt_zoom);


        //endregion
        LOC.addObserver(this);

        return root;
    }

    private void searchAutoComplete(JSONObject response) {
        try {
            JSONArray searchResults = response.getJSONArray("features");
            String[] searchList = new String[searchResults.length()];
            LatLng[] searchCoords = new LatLng[searchResults.length()];
            for (int i = 0; i < searchResults.length(); i++) {
                JSONObject jo = searchResults.getJSONObject(i);
                searchList[i] = jo.getJSONObject("properties").getString("label");
                JSONArray jsonArray = jo.getJSONObject("geometry").getJSONArray("coordinates");
                searchCoords[i] = new LatLng(jsonArray.getDouble(1), jsonArray.getDouble(0));
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, searchList);

            lv_searchResults.setAdapter(adapter);
            lv_searchResults.setOnItemClickListener((adapterView, view, i, l) -> {
                isCamLock = false;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(searchCoords[i])
                        .zoom(speedZoom)
                        .bearing(bearing)
                        .tilt(speedTilt)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Tools.CAMERAUPDATE, null);
                map.setPadding(0, 0, 0, 0);
                input_search.setText(searchList[i]);
            });
            lv_searchResults.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //LatLng target = searchList.
                    routing(searchCoords[i]);
                    return true;
                }
            });
            //lv_searchResults.notify();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void routing(LatLng target) {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car" +
                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62" +
                "&start=" + LOC.getNow().getLongitude() + ",%20" + LOC.getNow().getLatitude() +
                "&end=" + target.longitude + ",%20" + target.latitude;

        //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    if (route != null) route.removeLayerFromMap();
                    route = new GeoJsonLayer(map, response);

                    PolylineOptions rectLine = new PolylineOptions().width(10).color(ACT.getResources().getColor(R.color.colorRoute, null));

                    GeoJsonLineStringStyle lineStringStyle = route.getDefaultLineStringStyle();
                    lineStringStyle.setColor(R.color.colorRoute);

                    for (GeoJsonFeature feature : route.getFeatures()) {
                        GeoJsonLineStringStyle gs = feature.getLineStringStyle();
                        gs.setColor(R.color.colorRoute);
                        feature.setLineStringStyle(gs);
                        Log.d(TAG, "routing: " + feature);
                    }

//                    GeoJsonLineStringStyle asdf =
//                    GeoJsonFeature gjfeat = new GeoJsonFeature( )
//                    route.addFeature( );
                    //route.getDefaultLineStringStyle().setColor(R.color.colorRoute);
                    //route.getDefaultPointStyle().setVisible(true);
                    route.addLayerToMap();

                    geoLocationReversed(target);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(target)
                            .zoom(speedZoom)
                            .bearing(bearing)
                            .tilt(speedTilt)
                            .build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Tools.CAMERAUPDATE, null);
                    map.setPadding(0, 0, 0, 0);
                    //map.setLatLngBoundsForCameraTarget(route.getBoundingBox());

                    layout_search.setVisibility(View.GONE);
                    fullscreen();
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

    private void geoLocationReversed(LatLng latLng) {
        String url = "https://api.openrouteservice.org/geocode/reverse" +
                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62" +
                "&point.lat=" + latLng.latitude +
                "&point.lon=" + latLng.longitude +
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
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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

        map.setOnMapClickListener(latLng -> {
            geoLocationReversed(latLng);
            isCamLock = false;
        });

        map.setOnMapLongClickListener(this::routing);

        new Timer("Cam").schedule(new TimerTask() {
            @Override
            public void run() {
                updateCamera();
            }
        }, 0, TIMERUPDATE);
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
            img_compass.setRotation(bearing);
//            txt_Zoom.setText("s" + (int) speed + " /z-sz " + (int) zoom + "/" + (int) speedZoom
//                    + " t/st " + (int) tilt + "/" + (int) speedTilt);

            // 10 kmt (18 - 3 /6 = 16.5 * (1 + location.getSpeed()/100))
            // 110 kmt (18 - 30 /6 = 12 * (1 + location.getSpeed()/100)

            if (isCamLock) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(pos)
                        .zoom(speedZoom)
                        .bearing(bearing)
                        .tilt(speedTilt)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Tools.CAMERAUPDATE, null);
                map.setPadding(0, 350, 0, 0);
            } else {
                map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                map.setPadding(0, 0, 0, 0);
            }

        });
    }
}