package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityMainFormBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class MainActivityForm : AppCompatActivity() {
    private lateinit var binding: ActivityMainFormBinding
    var uri: Uri? = null
    var firebaseStorage: FirebaseStorage? = null
    var firebaseDatabase: FirebaseDatabase? = null

//

    companion object {
        private val IMAGE_PICK_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_form)

        binding = ActivityMainFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = listOf("Dracaena", "Antirrhinum majus", "Aloe vera", "Ficus")

        val autoComplete : AutoCompleteTextView = findViewById(R.id.tipoPlanta)

        val adapter = ArrayAdapter(this,R.layout.list_item,items)

        autoComplete.setAdapter(adapter)


        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        binding.imgButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        binding.buttonSee.setOnClickListener{
            val intent = Intent(this, MainActivityPlantas::class.java)
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
                        finish()
                    }
                    .addOnSuccessListener {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
