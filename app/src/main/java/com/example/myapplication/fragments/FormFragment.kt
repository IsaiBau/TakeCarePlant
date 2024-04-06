package com.example.myapplication.fragments

import android.app.Activity
import android.content.ContentValues
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
import com.example.myapplication.MainActivityForm
import com.example.myapplication.MainActivityPlantas
import com.example.myapplication.Model
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainFormBinding
import com.example.myapplication.databinding.FragmentFormBinding
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FormFragment : Fragment() {
    private lateinit var binding: FragmentFormBinding
    var uri: Uri? = null
    var firebaseStorage: FirebaseStorage? = null
    var firebaseDatabase: FirebaseDatabase? = null
    
    companion object {

        private val IMAGE_PICK_CODE = 1000
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FormFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
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
        // Inflate the layout for this fragment
        binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentFormBinding.inflate(layoutInflater)

        val items = listOf("Dracaena", "Antirrhinum majus", "Aloe vera", "Ficus")

        val autoComplete : AutoCompleteTextView = binding.tipoPlanta

        val adapter = ArrayAdapter(requireContext(),R.layout.list_item,items)

        autoComplete.setAdapter(adapter)


        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        binding.imgButton.setOnClickListener{
            Log.d(FormFragment.toString(), "Failed to read value.")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        binding.buttonSee.setOnClickListener{
            val intent = Intent(requireContext(), MainActivityPlantas::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            uri = data?.data
            subirImagen()
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