package com.vincentz.driver;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tools.checkPermissions(this);
        Tools.getLocation(this);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fl_big_center, new MapFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_left_top, new SpotifyFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_left_bottom, new InfoFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
        fm.beginTransaction().replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit();


        //        findViewById(R.id.fl_left_top).setOnLongClickListener(v -> {
//
//            return false; });

//
//
//
//
//        findViewById(R.id.fl_right_top).setOnLongClickListener(v -> {
//            fm.beginTransaction().replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
//            return false; });
//        findViewById(R.id.fl_right_bottom).setOnLongClickListener(v -> {
//            fm.beginTransaction().replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit();
//            return false; });
////        findViewById(R.id.fl_big_center).setOnLongClickListener(v -> {
////            fm.beginTransaction().replace(R.id.fl_big_center, new SelectorFragment(), "").commit();
////            return false; });
    }
}