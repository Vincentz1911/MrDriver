package com.vincentz.driver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.MapFragment.map;
import static com.vincentz.driver.MapFragment.route;
import static com.vincentz.driver.Tools.*;

public class NavigationFragment extends Fragment {

    ListView LocationsListView;
    EditText Searchbox;
    TextView txt_Destination;
    LocationModel destination;
    NavigationListAdapter arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_navigation, vg, false);
        init(view);
        return view;
    }

    private void init(View view) {

        ImageView searchButton = view.findViewById(R.id.search_destination);
        ImageView savedLocButton = view.findViewById(R.id.saved_destinations);
        ImageView routeButton = view.findViewById(R.id.route_to_destination);
        Searchbox = getActivity().findViewById(R.id.searchbox);
        LocationsListView = view.findViewById(R.id.listview_locations);
        txt_Destination = getActivity().findViewById(R.id.txt_destination);

        LocationsListView.setVisibility(View.VISIBLE);
        fillSearchListView(loadLocations());

        savedLocButton.setOnClickListener(v -> {
            LocationsListView.setVisibility(View.VISIBLE);
            fillSearchListView(loadLocations());
        });

        savedLocButton.setOnLongClickListener(v -> {
            LocationsListView.setVisibility(View.VISIBLE);
            getLocationsFromAPI(1);
            return true;
        });

        searchButton.setOnClickListener(v -> {
            if (Searchbox.getVisibility() == View.GONE) {
                Searchbox.setVisibility(View.VISIBLE);
                LocationsListView.setVisibility(View.VISIBLE);
                // fillSearchListView(loadLocations());
            } else {
                hideKeyboard();
                Searchbox.setVisibility(View.GONE);
                //LocationsListView.setVisibility(View.GONE);
            }
        });

        searchButton.setOnLongClickListener(v -> {
            Searchbox.setVisibility(View.VISIBLE);
            Searchbox.setText("");
            return true;
        });

        Searchbox.addTextChangedListener(requestAutoComplete());
        Searchbox.setOnClickListener(v -> {
            if (!Searchbox.isFocused()) Searchbox.setSelection(Searchbox.getText().length());
        });

        routeButton.setOnClickListener(v -> {
            if (destination != null) routing(destination.latLng);
        });


//        txt_Destination.setOnClickListener(v -> routing(destination.latLng));
//        txt_Destination.setOnLongClickListener(v -> {
//            saveLocation(destination);
//            return true;
//        });


    }


    private void onClickListViewItem(LocationModel location) {
        destination = location;
        txt_Destination.setText(destination.name);
        MapFragment.isCamLock = false;
        map.setPadding(0, 0, 0, 0);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(destination.latLng).zoom(17).bearing(0).tilt(0).build()));

        if (!location.saved) {
            Searchbox.setText(destination.name + " ");
            Searchbox.setSelection(Searchbox.getText().length());
            Searchbox.setOnEditorActionListener((textView, i1, keyEvent) -> {
                if (i1 == EditorInfo.IME_ACTION_DONE) {
//                    Searchbox.setVisibility(View.GONE);
//                    // LocationsListView.setVisibility(View.GONE);
//                    routing(destination.latLng);
                    return true;
                }
                return false;
            });

        }
        hideKeyboard();
    }

    private TextWatcher requestAutoComplete() {
        Timer timer = new Timer("Timer");
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //ONLY START AUTOFILL WITH 3 LETTERS. REQUESTS AFTER 1 SECOND DELAY
                if (editable.length() < 3) return;
                timer.purge();
                timer.schedule(new TimerTask() {
                    public void run() {
                        searchRequest(editable);
                    }
                }, 500L);
            }
        };
    }

    void getLocationsFromAPI(int id) {
        String url = "http://vincentz.tk/api/location/" + id;
        RQ.add(new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Type type = new TypeToken<ArrayList<LocationModel>>(){}.getType();
                    ArrayList<LocationModel> LocationList = new Gson().fromJson(response.toString(), type);
                    fillSearchListView(LocationList);
                    arrayAdapter.notifyDataSetChanged();
                },
                error -> msg("Volley AutoComplete Error")));

    }

    private void searchRequest(Editable editable) {
        getActivity().runOnUiThread(() -> {
            //LatLng mapPos = map.getCameraPosition().target;
            String url = "https://api.openrouteservice.org/geocode/search" +
                    "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62"
                    + "&text=" + editable
                    + "&focus.point.lon=" + map.getCameraPosition().target.longitude
                    + "&focus.point.lat=" + map.getCameraPosition().target.latitude;
            url = url.replace(",", "").replace(" ", "+");

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
            RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> fillSearchListView(JSONsearchToModel(response)),
                    error -> msg("Volley AutoComplete Error")));
        });


//        Map<String, String> params = new HashMap<String, String>();
//        params.put("comment", "someOtherVal");
//        params.put("name", "someVal");


//        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
//                url, jsonObject,
//                new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.d("JSONPost", response.toString());
//                        //pDialog.hide();
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d("JSONPost", "Error: " + error.getMessage());
//                //pDialog.hide();
//            }
//        })


    }


    private void fillSearchListView(ArrayList<LocationModel> list) {
        if (list == null || list.size() == 0) return;
        arrayAdapter = new NavigationListAdapter(getContext(), list);
        LocationsListView.setAdapter(arrayAdapter);

        LocationsListView.setOnItemClickListener((adapterView, view, i, l) -> onClickListViewItem(list.get(i)));

        LocationsListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            if (list.get(i).saved) {
                deleteLocation(list, i);
                loadLocations();
                arrayAdapter.notifyDataSetChanged();
            } else saveLocation(list.get(i));
            return true;
        });

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
                    lineStringStyle.setColor(ACT.getResources().getColor(R.color.colorRouteDay, ACT.getTheme()));
                    lineStringStyle.setWidth(12);
                    route.addLayerToMap();
                    geoLocationReversed(target);
                    hideKeyboard();
                    fullscreen();
                    Searchbox.setVisibility(View.GONE);
                    //listView.setVisibility(View.GONE);
                    MapFragment.isCamLock = false;
                    map.setPadding(200, 200, 200, 200);
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
                destination = JSONsearchToModel(response).get(0);
                txt_Destination.setText(destination.name);
            }
        }, error -> msg("Volley GeoLocation Error")
        ));
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

                String street = (prop.has("street")
                        ? street = prop.getString("street") : prop.getString("region"));

                list.add(new LocationModel(
                        prop.getString("name"),
                        street,
                        area,
                        new LatLng(geom.getDouble(1), geom.getDouble(0)),
                        //new LatLng(geom.getDouble(1), geom.getDouble(0)),
                        ((int) (prop.getDouble("distance") * 10)) / 10f,
                        false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}