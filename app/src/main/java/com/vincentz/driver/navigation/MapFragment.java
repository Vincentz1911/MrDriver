package com.vincentz.driver.navigation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import com.google.maps.android.PolyUtil;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.vincentz.driver.R;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.Tools.*;

public class MapFragment extends Fragment implements Observer, OnMapReadyCallback {
    //TODO http://www.overpass-api.de/api/xapi?*[maxspeed=*][bbox=12.4831598,55.682295,12.4842598,55.683395]

    private Activity act;
    private String TAG = "MAP";
    public static GoogleMap map;
    public static boolean isCamLock = true;
    public static GeoJsonLayer route;
    private Thread markerThread;
    private Timer camTimer;

    private int counter = 0, zoom = 18, tilt = 4;
    private boolean haveLocation, isTraffic, isHybrid;

    private TextView txt_Speed, txt_Bearing, txt_Compass, txt_Zoom, txt_Tilt, txt_Steps, txt_Destination;
    private ImageView img_Compass;
    private NumberPicker np_Tilt, np_Zoom;
    private View v_Tilt, v_Zoom, btn_Speed, btn_Compass;

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (markerThread != null) {
            markerThread.interrupt();
        }
        if (camTimer != null){
            camTimer.purge();
            camTimer.cancel();
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        if (getActivity() != null) act = getActivity();

        View view = li.inflate(R.layout.fragment_map, vg, false);
        ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        txt_Destination = view.findViewById(R.id.txt_destination);
        txt_Steps = view.findViewById(R.id.txt_step);
        txt_Zoom = view.findViewById(R.id.txt_zoom);
        txt_Tilt = view.findViewById(R.id.txt_tilt);
        txt_Bearing = view.findViewById(R.id.txt_bearing);
        txt_Compass = view.findViewById(R.id.txt_compass);
        btn_Compass = view.findViewById(R.id.btn_compass);
        txt_Speed = view.findViewById(R.id.txt_speed);
        btn_Speed = view.findViewById(R.id.btn_speed);
        img_Compass = view.findViewById(R.id.img_compass);

        v_Zoom = view.findViewById(R.id.v_zoom);
        v_Tilt = view.findViewById(R.id.v_tilt);
        np_Tilt = view.findViewById(R.id.np_tilt);
        np_Tilt.setMinValue(0);
        np_Tilt.setMaxValue(9);
        np_Tilt.setValue(tilt);
        np_Zoom = view.findViewById(R.id.np_zoom);
        np_Zoom.setMinValue(0);
        np_Zoom.setMaxValue(9);
        np_Zoom.setValue((zoom - 5) / 2);
        //endregion
        LOC.addObserver(this);
        return view;
    }

    //STARTING UPDATING MARKER AND MAPCAMERA WHEN GET FIRST LOCATION AND MAP IS READY
    @Override
    public void update(Observable locModel, Object loc) {
        counter = 0;
        if (!haveLocation && map != null && LOC.now() != null && LOC.last() != null) {
            haveLocation = true;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LOC.latlng(), 10));
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
        map.getUiSettings().setCompassEnabled(false);
        int mapstyle;

