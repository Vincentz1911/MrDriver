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
import com.vincentz.driver.navigation.StepsModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyAdapter extends ArrayAdapter<WeatherDailyModel> {

    private List<WeatherDailyModel> list;

    DailyAdapter(@NonNull Context context, ArrayList<WeatherDailyModel> list) {
        super(context, 0, list);
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.adapter_daily_listview, parent, false);

        WeatherDailyModel day = list.get(position);
        TextView date = listItem.findViewById(R.id.txt_date);

        //String dateAsText = new SimpleDateFormat("d mmm MM-dd").format(new Date(day.dt * 1000L));
        date.setText(new SimpleDateFormat("EEE d. MMMM", Locale.getDefault()).format(new Date(day.dt * 1000L)));

        ImageView icon = listItem.findViewById(R.id.img_weather);
        icon.setImageDrawable(Tools.getWeatherIcon((Activity) getContext(), day.weather[0].icon));
        TextView highLow = listItem.findViewById(R.id.txt_low_high_temp);
        highLow.setText(getContext().getString(R.string.minmax_temp, day.temp.max, day.temp.min));

        return listItem;
    }
}
