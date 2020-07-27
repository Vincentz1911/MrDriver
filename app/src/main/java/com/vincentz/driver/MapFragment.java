package com.vincentz.driver;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.Tools.*;

public class MapFragment extends Fragment implements Observer, OnMapReadyCallback {
    //TODO http://www.overpass-api.de/api/xapi?*[maxspeed=*][bbox=12.4831598,55.682295,12.4842598,55.683395]
    private GoogleMap map;
    private GeoJsonLayer route;
    private LocationModel destination;
    private Thread markerThread;
    private Timer camTimer;

    private int counter = 0, zoom = 18, tilt = 4;
    private boolean haveLocation, isTraffic, isHybrid = true, isCamLock = true;

    private TextView txt_Speed, txt_Bearing, txt_Compass, txt_Zoom, txt_Tilt, txt_Destination;
    private ImageView img_Compass, img_directions;
    private NumberPicker np_Tilt, np_Zoom;
    private View v_Tilt, v_Zoom, btn_Speed, btn_Compass;
    private EditText input;
    private ListView listView;

    @Override
    public void onDestroy() {
        super.onDestroy();

        markerThread.interrupt();
        camTimer.purge();
        camTimer.cancel();
    }

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View root = li.inflate(R.layout.fragment_map, vg, false);
        Objects.requireNonNull((SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMapAsync(this);

        input = root.findViewById(R.id.input_search);
        input.setVisibility(View.GONE);
        listView = root.findViewById(R.id.listview_locations);
        listView.setVisibility(View.GONE);

        txt_Zoom = root.findViewById(R.id.txt_zoom);
        txt_Tilt = root.findViewById(R.id.txt_tilt);
        txt_Bearing = root.findViewById(R.id.txt_bearing);
        txt_Compass = root.findViewById(R.id.txt_compass);
        btn_Compass = root.findViewById(R.id.btn_compass);
        txt_Destination = root.findViewById(R.id.txt_destination);
        txt_Speed = root.findViewById(R.id.txt_speed);
        btn_Speed = root.findViewById(R.id.btn_speed);
        img_directions = root.findViewById(R.id.img_directions);
        img_Compass = root.findViewById(R.id.img_compass);

        v_Zoom = root.findViewById(R.id.v_zoom);
        v_Tilt = root.findViewById(R.id.v_tilt);
        np_Tilt = root.findViewById(R.id.np_tilt);
        np_Tilt.setMinValue(0);
        np_Tilt.setMaxValue(8);
        np_Tilt.setValue(tilt);
        np_Zoom = root.findViewById(R.id.np_zoom);
        np_Zoom.setMinValue(0);
        np_Zoom.setMaxValue(8);
        np_Zoom.setValue((zoom - 10) / 2);
        //endregion
        LOC.addObserver(this);
        return root;
    }

    //STARTING UPDATING MARKER AND MAPCAMERA WHEN GET FIRST LOCATION AND MAP IS READY
    @Override
    public void update(Observable locModel, Object loc) {
        counter = 0;
        if (!haveLocation && map != null && LOC.now() != null && LOC.last() != null) {
            haveLocation = true;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LOC.latlng(), 15));
            Marker marker = map.addMarker(new MarkerOptions()
                    .icon(SVG2Bitmap(R.drawable.mic_navigation_black_24dp))
                    .anchor(0.5f, 0.5f).rotation(LOC.bearing()).flat(true)
                    .position(LOC.latlng()));
            markerThread = new Thread() {
                public void run() {
                    moveMarker(marker);
                }
            };
            markerThread.start();
            camTimer = new Timer();
            camTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateCamera(400);
                }
            }, 0, 500);
            initOnClick();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    //TODO Set mapstyle depending on time of day
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (isHybrid) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setTrafficEnabled(isTraffic);
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(ACT, R.raw.mapstyle_day));
    }

    private void initOnClick() {
        np_Zoom.setOnValueChangedListener((numberPicker, oldVal, newVal) -> {
            if (!isCamLock) {
                map.animateCamera(CameraUpdateFactory.zoomTo(newVal * 2 + 10));
            }
        });

        np_Tilt.setOnValueChangedListener((numberPicker, oldVal, newVal) -> {
            if (!isCamLock) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(map.getCameraPosition().target)
                        .bearing(map.getCameraPosition().bearing)
                        .zoom(map.getCameraPosition().zoom)
                        .tilt(numberPicker.getValue() * 10)
                        .build()), 1000, null);
            }
        });

        //''' STREET TEXT BOTTOM '''
        //CLICK = ROUTING, LONGCLICK = SAVE LOCATION
        txt_Destination.setOnClickListener(view -> routing(destination.latLng));
        txt_Destination.setOnLongClickListener(view -> {
            saveLocation(destination);
            return true;
        });

        //''' INPUT TEXT BAR '''
        //CLICK = SET CURSOR AT END, LONGCLICK = CLEAR TEXT, TEXT CHANGED = AUTOCOMPLETE
        input.setOnClickListener(view -> {
            if (!input.isFocused()) input.setSelection(input.getText().length());
        });
        input.setOnLongClickListener(view -> {
            input.setText("");
            fillSearchListView(loadLocations());
            return false;
        });
        input.addTextChangedListener(requestAutoComplete());

        //''' SPEED SIGN '''
        //CLICK = GET ADRESS, LONGCLICK = TRAFFIC ON/OFF
        btn_Speed.setOnClickListener(view -> geoLocationReversed(LOC.latlng()));
        btn_Speed.setOnLongClickListener(view -> {
            isTraffic = !isTraffic;
            map.setTrafficEnabled(isTraffic);
            msg("Show Traffic: " + isTraffic);
            return true;
        });

        //''' DIRECTIONS SIGN '''
        //CLICK = SHOW/HIDE INPUT AND LISTVIEW, LONGCLICK = ROUTING
        img_directions.setOnClickListener(view -> {
            if (input.getVisibility() == View.GONE) {
                input.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);
                fillSearchListView(loadLocations());
            } else {
                hideKeyboard();
                input.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
            }
        });
        img_directions.setOnLongClickListener(view -> {
            if (destination != null) {
                routing(destination.latLng);
                return true;
            }
            return false;
        });

        //''' COMPASS ICON '''
        //CLICK = , LONGCLICK = CHANGE BETWEEN HYBRID AND NORMAL MAP
        btn_Compass.setOnClickListener(view -> {
            if (!isCamLock) {
                isCamLock = true;
                np_Zoom.setValue(4);
                np_Tilt.setValue(4);
                msg("Follow enabled");
                return;
                //                tilt = 0;
//                np_Tilt.setValue((int) tilt);
            }

            if (route != null) {
                isCamLock = false;
                map.setPadding(50, 50, 50, 50);
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(route.getBoundingBox(), 0));

                msg("Showing Route");
            } else {
                isCamLock = false;
                zoom = 16;
                np_Zoom.setValue((zoom - 10) / 2);
                tilt = 0;
                np_Tilt.setValue(tilt);
                map.setPadding(0, 0, 0, 0);
                map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(LOC.latlng()).bearing(0)
                        .zoom(zoom)
                        .tilt(0)
                        .build()), 1000, null);
                msg("Free Camera");
            }
        });
        btn_Compass.setOnLongClickListener(view -> {
            isHybrid = !isHybrid;
            if (isHybrid) map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            else map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        });

        //''' GOOGLE MAP '''
        //CLICK = CAMLOCK FALSE AND REV.GEOLOC,
        //LONGCLICK ROUTING TO POINT CLICKED
        map.setOnMapClickListener(latLng -> {
            //geoLocationReversed(latLng);
            isCamLock = false;
        });
        map.setOnMapLongClickListener(target -> routing(target));
    }

    //TODO DELETE LOCATION
    //TODO interpolate() – Returns the latitude/longitude coordinates of a point that lies a given fraction of the distance between two given points. You can use this to animate a marker between two points, for example.
    private void moveMarker(Marker marker) {
        while (!markerThread.isInterrupted()) {
            if (LOC.now() == null || LOC.last() == null) continue;

            int timeBetween = (int) (LOC.now().getElapsedRealtimeNanos()
                    - LOC.last().getElapsedRealtimeNanos()) / 1000000;
            if (LOC.now().hasBearing() && LOC.now().hasSpeed() && timeBetween > 100) {
                if (route != null) {
                    for (GeoJsonFeature feature : route.getFeatures()) {
//                        if (feature.getGeometry())
//                        boolean isOnRoute = PolyUtil.isLocationOnPath(
//                                location.get, feature.getGeometry(), false, 10.0f);
                        //Log.d(TAG, "moveMarker: " + feature);

//                        if (feature.hasGeometry()) Log.d(TAG, "G: " + feature.getGeometry());
//                        if (feature.hasProperty(""))
//
//                        feature.getGeometry().getType();
//                        if (feature.hasProperty(“Ocean”)) {
//                            String oceanProperty = feature.getProperty(“Ocean”);
                    }
                }

                LatLng markerLoc = new LatLng(
                        LOC.now().getLatitude() + (LOC.now().getLatitude()
                                - LOC.last().getLatitude()) * counter / 10,
                        LOC.now().getLongitude() + (LOC.now().getLongitude()
                                - LOC.last().getLongitude()) * counter / 10);

                ACT.runOnUiThread(() -> {
                    marker.setPosition(markerLoc);
                    marker.setRotation(LOC.bearing());
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

    //TODO make zoom and tilt compatible with native gmap zoom and tilt
    private void updateCamera(int camTime) {
        ACT.runOnUiThread(() -> {
            zoom = np_Zoom.getValue() * 2 + 10;
            tilt = np_Tilt.getValue() * 10;
            if (isCamLock) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(LOC.latlng()).bearing(LOC.bearing())
                        .zoom((zoom - LOC.speed() / 10 < 20) ? zoom - LOC.speed() / 10 : 20)
                        .tilt((tilt + LOC.speed() / 2 < 70) ? tilt + LOC.speed() / 2 : 70)
                        .build()), camTime, null);
                map.setPadding(0, 350 - tilt, 0, 0);
            }

            ViewGroup.LayoutParams params = v_Zoom.getLayoutParams();
            params.height = (int) map.getCameraPosition().zoom * np_Zoom.getHeight() / 20;
            v_Zoom.setLayoutParams(params);

            params = v_Tilt.getLayoutParams();
            params.height = (int) map.getCameraPosition().tilt * np_Tilt.getHeight() / 70;
            v_Tilt.setLayoutParams(params);

            txt_Tilt.setText(String.valueOf((int) map.getCameraPosition().tilt));
            txt_Zoom.setText(String.valueOf((int) map.getCameraPosition().zoom));
            txt_Speed.setText(String.valueOf((int) (LOC.speed() * 3.6)));
            txt_Bearing.setText((int) LOC.bearing() + "\u00B0");
            txt_Compass.setText(getCompassDirection(map.getCameraPosition().bearing));
            img_Compass.setRotation(map.getCameraPosition().bearing);
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

            //TODO SET UP URL FOR COUNTRY OR BETTER WAY TO GET LOCAL RESULTS
            @Override
            public void afterTextChanged(Editable editable) {
                //ONLY START AUTOFILL WITH 3 LETTERS. SETUP URL AND HTTP REQUEST SUGGESTIONS
                if (editable.length() < 3) return;
                String url = "https://api.openrouteservice.org/geocode/search" +
                        "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62"
                        + "&text=" + editable
                        + "&focus.point.lon=" + LOC.now().getLongitude()
                        + "&focus.point.lat=" + LOC.now().getLatitude()
                        + "&boundary.circle.lon=" + LOC.now().getLongitude()
                        + "&boundary.circle.lat=" + LOC.now().getLatitude()
                        + "&boundary.circle.radius=500&boundary.country=DK";
                url = url.replace(",", "");
                url = url.replace(" ", "+");

                //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
                RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                        response -> fillSearchListView(JSONsearchToModel(response)),
                        error -> msg("Volley AutoComplete Error")));
            }
        };
    }

    //PUTS JSON RESPONSE INTO LOCATIONMODEL ARRAYLIST
    private ArrayList<LocationModel> JSONsearchToModel(JSONObject response) {
        ArrayList<LocationModel> list = new ArrayList<>();
        try {
            JSONArray searchResults = response.getJSONArray("features");
            if (searchResults.length() == 0) return null;
            for (int i = 0; i < searchResults.length(); i++) {
                JSONObject prop = searchResults.getJSONObject(i).getJSONObject("properties");
                JSONArray geom = searchResults.getJSONObject(i).getJSONObject("geometry").
                        getJSONArray("coordinates");

                String area = (prop.has("neighbourhood") && prop.getString("neighbourhood") != null)
                        ? prop.getString("neighbourhood") : prop.getString("localadmin");

                list.add(new LocationModel(
                        area,
                        prop.getString("name"),
                        new LatLng(geom.getDouble(1), geom.getDouble(0)),
                        ((int) (prop.getDouble("distance") * 10)) / 10f,
                        false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void fillSearchListView(ArrayList<LocationModel> list) {
        ArrayList<String> nameList = new ArrayList<>();
        for (LocationModel lm : list) {
            if (lm.saved) nameList.add("S " + lm.name + ", " + lm.area);
            else nameList.add(lm.distance + "km " + lm.name + ", " + lm.area);
        }

        listView.setAdapter(new ArrayAdapter<>(ACT, R.layout.adapter_maps_listview, nameList));
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            if (list.get(i).saved) {
                deleteLocation(list, i);
                loadLocations();
            } else saveLocation(list.get(i));
            return true;
        });
        listView.setOnItemClickListener((adapterView, view, i, l) -> onClickListViewItem(list.get(i)));
    }

    private void onClickListViewItem(LocationModel location) {
        isCamLock = false;
        destination = location;
        txt_Destination.setText(destination.name);
        map.setPadding(0, 0, 0, 0);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(destination.latLng).zoom(17).bearing(0).tilt(0).build()));

        input.setText(destination.name + " ");
        input.setSelection(input.getText().length());
        input.setOnEditorActionListener((textView, i1, keyEvent) -> {
            if (i1 == EditorInfo.IME_ACTION_DONE) {
                input.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                routing(destination.latLng);
                return true;
            }
            return false;
        });
        hideKeyboard();
    }

    private void routing(LatLng target) {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car" +
                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62" +
                "&start=" + LOC.now().getLongitude() + ",%20" + LOC.now().getLatitude() +
                "&end=" + target.longitude + ",%20" + target.latitude;

        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    if (route != null) route.removeLayerFromMap();
                    route = new GeoJsonLayer(map, response);
                    GeoJsonLineStringStyle lineStringStyle = route.getDefaultLineStringStyle();
                    lineStringStyle.setColor(getResources().getColor(R.color.colorRoute, ACT.getTheme()));
                    lineStringStyle.setWidth(10);
                    route.addLayerToMap();
                    geoLocationReversed(target);
                    hideKeyboard();
                    fullscreen();
                    isCamLock = false;
                    input.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    map.setPadding(50, 100, 50, 50);
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(route.getBoundingBox(), 0));
                    String[] arr = getDistanceAndTime();
                    msg("Distance: " + arr[0] + " Time: " + arr[1]);
                }, error -> msg("Volley Routing Error")));
    }

    private String[] getDistanceAndTime() {
        String[] array = new String[2];
        for (GeoJsonFeature feature : route.getFeatures()) {
            if (feature.hasProperty("summary")) {
                HashMap<String, Float> hash =
                        new Gson().fromJson(feature.getProperty("summary"),
                                new TypeToken<HashMap<String, Float>>() {
                                }.getType());
                if (hash.get("distance") != null && hash.get("duration") != null) {
                    float d = hash.get("distance");
                    array[0] = (d < 1000) ? d + "m" : ((int) (d /100)) / 10f + "km";
                    array[1] = DateUtils.formatElapsedTime((hash.get("duration")).longValue());
                }
            }
        }
        return array;
    }

    private void geoLocationReversed(LatLng latLng) {
        String url = "https://api.openrouteservice.org/geocode/reverse" +
                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62" +
                "&point.lat=" + latLng.latitude + "&point.lon=" + latLng.longitude +
                "&size=1&layers=address";
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            if (JSONsearchToModel(response) != null) {
                destination = JSONsearchToModel(response).get(0);
                txt_Destination.setText(destination.name);
            }
        }, error -> msg("Volley GeoLocation Error")
        ));
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

    private ArrayList<LocationModel> loadLocations() {
        Type type = new TypeToken<ArrayList<LocationModel>>() {
        }.getType();
        ArrayList<LocationModel> list = new Gson()
                .fromJson(IO.getString("locations", null), type);
        if (list == null) list = new ArrayList<>();
        return list;
    }

    private void saveLocation(LocationModel location) {
        if (location == null || location.saved) return;
        ArrayList<LocationModel> list = loadLocations();
        location.saved = true;
        list.add(location);
        msg("Saved Location: " + location.name);
        IO.edit().putString("locations", new Gson().toJson(list)).apply();
    }

    private void deleteLocation(ArrayList<LocationModel> list, int i) {
//        ArrayList<LocationModel> list = loadLocations();
        msg("Deleted Location: " + list.get(i).name);
        list.remove(i);
        IO.edit().putString("locations", new Gson().toJson(list)).apply();
    }
}