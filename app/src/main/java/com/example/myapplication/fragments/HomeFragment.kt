package com.example.myapplication.fragments

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
//import com.example.myapplication.MainActivityHome
import com.example.myapplication.WeatherService
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)

//        Firebase

        val database = Firebase.database
        val myRef = database.getReference("TakeCare/Sensor")
        val txtHumedad = binding.TextViewPorcentajeHumedad

        // Inicializar la referencia a la base de datos de Firebase
        databaseReference = FirebaseDatabase.getInstance().reference

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Long::class.java)
                txtHumedad.setText(value.toString() + " %")
                Log.d(ContentValues.TAG, "Value is: $value")
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

        getWeatherData()
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
        val myRefPlants = database.getReference("users/6d0qczfrMHaeUz1HhxPYiIpoxXS2/plants/-Nv7PDzDVplKmePGgysr/nombrePlanta")
        val txtHumedad = binding.TextViewPorcentajeHumedad
        val txtNombrePlanta = binding.TextViewNombrePlanta
        val txtNombre = binding.nombre
        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Long::class.java)
                txtHumedad.text = "${value.toString()} %"
                if (value != null) {
                    if (value > 45) {
                        // Si el valor del sensor es mayor a 45%, cambia el color del texto a rojo
                        binding.TextViewEstado.setTextColor(resources.getColor(R.color.BluePrincipal))
                        binding.TextViewEstado.text = "HÚMEDO"
                        binding.leyenda.setText("NO NECESITA RIEGO")
                    } else {
                        // Si el valor del sensor es menor o igual a 45%, utiliza el color por defecto y el estado "Seco"
                        binding.TextViewEstado.setTextColor(resources.getColor(R.color.Seco))
                        binding.TextViewEstado.text = "SECO"
                        binding.leyenda.setText("NECESITA RIEGO URGENTE")
                    }
                }
                Log.d(ContentValues.TAG, "Value is: $value")
            } override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

        currentUser?.let { user ->
            val userId = user.uid

            // Referencia a la última planta del usuario actual
            val lastUserPlantRef = FirebaseDatabase.getInstance().reference
                .child("users").child(userId).child("plants").limitToLast(1)

            lastUserPlantRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Verifica si hay datos en la referencia de la última planta
                    if (dataSnapshot.exists()) {
                        for (plantSnapshot in dataSnapshot.children) {
                            // Obtén los datos de la última planta
                            val nombrePlanta = plantSnapshot.child("nombrePlanta").getValue(String::class.java)
                            // Actualiza la interfaz de usuario con los datos de la última planta
                            txtNombre.text = nombrePlanta
                        }
                    } else {
                        binding.buttonRegar.visibility = View.VISIBLE
                        binding.buttonRegar.setOnClickListener {
                            // Navega al fragmento de formulario
                            navigateToFormFragment()
                        }
                        binding.TextViewNombrePlanta.setText("")
                        binding.leyenda.setText("AGREGA TU PROPIA PLANTA")
                        binding.TextViewEstado.setText("SIN DATOS")
                        // Maneja el caso en el que no hay ninguna planta para este usuario
                        Log.d(TAG, "No se encontraron plantas para este usuario.")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar el error si la lectura de datos falla
                    Log.w(TAG, "Error al leer datos: ", databaseError.toException())
                }
            })
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherService::class.java)

        getWeatherData()

    }
    private fun navigateToFormFragment() {
        // Crea una instancia del fragmento de formulario
        val formFragment = FormFragment()

        // Reemplaza el fragmento actual por el fragmento de formulario
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fl_wrapper, formFragment)
            .addToBackStack(null) // Opcional: permite navegar hacia atrás
            .commit()
    }
    private fun getWeatherData() {
        val textViewClima = binding.TextViewClima
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