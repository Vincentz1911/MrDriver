package com.vincentz.driver;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Tools.checkPermissions(this);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_big, new MapFragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_left_top, new SpotifyFragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_right_top, new OBD2Fragment(), "").commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_right_bottom, new CameraFragment(), "").commit();
    }
}