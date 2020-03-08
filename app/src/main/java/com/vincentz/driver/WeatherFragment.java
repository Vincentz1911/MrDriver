package com.vincentz.driver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


public class WeatherFragment extends Fragment {

    TextView txt_weather;
    JSONObject JSONResponse, weather, main, wind, sys;


    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_weather, vg, false);

        txt_weather = view.findViewById(R.id.txt_week);

        Timer weatherTimer = new Timer("WeatherTimer");
        weatherTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateWeather();
            }
        }, 1000, 1000);

        return view;
    }

    private void updateWeather() {
        if (MainActivity.location == null) return;

        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + MainActivity.location.getLatitude()
                + "&lon=" + MainActivity.location.getLongitude()
                + "&appid=366be396325d10cf0b15b97a1e8dde63";
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, url, null, response -> JSONResponse = response,
                    error -> Tools.msg(getActivity(), "Error in JSON Response"));
            Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);

            //JSONArray weatherArray = (JSONResponse.getJSONArray("weather"));
            weather = (JSONObject) JSONResponse.getJSONArray("weather").get(0);
            main = JSONResponse.getJSONObject("main");
            wind = JSONResponse.getJSONObject("wind");
            sys = JSONResponse.getJSONObject("sys");
        } catch (Exception e) { e.printStackTrace(); }

    }


    void updateUI() throws JSONException {
        if (JSONResponse == null) return;

        //weather.setText("Response: " + jsonWeather);

        String string = weather.get("id").toString();

    }

}

/*
{
    "coord": {
        "lon": -0.13,
        "lat": 51.51
    },
    "weather": [
        {
            "id": 500,
            "main": "Rain",
            "description": "light rain",
            "icon": "10n"
        }
    ],
    "base": "cmc stations",
    "main": {
        "temp": 286.164,
        "pressure": 1017.58,
        "humidity": 96,
        "temp_min": 286.164,
        "temp_max": 286.164,
        "sea_level": 1027.69,
        "grnd_level": 1017.58
    },
    "wind": {
        "speed": 3.61,
        "deg": 165.001
    },
    "rain": {
        "3h": 0.185
    },
    "clouds": {
        "all": 80
    },
    "dt": 1446583128,
    "sys": {
        "message": 0.003,
        "country": "GB",
        "sunrise": 1446533902,
        "sunset": 1446568141
    },
    "id": 2643743,
    "name": "London",
    "cod": 200
}
 */
