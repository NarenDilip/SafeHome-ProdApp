package com.salzerproduct.safehome

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

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