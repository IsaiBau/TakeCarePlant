package com.example.myapplication

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class MainActivityHome : AppCompatActivity() {
    private val apiKey = "88882643d87ed372860fbcb34135baef"
    private lateinit var weatherService: WeatherService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_home)

//        Firebase

        val database = Firebase.database
        val myRef = database.getReference("TakeCare/Sensor")
        val txtHumedad = findViewById<TextView>(R.id.TextViewPorcentajeHumedad)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Long::class.java)
                txtHumedad.setText(value.toString() + " %")
                Log.d(TAG, "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

//        Api
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherService::class.java)

        getWeatherData()
    }

    private fun getWeatherData() {

        val textViewClima = findViewById<TextView>(R.id.TextViewClima)
        lifecycleScope.launch {
            try {
                val response = weatherService.getWeather("Villahermosa", apiKey)
                val tempInCelsius = response.main.temp - 273.15
                val roundedTemp = tempInCelsius.roundToInt()
                textViewClima.text = "$roundedTemp°C"
                Log.d("MainActivityHome", "Response: $response")
            } catch (e: Exception) {
                Log.e("MainActivityHome", "Error: ", e)
            }
        }
    }
}