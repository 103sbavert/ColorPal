package com.sbeve.colorpal.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.sbeve.colorpal.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        const val MAIN_ACTIVITY_SHAREDPREFERENCE_KEY = "MainActivity"
    }

    private val mainActivityBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(navController.graph)
    }
    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(mainActivityBinding.mainNavHost.id) as NavHostFragment
    }
    val navController by lazy {
        navHostFragment.navController
    }

    val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(MAIN_ACTIVITY_SHAREDPREFERENCE_KEY, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainActivityBinding.root)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }
}
