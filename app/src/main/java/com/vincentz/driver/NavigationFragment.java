package com.vincentz.driver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
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

import static com.vincentz.driver.Tools.*;

public class NavigationFragment extends Fragment {

    private ImageView SearchButton, SavedLocButton, RouteButton;
    ListView LocationsListView;
    EditText Searchbox;
    TextView txt_Destination;
    LocationModel destination;
    private GeoJsonLayer route;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_navigation, vg, false);
        init(view);
        return view;
    }

    private void init(View view) {

        SearchButton = view.findViewById(R.id.search_destination);
        SavedLocButton = view.findViewById(R.id.saved_destinations);
        RouteButton = view.findViewById(R.id.route_to_destination);
        Searchbox = getActivity().findViewById(R.id.searchbox);
        LocationsListView = view.findViewById(R.id.listview_locations);
        txt_Destination = getActivity().findViewById(R.id.txt_destination);

        LocationsListView.setVisibility(View.VISIBLE);
        fillSearchListView(loadLocations());

        SavedLocButton.setOnClickListener(v -> {
            LocationsListView.setVisibility(View.VISIBLE);
            fillSearchListView(loadLocations());
        });

        Searchbox.addTextChangedListener(requestAutoComplete());
        Searchbox.setOnClickListener(v2 -> {
            if (!Searchbox.isFocused()) Searchbox.setSelection(Searchbox.getText().length());
        });

        SearchButton.setOnClickListener(v -> {
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

        RouteButton.setOnClickListener(v -> {
            if (destination != null) routing(destination.latLng);
        });



//        txt_Destination.setOnClickListener(v -> routing(destination.latLng));
//        txt_Destination.setOnLongClickListener(v -> {
//            saveLocation(destination);
//            return true;
//        });


    }

    private void fillSearchListView(ArrayList<LocationModel> list) {
        NavigationListAdapter arrayAdapter = new NavigationListAdapter(getContext(), list);
        LocationsListView.setAdapter(arrayAdapter);

        LocationsListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            if (list.get(i).saved) {
                deleteLocation(list, i);
                loadLocations();
            } else saveLocation(list.get(i));
            return true;
        });

        LocationsListView.setOnItemClickListener((adapterView, view, i, l) -> onClickListViewItem(list.get(i)));
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

    private void onClickListViewItem(LocationModel location) {
        destination = location;

        txt_Destination.setText(destination.name);
//        isCamLock = false;
//        map.setPadding(0, 0, 0, 0);
//        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
//                .target(destination.latLng).zoom(17).bearing(0).tilt(0).build()));

        Searchbox.setText(destination.name + " ");
        Searchbox.setSelection(Searchbox.getText().length());
        Searchbox.setOnEditorActionListener((textView, i1, keyEvent) -> {
            if (i1 == EditorInfo.IME_ACTION_DONE) {
                Searchbox.setVisibility(View.GONE);
               // LocationsListView.setVisibility(View.GONE);
                routing(destination.latLng);
                return true;
            }
            return false;
        });
        hideKeyboard();
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
                    //route = new GeoJsonLayer(map, response);
                    GeoJsonLineStringStyle lineStringStyle = route.getDefaultLineStringStyle();
                    lineStringStyle.setColor(ACT.getResources().getColor(R.color.colorRouteDay, ACT.getTheme()));
                    lineStringStyle.setWidth(10);
                    route.addLayerToMap();
                    geoLocationReversed(target);
                    hideKeyboard();
                    fullscreen();
                    Searchbox.setVisibility(View.GONE);
                    //listView.setVisibility(View.GONE);
//                    isCamLock = false;
//                    map.setPadding(50, 100, 50, 50);
//                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(route.getBoundingBox(), 0));
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
                        prop.getString("name"),
                        prop.getString("street"),
                        area,
                        new LatLng(geom.getDouble(1), geom.getDouble(0)),
                        ((int) (prop.getDouble("distance") * 10)) / 10f,
                        false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}