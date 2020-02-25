package com.vincentz.driver;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class InfoFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_info, vg, false);

        (view.findViewById(R.id.btn_left_top)).setOnClickListener(v ->
               getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_left_top, new SpotifyFragment(), "").commit());

        (view.findViewById(R.id.btn_big)).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_big, new MapFragment(), "").commit());

        (view.findViewById(R.id.btn_right_top)).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_right_top, new OBD2Fragment(), "").commit());

        (view.findViewById(R.id.btn_right_bottom)).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_right_bottom, new CameraFragment(), "").commit());

        return view;
    }

}
