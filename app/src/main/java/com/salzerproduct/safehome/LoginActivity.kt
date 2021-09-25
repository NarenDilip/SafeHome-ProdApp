package com.salzerproduct.safehome

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.salzerproduct.http.Response
import com.salzerproduct.http.ResponseListener
import kotlinx.android.synthetic.main.activity_login_screen.*
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.GET_ACCOUNTS
import android.support.v4.app.ActivityCompat

import android.util.Patterns
import java.util.regex.Pattern
import android.accounts.Account
import android.accounts.AccountManager
import android.widget.ArrayAdapter
import android.widget.Toast
import android.content.pm.PackageManager
import kotlinx.android.synthetic.main.activity_login_screen.toolbar

class LoginActivity : AppCompatActivity(), ResponseListener {

    private val RequestPermissionCode = 1
    var SampleArrayList: ArrayList<String>? = null
    var arrayAdapter: ArrayAdapter<String>? = null
    var pattern: Pattern? = null
    var account: Array<Account>? = null
    var StringArray: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        EnableRuntimePermission();
        pattern = Patterns.EMAIL_ADDRESS;

        GetAccountsName();

        submitbtn.setOnClickListener {
            finish()
            startActivity(Intent(this, Dashboard::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun GetAccountsName() {
        try {
            account = AccountManager.get(this@LoginActivity).accounts
        } catch (e: SecurityException) {
        }
        for (TempAccount in account!!) {
            if (pattern!!.matcher(TempAccount.name).matches()) {
                SampleArrayList!!.add(TempAccount.name)
//                emailaddress.setText(TempAccount.name)
            }
        }
    }

    private fun EnableRuntimePermission() {
        ActivityCompat.requestPermissions(
            this@LoginActivity,
            arrayOf(GET_ACCOUNTS, READ_PHONE_STATE),
            RequestPermissionCode
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RequestPermissionCode ->
                if (grantResults.size > 0) {
                    val GetAccountPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val ReadPhoneStatePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (GetAccountPermission && ReadPhoneStatePermission) {
                        Toast.makeText(this@LoginActivity, "Permission Granted", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Permission Denied", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onResponse(r: Response?) {
    }
}