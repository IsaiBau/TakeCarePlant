package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivityRegister : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityMainRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_register)

        //binding
        binding = ActivityMainRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, MainActivityLogin::class.java)
            startActivity(intent)
            finish()
        }

        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val pass = binding.editTextPassword.text.toString()
            val passConfirm = binding.editTextPasswordConfirm.text.toString()
            if(email.isNotEmpty() && pass.isNotEmpty() && passConfirm.isNotEmpty()){
                if(pass == passConfirm){
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if(it.isSuccessful){
                            //userRegister(username, password, apellidos, numero, email)
                            val dbRef = database.reference.child("users").child(firebaseAuth.currentUser!!.uid)
                            val users: Users = Users(email, firebaseAuth.currentUser!!.uid)
                            dbRef.setValue(users).addOnCompleteListener{
                                if(it.isSuccessful){
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Los campos no pueden estar vacios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i("datoregister","On start")
    }

    override fun onResume() {
        super.onResume()
        Log.i("datoregister","On resume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("datoregister","On pause")
    }

    override fun onStop() {
        super.onStop()
        Log.i("datoregister","On stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("datoregister","On destroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("datoregister","On restart")
    }
}