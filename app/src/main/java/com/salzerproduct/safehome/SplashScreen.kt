package com.salzerproduct.safehome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.salzerproduct.http.Response
import com.salzerproduct.http.ResponseListener
import com.salzerproduct.safehome.model.LoginResponse
import com.salzerproduct.safehome.model.User
import com.salzerproduct.safehome.webservice.ThingsManager
import com.salzerproduct.util.Utility
import com.salzerproduct.widget.AppDialogs
import kotlinx.android.synthetic.main.activity_splash_screen.*

// Basic splash screen with timer thread implementataion and data check for navigating the
// needed activity class

class SplashScreen : AppCompatActivity(), ResponseListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            if (Utility.isInternetAvailable(this)) {
                var udetao = AppPreference.get(this, AppPreference.Key.loginUser, "")
                if (udetao!!.isEmpty()) {
                    ThingsManager.getUser(c = this)
                } else {
                    loadDashboard()
                }
            } else {
                Snackbar.make(splashText, R.string.nointernet, Snackbar.LENGTH_LONG).show()
            }
        }, 1000)
    }

    private fun loadDashboard() {
        finish()
        val intent = Intent(this@SplashScreen, Dashboard::class.java);
        intent.putExtra("NewApp", "true")
        startActivity(intent)
    }

    private fun callloginmethod() {
        ThingsManager.login(
            c = this!!,
            username = "hgss@schnellenergy.com",
            password = "ce1hg"+"$"+"s"
//            username = "hgss@gmail.com",
//            password = "schnell@321"
        )
    }

    override fun onResponse(r: Response?) {
        try {
            AppDialogs.hideProgressDialog()
            if (r == null) {
                return
            }
            if (r.message == "Token has expired" || r.errorCode == 11 && r.status == 401 || r.message == "Authentication failed") {
                callloginmethod()
            }

            when (r.requestType) {

                ThingsManager.API.login.hashCode() -> {
                    if (r is LoginResponse) {
                        AppPreference.put(
                            this,
                            AppPreference.Key.accessToken,
                            r.token.toString()
                        )
                        AppPreference.put(
                            this,
                            AppPreference.Key.refreshToken,
                            r.refreshToken.toString()
                        )
                        ThingsManager.getUser(c = this)
                    } else {
                        Snackbar.make(splashText, r.message.toString(), Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }

                ThingsManager.API.user.hashCode() -> {
                    if (r is User) {
                        AppPreference.storeGson(
                            c = this,
                            key = AppPreference.Key.loginUser,
                            data = r
                        )
                        loadDashboard()
                    } else {
                        callloginmethod()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}