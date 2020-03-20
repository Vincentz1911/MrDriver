package com.vincentz.driver;

import androidx.fragment.app.Fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.Tools.*;

public class WeatherFragment extends Fragment implements Observer {

    //private MainActivity activity;
    private Timer weatherTimer;
    private ImageView weather_icon;
    private TextView txt_temp, txt_clouds, txt_wind, txt_minmax, txt_press_humid, txt_sunrise_sunset;
    private boolean startup;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View root = li.inflate(R.layout.fragment_weather, vg, false);
        weather_icon = root.findViewById(R.id.img_weather);
        txt_temp = root.findViewById(R.id.txt_temp);
        txt_clouds = root.findViewById(R.id.txt_clouds);
        txt_wind = root.findViewById(R.id.txt_wind);
        txt_minmax = root.findViewById(R.id.txt_low_high_temp);
        txt_press_humid = root.findViewById(R.id.txt_pressure_humidity);
        txt_sunrise_sunset = root.findViewById(R.id.txt_sunrise_sunset);
        //endregion
//        if (getActivity() != null) activity = (MainActivity) getActivity();
//        else return root;
        LOC.addObserver(this); //Add Observer on LocationModel in MainActivity
        return root;
    }

    public void update(Observable locModel, Object loc) {
        //Listener for LocationModel Observable.
        if (loc == null || startup) return;
        startup = true;
        LOC.deleteObserver(this);
        weatherTimer = new Timer("WeatherTimer");
        weatherTimer.schedule(new TimerTask() {
            @Override
            public void run() { requestWeather(); }}, 0, 10000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //LOC.deleteObserver(this);
        if (weatherTimer != null) {
            weatherTimer.cancel();
            weatherTimer.purge();
        }
    }

    private void requestWeather() {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat="
                + LOC.getNow().getLatitude() + "&lon="
                + LOC.getNow().getLongitude()
                + "&appid=366be396325d10cf0b15b97a1e8dde63";

        //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> ACT.runOnUiThread(() -> updateUI(response)),
                error -> msg(ACT, "Volley Error")));
    }

    private void updateUI(JSONObject response) {
        try {

            //SPLITS JSONRESPONSE INTO JSONOBJECTS
            JSONObject weather = (JSONObject) response.getJSONArray("weather").get(0);
            JSONObject main = response.getJSONObject("main");
            JSONObject wind = response.getJSONObject("wind");
            JSONObject sys = response.getJSONObject("sys");

            //CREATES A SPANNABLE STRING FOR LOC, TEMP AND FEELS
            String location = response.getString("name").split(" ")[0];
            String temp = getString(R.string.temperature, (int) main.getDouble("temp") - 273);
            String feels = getString(R.string.feels_temp, main.getDouble("feels_like") - 273.15);
            SpannableString span1 = new SpannableString(location);
            SpannableString span2 = new SpannableString(temp);
            SpannableString span3 = new SpannableString(feels);
            span1.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.h5)), 0, location.length(), 0);
            span2.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.h1)), 0, temp.length(), 0);
            span3.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.h5)), 0, feels.length(), 0);

            //SETS DATA FROM JSON OBJECTS
            String dir = "NA";
            if (wind.has("deg")) dir = getDirection((float) wind.getDouble("deg"));
            double windspeed = wind.getDouble("speed");
            double maxTemp = main.getDouble("temp_max") - 273.15;
            double minTemp = main.getDouble("temp_min") - 273.15;
            int pressure = (int) main.getDouble("pressure");
            int humidity = (int) main.getDouble("humidity");
            Date sunrise = new Date(sys.getInt("sunrise") * 1000L);
            Date sunset = new Date(sys.getInt("sunset") * 1000L);
            int clouds = response.getJSONObject("clouds").getInt("all");
            String description = weather.getString("description");
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            //UPDATES VIEW WITH DATA
            weather_icon.setImageDrawable(getWeatherIcon(weather.getString("icon")));
            txt_clouds.setText(description + clouds+"%");
            txt_temp.setText(TextUtils.concat(span1, "\n", span2, "\n", span3));
            txt_wind.setText(getString(R.string.wind, windspeed, dir));
            txt_minmax.setText(getString(R.string.minmax_temp, maxTemp, minTemp));
            txt_press_humid.setText(getString(R.string.press_humid, pressure, humidity));
            txt_sunrise_sunset.setText(getString(R.string.sunrise_sunset, sdf.format(sunrise), sdf.format(sunset)));

            //msg("WeatherPos: " + response.getString("name"));
        } catch (JSONException e) {
            msg(ACT, "JSON Error!");
        }
    }

    private Drawable getWeatherIcon(String icon) {
        //PICKS ICON DEPENDING ON ICON CODE IN JSONRESPONSE
        switch (icon) {
            case "01d":
                return getResources().getDrawable(R.drawable.wic_01d_day_clear, null);
            case "01n":
                return getResources().getDrawable(R.drawable.wic_01n_night_clear, null);
            case "02d":
                return getResources().getDrawable(R.drawable.wic_02d_day_partial_cloud, null);
            case "02n":
                return getResources().getDrawable(R.drawable.wic_02n_night_partial_cloud, null);
            case "03d":
                return getResources().getDrawable(R.drawable.wic_03_cloudy, null);
            case "03n":
                return getResources().getDrawable(R.drawable.wic_03_cloudy, null);
            case "04d":
                return getResources().getDrawable(R.drawable.wic_04_angry_clouds, null);
            case "04n":
                return getResources().getDrawable(R.drawable.wic_04_angry_clouds, null);
            case "09d":
                return getResources().getDrawable(R.drawable.wic_09_rain, null);
            case "09n":
                return getResources().getDrawable(R.drawable.wic_09_rain, null);
            case "10d":
                return getResources().getDrawable(R.drawable.wic_10d_day_rain, null);
            case "10n":
                return getResources().getDrawable(R.drawable.wic_10n_night_rain, null);
            case "11d":
                return getResources().getDrawable(R.drawable.wic_11d_rain_thunder, null);
            case "11n":
                return getResources().getDrawable(R.drawable.wic_11d_rain_thunder, null);
            case "13d":
                return getResources().getDrawable(R.drawable.wic_13_snow, null);
            case "13n":
                return getResources().getDrawable(R.drawable.wic_13_snow, null);
            case "50d":
                return getResources().getDrawable(R.drawable.wic_50_fog, null);
            case "50n":
                return getResources().getDrawable(R.drawable.wic_50_fog, null);
            default:
                return getResources().getDrawable(R.drawable.wic_11d_day_rain_thunder, null);
        }
    }
}

//WEATHER NOW
//{"coord":{"lon":12.48,"lat":55.68},
// "weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10n"}],
// "base":"stations",
// "main":{"temp":280.81,"feels_like":277.04,"temp_min":280.15,"temp_max":281.15,"pressure":991,"humidity":100},
// "visibility":9000,
// "wind":{"speed":4.6,"deg":240,"gust":11.3},
// "clouds":{"all":100},
// "dt":1583863378,
// "sys":{"type":1,"id":9710,"country":"DK","sunrise":1583818656,"sunset":1583859790},
// "timezone":3600,
// "id":2614600,
// "name":"Rødovre Municipality",
// "cod":200}

