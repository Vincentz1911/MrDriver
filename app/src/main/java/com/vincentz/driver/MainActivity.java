package com.vincentz.driver;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Tools.checkPermissions(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_left_bottom, new InfoFragment(), "").commit();
//
//        (findViewById(R.id.btn_left_top)).setOnClickListener(v ->
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fl_left_top, new SpotifyFragment(), "").commit());
//
//        (findViewById(R.id.btn_big)).setOnClickListener(v ->
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fl_big, new MapFragment(), "").commit());
//
//        (findViewById(R.id.btn_right_top)).setOnClickListener(v ->
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fl_right_top, new OBD2Fragment(), "").commit());
//
//        (findViewById(R.id.btn_right_bottom)).setOnClickListener(v ->
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fl_right_bottom, new CameraFragment(), "").commit());
    }
}