package com.vincentz.driver;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.vincentz.driver.Tools.IO;

public class SettingsFragment extends Fragment {

    Spinner themeSpinner;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_settings, vg, false);

        List<String> themes = new ArrayList<>(Arrays.asList("Air", "Blue", "Day", "Night"));
        List<Locale> languages = new ArrayList<>(Arrays.asList(Locale.US, Locale.ENGLISH));



        themeSpinner = view.findViewById(R.id.spn_theme);

        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, themes);
        //themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);
        themeSpinner.setSelection(IO.getInt("Theme", 0));
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == IO.getInt("Theme", 0)) return;
                IO.edit().putInt("Theme", position).apply();
                getActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
}