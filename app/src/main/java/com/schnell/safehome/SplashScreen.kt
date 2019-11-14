package com.schnell.safehome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.schnell.http.Response
import com.schnell.http.ResponseListener
import com.schnell.safehome.model.LoginResponse
import com.schnell.safehome.model.User
import com.schnell.safehome.webservice.ThingsManager
import com.schnell.util.Utility
import com.schnell.widget.AppDialogs
import kotlinx.android.synthetic.main.activity_splash_screen.*
import android.view.Window
import android.view.WindowManager

class SplashScreen : AppCompatActivity(), ResponseListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            if (Utility.isInternetAvailable(this)) {
                ThingsManager.getUser(c = this)
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
            username = "boopathi.schnell@gmail.com",
            password = "aa123"
        )
    }

    override fun onResponse(r: Response?) {
        try {
            AppDialogs.hideProgressDialog()
            if (r == null) {
                return
            }
            if (r.message == "Token has expired" || r.errorCode == 11 && r.status == 401) {
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
                        Snackbar.make(splashText, r.message.toString(), Snackbar.LENGTH_SHORT).show()
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
                        ThingsManager.login(
                            c = this,
                            username = "boopathi.schnell@gmail.com",
                            password = "aa123"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}