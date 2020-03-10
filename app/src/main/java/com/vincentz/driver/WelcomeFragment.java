package com.vincentz.driver;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class WelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_welcome, vg, false);

        Button welcomeButton;
        welcomeButton = root.findViewById(R.id.btn_welcome);

        welcomeButton.setOnClickListener(view -> ((MainActivity)getActivity()).firstRun());
        return root;
    }
}
