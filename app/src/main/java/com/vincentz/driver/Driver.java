package com.vincentz.driver;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import static com.vincentz.driver.Tools.*;

public class Driver {

    public void checkDriver(Context ctx) {
        int driverID = IO.getInt("Driver", 0);
        if (driverID != 0) getDriver(ctx, driverID);
        else createDriver(ctx);
    }

    void getDriver(Context ctx, int driverID) {
        String url = "https://vincentz.tk/api/driver/id/" + driverID;
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    DRV = new Gson().fromJson(String.valueOf(response), DriverModel.class);
                    say("Welcome " + DRV.driver);
                },
                error -> msg(ctx, "Volley Load Driver Error")));
    }

    void createDriver(Context ctx) {
        String url = "https://vincentz.tk/api/driver/new";
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    DRV = new Gson().fromJson(String.valueOf(response), DriverModel.class);
                    IO.edit().putInt("Driver", DRV.id).apply();
                    say("Welcome Driver " + DRV.id);
                },
                error -> msg(ctx, "Volley Create Driver Error")));
    }
}
