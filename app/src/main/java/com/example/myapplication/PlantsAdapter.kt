package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlantsAdapter(private val plants: List<String>) :
    RecyclerView.Adapter<PlantsAdapter.PlantViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plant_item_layout, parent, false)
        return PlantViewHolder(view)
    }
    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plantName = plants[position]
        holder.bind(plantName)
    }
    /*
    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plants[position]
        holder.bind(plant)
    }*/

    override fun getItemCount(): Int {
        return plants.size
    }
    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(plantName: String) {
            // Aquí configura el elemento de la vista del card para mostrar el nombre de la planta
            val txtNombrePlanta = itemView.findViewById<TextView>(R.id.nombre)
            txtNombrePlanta.text = plantName
        }
    }
/*
    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(plant: DbHelper.Plant) {
            // Aquí configura los elementos de la vista del card para mostrar los datos de la planta
            itemView.findViewById<TextView>(R.id.TextViewNombrePlanta).text = plant.nombre

        }
    }*/
}