package com.vincentz.driver;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.vincentz.driver.navigation.LocationModel;

import java.nio.charset.StandardCharsets;

import static com.vincentz.driver.Tools.*;

public class Driver {

    public static void checkDriver(Context ctx) {
        String driver = IO.getString("Driver", "");
        String password = IO.getString("Password", "");
        if (driver != "") getDriver(ctx, driver, password);
        //else createDriver(ctx);
    }

    void getDriverold(Context ctx, String driver, String password) {
        String url = "https://vincentz.tk/home/" + driver + "&" + password;
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    DRV = new Gson().fromJson(String.valueOf(response), UserModel.class);
                    say("Welcome " + DRV.username);
                },
                error -> msg(ctx, "Volley Load Driver Error")));
    }

    void createDriver(Context ctx) {
        String url = "https://vincentz.tk/api/driver/new";
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    DRV = new Gson().fromJson(String.valueOf(response), UserModel.class);
                    IO.edit().putString("Driver", DRV.username).apply();
                    say("Welcome Driver " + DRV.username);
                },
                error -> msg(ctx, "Volley Create Driver Error")));
    }

    private static void getDriver(Context ctx, String username, String password) {
        String URL = "https://vincentz.tk/driver";
        UserModel driver = new UserModel();
        driver.username = username;
        driver.password = password;

        final String requestBody = new Gson().toJson(driver);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
            Log.i("VOLLEY", response);
                },
                error -> Log.e("VOLLEY", error.toString())) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                assert response != null;
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        RQ.add(stringRequest);
    }
}
