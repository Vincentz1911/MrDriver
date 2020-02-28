package com.vincentz.driver;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tools.checkPermissions(this);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fl_big, new SelectorFragment(), "").commit();

        findViewById(R.id.fl_left_top).setOnLongClickListener(v -> {
            fm.beginTransaction().replace(R.id.fl_left_top, new SelectorFragment(), "").commit();
            return false; });
        findViewById(R.id.fl_left_bottom).setOnLongClickListener(v -> {
            fm.beginTransaction().replace(R.id.fl_left_bottom, new SelectorFragment(), "").commit();
            return false; });
        findViewById(R.id.fl_right_top).setOnLongClickListener(v -> {
            fm.beginTransaction().replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
            return false; });
        findViewById(R.id.fl_right_bottom).setOnLongClickListener(v -> {
            fm.beginTransaction().replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit();
            return false; });
//        findViewById(R.id.fl_big).setOnLongClickListener(v -> {
//            fm.beginTransaction().replace(R.id.fl_big, new SelectorFragment(), "").commit();
//            return false; });
    }
}