package com.example.covidapp

import retrofit2.Call
import retrofit2.http.GET

interface CovidService {
    @GET("us/daily.json")
    fun getNationalDataUs() : Call<List<CovidData>>

    @GET("states/daily.json")
    fun getStatesData(): Call<List<CovidData>>
}