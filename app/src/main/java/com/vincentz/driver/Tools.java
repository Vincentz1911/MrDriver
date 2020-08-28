package com.vincentz.driver;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.android.volley.RequestQueue;
import com.vincentz.driver.navigation.GPSLocationModel;
import com.vincentz.driver.navigation.LocationModel;
import com.vincentz.driver.navigation.NavigationModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Tools {
    public static RequestQueue RQ;
    public static SharedPreferences IO;
    public static TextToSpeech TTS;
    public static GPSLocationModel LOC;
    public static DriverModel DRV;
    public static LocationModel DEST;
    public static NavigationModel NAV;

    static boolean[] PERMISSIONS;

    public static List<Locale> LANG = new ArrayList<>(Arrays.asList(Locale.ENGLISH,
            Locale.GERMAN, Locale.FRENCH, Locale.ITALIAN));

    public static String formatDate(String format, Date date) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public static void msg(Context ctx, final String text) {
        ((Activity) ctx).runOnUiThread(() -> Toast.makeText(ctx, text, Toast.LENGTH_LONG).show());
    }

    public static void say(String speech) {
        TTS.speak(speech, TextToSpeech.QUEUE_ADD, null, null);
    }

    public static Drawable getDrawable(Activity act, int id) {
        return ResourcesCompat.getDrawable(act.getResources(), id, null);
    }

    public static void fullscreen(Activity act) {
        act.getWindow().getDecorView().setSystemUiVisibility(
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

    //PICKS ICON DEPENDING ON ICON CODE IN JSONRESPONSE
    public static Drawable getWeatherIcon(Activity act, String icon) {
        switch (icon) {
            case "01d":
                return getDrawable(act, R.drawable.wic_01d_day_clear);
            case "01n":
                return getDrawable(act, R.drawable.wic_01n_night_clear);
            case "02d":
                return getDrawable(act, R.drawable.wic_02d_day_clear);
            case "02n":
                return getDrawable(act, R.drawable.wic_02n_night_clear);
            case "03d":
                return getDrawable(act, R.drawable.wic_03d_day_partial_cloud);
            case "03n":
                return getDrawable(act, R.drawable.wic_03n_night_partial_cloud);
            case "04d":
                return getDrawable(act, R.drawable.wic_04_cloudy);
            case "04n":
                return getDrawable(act, R.drawable.wic_04_cloudy);
            case "09d":
                return getDrawable(act, R.drawable.wic_09_rain);
            case "09n":
                return getDrawable(act, R.drawable.wic_09_rain);
            case "10d":
                return getDrawable(act, R.drawable.wic_10d_day_rain);
            case "10n":
                return getDrawable(act, R.drawable.wic_10n_night_rain);
            case "11d":
                return getDrawable(act, R.drawable.wic_11d_rain_thunder);
            case "11n":
                return getDrawable(act, R.drawable.wic_11d_rain_thunder);
            case "13d":
                return getDrawable(act, R.drawable.wic_13_snow);
            case "13n":
                return getDrawable(act, R.drawable.wic_13_snow);
            case "50d":
                return getDrawable(act, R.drawable.wic_50_fog);
            case "50n":
                return getDrawable(act, R.drawable.wic_50_fog);
            default:
                return getDrawable(act, R.drawable.wic_11d_day_rain_thunder);
        }
    }
}