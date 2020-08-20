package com.vincentz.driver.weather;

public class WeatherHourlyModel { //"current":
    int dt; //"dt": 1596449514,
    long sunrise; //"sunrise": 1596424817,
    long sunset; // "sunset": 1596481995,
    double temp; //"temp": 291.56,
    double feels_like; //"feels_like": 289.85,
    int pressure; //"pressure": 1012,
    int humidity; //"humidity": 59,
    double dew_point; //"dew_point": 283.41,
    double uvi; //"uvi": 5.01,
    int clouds; //"clouds": 39,
    int visibility; //"visibility": 10000,
    double wind_speed; //"wind_speed": 2.6,
    int wind_deg; //"wind_deg": 280,
    double pop; //"pop": 0
    WeatherDescriptionModel[] weather;
    RainModel rain = new RainModel(); //    "rain": {"1h": 2.93}


}
