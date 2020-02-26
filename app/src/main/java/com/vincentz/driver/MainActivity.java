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

//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fl_left_bottom, new InfoFragment(), "").commit();


        findViewById(R.id.fl_left_top).setOnLongClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_left_top, new SelectorFragment(), "").commit();
            return false;
        });

        findViewById(R.id.fl_left_bottom).setOnLongClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_left_bottom, new SelectorFragment(), "").commit();
            return false;
        });

        findViewById(R.id.fl_right_top).setOnLongClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_right_top, new SelectorFragment(), "").commit();
            return false;
        });

        findViewById(R.id.fl_right_bottom).setOnLongClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit();
            return false;
        });

        findViewById(R.id.fl_big).setOnLongClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_big, new SelectorFragment(), "").commit();
            return false;
        });
    }
}