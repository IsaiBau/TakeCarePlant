package com.example.myapplication.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.myapplication.Model
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFormBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FormFragment : Fragment() {
    private lateinit var binding: FragmentFormBinding
    var uri: Uri? = null
    var firebaseStorage: FirebaseStorage? = null
    var firebaseDatabase: FirebaseDatabase? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFormBinding.inflate(inflater, container, false)

        binding.imgButton.setOnClickListener{
            Log.d(FormFragment.toString(), "Failed to read value.")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        val items = listOf("Dracaena", "Antirrhinum majus", "Aloe vera", "Ficus")
        val autoComplete : AutoCompleteTextView = binding.tipoPlanta
        val adapter = ArrayAdapter(activity?.applicationContext ?: requireContext(), R.layout.list_item, items)
        autoComplete.setAdapter(adapter)

        binding.buttonSave.setOnClickListener {
            if (uri != null) {
                subirImagen()
            } else {
                Toast.makeText(requireContext(), "Por favor seleccione una imagen", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentFormBinding.inflate(layoutInflater)
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            uri = data?.data
            Glide.with(requireContext()).load(uri).into(binding.image)
        }
    }
    private fun subirImagen(){
        val reference = firebaseStorage!!.reference.child("Images").child(System.currentTimeMillis().toString()+"")
        reference.putFile(uri!!).addOnSuccessListener {
            reference.downloadUrl.addOnSuccessListener { uri -> 
                val model = Model()
                model.image = uri.toString()
                firebaseDatabase!!.reference.child("Imagenes").push()
                    .setValue(model).addOnSuccessListener {
                        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
                    }
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}