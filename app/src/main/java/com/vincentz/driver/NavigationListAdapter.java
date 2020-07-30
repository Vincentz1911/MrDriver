package com.vincentz.driver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vincentz.driver.LocationModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NavigationListAdapter extends ArrayAdapter<LocationModel>{


        private List<LocationModel> list;

    NavigationListAdapter(@NonNull Context context, ArrayList<LocationModel> list) {
            super(context, 0, list);
            this.list = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;

            if (listItem == null)
                listItem = LayoutInflater.from(getContext()).inflate(R.layout.adapter_maps_listview, parent, false);

            LocationModel alarm = list.get(position);

            TextView date = listItem.findViewById(R.id.txt_date);
            date.setText(MainActivity.datetime.format(alarm.getDate()));

            ImageView imageRepeat = listItem.findViewById(R.id.listImageRepeat);
            if (alarm.getRepeat() == 0) {
                if (alarm.getDate().before(new Date()))
                    imageRepeat.setImageResource(R.drawable.ic_timer_off_36dp);
                else imageRepeat.setImageResource(R.drawable.ic_timer_36dp);
            } else imageRepeat.setImageResource(R.drawable.ic_autorenew_36p);

            ImageView imageView = listItem.findViewById(R.id.listImage);
            imageView.setImageResource(alarm.getIcon());

            TextView date = listItem.findViewById(R.id.txt_date);
            date.setText(MainActivity.datetime.format(alarm.getDate()));

            return listItem;
        }

}
