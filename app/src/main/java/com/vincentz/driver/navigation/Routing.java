package com.vincentz.driver.navigation;

import android.app.Activity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;
import com.vincentz.driver.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.vincentz.driver.Tools.*;

import static com.vincentz.driver.navigation.MapFragment.map;
import static com.vincentz.driver.navigation.MapFragment.route;

public class Routing {

    Activity activity;

    public Routing(Activity activity) {
        this.activity = activity;
    }

    void routing(LatLng target) {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car" +
                "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62" +
                "&start=" + LOC.now().getLongitude() + ",%20" + LOC.now().getLatitude() +
                "&end=" + target.longitude + ",%20" + target.latitude;

        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    geoLocationReversed(target);
                    handleRouteResponse(target, response);
                }, error -> msg(activity,"Volley Routing Error")));
    }

    private void handleRouteResponse(LatLng target, JSONObject response) {
        if (route != null) route.removeLayerFromMap();
        try {
            parseJSONRoute(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        route = new GeoJsonLayer(map, response);
        GeoJsonLineStringStyle lineStringStyle = route.getDefaultLineStringStyle();
        lineStringStyle.setColor(activity.getResources().getColor(R.color.colorRouteDay, activity.getTheme()));
        lineStringStyle.setWidth(12);
        route.addLayerToMap();

        hideKeyboard(activity);
        fullscreen(activity);

        EditText Searchbox = activity.findViewById(R.id.searchbox);

        Searchbox.setVisibility(View.GONE);
        MapFragment.isCamLock = false;
        map.setPadding(50, 50, 50, 200);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(route.getBoundingBox(), 0));
        String[] arr = getDistanceAndTime();
        msg(activity, "Distance: " + arr[0] + " Time: " + arr[1]);
    }

    private void parseJSONRoute(JSONObject response) throws JSONException {
        JSONObject features = response.getJSONArray("features").getJSONObject(0);
        JSONObject geometry = features.getJSONObject("geometry");
        JSONObject properties = features.getJSONObject("properties");

        JSONArray coordinates = geometry.getJSONArray("coordinates");
        List<List<Float>> coordinatesList = new Gson().fromJson(String.valueOf(coordinates),
                new TypeToken<List<List<Float>>>() {
                }.getType());

        NAV = new NavigationModel();
        for (List<Float> coordinate: coordinatesList) {
            NAV.latLngList.add(new LatLng(coordinate.get(1), coordinate.get(0)));
        }

        JSONArray segments = properties.getJSONArray("segments");
        JSONArray steps = segments.getJSONObject(0).getJSONArray("steps");
        NAV.stepsList = new Gson().fromJson(String.valueOf(steps), new TypeToken<ArrayList<StepsModel>>() {}.getType());

        //ADDS STEPS TO LISTVIEW WITH NEW ADAPTER
        StepsListAdapter stepsListAdapter = new StepsListAdapter(activity, NAV.stepsList);
        ListView LocationsListView = activity.findViewById(R.id.listview_navigation);
        LocationsListView.setAdapter(stepsListAdapter);
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
                    array[0] = (d < 1000) ? d + "m" : ((int) (d / 100)) / 10f + "km";
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
                DEST = JSONsearchToModel(response).get(0);
                TextView txt_Destination = activity.findViewById(R.id.txt_destination);
                txt_Destination.setText(DEST.name);
            }
        }, error -> msg(activity,"Volley GeoLocation Error")
        ));
    }

    //PUTS JSON RESPONSE INTO LOCATIONMODEL ARRAYLIST
    ArrayList<LocationModel> JSONsearchToModel(JSONObject response) {
        ArrayList<LocationModel> list = new ArrayList<>();
        try {
            JSONArray searchResults = response.getJSONArray("features");
            if (searchResults.length() == 0) return null;
            for (int i = 0; i < searchResults.length(); i++) {
                JSONObject prop = searchResults.getJSONObject(i).getJSONObject("properties");
                JSONArray geom = searchResults.getJSONObject(i).getJSONObject("geometry").
                        getJSONArray("coordinates");
                String area;

                if (prop.has("neighbourhood") && prop.getString("neighbourhood") != null)
                    area = prop.getString("neighbourhood");
                else if (prop.has("localadmin") && prop.getString("localadmin") != null)
                    area = prop.getString("localadmin");
                else if (prop.has("county") && prop.getString("county") != null)
                    area = prop.getString("county");
                else area = prop.getString("country");
//                String area = (prop.has("neighbourhood") && prop.getString("neighbourhood") != null)
//                        ? prop.getString("neighbourhood") : prop.getString("localadmin");



                String street = (prop.has("street")
                        ? street = prop.getString("street") : prop.getString("region"));

                list.add(new LocationModel(
                        1,
                        prop.getString("name"),
                        street,
                        area,
                        new LatLng(geom.getDouble(1), geom.getDouble(0)),
                        //new LatLng(geom.getDouble(1), geom.getDouble(0)),
                        ((int) (prop.getDouble("distance") * 10)) / 10f,
                        0));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

}
