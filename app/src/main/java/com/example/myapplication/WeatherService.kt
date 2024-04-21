package com.example.myapplication

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): WeatherData

    // New function to get weather by coordinates
    @GET("weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): WeatherData
}


//interface WeatherService {
//    @GET("weather")
//    suspend fun getWeather(
//        @Query("q") city: String,
//        @Query("appid") apiKey: String
//    ): WeatherData
//}
