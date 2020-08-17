package com.vincentz.driver.weather;

import java.util.ArrayList;

public class WeatherModel {

    WeatherHourlyModel current;
    ArrayList<WeatherHourlyModel> hourlyList;
    ArrayList<WeatherDailyModel> dailyList;

    public WeatherModel(WeatherHourlyModel current, ArrayList<WeatherHourlyModel> hourlyList, ArrayList<WeatherDailyModel> dailyList) {
        this.current = current;
        this.hourlyList = hourlyList;
        this.dailyList = dailyList;
    }

//    	            "lat": 55.73,
//                "lon": 12.34,
//                "timezone": "Europe/Copenhagen",
//                "timezone_offset": 7200,

//    int id; //"id": 802,
//    String main; //"main": "Clouds",
//    String description; //"description": "scattered clouds",
//    String icon; //"icon": "03d"
}