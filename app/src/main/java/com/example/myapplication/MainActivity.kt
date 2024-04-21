package com.example.myapplication

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.fragments.FormFragment
import com.example.myapplication.fragments.HomeFragment
import com.example.myapplication.fragments.HomeFragment.Companion.MY_CHANNEL_ID
import com.example.myapplication.fragments.HomeFragment.Companion.NOTIFICATION_ID
import com.example.myapplication.fragments.PlantsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.internal.notify

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyApplication)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val formFragment = FormFragment()
        val plantsFragment = PlantsFragment()

        makeCurrentFragment(formFragment)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_home -> makeCurrentFragment(homeFragment)
                R.id.ic_form -> makeCurrentFragment(formFragment)
                R.id.ic_plant -> makeCurrentFragment(plantsFragment)
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