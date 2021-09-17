package com.vincentz.driver;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.vincentz.driver.Tools.*;

public class SettingsFragment extends Fragment {

    Activity act;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_settings, vg, false);
        if (getActivity() == null) return view;
        else act = getActivity();
        EditText driver = view.findViewById(R.id.input_driver);
        EditText pin = view.findViewById(R.id.input_pin);

        if (DRV != null) {
            driver.setText(DRV.username);
            pin.setText(DRV.password);
        }

        language(view);
        theme(view);
        weatherReport(view);
        return view;
    }

    private void language(View view) {
        ArrayAdapter<Locale> languageAdapter = new ArrayAdapter<>(act, R.layout.spinner_item, LANG);
        Spinner languageSpinner = view.findViewById(R.id.spn_language);
        languageSpinner.setAdapter(languageAdapter);
        languageSpinner.setSelection(IO.getInt("Language", 0));
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == IO.getInt("Language", 0)) return;
                IO.edit().putInt("Language", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void theme(View view) {
        List<String> themes = new ArrayList<>(Arrays.asList("Air", "Blue", "Day", "Night"));
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(act, R.layout.spinner_item, themes);
        Spinner themeSpinner = view.findViewById(R.id.spn_theme);
        themeSpinner.setAdapter(themeAdapter);
        themeSpinner.setSelection(IO.getInt("Theme", 0));
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == IO.getInt("Theme", -1)) return;
                IO.edit().putInt("Theme", position).apply();
                act.recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void weatherReport(View view) {
        CheckBox cb = view.findViewById(R.id.cb_weather);
        boolean speakWeather = IO.getBoolean("WeatherReport", true);
        if (speakWeather) cb.setChecked(true); else cb.setChecked(false);
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            IO.edit().putBoolean("WeatherReport", isChecked);

            //cb.setChecked(cb.isEnabled());
        });


    }
}