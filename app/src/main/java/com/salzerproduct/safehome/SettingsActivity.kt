package com.salzerproduct.safehome

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

//  GatewaySettings , this page act as settings page for gateway , it consit of change sim number,
//  Device factory reset, Device Recovery mode and application logout settings, for this options
//  we need to call the api based on user actions we need to display the selected page and actions
//  based on the user selected, seperate ui will be developed for each action page.

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onBackPressed() {
        val intent = Intent(this, Dashboard::class.java);
        intent.putExtra("NewApp", "true")
        startActivity(intent)
        finish()
    }
}