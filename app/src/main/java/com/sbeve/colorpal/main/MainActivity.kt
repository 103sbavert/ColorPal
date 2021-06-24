package com.sbeve.colorpal.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.sbeve.colorpal.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        const val MAIN_ACTIVITY_SHARED_PREFERENCE_KEY = "MainActivity"
    }

    private lateinit var mainActivityBinding: ActivityMainBinding
    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(navController.graph)
    }
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        navHostFragment = supportFragmentManager.findFragmentById(mainActivityBinding.mainNavHost.id) as NavHostFragment
        navController = navHostFragment.navController
        sharedPreferences = getSharedPreferences(MAIN_ACTIVITY_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }
}
