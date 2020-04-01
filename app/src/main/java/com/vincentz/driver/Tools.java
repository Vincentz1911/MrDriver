package com.vincentz.driver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.android.volley.RequestQueue;

class Tools {

    static Activity ACT;
    static FragmentManager FM;
    static RequestQueue RQ;
    static GPSLocationModel LOC;
    static SharedPreferences IO;
    static boolean[] PERMISSIONS;

    static void msg(final String text) {
        ACT.runOnUiThread(() -> Toast.makeText(ACT, text, Toast.LENGTH_LONG).show());
    }

    static void fullscreen() {
        ACT.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    static void hideKeyboard() {
        ACT.findViewById(R.id.map).requestFocus();
        InputMethodManager imm = (InputMethodManager)
                ACT.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (ACT.getCurrentFocus() != null) {
                IBinder ib = ACT.getCurrentFocus().getWindowToken();
                imm.hideSoftInputFromWindow(ib, 0);
            }
        }
    }

    static String getCompassDirection(float b) {
        b -= 11.5;
        if (b <= 0 || b > 337.5) return "N";
        else if (b <= 22.5) return "NNE";
        else if (b <= 45) return "NE";
        else if (b <= 67.5) return "ENE";
        else if (b <= 90) return "E";
        else if (b <= 112.5) return "ESE";
        else if (b <= 135) return "SE";
        else if (b <= 157.5) return "SSE";
        else if (b <= 180) return "S";
        else if (b <= 202.5) return "SSW";
        else if (b <= 225) return "SW";
        else if (b <= 247.5) return "WSW";
        else if (b <= 270) return "W";
        else if (b <= 297.5) return "WNW";
        else if (b <= 315) return "NW";
        else if (b <= 337.5) return "NNW";
        else return "";
    }
}