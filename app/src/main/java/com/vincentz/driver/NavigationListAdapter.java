package com.vincentz.driver;

import androidx.annotation.NonNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NavigationListAdapter extends ArrayAdapter<LocationModel> {

    private List<LocationModel> list;

    NavigationListAdapter(@NonNull Context context, ArrayList<LocationModel> list) {
        super(context, 0, list);
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.adapter_maps_listview, parent, false);

        LocationModel location = list.get(position);

        TextView name = listItem.findViewById(R.id.txt_name);
        name.setText(location.name);

//        TextView street = listItem.findViewById(R.id.txt_address);
//        street.setText(location.street);

        TextView city = listItem.findViewById(R.id.txt_city);
        city.setText(location.city);

//        final ImageView save = listItem.findViewById(R.id.save_button);
//        final ImageView edit = listItem.findViewById(R.id.edit_button);
//        final ImageView delete = listItem.findViewById(R.id.delete_button);

//        if (!location.saved)
//        {
//            edit.setVisibility(View.GONE);
//            delete.setVisibility(View.GONE);
//        } else {
//            save.setVisibility(View.GONE);
//        }
        return listItem;
    }
}
