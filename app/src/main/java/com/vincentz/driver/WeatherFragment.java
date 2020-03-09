package com.vincentz.driver;

import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class WeatherFragment extends Fragment {

    JSONObject JSONResponse, weather, main, wind, sys;
    TextView txt_temp, txt_feels_temp, txt_wind, txt_minmax, txt_press_humid, txt_sunrise_set;
    Timer weatherTimer;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_weather, vg, false);

        txt_temp = view.findViewById(R.id.txt_temp);
        txt_feels_temp = view.findViewById(R.id.txt_feels);
        txt_wind = view.findViewById(R.id.txt_wind);
        txt_minmax = view.findViewById(R.id.txt_low_high_temp);
        txt_press_humid = view.findViewById(R.id.txt_pressure_humidity);
        txt_sunrise_set = view.findViewById(R.id.txt_sunrise_sunset);

        weatherTimer = new Timer("WeatherTimer");
        weatherTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateWeather();
            }
        }, 2000, 10000);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        weatherTimer.cancel();
        weatherTimer.purge();

    }

    private void updateWeather() {
        double lat, lon;
        if (MainActivity.location != null) {
            lat = MainActivity.location.getLatitude();
            lon = MainActivity.location.getLongitude();
        } else if (MainActivity.lastLocation != null) {
            lat = MainActivity.lastLocation.getLatitude();
            lon = MainActivity.lastLocation.getLongitude();
        } else return;

        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (main != null) getActivity().runOnUiThread(() -> updateUI());

    }


    private void updateUI() {

        try {
            int temp = (int) main.getDouble("temp") - 273;
            double feelstemp = main.getDouble("feels_like") - 273.15;
            double windspeed = wind.getDouble("speed");
            String dir = Tools.getDirection((float) wind.getDouble("deg"));
            double maxTemp = main.getDouble("temp_max") - 273.15;
            double minTemp = main.getDouble("temp_min") - 273.15;
            int pressure = (int) main.getDouble("pressure");
            int humidity = (int) main.getDouble("humidity");
            Date sunrise = new Date(sys.getInt("sunrise") * 1000L);
            Date sunset = new Date(sys.getInt("sunset") * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            txt_temp.setText(getString(R.string.temperature, temp));
            txt_feels_temp.setText(getString(R.string.feels_temp, feelstemp));
            txt_wind.setText(getString(R.string.wind, windspeed, dir));
            txt_minmax.setText(getString(R.string.minmax_temp, maxTemp, minTemp));
            txt_press_humid.setText(getString(R.string.press_humid, pressure, humidity));
            txt_sunrise_set.setText(getString(R.string.sunrise_sunset, sdf.format(sunrise), sdf.format(sunset)));

        } catch (JSONException e) {
            e.printStackTrace();
        }

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
