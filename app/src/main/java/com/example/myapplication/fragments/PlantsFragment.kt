package com.example.myapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.PlantsAdapter
import com.example.myapplication.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlantsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlantsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView: RecyclerView
    private var plantNames: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        obtenerNombresDePlantasDesdeFirebase()
        configurarAdapter(plantNames)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plants, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Llama a la función para obtener los nombres de las plantas
        obtenerNombresDePlantasDesdeFirebase()
        // Inflate the layout for this fragment
        return view
    }
    private fun obtenerNombresDePlantasDesdeFirebase() {
        // Obtener la instancia de Firebase Authentication
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let {
            val userId = it.uid // Obtener la ID del usuario en sesión

            val databaseReference = FirebaseDatabase.getInstance().reference
            val userPlantsRef = databaseReference.child("users").child(userId).child("plants")

            userPlantsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val plantNames = mutableListOf<String>()

                    for (plantSnapshot in dataSnapshot.children) {
                        val nombrePlanta = plantSnapshot.child("nombrePlanta").getValue(String::class.java)
                        nombrePlanta?.let { plantNames.add(it) }
                    }

                    // Una vez que hayas obtenido todos los nombres, configura el adaptador
                    configurarAdapter(plantNames)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar el error si la lectura de datos falla
                }
            })
        }
    }

    private fun configurarAdapter(plantNames: List<String>) {
        val adapter = PlantsAdapter(plantNames)
        recyclerView.adapter = adapter
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlantsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String? = null, param2: String? = null) =
            PlantsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}