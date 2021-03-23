package com.example.covidapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.robinhood.spark.SparkView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val BASE_URL = "https://covidtracking.com/api/v1/"
private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var perStateDailyData: Map<String, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>
    private lateinit var tvMetricLabel : TextView
    private lateinit var tvDateLabel : TextView
    private lateinit var rbPositive : RadioButton
    private lateinit var rbMax : RadioButton
    private lateinit var sparkView: SparkView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvMetricLabel = findViewById(R.id.tvMetricLabel)
        tvDateLabel = findViewById(R.id.tvDateLabel)
        rbPositive = findViewById(R.id.radioButtonPositive)
        rbMax = findViewById(R.id.radioButtonMax)
        sparkView = findViewById(R.id.sparkView)

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        val covidService = retrofit.create(CovidService::class.java)
        //fetch the national data
        covidService.getNationalDataUs().enqueue(object : Callback<List<CovidData>>{
            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

            override fun onResponse(call: Call<List<CovidData>>, response: Response<List<CovidData>>) {
                Log.i(TAG, "onResponse $response")
                val nationalData = response.body()
                if (nationalData == null){
                    Log.w(TAG, "Did not receive a valid response body")
                    return
                }
                nationalDailyData = nationalData.reversed()
                Log.i(TAG, "update graph with national data")
                updateDisplayWithData(nationalDailyData)
            }
        })

        //fetch the state data
        covidService.getStatesData().enqueue(object : Callback<List<CovidData>>{
            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

            override fun onResponse(call: Call<List<CovidData>>, response: Response<List<CovidData>>) {
                Log.i(TAG, "onResponse $response")
                val statesData = response.body()
                if (statesData == null){
                    Log.w(TAG, "Did not receive a valid response body")
                    return
                }
                perStateDailyData = statesData.reversed().groupBy {it.state}
                Log.i(TAG, "update spinner with state names")
            }
        })
    }

    private fun updateDisplayWithData(dailyData: List<CovidData>) {
        val adapter = CovidSparkAdapter(dailyData)
        sparkView.adapter = adapter
        rbPositive.isChecked = true
        rbMax.isChecked = true
        updateInfoForDate(dailyData.last())
    }

    private fun updateInfoForDate(covidData: CovidData) {
        tvMetricLabel.text = NumberFormat.getInstance().format(covidData.positiveIncrease)
        val outputDateFormat = SimpleDateFormat("MM dd, yyyyy", Locale.US)
        tvDateLabel.text = outputDateFormat.format(covidData.dateChecked)
    }
}