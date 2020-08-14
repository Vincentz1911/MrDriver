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


//    int id; //"id": 802,
//    String main; //"main": "Clouds",
//    String description; //"description": "scattered clouds",
//    String icon; //"icon": "03d"
}