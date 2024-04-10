package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DbHelper(context: Context): SQLiteOpenHelper(context, DBNAME, null, DB_VERSION) {

    data class Plant(
        val nombre: String,
        val planta: String,
        val desc: String,
        val user_email: String,
        val defaultImage: String
    )

    companion object {
        private const val DBNAME = "App.db"
        private const val DB_VERSION = 5
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val queryPlants = ("CREATE TABLE PLANTS(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "nombre_planta TEXT, " +
                "desc TEXT, " +
                "default_image TEXT)")
        db?.execSQL(queryPlants)

        val queryUserPlants = ("CREATE TABLE USER_PLANTS(" +
                "user_email TEXT, " +
                "plant_id INTEGER, " +
                "FOREIGN KEY(user_email) REFERENCES USERS(email), " +
                "FOREIGN KEY(plant_id) REFERENCES PLANTS(id))")
        db?.execSQL(queryUserPlants)

        insertSamplePlants(db)
    }


    private fun insertSamplePlants(db: SQLiteDatabase?) {
        // Crear una lista de plantas que deseas agregar
        val plantsToAdd = listOf(
            Plant("Dracaena", "Especie 1", "Desc", "isaisabi02@gmail.com", "a" ),
        )

        // Iterar sobre la lista de plantas y agregar cada una a la base de datos
        for (plant in plantsToAdd) {
            val contentValues = ContentValues()
            contentValues.put("nombre", plant.nombre)
            contentValues.put("nombre_planta", plant.planta)
            contentValues.put("desc", plant.desc)
            contentValues.put("user_email", plant.user_email)
            contentValues.put("default_image", plant.defaultImage)

            val newRowId = db?.insert("PLANTS", null, contentValues)

            if (newRowId == -1L) {
                Log.e("Insertion", "Error inserting data into database for plant ${plant.nombre}")
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}