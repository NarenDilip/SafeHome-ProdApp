package com.schnell.safehome

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.schnell.http.Response
import com.schnell.http.ResponseListener
import com.schnell.safehome.model.History
import com.schnell.safehome.webservice.ThingsManager
import kotlinx.android.synthetic.main.item_recycler_contact.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.activty_history_view.*
import kotlinx.android.synthetic.main.activty_history_view.toolbar

class HistoryActivity : AppCompatActivity(), ResponseListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_history_view)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        ThingsManager.telemetryHistory(
            applicationContext!!,
            l = this!!,
            entityType = "DEVICE",
            entityId = "72d3dc80-842e-11e9-83f6-af3f2c629ae5",
            Keys = "alert",
            fromDate = "1559623798000",
            toDate = "1559632800000"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onResponse(r: Response?) {
        if (r == null) {
            return
        }
        try {
            if (r.message == "Token has expired" || r.errorCode == 11 && r.status == 401) {
                AppPreference.clearAll(c = applicationContext!!)
                Snackbar.make(view!!, "Session expired. Please login again", Snackbar.LENGTH_LONG).show()
                Handler().postDelayed({
                    finish()
                }, 2000)
            } else when (r.requestType) {
                ThingsManager.API.telemetryData.hashCode() -> {
                    if (r is History) {
                        r.historyList?.size

                        //creating our adapter
                        val adapter = MyHistoryAdapter(r.historyList)
                        recyclerview.adapter = adapter
                        //now adding the adapter to recyclerview
                    }
                    System.out.println("response---->$")
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}