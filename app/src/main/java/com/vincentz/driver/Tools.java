package com.vincentz.driver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.vincentz.driver.navigation.GPSLocationModel;
import com.vincentz.driver.navigation.LocationModel;
import com.vincentz.driver.navigation.NavigationModel;

public class Tools {
    public static RequestQueue RQ;
    public static GPSLocationModel LOC;
    public static LocationModel DEST;
    public static NavigationModel NAV;
    public static SharedPreferences IO;
    static boolean[] PERMISSIONS;

    public static void msg(Activity activity, final String text) {
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_LONG).show());
    }

    public static void fullscreen(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static void hideKeyboard(Activity activity) {
        activity.findViewById(R.id.map).requestFocus();
        InputMethodManager imm = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (activity.getCurrentFocus() != null) {
                IBinder ib = activity.getCurrentFocus().getWindowToken();
                imm.hideSoftInputFromWindow(ib, 0);
            }
        }
    }

    public static String getCompassDirection(float b) {
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