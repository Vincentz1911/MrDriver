package com.vincentz.driver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SelectorFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_selector, vg, false);

        (view.findViewById(R.id.btn_spotify)).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(this.getId(), new SpotifyFragment(), "").commit());

        (view.findViewById(R.id.btn_maps)).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(this.getId(), new MapFragment(), "").commit());

        (view.findViewById(R.id.btn_obd2)).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(this.getId(), new OBD2Fragment(), "").commit());

        (view.findViewById(R.id.btn_recorder)).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(this.getId(), new CameraFragment(), "").commit());

        (view.findViewById(R.id.btn_info)).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(this.getId(), new InfoFragment(), "").commit());


        return view;
    }

}
