package com.vincentz.driver;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.loader.app.LoaderManager.getInstance;

public class InfoFragment extends Fragment {

    JSONArray jsonWeather;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_info, vg, false);
        ImageView big = view.findViewById(R.id.btn_big);
        ImageView ltop = view.findViewById(R.id.btn_left_top);
        ImageView lbtm = view.findViewById(R.id.btn_left_bottom);
        ImageView rtop = view.findViewById(R.id.btn_right_top);
        ImageView rbtm = view.findViewById(R.id.btn_right_bottom);
        TextView time = view.findViewById(R.id.txt_time);
        TextView date = view.findViewById(R.id.txt_date);
        TextView weather = view.findViewById(R.id.txt_weather);

        // SharedPreferences pref = getActivity().getSharedPreferences("12", Context.MODE_PRIVATE);
        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getActivity().runOnUiThread(() -> {
                            time.setText(Tools.timeFormat.format(new Date()));
                            date.setText(Tools.dateFormat.format(new Date()));
                            weather.setText("Response: " + jsonWeather);
                        }
                );
            }
        }, 1000, 1000);

        Timer weatherTimer = new Timer("WeatherTimer");
//        TimerTask tt = new TimerTask() {
//            @Override
//            public void run() {updateWeather();}};
        weatherTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {updateWeather();}}, 1000, 1000);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        (view.findViewById(R.id.btn_left_top)).setOnClickListener(v ->
        {
            Fragment fr = fm.findFragmentById(R.id.fl_left_top);
            View vw = fr.getView();
            ViewGroup parent = (ViewGroup) vw.getParent();
            parent.removeView(vw);

            Fragment frbig = fm.findFragmentById(R.id.fl_big_center);
            View vwbig = frbig.getView();
            ViewGroup parentbig = (ViewGroup) vwbig.getParent();
            parentbig.removeView(vwbig);

            parentbig.addView(vw);
            parent.addView(vwbig);

            if (fr instanceof SpotifyFragment)
                big.setImageResource(R.drawable.ic_spotify_logo_200dp);
            if (fr instanceof MapFragment) big.setImageResource(R.drawable.ic_maps_200dp);
            if (frbig instanceof SpotifyFragment)
                ltop.setImageResource(R.drawable.ic_spotify_logo_200dp);
            if (frbig instanceof MapFragment) ltop.setImageResource(R.drawable.ic_maps_200dp);
        });


        //TODO new thread

//                View vv = fragment.getView();
//        ViewGroup parent = (ViewGroup)vv.getParent();
//        parent.removeView(vv);
//        newparent.addView(vv, layoutParams);

//                fm.beginTransaction()
//                .replace(R.id.fl_left_top, new SelectorFragment(), "").commit());

        big.setOnClickListener(v -> fm.beginTransaction()
                .replace(R.id.fl_big_center, new SelectorFragment(), "").commit());

        rtop.setOnClickListener(v -> fm.beginTransaction()
                .replace(R.id.fl_right_top, new SelectorFragment(), "").commit());

        rbtm.setOnClickListener(v -> fm.beginTransaction()
                .replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit());

        return view;
    }

    private void updateWeather() {
        if (Tools.location == null) return;

        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "api.openweathermap.org/data/2.5/weather?lat=" + Tools.location.getLatitude()
                + "&lon=" + Tools.location.getLongitude() + "&appid=366be396325d10cf0b15b97a1e8dde63";
        //http://samples.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=b6907d289e10d714a6e88b30761fae22

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url,
                null, response -> jsonWeather = response,
                error -> Tools.msg(getContext(), "Error in JSON Response"));
        queue.add(jsonObjectRequest);
    }
}
