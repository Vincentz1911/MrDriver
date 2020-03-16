package com.vincentz.driver;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static com.vincentz.driver.Tools.ACT;

public class WelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_welcome, vg, false);

        Button welcomeButton;
        welcomeButton = root.findViewById(R.id.btn_welcome);

        welcomeButton.setOnClickListener(view -> ((MainActivity) ACT).firstRun());
        return root;
    }
}