        switch (IO.getInt("Theme", 0)){
            case 0:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(act, R.raw.mapstyle_day));
                break;
            case 1:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(act, R.raw.mapstyle_night));
                break;
            case 2:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(act, R.raw.mapstyle_day));
                break;
            case 3:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(act, R.raw.mapstyle_night));
                break;
            default:
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(act, R.raw.mapstyle_day));
        }


        //if (getActivity() != null)
        //map.setMapStyle(MapStyleOptions.loadRawResourceStyle(act, mapstyle));
    }

    private void initOnClick() {
        np_Zoom.setOnValueChangedListener((numberPicker, oldVal, newVal) -> {
            if (!isCamLock) {
                map.animateCamera(CameraUpdateFactory.zoomTo(newVal * 2 + 5));
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

        //TODO
//        btn_Speed.setOnClickListener(view -> geoLocationReversed(LOC.latlng()));
        btn_Speed.setOnLongClickListener(view -> {
            isTraffic = !isTraffic;
            map.setTrafficEnabled(isTraffic);
            msg(getActivity(), "Show Traffic: " + isTraffic);
            return true;
        });

        //''' STREET TEXT BOTTOM '''
        //CLICK = ROUTING, LONGCLICK = SAVE LOCATION
//        txt_Destination.setOnClickListener(view -> routing(destination.latLng));
//        txt_Destination.setOnLongClickListener(view -> {
//            saveLocation(destination);
//            return true;
//        });

        //''' INPUT TEXT BAR '''
        //CLICK = SET CURSOR AT END, LONGCLICK = CLEAR TEXT, TEXT CHANGED = AUTOCOMPLETE
//        input.setOnClickListener(view -> {
//            if (!input.isFocused()) input.setSelection(input.getText().length());
//        });
//        input.setOnLongClickListener(view -> {
//            input.setText("");
//            fillSearchListView(loadLocations());
//            return false;
//        });
//        input.addTextChangedListener(requestAutoComplete());

        //''' SPEED SIGN '''
        //CLICK = GET ADRESS, LONGCLICK = TRAFFIC ON/OFF


        //''' DIRECTIONS SIGN '''
        //CLICK = SHOW/HIDE INPUT AND LISTVIEW, LONGCLICK = ROUTING
        //img_directions = getView().getRootView().findViewById(R.id.btn_navigation);


//        img_directions.setOnClickListener(view -> {
//            if (input.getVisibility() == View.GONE) {
//                input.setVisibility(View.VISIBLE);
//                listView.setVisibility(View.VISIBLE);
//                fillSearchListView(loadLocations());
//            } else {
//                hideKeyboard();
//                input.setVisibility(View.GONE);
//                listView.setVisibility(View.GONE);
//            }
//        });
//        img_directions.setOnLongClickListener(view -> {
//            if (destination != null) {
//                routing(destination.latLng);
//                return true;
//            }
//            return false;
//        });

        //''' COMPASS ICON '''
        //CLICK = , LONGCLICK = CHANGE BETWEEN HYBRID AND NORMAL MAP
        btn_Compass.setOnClickListener(view -> {
            if (!isCamLock) {
                isCamLock = true;
                np_Zoom.setValue(6);
                np_Tilt.setValue(6);
                msg(getActivity(), "Follow enabled");
                return;
                //                tilt = 0;
//                np_Tilt.setValue((int) tilt);
            }

            if (route != null) {
                isCamLock = false;
                map.setPadding(50, 50, 50, 100);
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(route.getBoundingBox(), 0));

                msg(getActivity(), "Showing Route");
            } else {
                isCamLock = false;
                zoom = 16;
                np_Zoom.setValue((zoom - 5) / 2);
                tilt = 0;
                np_Tilt.setValue(tilt);
                map.setPadding(0, 0, 0, 0);
                map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(LOC.latlng()).bearing(0)
                        .zoom(zoom)
                        .tilt(0)
                        .build()), 1000, null);
                msg(getActivity(), "Free Camera");
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
        map.setOnMapLongClickListener(target -> new Routing(getActivity()).routing(target));
    }


    String oldDirections;
    //TODO DELETE LOCATION
    //TODO interpolate() – Returns the latitude/longitude coordinates of a point that lies a given fraction of the distance between two given points. You can use this to animate a marker between two points, for example.
    private void moveMarker(Marker marker) {

        while (!markerThread.isInterrupted()) {
            if (LOC.now() == null || LOC.last() == null) continue;

            int timeBetween = (int) (LOC.now().getElapsedRealtimeNanos()
                    - LOC.last().getElapsedRealtimeNanos()) / 1000000;
            if (LOC.now().hasBearing() && LOC.now().hasSpeed() && timeBetween > 100) {

                if (route != null) checkIfOnRoute();

                LatLng markerLoc = new LatLng(
                        LOC.now().getLatitude() + (LOC.now().getLatitude()
                                - LOC.last().getLatitude()) * counter / 10,
                        LOC.now().getLongitude() + (LOC.now().getLongitude()
                                - LOC.last().getLongitude()) * counter / 10);

                act.runOnUiThread(() -> {
                    marker.setPosition(markerLoc);
                    marker.setRotation(LOC.bearing());
                    if (DEST == null) txt_Destination.setText("No Destination");
                    txt_Steps.setText(directions);
                    if (!directions.equals(oldDirections)) say(directions);
                    oldDirections = directions;
//                    if (isOnRoute) txt_Steps.setText(NAV.stepsList.get(step).instruction);
//                    else txt_Steps.setText("No Route");

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

    int step = 0;
    //boolean isOnRoute = false;
    Timer timer = new Timer("Timer");
    List<LatLng> stepCoords;
    String directions ="No Route";

    private void checkIfOnRoute() {
        stepCoords = NAV.latLngList.subList(
                NAV.stepsList.get(step).way_points[0], NAV.stepsList.get(step).way_points[1]);

        //IS LOCATION ON ROUTE THEN CANCEL TIMER
        if (PolyUtil.isLocationOnPath(LOC.latlng(), stepCoords, false, 10.0f)) {
            directions = NAV.stepsList.get(step).instruction;
            timer.cancel();
            timer = new Timer();

        } else {
            //CHECK IF NEXT STEP IS ON ROUTE
            if (step + 1 < NAV.stepsList.size()) {
                stepCoords = NAV.latLngList.subList(
                        NAV.stepsList.get(step + 1).way_points[0],
                        NAV.stepsList.get(step + 1).way_points[1]);
            }

            if (PolyUtil.isLocationOnPath(LOC.latlng(), stepCoords, false, 10.0f)) {
                Log.d(TAG, "NEXT STEP IS ON ROUTE");
                step++;
                timer.cancel();
                timer.purge();
                timer = new Timer();
            } else {
                //START TIMER TO RECALCULATE ROUTE.
                timer.cancel();
                timer.purge();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        Log.d(TAG, "CALCULATING NEW ROUTE");
                        directions = "Calculating new route";
                        //msg(getActivity(), "Calculating new route");
                        step = 0;
                        new Routing(getActivity()).routing(DEST.latLng);
                    }
                }, 3000L);
            }
        }
    }


    //TODO make zoom and tilt compatible with native gmap zoom and tilt
    private void updateCamera(int camTime) {
        act.runOnUiThread(() -> {
            zoom = np_Zoom.getValue() * 2 + 5;
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
            img_Compass.setRotation(-map.getCameraPosition().bearing);
        });
    }

    private BitmapDescriptor SVG2Bitmap(int vectorResId) {
        Drawable icon = ContextCompat.getDrawable(act, vectorResId);
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