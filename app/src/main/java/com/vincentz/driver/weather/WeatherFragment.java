package com.vincentz.driver.weather;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vincentz.driver.R;
import com.vincentz.driver.Tools;
import com.vincentz.driver.navigation.NavigationListAdapter;

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
    public WeatherModel wm;
    private ImageView weather_icon;
    private TextView txt_temp, txt_feels_like, txt_clouds, txt_wind, txt_minmax, txt_press_humid, txt_sunrise_sunset;
    private View current_weather_data;
    private ListView hourly_weather_data, daily_weather_data;
    private boolean startup;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        //region INIT UI
        View view = li.inflate(R.layout.fragment_weather, vg, false);
        current_weather_data = view.findViewById(R.id.current_weather_data);
        hourly_weather_data = view.findViewById(R.id.hourly_weather_data);
        daily_weather_data = view.findViewById(R.id.daily_weather_data);
        ImageView weather_now = view.findViewById(R.id.weather_now);
        ImageView weather_hourly = view.findViewById(R.id.weather_hourly);
        ImageView weather_daily = view.findViewById(R.id.weather_daily);
        weather_icon = view.findViewById(R.id.img_weather);
        txt_temp = view.findViewById(R.id.txt_temp);
        txt_feels_like = view.findViewById(R.id.txt_feels_like);
        txt_clouds = view.findViewById(R.id.txt_clouds);
        txt_wind = view.findViewById(R.id.txt_wind);
        txt_minmax = view.findViewById(R.id.txt_low_high_temp);
        txt_press_humid = view.findViewById(R.id.txt_pressure_humidity);
        txt_sunrise_sunset = view.findViewById(R.id.txt_sunrise_sunset);
        //endregion
        LOC.addObserver(this); //Add Observer on GPSLocationModel in MainActivity
        weather_now.setOnClickListener(v -> showWeather(0));
        weather_hourly.setOnClickListener(v -> showWeather(1));
        weather_daily.setOnClickListener(v -> showWeather(2));

        return view;
    }

    private void showWeather(int type) {
        current_weather_data.setVisibility(View.GONE);
        hourly_weather_data.setVisibility(View.GONE);
        daily_weather_data.setVisibility(View.GONE);
        if (type == 0) current_weather_data.setVisibility(View.VISIBLE);
        if (type == 1) hourly_weather_data.setVisibility(View.VISIBLE);
        if (type == 2) daily_weather_data.setVisibility(View.VISIBLE);
    }

    public void update(Observable locModel, Object loc) {
        //Listener for GPSLocationModel Observable.
        if (loc == null || startup) return;
        startup = true;
        LOC.deleteObserver(this);
        weatherTimer = new Timer("WeatherTimer");
        weatherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                requestWeather();
            }
        }, 0, 60 * 1000);
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

        //SEND JSON OBJECT REQUEST TO QUEUE. IF RESPONSE UPDATE UI
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> getActivity().runOnUiThread(() -> json2object(response)),
                error -> msg(getActivity(), "Volley Weather Error")));
    }

    private void json2object(JSONObject response) {
        try {
            //GET THE CURRENT WEATHER
            JSONObject Jcurrent = response.getJSONObject("current");
            WeatherHourlyModel current = new Gson().fromJson(Jcurrent.toString(), WeatherHourlyModel.class);

            //GET HOURLY
            JSONArray Jhourly = response.getJSONArray("hourly");
            Type Htype = new TypeToken<ArrayList<WeatherHourlyModel>>() {
            }.getType();
            ArrayList<WeatherHourlyModel> hourlyList = new Gson().fromJson(Jhourly.toString(), Htype);

            //GET DAILY
            JSONArray Jdaily = response.getJSONArray("daily");
            Type Dtype = new TypeToken<ArrayList<WeatherDailyModel>>() {
            }.getType();
            ArrayList<WeatherDailyModel> dailyList = new Gson().fromJson(Jdaily.toString(), Dtype);

            wm = new WeatherModel(current = current, hourlyList = hourlyList, dailyList = dailyList);

            updateUI();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        weather_icon.setImageDrawable(Tools.getWeatherIcon(getActivity(), wm.current.weather[0].icon));
        txt_clouds.setText(wm.current.clouds + "% " + wm.current.weather[0].description);
        txt_temp.setText(getString(R.string.temperature, (int) wm.current.temp));
        txt_feels_like.setText(getString(R.string.feels_temp, (int) wm.current.feels_like));

        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.getDefault());
        txt_sunrise_sunset.setText(getString(R.string.sunrise_sunset,
                time.format(new Date(wm.current.sunrise * 1000)),
                time.format(new Date(wm.current.sunset * 1000)),
                wm.current.uvi));
        txt_wind.setText(getString(R.string.wind,
                wm.current.wind_speed,
                wm.current.wind_deg,
                getCompassDirection(wm.current.wind_deg)));
        txt_minmax.setText(getString(R.string.minmax_temp,
                wm.dailyList.get(0).temp.max,
                wm.dailyList.get(0).temp.min));
        txt_press_humid.setText(getString(R.string.press_humid,
                wm.current.pressure,
                wm.current.humidity,
                (int) wm.current.dew_point
        ));

        ArrayAdapter dailyAdapter = new DailyAdapter(getContext(), wm.dailyList);
        daily_weather_data.setVisibility(View.VISIBLE);
        daily_weather_data.setAdapter(dailyAdapter);
        daily_weather_data.setOnItemClickListener((parent, view, position, id) -> {});

    }
}