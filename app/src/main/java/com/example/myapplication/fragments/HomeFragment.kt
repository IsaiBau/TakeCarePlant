package com.example.myapplication.fragments

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
//import com.example.myapplication.MainActivityHome
import com.example.myapplication.WeatherService
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale
import kotlin.math.roundToInt
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private val LOCATION_PERMISSION_REQUEST_CODE = 1001

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var binding: FragmentHomeBinding
    private val apiKey = "88882643d87ed372860fbcb34135baef"
    private lateinit var weatherService: WeatherService
    private lateinit var databaseReference: DatabaseReference

    companion object{
        const val MY_CHANNEL_ID = "myChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)

//        Firebase

        val database = Firebase.database
        val myRef = database.getReference("TakeCare/Sensor")
        val txtHumedad = binding.TextViewPorcentajeHumedad

//         Inicializar la referencia a la base de datos de Firebase
        databaseReference = FirebaseDatabase.getInstance().reference

        myRef.addValueEventListener(object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Long::class.java)
                txtHumedad.text = "${value.toString()} %"
                Log.d(ContentValues.TAG, "Value is: $value")

                if (value != null && value < 40) {
                    sendNotification()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

//        Api
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = Firebase.database
        val myRef = database.getReference("TakeCare/Sensor")
        val myRefPlants = database.getReference("users/6d0qczfrMHaeUz1HhxPYiIpoxXS2/plants/-NvO1xOd9zWFqTZ7ifBg/nombrePlanta")
        val txtHumedad = binding.TextViewPorcentajeHumedad
        val txtNombrePlanta = binding.TextViewNombrePlanta
        val txtNombre = binding.nombre

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Long::class.java)
                txtHumedad.text = "${value.toString()} %"
                Log.d(ContentValues.TAG, "Value is: $value")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

        myRefPlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                txtNombre.text = value.toString()
                Log.d(ContentValues.TAG, "Value plant is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherService::class.java)

        getWeatherData()
    }

    private fun getWeatherData() {
        val textViewClima = binding.TextViewClima
        val textViewCiudad = binding.TextViewCiudad
        lifecycleScope.launch {
            try {
                // Verifica si el permiso de ubicación está habilitado
                if (checkLocationPermission()) {
                    val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    // Si la ubicación se obtiene correctamente
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val response = weatherService.getWeatherByCoordinates(latitude, longitude, apiKey)
                        val tempInCelsius = response.main.temp - 273.15
                        val roundedTemp = tempInCelsius.roundToInt()
                        val cityName = response.name

                        // Update the textViewClima with the temperature
                        textViewClima.text = "$roundedTemp°C"
                        textViewCiudad.text = cityName

                        Log.d("Temperatura", "Temperatura is: $roundedTemp")
                    } else {
                        // Maneja el caso en que no se pueda obtener la ubicación actual
                        Log.d("MainActivityHome", "No se pudo obtener la ubicación actual")
                    }
                } else {
                    // Solicita el permiso de ubicación al usuario
                    requestLocationPermission()
                }
            } catch (e: Exception) {
                Log.e("MainActivityHome", "Error: ", e)
            }
        }
    }

    fun sendNotification() {

        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fromNotification", true)
        }

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, flag)

        val notification = NotificationCompat.Builder(requireContext(), MY_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Warning!")
            .setContentText("El nivel de humedad es bajo")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }


    // Función para verificar el permiso de ubicación
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Función para solicitar el permiso de ubicación
    private fun requestLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
    }

    // Código para manejar la respuesta del permiso de ubicación (se agrega en tu Activity o Fragment)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado, procede a obtener la ubicación
                getWeatherData()
            } else {
                // Permiso denegado, maneja el caso en que el usuario no quiera dar el permiso
                Log.d("MainActivityHome", "Permiso de ubicación denegado")
            }
        }
    }
}