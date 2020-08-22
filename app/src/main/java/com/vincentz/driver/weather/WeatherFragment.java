package com.vincentz.driver.weather;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vincentz.driver.R;
import com.vincentz.driver.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.Tools.*;

public class WeatherFragment extends Fragment implements Observer {

    private Timer weatherTimer;
    private ImageView weather_icon;
    private TextView txt_temp, txt_feels, txt_clouds, txt_wind, txt_minmax, txt_press_humid, txt_sunrise_sunset;
    private View currentView;
    private ListView hourlyListView, dailyListView;
    private boolean isStarted;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        LOC.addObserver(this); //Add Observer on GPSLocationModel in MainActivity
        //region INIT UI
        View view = li.inflate(R.layout.fragment_weather, vg, false);
        currentView = view.findViewById(R.id.current_weather_data);
        hourlyListView = view.findViewById(R.id.hourly_weather_data);
        dailyListView = view.findViewById(R.id.daily_weather_data);
        weather_icon = view.findViewById(R.id.img_weather);
        txt_temp = view.findViewById(R.id.txt_temp);
        txt_feels = view.findViewById(R.id.txt_feels_like);
        txt_clouds = view.findViewById(R.id.txt_clouds);
        txt_wind = view.findViewById(R.id.txt_wind);
        txt_minmax = view.findViewById(R.id.txt_low_high_temp);
        txt_press_humid = view.findViewById(R.id.txt_pressure_humidity);
        txt_sunrise_sunset = view.findViewById(R.id.txt_sunrise_sunset);

        ImageView weather_now = view.findViewById(R.id.weather_now);
        ImageView weather_hourly = view.findViewById(R.id.weather_hourly);
        ImageView weather_daily = view.findViewById(R.id.weather_daily);
        weather_now.setOnClickListener(v -> showWeather(0));
        weather_hourly.setOnClickListener(v -> showWeather(1));
        weather_daily.setOnClickListener(v -> showWeather(2));
        return view;
        //endregion
    }

    private void showWeather(int type) {
        currentView.setVisibility(View.GONE);
        hourlyListView.setVisibility(View.GONE);
        dailyListView.setVisibility(View.GONE);
        if (type == 0) currentView.setVisibility(View.VISIBLE);
        if (type == 1) hourlyListView.setVisibility(View.VISIBLE);
        if (type == 2) dailyListView.setVisibility(View.VISIBLE);
    }

    public void update(Observable locModel, Object loc) {
        //Listener for GPSLocationModel Observable.
        if (loc == null || isStarted) return;
        isStarted = true;
        LOC.deleteObserver(this);
        weatherTimer = new Timer("WeatherTimer");
        weatherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                requestWeather();
            }
        }, 0, 10 * 60 * 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (weatherTimer != null) {
            weatherTimer.cancel();
            weatherTimer.purge();
        }
    }

    private void requestWeather() {
        String url = "https://api.openweathermap.org/data/2.5/onecall?units=metric&exclude=minutely"
                + "&lat=" + LOC.now().getLatitude() + "&lon=" + LOC.now().getLongitude()
                + "&appid=366be396325d10cf0b15b97a1e8dde63";

        //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE, PARSE THEN UPDATE UI
        if (getActivity() != null) {
            RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> getActivity().runOnUiThread(() -> json2object(response)),
                    error -> msg(getActivity(), "Weather API Error")));
        }
    }

    private void json2object(JSONObject response) {
        try {
            //GET THE CURRENT WEATHER
            JSONObject Jcurrent = response.getJSONObject("current");
            WeatherHourlyModel current = new Gson().fromJson(Jcurrent.toString(), WeatherHourlyModel.class);

            //GET HOURLY
            JSONArray Jhourly = response.getJSONArray("hourly");
            Type Htype = new TypeToken<ArrayList<WeatherHourlyModel>>() {}.getType();
            ArrayList<WeatherHourlyModel> hourlyList = new Gson().fromJson(Jhourly.toString(), Htype);

            //GET DAILY
            JSONArray Jdaily = response.getJSONArray("daily");
            Type Dtype = new TypeToken<ArrayList<WeatherDailyModel>>() {}.getType();
            ArrayList<WeatherDailyModel> dailyList = new Gson().fromJson(Jdaily.toString(), Dtype);

            updateUI(new WeatherModel(current, hourlyList, dailyList));
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void updateUI(WeatherModel wm) {
        if (getContext() == null) return;
        weather_icon.setImageDrawable(Tools.getWeatherIcon(getActivity(), wm.current.weather[0].icon));
        txt_clouds.setText(getString(R.string.clouds,
                wm.current.clouds, wm.current.weather[0].description));
        txt_temp.setText(getString(R.string.temperature, (int) wm.current.temp));
        txt_feels.setText(getString(R.string.feels_temp, (int) wm.current.feels_like));

        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.getDefault());
        txt_sunrise_sunset.setText(getString(R.string.sunrise_sunset,
                time.format(new Date(wm.current.sunrise * 1000)),
                time.format(new Date(wm.current.sunset * 1000)),
                wm.current.uvi));
        txt_wind.setText(getString(R.string.wind,
                wm.current.wind_speed,
                wm.current.wind_deg,
                getCompassDirection(wm.current.wind_deg),
                getWindDescription(wm.current.wind_speed)));
        txt_minmax.setText(getString(R.string.minmax_temp,
                wm.dailyList.get(0).temp.max,
                wm.dailyList.get(0).temp.min,
                wm.current.rain._1h));
        txt_press_humid.setText(getString(R.string.press_humid,
                wm.current.pressure,
                wm.current.humidity,
                (int) wm.current.dew_point
        ));

        dailyListView.setAdapter(new DailyAdapter(getContext(), wm.dailyList));
        dailyListView.setOnItemClickListener((parent, view, position, id) -> { });
        hourlyListView.setAdapter(new HourlyAdapter(getContext(), wm.hourlyList));
        hourlyListView.setOnItemClickListener((parent, view, position, id) -> { });

        say("The weather report!");
    }
}