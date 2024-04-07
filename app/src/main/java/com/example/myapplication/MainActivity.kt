package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.myapplication.fragments.FormFragment
import com.example.myapplication.fragments.HomeFragment
import com.example.myapplication.fragments.PlantsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val formFragment = FormFragment()
        val plantsFragment = PlantsFragment()

        makeCurrentFragment(homeFragment)

        // Busca la vista bottom_navigation en el layout inflado
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_home -> makeCurrentFragment(homeFragment)
                R.id.ic_form -> makeCurrentFragment(formFragment)
                R.id.ic_plant -> makeCurrentFragment(plantsFragment)
                // Agrega más casos según sea necesario para otros elementos del menú
            }
            true
        }
    }


    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
}