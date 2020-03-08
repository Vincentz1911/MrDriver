package com.vincentz.driver;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

public class SelectorFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_selector, vg, false);

        FragmentManager fm = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        view.findViewById(R.id.btn_spotify).setOnClickListener(v -> fm.beginTransaction()
                .replace(this.getId(), new SpotifyFragment(), "").commit());
        view.findViewById(R.id.btn_maps).setOnClickListener(v -> fm.beginTransaction()
                .replace(this.getId(), new MapFragment(), "").commit());
        view.findViewById(R.id.btn_obd2).setOnClickListener(v -> fm.beginTransaction()
                .replace(this.getId(), new OBD2Fragment(), "").commit());
        view.findViewById(R.id.btn_recorder).setOnClickListener(v -> fm.beginTransaction()
                .replace(this.getId(), new CameraFragment(), "").commit());
        view.findViewById(R.id.btn_info).setOnClickListener(v -> fm.beginTransaction()
                .replace(this.getId(), new InfoFragment(), "").commit());
        view.findViewById(R.id.btn_weather).setOnClickListener(v -> fm.beginTransaction()
                .replace(this.getId(), new WeatherFragment(), "").commit());

        return view;
    }
}