package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityMainLoginBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivityLogin : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_login)
        binding = ActivityMainLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail!!.text.toString()
            val pass = binding.editTextPassword.text.toString()
            //val password = binding.editTextPassword.text.toString()
            //login(email, password)
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        // Autenticación exitosa, obtén el ID del usuario
                        val firebaseUser = firebaseAuth.currentUser
                        val idUsuario = firebaseUser?.uid

                        if (idUsuario != null) {
                            // Usuario autenticado, haz lo que necesites con su ID
                            Log.d("MainActivity", "ID de usuario: $idUsuario")
                            val nombreUsu = firebaseUser.displayName ?: "Nombre de usuario por defecto"
                            val preferences = getSharedPreferences("user_data", MODE_PRIVATE)
                            val editor = preferences.edit()
                            editor.putString("user_name", nombreUsu)
                            editor.putString("user_id", idUsuario)
                            editor.apply()

                            // Continuar con la lógica de tu aplicación
                            Toast.makeText(this, "INICIASTE SESION", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java).apply {
                                putExtra("ID_USUARIO", idUsuario)
                                putExtra("nombre", nombreUsu)
                            }
                            startActivity(intent)
                            finish()

                        } else {
                            // No se pudo obtener el ID del usuario
                            Log.e("MainActivity", "No se pudo obtener el ID del usuario")
                        }
                    }
                    else {
                        Toast.makeText(this, "El usuario o contraseña es incorrecto", Toast.LENGTH_SHORT).show()
                        // Error en la autenticación
                        //Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    }}
//            val user = binding.editTextName.text.toString()
//            val psw = binding.editTextPassword.text.toString()
//            login(user, psw)
//            if (user.isNotEmpty() && psw.isNotEmpty()){
//
//            } else {
//                Toast.makeText(this, "Los campos no pueden estar vacios", Toast.LENGTH_SHORT).show()
//            }
            } else {
                Toast.makeText(this, "Campos vacios", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, MainActivityRegister::class.java)
            startActivity(intent)
            finish()
        }


    }
}