package com.vincentz.driver;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

class Tools {
    static SimpleDateFormat dateFormat = new SimpleDateFormat(
            "EEEE dd/M-yy - w", Locale.getDefault());
    static SimpleDateFormat timeFormat = new SimpleDateFormat(
            "HH:mm:ss", Locale.getDefault());

    static Location location, lastLocation;

    static void msg(final Context context, final String text) {
        ((Activity) context).runOnUiThread(() ->
                Toast.makeText(context, text, Toast.LENGTH_LONG).show());
    }

    static void checkPermissions(Activity activity) {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.RECORD_AUDIO,
        };

        if (!hasPermissions(activity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    private static boolean hasPermissions(Activity activity, String... permissions) {
        if (activity != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    static void getLocation(Activity a) {
        if (a.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && a.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Tools.checkPermissions(a);
            return;
        }
        //Checks if GPS is on, sets a Listener and if there is a last position move camera
        LocationManager lm = (LocationManager) a.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GPSlistener);
        }
    }


    private static LocationListener GPSlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location onChanged) {
            location = onChanged;
            Log.d("Location ", "Time " + new Date());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}