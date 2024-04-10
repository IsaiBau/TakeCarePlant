package com.example.myapplication.fragments

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.example.myapplication.DbHelper
import com.example.myapplication.Model
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFormBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FormFragment : Fragment() {

    private lateinit var binding: FragmentFormBinding
    private var uri: Uri? = null
    private  var isImageUpload = false
    private lateinit var dbHelper: DbHelper
    private lateinit var auth: FirebaseAuth

    var firebaseStorage: FirebaseStorage? = null
    var firebaseDatabase: FirebaseDatabase? = null
    private val imageViewModel: ImageViewModel by activityViewModels()
    companion object {
        private val IMAGE_PICK_CODE = 1000

        @JvmStatic
        fun newInstance(param1: String? = null, param2: String?= null) =
            FormFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    class ImageViewModel : ViewModel() {
        var imageUrl: String? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentFormBinding.inflate(layoutInflater)
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFormBinding.inflate(inflater, container, false)

        dbHelper = DbHelper(requireContext())
        var plants = dbHelper.getAllPlants()
        var plantNames = plants.map { it.nombre }
        val databaseReference = firebaseDatabase?.reference?.child("TakeCare")

        // Obtener los nombres de los campos Sensor desde la base de datos
        val sensorNames = mutableListOf<String>()
        databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (sensorSnapshot in dataSnapshot.children) {
                    val sensorName = sensorSnapshot.key?.takeIf { it.contains("Sensor") }
                        ?.let { sensorNames.add(it) }
                }
                // Configurar el adaptador del AutoCompleteTextView con los nombres de los campos Sensor
                val sensorAdapter = ArrayAdapter(requireContext(), R.layout.list_item, sensorNames)
                binding.sensor.setAdapter(sensorAdapter)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error si la lectura de datos falla
            }
        })

        imageViewModel.imageUrl?.let { imageUrl ->
            Glide.with(this)
                .load(imageUrl)
                .into(binding.image) // Ajusta esto según tu ImageView
        }

        binding.tipoPlanta.setOnItemClickListener { parent, view, position, id ->
            val plantaSeleccionada = parent.getItemAtPosition(position) as String
            cargarImagenDesdeSQLite(plantaSeleccionada)
        }

//        binding.imgButton.setOnClickListener {
//            isImageUpload = true
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, IMAGE_PICK_CODE)
//        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, plantNames)
        var autoComplete: AutoCompleteTextView = binding.tipoPlanta
        autoComplete.setAdapter(adapter)

        binding.buttonSave.setOnClickListener {
            val selectedPlant = plants.find { it.nombre == autoComplete.text.toString() }
            selectedPlant?.let { plant ->

                val nombrePlanta = binding.editTextNombrePlanta.text.toString()
                val sensor = binding.sensor.text.toString()
                val tipo = binding.tipoPlanta
                val tipoTexto = tipo.text.toString()
                val currentUser = auth.currentUser
                val currentUserEmail = currentUser?.email ?: ""

                dbHelper.addUserPlant(auth, currentUserEmail, plant.id, nombrePlanta, sensor, tipoTexto)
                Toast.makeText(
                    requireContext(),
                    "Planta guardada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                binding.editTextNombrePlanta.text!!.clear()
                binding.sensor.text.clear()
                autoComplete.setText("")

            } ?: run {
                Toast.makeText(
                    requireContext(),
                    "Por favor selecciona una planta válida",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

        private fun loadImageFromUri(uri: Uri?) {
            uri?.let {
                Glide.with(this)
                    .load(uri)
                    .into(binding.image)
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
                uri = data?.data
                uri?.let {
                    imageViewModel.imageUrl = it.toString()
                    loadImageFromUri(it)
                }
                Glide.with(requireContext()).load(uri).into(binding.image)
            }
        }

        private fun clearImageView() {
            Glide.with(requireContext())
                .load(null as Uri?)
                .into(binding.image)
        }

        private fun subirImagen() {
            clearImageView()
            //binding.image.visibility = View.GONE
            val reference = firebaseStorage!!.reference.child("Images")
                .child(System.currentTimeMillis().toString() + "")
            reference.putFile(uri!!).addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener { uri ->
                    val model = Model()
                    model.image = uri.toString()
                    firebaseDatabase!!.reference.child("Imagenes").push()
                        .setValue(model).addOnSuccessListener {
                            requireActivity().supportFragmentManager.beginTransaction().remove(this)
                                .commit()
                        }
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

    private fun cargarImagenDesdeSQLite(nombrePlanta: String) {
        val plants = dbHelper.getAllPlants()
        val planta = plants.find { it.nombre == nombrePlanta }

        planta?.let { plant ->
            val bitmap = BitmapFactory.decodeByteArray(plant.defaultImage, 0, plant.defaultImage.size)
            binding.image.setImageBitmap(bitmap)
        }
    }

}