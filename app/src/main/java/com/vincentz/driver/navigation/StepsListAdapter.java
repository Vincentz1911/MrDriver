package com.vincentz.driver.navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vincentz.driver.R;

import java.util.ArrayList;
import java.util.List;

public class StepsListAdapter extends ArrayAdapter<StepsModel> {

    private List<StepsModel> list;

    StepsListAdapter(@NonNull Context context, ArrayList<StepsModel> list) {
        super(context, 0, list);
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.adapter_maps_listview, parent, false);

        StepsModel step = list.get(position);

        TextView name = listItem.findViewById(R.id.txt_name);
        name.setText(step.instruction);

        String unit = "m ";
        float distance = Math.abs(step.distance);

        if (distance > 800) {
            distance = ((float)((int)(distance/100)))/10;
            unit = "km ";
        }


        TextView city = listItem.findViewById(R.id.txt_city);
        city.setText(distance + unit + step.duration);

        return listItem;
    }
}
