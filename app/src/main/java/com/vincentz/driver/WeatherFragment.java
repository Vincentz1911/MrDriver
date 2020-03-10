package com.vincentz.driver;

import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class WeatherFragment extends Fragment {

    private Activity activity;
    private JSONObject JSONResponse, weather, main, wind, sys;
    private TextView txt_temp, txt_wind, txt_minmax, txt_press_humid, txt_sunrise_sunset;
    private ImageView weather_icon;
    private Timer weatherTimer;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View root = li.inflate(R.layout.fragment_weather, vg, false);
        weather_icon = root.findViewById(R.id.img_weather);
        txt_temp = root.findViewById(R.id.txt_temp);
        txt_wind = root.findViewById(R.id.txt_wind);
        txt_minmax = root.findViewById(R.id.txt_low_high_temp);
        txt_press_humid = root.findViewById(R.id.txt_pressure_humidity);
        txt_sunrise_sunset = root.findViewById(R.id.txt_sunrise_sunset);
        //endregion
        activity = getActivity();
        weatherTimer = new Timer("WeatherTimer");
        weatherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateWeather();
            }
        }, 0, 10000);
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        weatherTimer.cancel();
        weatherTimer.purge();
    }

    private void updateWeather() {
        double lat, lon;
        if (MainActivity.LOCATION != null) {
            lat = MainActivity.LOCATION.getLatitude();
            lon = MainActivity.LOCATION.getLongitude();
        } else if (MainActivity.LASTLOCATION != null) {
            lat = MainActivity.LASTLOCATION.getLatitude();
            lon = MainActivity.LASTLOCATION.getLongitude();
        } else return;

        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon
                + "&appid=366be396325d10cf0b15b97a1e8dde63";
        Volley.newRequestQueue(activity).add(new JsonObjectRequest(
                Request.Method.GET, url, null, response -> JSONResponse = response,
                error -> Tools.msg(activity, "Error in JSON Response")));

        try {
            //JSONArray weatherArray = (JSONResponse.getJSONArray("weather"));
            weather = (JSONObject) JSONResponse.getJSONArray("weather").get(0);
            main = JSONResponse.getJSONObject("main");
            wind = JSONResponse.getJSONObject("wind");
            sys = JSONResponse.getJSONObject("sys");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //if (main != null) activity.runOnUiThread(this::updateUI);
    }

    private void updateUI() {
        try {
            String location = JSONResponse.getString("name").split(" ")[0];
            String temp = getString(R.string.temperature, (int) main.getDouble("temp") - 273);
            String feels = getString(R.string.feels_temp, main.getDouble("feels_like") - 273.15);

            SpannableString span1 = new SpannableString(location);
            span1.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.h5)),
                    0, location.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            SpannableString span2 = new SpannableString(temp);
            span2.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.h1)),
                    0, temp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            SpannableString span3 = new SpannableString(feels);
            span3.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.h5)),
                    0, feels.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            double windspeed = wind.getDouble("speed");
            String dir = Tools.getDirection((float) wind.getDouble("deg"));
            double maxTemp = main.getDouble("temp_max") - 273.15;
            double minTemp = main.getDouble("temp_min") - 273.15;
            int pressure = (int) main.getDouble("pressure");
            int humidity = (int) main.getDouble("humidity");
            Date sunrise = new Date(sys.getInt("sunrise") * 1000L);
            Date sunset = new Date(sys.getInt("sunset") * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            weather_icon.setImageDrawable(getWeatherIcon(weather.getString("icon")));
            txt_temp.setText(TextUtils.concat(span1, "\n", span2, "\n", span3));
            txt_wind.setText(getString(R.string.wind, windspeed, dir));
            txt_minmax.setText(getString(R.string.minmax_temp, maxTemp, minTemp));
            txt_press_humid.setText(getString(R.string.press_humid, pressure, humidity));
            txt_sunrise_sunset.setText(getString(R.string.sunrise_sunset, sdf.format(sunrise), sdf.format(sunset)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Drawable getWeatherIcon(String icon) {
        switch (icon) {
            case "01d":
                return getResources().getDrawable(R.drawable.ic_w01d_day_clear, null);
            case "01n":
                return getResources().getDrawable(R.drawable.ic_w01n_night_clear, null);
            case "02d":
                return getResources().getDrawable(R.drawable.ic_w02d_day_partial_cloud, null);
            case "02n":
                return getResources().getDrawable(R.drawable.ic_w02n_night_partial_cloud, null);
            case "03d":
                return getResources().getDrawable(R.drawable.ic_w03_cloudy, null);
            case "03n":
                return getResources().getDrawable(R.drawable.ic_w03_cloudy, null);
            case "04d":
                return getResources().getDrawable(R.drawable.ic_w04_angry_clouds, null);
            case "04n":
                return getResources().getDrawable(R.drawable.ic_w04_angry_clouds, null);
            case "09d":
                return getResources().getDrawable(R.drawable.ic_w09_rain, null);
            case "09n":
                return getResources().getDrawable(R.drawable.ic_w09_rain, null);
            case "10d":
                return getResources().getDrawable(R.drawable.ic_w10d_day_rain, null);
            case "10n":
                return getResources().getDrawable(R.drawable.ic_w10n_night_rain, null);
            case "11d":
                return getResources().getDrawable(R.drawable.ic_w11d_rain_thunder, null);
            case "11n":
                return getResources().getDrawable(R.drawable.ic_w11d_rain_thunder, null);
            case "13d":
                return getResources().getDrawable(R.drawable.ic_w13_snow, null);
            case "13n":
                return getResources().getDrawable(R.drawable.ic_w13_snow, null);
            case "50d":
                return getResources().getDrawable(R.drawable.ic_w50_fog, null);
            case "50n":
                return getResources().getDrawable(R.drawable.ic_w50_fog, null);
            default:
                return getResources().getDrawable(R.drawable.ic_w11d_day_rain_thunder, null);
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
