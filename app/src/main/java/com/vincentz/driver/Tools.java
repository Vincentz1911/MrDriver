package com.vincentz.driver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
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
    public static TextToSpeech TTS;
    static boolean[] PERMISSIONS;

    public static void msg(Activity activity, final String text) {
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_LONG).show());
    }

    public static void speakWords(String speech) {
        TTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null);
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

    //PICKS ICON DEPENDING ON ICON CODE IN JSONRESPONSE
    public static Drawable getWeatherIcon(Activity act, String icon) {
        switch (icon) {
            case "01d":
                return act.getResources().getDrawable(R.drawable.wic_01d_day_clear, null);
            case "01n":
                return act.getResources().getDrawable(R.drawable.wic_01n_night_clear, null);
            case "02d":
                return act.getResources().getDrawable(R.drawable.wic_02d_day_clear, null);
            case "02n":
                return act.getResources().getDrawable(R.drawable.wic_02n_night_clear, null);
            case "03d":
                return act.getResources().getDrawable(R.drawable.wic_03d_day_partial_cloud, null);
            case "03n":
                return act.getResources().getDrawable(R.drawable.wic_03n_night_partial_cloud, null);
            case "04d":
                return act.getResources().getDrawable(R.drawable.wic_04_cloudy, null);
            case "04n":
                return act.getResources().getDrawable(R.drawable.wic_04_cloudy, null);
            case "09d":
                return act.getResources().getDrawable(R.drawable.wic_09_rain, null);
            case "09n":
                return act.getResources().getDrawable(R.drawable.wic_09_rain, null);
            case "10d":
                return act.getResources().getDrawable(R.drawable.wic_10d_day_rain, null);
            case "10n":
                return act.getResources().getDrawable(R.drawable.wic_10n_night_rain, null);
            case "11d":
                return act.getResources().getDrawable(R.drawable.wic_11d_rain_thunder, null);
            case "11n":
                return act.getResources().getDrawable(R.drawable.wic_11d_rain_thunder, null);
            case "13d":
                return act.getResources().getDrawable(R.drawable.wic_13_snow, null);
            case "13n":
                return act.getResources().getDrawable(R.drawable.wic_13_snow, null);
            case "50d":
                return act.getResources().getDrawable(R.drawable.wic_50_fog, null);
            case "50n":
                return act.getResources().getDrawable(R.drawable.wic_50_fog, null);
            default:
                return act.getResources().getDrawable(R.drawable.wic_11d_day_rain_thunder, null);
        }
    }

    public static String getWindDescription(double wind) {
        if (wind < 0.3) return "Calm";
        if (wind < 1.5) return "Light air";
        if (wind < 3.3) return "Light breeze";
        if (wind < 5.5) return "Gentle breeze";
        if (wind < 8.0) return "Moderate breeze";
        if (wind < 10.8) return "Fresh breeze";
        if (wind < 13.9) return "Strong breeze";
        if (wind < 17.2) return "High wind";
        if (wind < 20.7) return "Fresh Gale";
        if (wind < 24.5) return "Strong Gale";
        if (wind < 28.4) return "Storm";
        if (wind < 32.6) return "Violent storm";
        return "Hurricane";
    }
}