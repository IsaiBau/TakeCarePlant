package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class DbHelper(context: Context) : SQLiteOpenHelper(context, DBNAME, null, DB_VERSION) {

    data class Plant(
        val id: Long? = null,
        val nombre: String,
        val desc: String,
        val defaultImage: String
    )
    companion object {
        private const val DBNAME = "App.db"
        private const val DB_VERSION = 17
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val queryPlants = (
                "CREATE TABLE PLANTS(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT, " +
                        "desc TEXT, " +
                        "default_image TEXT)"
                )
        db?.execSQL(queryPlants)

        val queryUserPlants = (
                "CREATE TABLE USER_PLANTS(" +
                        "user_email TEXT, " +
                        "plant_id INTEGER, " +
                        "nombre_planta TEXT, " +
                        "FOREIGN KEY(user_email) REFERENCES USERS(email), " +
                        "FOREIGN KEY(plant_id) REFERENCES PLANTS(id))"
                )
        db?.execSQL(queryUserPlants)

        insertSampleData(db)
    }

    private fun insertSampleData(db: SQLiteDatabase?) {
        val plantsToAdd = listOf(
            Plant(nombre = "Dracaena", desc = "Prueba", defaultImage = "Prueba")
        )

        for (plant in plantsToAdd) {
            val contentValues = ContentValues().apply {
                plant.id?.let { put("id", it) }
                put("nombre", plant.nombre)
                put("desc", plant.desc)
                put("default_image", plant.defaultImage)
            }

            val newRowId = db?.insert("PLANTS", null, contentValues)

            if (newRowId == -1L) {
                Log.e("Insertion", "Error inserting data into database for plant ${plant.nombre}")
            }
        }
    }
    fun addUserPlant(userEmail: String, plantId: Long?, nombrePlanta: String) {
        if (plantId == null) {
            Log.e("AddUserPlant", "Error: plantId is null")
            return
        }

        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("user_email", userEmail)
            put("plant_id", plantId) // Guardar el nombre de la planta
        }
        db.insert("USER_PLANTS", null, contentValues)
    }
    fun getAllPlants(): List<Plant> {
        val plants = mutableListOf<Plant>()
        val query = "SELECT * FROM PLANTS"
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id"))
                val nombre = it.getString(it.getColumnIndexOrThrow("nombre"))
                val desc = it.getString(it.getColumnIndexOrThrow("desc"))
                val defaultImage = it.getString(it.getColumnIndexOrThrow("default_image"))
                val plant = Plant(id, nombre, desc, defaultImage ?: "") // Proporcionar un valor predeterminado para defaultImage
                plants.add(plant)
            }
        }
        return plants
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}

//class DbHelper(context: Context): SQLiteOpenHelper(context, DBNAME, null, DB_VERSION) {
//
//    data class Plant(
//        val id: Long,
//        val nombre: String,
//        val desc: String,
//        val defaultImage: String
//    )
//
//    companion object {
//        private const val DBNAME = "App.db"
//        private const val DB_VERSION = 2
//    }
//
//    override fun onCreate(db: SQLiteDatabase?) {
//        val queryPlants = ("CREATE TABLE PLANTS(" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "nombre TEXT, " +
//                "desc TEXT, " +
//                "default_image TEXT)")
//        db?.execSQL(queryPlants)
//
//        val queryUserPlants = ("CREATE TABLE USER_PLANTS(" +
//                "user_email TEXT, " +
//                "plant_id INTEGER, " +
//                "nombre_planta TEXT, " +
//                "FOREIGN KEY(user_email) REFERENCES USERS(email), " +
//                "FOREIGN KEY(plant_id) REFERENCES PLANTS(id))")
//        db?.execSQL(queryUserPlants)
//
//        insertSampleData(db)
//
//    }
//
//    fun insertSampleData(db: SQLiteDatabase?) {
//        val db = writableDatabase
//        val plantsToAdd = listOf(
//            Plant(0, "Dracaena", "Prueba", "Prueba")
//        )
//
//        for (plant in plantsToAdd) {
//            val contentValues = ContentValues().apply {
//                put("nombre", plant.nombre)
//                put("desc", plant.desc)
//                put("default_image", plant.defaultImage)
//            }
//
//            val newRowId = db?.insert("PLANTS", null, contentValues)
//
//            if (newRowId == -1L) {
//                Log.e("Insertion", "Error inserting data into database for plant ${plant.nombre}")
//            }
//        }
//    }


//    private fun insertSamplePlants(db: SQLiteDatabase?) {
//        // No es necesario agregar el nombre de la planta aquí
//        val plantsToAdd = listOf(
//            Plant(0, "Dracaena", "Oficina", "Prueba", "isaisabi02@gmail.com", "aa")
//        )
//
//        for (plant in plantsToAdd) {
//            val contentValues = ContentValues().apply {
//                put("nombre_planta", plant.nombre)
//                put("desc", plant.desc)
//                put("user_email", plant.user_email)
//                put("default_image", plant.defaultImage)
//            }
//
//            val newRowId = db?.insert("PLANTS", null, contentValues)
//
//            if (newRowId == -1L) {
//                Log.e("Insertion", "Error inserting data into database for plant ${plant.nombre_planta}")
//            }
//        }
//    }
//
//    fun addUserPlant(userEmail: String, plantId: Long, nombrePlanta: String) {
//        val db = writableDatabase
//        val contentValues = ContentValues().apply {
//            put("user_email", userEmail)
//            put("plant_id", plantId) // Guardar el nombre de la planta
//        }
//        db.insert("USER_PLANTS", null, contentValues)
//    }
//
//    fun getAllPlants(): List<Plant> {
//        val plants = mutableListOf<Plant>()
//        val query = "SELECT * FROM PLANTS"
//        val db = readableDatabase
//        val cursor = db.rawQuery(query, null)
//        cursor.use {
//            while (it.moveToNext()) {
//                val id = it.getLong(it.getColumnIndexOrThrow("id"))
//                val nombre = it.getString(it.getColumnIndexOrThrow("nombre"))
//                val desc = it.getString(it.getColumnIndexOrThrow("desc"))
//                val defaultImage = it.getString(it.getColumnIndexOrThrow("default_image"))
//                val plant = Plant(id, nombre, desc, defaultImage?: "") // Proporcionar un valor predeterminado para defaultImage
//                plants.add(plant)
//            }
//        }
//        return plants
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        TODO("Not yet implemented")
//    }
//}