package com.vincentz.driver.weather;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vincentz.driver.R;
import com.vincentz.driver.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HourlyAdapter extends ArrayAdapter<WeatherHourlyModel> {

    private List<WeatherHourlyModel> list;

    HourlyAdapter(@NonNull Context context, ArrayList<WeatherHourlyModel> list) {
        super(context, 0, list);
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.adapter_hourly_listview, parent, false);

        WeatherHourlyModel hour = list.get(position);
        TextView date = listItem.findViewById(R.id.txt_time);
        date.setText(new SimpleDateFormat("HH:00\nd/M", Locale.getDefault()).format(new Date(hour.dt * 1000L)));

        ImageView icon = listItem.findViewById(R.id.img_weather);
        icon.setImageDrawable(Tools.getWeatherIcon((Activity) getContext(), hour.weather[0].icon));
        TextView temp = listItem.findViewById(R.id.txt_temp);
        temp.setText(getContext().getString(R.string.temperature, (int)hour.temp));

        return listItem;
    }
}
