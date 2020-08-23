package com.vincentz.driver;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import static com.vincentz.driver.Tools.*;

public class Driver {

    Activity act;
    public Driver(Activity activity) { act = activity; }

    public void checkDriver() {
        int driverID = IO.getInt("Driver", 0);
        if (driverID != 0) getDriver(driverID); else createDriver();
    }

    void getDriver(int driverID) {
        String url = "http://vincentz.tk/api/driver/id/" + driverID;
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    DRV = new Gson().fromJson(String.valueOf(response), DriverModel.class);
                    say("Welcome " + DRV.driver);
                },
                error -> msg(act, "Volley Load Driver Error")));
    }

    void createDriver() {
        String url = "http://vincentz.tk/api/driver/new";
        RQ.add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    DRV = new Gson().fromJson(String.valueOf(response), DriverModel.class);
                    IO.edit().putInt("Driver", DRV.id).apply();
                    say("Welcome Driver " + DRV.id);
                },
                error -> msg(act, "Volley Create Driver Error")));
    }


//    private void createDriver() {
//        String URL = "http://vincentz.tk/api/driver";
//        final String requestBody = new Gson().toJson();
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
//                response -> Log.i("VOLLEY", response),
//                error -> Log.e("VOLLEY", error.toString())) {
//            @Override
//            public String getBodyContentType() { return "application/json; charset=utf-8"; }
//
//            @Override
//            public byte[] getBody() { return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8); }
//
//            @Override
//            protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                String responseString = "";
//                if (response != null) {
//                    responseString = String.valueOf(response.statusCode);
//                }
//                assert response != null;
//                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
//            }
//        };
//        RQ.add(stringRequest);
//    }


}
