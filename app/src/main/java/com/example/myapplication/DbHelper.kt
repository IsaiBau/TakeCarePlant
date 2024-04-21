package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.AutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class DbHelper(private val context: Context) : SQLiteOpenHelper(context, DBNAME, null, DB_VERSION) {
    data class Plant(
        val id: Long? = null,
        val nombre: String,
        val desc: String,
        val defaultImage: ByteArray
    )
    companion object {
        private const val DBNAME = "App.db"
        private const val DB_VERSION = 21
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val queryPlants = (
                "CREATE TABLE PLANTS(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT, " +
                        "desc TEXT, " +
                        "default_image BLOB)"
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
        insertSampleData(db, context)
    }

    private fun insertSampleData(db: SQLiteDatabase?, context: Context) {
        val plantsToAdd = listOf(
            Plant(nombre = "Dracaena", desc = "Prueba", defaultImage = getImageAsByteArray(context, R.drawable.planta_ejemplo2)),
            Plant(nombre = "Aloe Vera", desc = "Prueba", defaultImage = getImageAsByteArray(context, R.drawable.planta_ejemplo2))
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

//    private fun insertSampleData(db: SQLiteDatabase?) {
//        val plantsToAdd = listOf(
//            Plant(nombre = "Dracaena", desc = "Prueba", defaultImage = getImageAsByteArray(context, R.drawable.dracaena))
//        )
//
//        for (plant in plantsToAdd) {
//            val contentValues = ContentValues().apply {
//                plant.id?.let { put("id", it) }
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

    fun addUserPlant(auth: FirebaseAuth, userEmail: String, plantId: Long?, nombrePlanta: String, sensor: String, tipo: String) {
        val currentUser = auth.currentUser
        val currentUserUid = currentUser?.uid ?: ""

        val databaseReference = FirebaseDatabase.getInstance().reference
        val userPlantRef = databaseReference.child("users").child(currentUserUid).child("plants").push()

        val userPlantMap = HashMap<String, Any>()
        userPlantMap["nombrePlanta"] = nombrePlanta
        userPlantMap["id"] = plantId!!
        userPlantMap["sensor"] = sensor
        userPlantMap["tipo"] = tipo

        userPlantRef.updateChildren(userPlantMap)
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
                val defaultImageByteArray = it.getBlob(it.getColumnIndexOrThrow("default_image"))
                val plant = Plant(id, nombre, desc, defaultImageByteArray)
                plants.add(plant)
            }
        }
        return plants
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS PLANTS")
        db?.execSQL("DROP TABLE IF EXISTS USER_PLANTS")
        onCreate(db)
    }

    private fun getImageAsByteArray(context: Context, resourceId: Int): ByteArray {
        val inputStream = context.resources.openRawResource(resourceId)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, length)
        }
        return byteArrayOutputStream.toByteArray()
    }

}