package com.vincentz.driver;

import android.app.Activity;
import android.widget.Toast;

class Tools {
    static void msg(Activity activity, final String text) {
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_LONG).show());
    }

    static String getDirection(float b) {
        b -= 22.5;
        if (b <= 0 || b > 315) return "N";
        else if (b <= 45) return "NE";
        else if (b <= 90) return "E";
        else if (b <= 135) return "SE";
        else if (b <= 180) return "S";
        else if (b <= 225) return "SW";
        else if (b <= 270) return "W";
        else if (b <= 315) return "NW";
        else return "";
    }
}