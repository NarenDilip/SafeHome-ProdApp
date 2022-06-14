package com.salzerproduct.safehome

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.WindowManager
import com.salzerproduct.database.model.AppDatabase
import com.salzerproduct.database.model.DeviceDAO
import com.salzerproduct.database.model.Devices
import com.salzerproduct.safehome.adapter.DeviceNotificationRecyclerAdapter
import kotlinx.android.synthetic.main.activity_notifications.*
import java.util.*

// NotificationActivity It contains the list of notifications recieved on the user mobile based on
// the installed user gateway and other sensor devices, the notification will display in details
// with recieved time and from which the gateway, ui card design.

class NotificationActivity : AppCompatActivity() {

    private var mDeviceDAO: DeviceDAO? = null
    private var mContactsRecyclerView: RecyclerView? = null
    private var mContactRecyclerAdapter: DeviceNotificationRecyclerAdapter? = null
    private val RC_CREATE_DEVICE = 1
    private val RC_UPDATE_CONTACT = 2
    private var devicelistLiveData: LiveData<List<Devices>>? = null
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_notifications)

        mDeviceDAO = Room.databaseBuilder(getApplicationContext(), AppDatabase::class.java, "db-devices")
            .allowMainThreadQueries() //Allows room to do operation on main thread
            .build()
            .deviceDAO

        mContactsRecyclerView = findViewById(R.id.contactsRecyclerView)
        mContactsRecyclerView!!.layoutManager = LinearLayoutManager(this)

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.colorAccent),
            ContextCompat.getColor(this, android.R.color.holo_red_light),
            ContextCompat.getColor(this, android.R.color.holo_orange_light),
            ContextCompat.getColor(this, android.R.color.holo_green_light),
            ContextCompat.getColor(this, android.R.color.holo_blue_dark),
            ContextCompat.getColor(this, android.R.color.holo_purple)
        )

        mContactRecyclerAdapter =
            DeviceNotificationRecyclerAdapter(this, ArrayList<Devices>(), ArrayList<Devices>(), colors)
        mContactsRecyclerView!!.adapter = mContactRecyclerAdapter
        loadDevices()


        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                mContactRecyclerAdapter!!.filter(text)
                return false
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun loadDevices() {
        devicelistLiveData = mDeviceDAO!!.allDevices
        if (devicelistLiveData != null) {
            devicelistLiveData?.observe(this, mContactRecyclerAdapter?.notificationObserver!!)
            mContactRecyclerAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        devicelistLiveData = mDeviceDAO!!.allDevices
        if (devicelistLiveData != null) {
            devicelistLiveData?.observe(this, mContactRecyclerAdapter?.notificationObserver!!)
            mContactRecyclerAdapter!!.notifyDataSetChanged()
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
        val intent = Intent(this, Dashboard::class.java);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("NewApp", "true")
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_CREATE_DEVICE && resultCode == Activity.RESULT_OK) {
            loadDevices()
        }
    }
}