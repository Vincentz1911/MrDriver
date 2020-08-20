package com.vincentz.driver.navigation;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vincentz.driver.R;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.navigation.MapFragment.map;
import static com.vincentz.driver.Tools.*;

public class NavigationFragment extends Fragment {

    ListView LocationsListView;
    EditText Searchbox;
    TextView txt_Destination;
    Routing routing;
    //LocationModel destination;
    NavigationListAdapter arrayAdapter;
    Activity act;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_navigation, vg, false);
        act = getActivity();
        //if (getActivity() != null) act = act;
        routing = new Routing(act);
        init(view);
        return view;
    }

    private void init(View view) {
        ImageView searchButton = view.findViewById(R.id.search_destination);
        ImageView savedLocButton = view.findViewById(R.id.saved_destinations);
        ImageView routeButton = view.findViewById(R.id.route_to_destination);
        Searchbox = act.findViewById(R.id.searchbox);
        LocationsListView = view.findViewById(R.id.listview_navigation);
        txt_Destination = act.findViewById(R.id.txt_destination);

        //fillSearchListView(loadLocations());
        getLocationsFromAPI(1);

        savedLocButton.setOnClickListener(v -> {
            hideKeyboard(act);
            Searchbox.setVisibility(View.GONE);
            fillSearchListView(loadLocations());
        });

        savedLocButton.setOnLongClickListener(v -> {
            hideKeyboard(act);
            Searchbox.setVisibility(View.GONE);
            getLocationsFromAPI(1);
            return true;
        });

        searchButton.setOnClickListener(v -> {
            if (Searchbox.getVisibility() == View.GONE) {
                Searchbox.setVisibility(View.VISIBLE);
                arrayAdapter.clear();
                arrayAdapter.notifyDataSetChanged();
            } else {
                hideKeyboard(act);
                Searchbox.setVisibility(View.GONE);
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
            if (DEST != null) routing.routing(DEST.latLng);
        });
    }

    private void onClickListViewItem(LocationModel location) {
        DEST = location;
        txt_Destination.setText(DEST.name);
        Searchbox.setText(DEST.name);
        MapFragment.isCamLock = false;
        map.setPadding(0, 0, 0, 0);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(DEST.latLng).zoom(17).bearing(0).tilt(0).build()));

        if (location.stored != 0) {
            Searchbox.setText(DEST.name + " ");
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
        hideKeyboard(act);
    }

    TimerTask task;
    private TextWatcher requestAutoComplete() {
        Timer timer = new Timer("Timer");

        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                //ONLY START AUTOFILL WITH 3 LETTERS. REQUESTS AFTER 1 SECOND DELAY
                if (editable.length() < 3) return;
                if (task != null) task.cancel();

                task = new TimerTask() {public void run() { searchRequest(editable); }};
                timer.schedule(task, 1000L);
           }
        };
    }

    void getLocationsFromAPI(int id) {
        String url = "http://vincentz.tk/api/location/" + id;
        RQ.add(new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Type type = new TypeToken<ArrayList<LocationModel>>() {
                    }.getType();
                    ArrayList<LocationModel> LocationList = new Gson().fromJson(response.toString(), type);
                    fillSearchListView(LocationList);
                },
                error -> msg(act,"Volley AutoComplete Error")));
    }

    private void searchRequest(Editable editable) {
        act.runOnUiThread(() -> {
            //LatLng mapPos = map.getCameraPosition().target;
            String url = "https://api.openrouteservice.org/geocode/search" +
                    "?api_key=5b3ce3597851110001cf6248acf21fffcf174a02b63b9c6dde867c62"
                    + "&focus.point.lon=" + map.getCameraPosition().target.longitude
                    + "&focus.point.lat=" + map.getCameraPosition().target.latitude
                    + "&text=" + editable;
            url = url.replace(",", "").replace(" ", "+");

//            JSONObject jsonObject = new JSONObject();
//            try { jsonObject.put("", ""); }
//            catch (JSONException e) { e.printStackTrace(); }

            //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
            RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> fillSearchListView(routing.JSONsearchToModel(response)),
                    error -> msg(act,"Volley AutoComplete Error")));
        });
    }

    private void fillSearchListView(ArrayList<LocationModel> list) {
        if (list == null || list.size() == 0) return;
        if (arrayAdapter != null) {
            arrayAdapter.clear();
            arrayAdapter.notifyDataSetChanged();
        }

        arrayAdapter = new NavigationListAdapter(act, list);
        LocationsListView.setVisibility(View.VISIBLE);
        LocationsListView.setAdapter(arrayAdapter);
        LocationsListView.setOnItemClickListener((adapterView, view, i, l) -> onClickListViewItem(list.get(i)));
        LocationsListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            if (list.get(i).stored != 0) {
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
        ArrayList<LocationModel> list =
                new Gson().fromJson(IO.getString("locations", null), type);
        if (list == null) list = new ArrayList<>();
        return list;
    }

    private void saveLocation(LocationModel location) {
        if (location == null || location.stored != 0) return;
        ArrayList<LocationModel> list = loadLocations();
        location.stored = 1;
        list.add(location);
        msg(act,"Saved Location: " + location.name);
        IO.edit().putString("locations", new Gson().toJson(list)).apply();

        uploadLocation(location);
    }

    private void uploadLocation(LocationModel location) {
        String URL = "http://vincentz.tk/api/location";
        final String requestBody = new Gson().toJson(location);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> Log.i("VOLLEY", response),
                error -> Log.e("VOLLEY", error.toString()))
        {
            @Override
            public String getBodyContentType() { return "application/json; charset=utf-8"; }

            @Override
            public byte[] getBody() {
                return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                assert response != null;
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        RQ.add(stringRequest);
    }


    private void deleteLocation(ArrayList<LocationModel> list, int i) {
//        ArrayList<LocationModel> list = loadLocations();
        msg(act,"Deleted Location: " + list.get(i).name);
        list.remove(i);
        IO.edit().putString("locations", new Gson().toJson(list)).apply();
    }
}