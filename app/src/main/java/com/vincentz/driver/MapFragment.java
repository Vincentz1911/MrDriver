package com.vincentz.driver;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
    private Marker marker = null;
    private LatLng markerLoc;
    private Thread markerThread;

    private int counter = 0;
    private String street;
    private boolean isTraffic = false, isHybrid = true, isFullscreen = false, isCamLock = true;
    private float speed = 0, bearing = 0, tilt = 4, zoom = 20, speedZoom, speedTilt;
    private TextView txt_Speed, txt_Bearing, txt_Street, txt_Zoom, txt_Tilt;
    private ImageView img_compass, img_fullscreen, img_directions;
    private NumberPicker np_Tilt, np_Zoom;
    private LinearLayout searchLayout;
    private EditText searchInput;
    private ListView searchListView;
    private boolean hasRunBefore;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View root = li.inflate(R.layout.fragment_map, vg, false);
        Objects.requireNonNull((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(this);

        searchLayout = root.findViewById(R.id.layout_search);
        searchListView = root.findViewById(R.id.listview_locations);
        searchInput = root.findViewById(R.id.input_search);
        txt_Zoom = root.findViewById(R.id.txt_zoom);
        txt_Tilt = root.findViewById(R.id.txt_tilt);
        txt_Bearing = root.findViewById(R.id.txt_bearing1);
        txt_Street = root.findViewById(R.id.txt_street);
        txt_Speed = root.findViewById(R.id.txt_speed);
        img_directions = root.findViewById(R.id.img_directions);
        img_fullscreen = root.findViewById(R.id.img_fullscreen);
        img_compass = root.findViewById(R.id.img_compass);

        np_Tilt = root.findViewById(R.id.np_tilt);
        np_Tilt.setMinValue(0);
        np_Tilt.setMaxValue(7);
        np_Tilt.setValue((int) tilt);
        np_Zoom = root.findViewById(R.id.np_zoom);
        np_Zoom.setMinValue(10);
        np_Zoom.setMaxValue(25);
        np_Zoom.setValue((int) zoom);
        //endregion
        LOC.addObserver(this);
        markerThread = new Thread() {
            public void run() {
                moveMarker();
            }
        };
        return root;
    }

    @Override
    public void update(Observable locModel, Object loc) {
        location = ((LocationModel) locModel).getNow();
        lastLocation = ((LocationModel) locModel).getLast();
        if (location.hasBearing()) bearing = location.getBearing();
        if (location.hasSpeed()) speed = location.getSpeed();
        counter = 0;

        if (map != null && location != null && !hasRunBefore) {
            hasRunBefore = true;
            markerThread.start();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (isHybrid) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(ACT, R.raw.mapstyle_night));
        //map.setMapStyle(new MapStyleOptions(getString(R.string.bluemapday)));
        map.setTrafficEnabled(isTraffic);
        initOnClick();

//        new Timer("Cam").schedule(new TimerTask() {
//            @Override
//            public void run() {
//                updateCamera();
//            }
//        }, 0, 100);
    }

    private void initOnClick() {
        searchInput.addTextChangedListener(requestAutoComplete());

        txt_Speed.setOnClickListener(view -> geoLocationReversed(
                new LatLng(location.getLatitude(), location.getLongitude())));
        txt_Speed.setOnLongClickListener(view -> {
            isTraffic = !isTraffic;
            map.setTrafficEnabled(isTraffic);
            msg("Show Traffic: " + isTraffic);
            return true;
        });

        img_directions.setOnClickListener(view -> {
                    if (searchLayout.getVisibility() == View.GONE)
                        searchLayout.setVisibility(View.VISIBLE);
                    else searchLayout.setVisibility(View.GONE);
                    fullscreen();
                }
        );

        img_fullscreen.setOnClickListener(view -> {
            isFullscreen = !isFullscreen;
            if (isFullscreen) {
                ACT.findViewById(R.id.left_side).setVisibility(View.GONE);
                ACT.findViewById(R.id.right_side).setVisibility(View.GONE);
                img_fullscreen.setImageResource(R.drawable.ic_fullscreen_exit_black_68dp);
            } else {
                ACT.findViewById(R.id.left_side).setVisibility(View.VISIBLE);
                ACT.findViewById(R.id.right_side).setVisibility(View.VISIBLE);
                img_fullscreen.setImageResource(R.drawable.ic_fullscreen_black_68dp);
            }
        });

        img_compass.setOnLongClickListener(view -> {
            isHybrid = !isHybrid;
            if (isHybrid) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            else map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        });

        img_compass.setOnClickListener(view -> {
            isCamLock = !isCamLock;
            if (isCamLock) {
                zoom = 18;
                np_Zoom.setValue((int) zoom);
                tilt = 5;
                np_Tilt.setValue((int) tilt);
            } else {
                bearing = 0;
                zoom = (int) speedZoom;
                np_Zoom.setValue((int) zoom);
                tilt = 0;
                np_Tilt.setValue((int) tilt);
            }

            msg("Camera Locked:" + isCamLock);
        });

        map.setOnMapClickListener(latLng -> {
            geoLocationReversed(latLng);
            isCamLock = false;
        });

        map.setOnMapLongClickListener(this::routing);
    }

    private void moveMarker() {
        while (!markerThread.isInterrupted()) {
            int timeBetween = (int) (location.getElapsedRealtimeNanos()
                    - lastLocation.getElapsedRealtimeNanos()) / 1000000;
            if (timeBetween > 100) {
                if (counter == 1) updateCamera(timeBetween);
                // Log.d(TAG, "moveMarker: " + counter);
                markerLoc = new LatLng(
                        location.getLatitude() + (location.getLatitude()
                                - lastLocation.getLatitude()) * counter / 10,
                        location.getLongitude() + (location.getLongitude()
                                - lastLocation.getLongitude()) * counter / 10);

                ACT.runOnUiThread(() -> {
                    if (marker == null) marker = map.addMarker(new MarkerOptions()
                            .icon(SVG2Bitmap(R.drawable.ic_navigation_black_24dp))
                            .anchor(0.5f, 1f).rotation(bearing).flat(true)
                            .position(new LatLng(location.getLatitude(), location.getLongitude())));
                    marker.setPosition(markerLoc);
                    marker.setRotation(bearing);
                });
                try {
                    Thread.sleep(timeBetween / 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
            }
        }
    }

    private void updateCamera(int camTime) {
        //pos = new LatLng(location.getLatitude(), location.getLongitude());
        zoom = np_Zoom.getValue();
        tilt = np_Tilt.getValue() * 10;

        ACT.runOnUiThread(() -> {

            if (isCamLock) {
                speedTilt = (tilt + speed / 2 < 70) ? tilt + speed / 2 : 70;
                speedZoom = (zoom - speed / 10 < 20) ? zoom - speed / 10 : 20;

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                        .zoom(speedZoom)
                        .bearing(bearing)
                        .tilt(speedTilt)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), camTime, null);
                map.setPadding(0, 350, 0, 0);
            } else {
                speedTilt = tilt;
                speedZoom = zoom;

                map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                map.setPadding(0, 0, 0, 0);
            }

            txt_Tilt.setText(String.valueOf((int) speedTilt));
            txt_Zoom.setText(String.valueOf((int) speedZoom));
            txt_Street.setText(street);
            txt_Speed.setText(getString(R.string.mapspeed, (int) (speed * 3.6)));
            txt_Bearing.setText(getString(R.string.bearing, Tools.getDirection(bearing), (int) bearing));
            img_compass.setRotation(bearing);

        });
    }

    private TextWatcher requestAutoComplete() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() < 2) return;

                String url = "https://api.openrouteservice.org/geocode/search" +
                        "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62"
                        + "&text=" + editable
                        + "&focus.point.lon" + LOC.getNow().getLongitude()
                        + "&focus.point.lat" + LOC.getNow().getLatitude()
                        + "&boundary.circle.lon=" + LOC.getNow().getLongitude()
                        + "&boundary.circle.lat=" + LOC.getNow().getLatitude()
                        + "&boundary.circle.radius=500&boundary.country=DK"
                        //+ "&layers=address"
                        ;
                url = url.replaceAll("[ ]", "%20");
                url = url.replaceAll("[,]", "");

                //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
                RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                        MapFragment.this::searchAutoComplete,
                        error -> msg(ACT, "Volley AutoComplete Error")));
            }
        };
    }

    private void searchAutoComplete(JSONObject response) {
        try {
            JSONArray searchResults = response.getJSONArray("features");
            String[] searchList = new String[searchResults.length()];
            String[] streetname = new String[searchResults.length()];
            LatLng[] searchCoords = new LatLng[searchResults.length()];
            for (int i = 0; i < searchResults.length(); i++) {
                JSONObject jo = searchResults.getJSONObject(i);
                searchList[i] = jo.getJSONObject("properties").getString("label");
                streetname[i] = jo.getJSONObject("properties").getString("name");
                JSONArray jsonArray = jo.getJSONObject("geometry").getJSONArray("coordinates");
                searchCoords[i] = new LatLng(jsonArray.getDouble(1), jsonArray.getDouble(0));
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(ACT,
                    android.R.layout.simple_list_item_1, searchList);

            searchListView.setAdapter(adapter);
            searchListView.setOnItemClickListener((adapterView, view, i, l) -> {
                isCamLock = false;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(searchCoords[i])
                        .zoom(18)
                        .bearing(0)
                        .tilt(0)
                        .build();
                map.setPadding(0, 0, 0, 0);
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);

                searchInput.setText(streetname[i]);

                searchInput.setOnEditorActionListener((textView, i1, keyEvent) -> {
                    if (i1 == EditorInfo.IME_ACTION_DONE) {
                        searchLayout.setVisibility(View.GONE);
                        routing(searchCoords[i]);
                        return true;
                    }
                    return false;
                });
            });

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

//                    try {
//                        JSONObject prop = response.getJSONObject("properties");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }

                    //String newString =  response.toString().replace("Case #", "Ticket #");
                    //response.put()
                    route = new GeoJsonLayer(map, response);

                    GeoJsonLineStringStyle lineStringStyle = route.getDefaultLineStringStyle();
                    lineStringStyle.setColor(Color.BLUE);
                    lineStringStyle.setWidth(10);

                    route.addLayerToMap();

                    geoLocationReversed(target);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(target)
                            .zoom(speedZoom)
                            .bearing(bearing)
                            .tilt(speedTilt)
                            .build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
                    map.setPadding(0, 0, 0, 0);
                    //map.setLatLngBoundsForCameraTarget(route.getBoundingBox());

                    searchLayout.setVisibility(View.GONE);
                    fullscreen();
                },
                error -> msg(ACT, "Volley Routing Error")));
    }

    private void geoLocationReversed(LatLng latLng) {
        String url = "https://api.openrouteservice.org/geocode/reverse" +
                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62" +
                "&point.lat=" + latLng.latitude +
                "&point.lon=" + latLng.longitude +
                "&size=1&layers=address";

        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                street = response.getJSONArray("features").getJSONObject(0)
                        .getJSONObject("properties").getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> msg(ACT, "Volley Location Error")));
    }

    private BitmapDescriptor SVG2Bitmap(int vectorResId) {
        Drawable icon = ACT.getDrawable(vectorResId);
        if (icon == null) return null;
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                icon.getIntrinsicWidth(),
                icon.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        icon.draw(new Canvas(bitmap));
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}