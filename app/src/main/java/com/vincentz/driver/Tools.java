package com.vincentz.driver;

import android.app.Activity;
import android.widget.Toast;
import androidx.fragment.app.FragmentManager;
import com.android.volley.RequestQueue;

class Tools {

    static int gpsUpdate = 1000;
    static int cameraUpdate = 950;
    static int timerUpdate = 1000;

    static Activity ACT;
    static FragmentManager FM;
    static RequestQueue RQ;
    static LocationModel LOC;
    static boolean[] PERMISSIONS;

    static void msg(Activity activity, final String text) {
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_LONG).show());
    }
    static void msg(final String text) {
        ACT.runOnUiThread(() -> Toast.makeText(ACT, text, Toast.LENGTH_LONG).show());
    }
    static String getDirection(float b) {
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