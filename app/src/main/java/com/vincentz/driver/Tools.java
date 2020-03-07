package com.vincentz.driver;

import android.app.Activity;
import android.widget.Toast;

class Tools {
    static void msg(Activity activity, final String text) {
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_LONG).show());
    }
}