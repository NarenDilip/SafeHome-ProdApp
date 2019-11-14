package com.schnell.safehome

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import com.schnell.database.model.AppDatabase
import com.schnell.database.model.DeviceDAO
import com.schnell.database.model.Devices
import kotlinx.android.synthetic.main.activity_device.*
import java.util.*

class NotificationActivity : AppCompatActivity() {

    private var mDeviceDAO: DeviceDAO? = null
    private var mContactsRecyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null

    private var mContactRecyclerAdapter: DeviceNotificationRecyclerAdapter? = null
    private val RC_CREATE_DEVICE = 1

    private val RC_UPDATE_CONTACT = 2
    private var devicelistLiveData: LiveData<List<Devices>>? = null

    private var fullDataList: ArrayList<Devices>? = null
    private var linearLayoutManager: LinearLayoutManager? = null

    private val PAGE_START = 0
    private var isLoading = false
    private var isLastPage = false
    private val TOTAL_PAGES = 3
    private var currentPage = PAGE_START

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        mDeviceDAO = Room.databaseBuilder(getApplicationContext(), AppDatabase::class.java, "db-devices")
            .allowMainThreadQueries() //Allows room to do operation on main thread
            .build()
            .deviceDAO

//        var lister = mDeviceDAO!!.devices!!

//        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        mContactsRecyclerView!!.setLayoutManager(linearLayoutManager)

//        mContactsRecyclerView!!.itemAnimator = DefaultItemAnimator();
//        fullDataList = devicelistLiveData!!.value as ArrayList<Devices>

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

        mContactRecyclerAdapter = DeviceNotificationRecyclerAdapter(this, ArrayList<Devices>(), colors)
        mContactsRecyclerView!!.adapter = mContactRecyclerAdapter
        loadDevices()

        // mocking network delay for API call
        Handler().postDelayed({ loadFirstPage() }, 1000)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadFirstPage() {
        val movies = mDeviceDAO!!.getcompleteDevices()
        progressBar!!.setVisibility(View.GONE)
        mContactRecyclerAdapter!!.addAll(movies)

        if (currentPage <= TOTAL_PAGES)
            mContactRecyclerAdapter!!.addLoadingFooter()
        else
            isLastPage = true
    }

    private fun loadNextPage() {
        val movies = mDeviceDAO!!.getcompleteDevices()
        mContactRecyclerAdapter!!.removeLoadingFooter()
        isLoading = false
        mContactRecyclerAdapter!!.addAll(movies)

        if (currentPage != TOTAL_PAGES)
            mContactRecyclerAdapter!!.addLoadingFooter()
        else
            isLastPage = true
    }

    fun updateData(device: ArrayList<Devices>) {
        mContactRecyclerAdapter!!.updateData(mDeviceDAO!!.getDevices() as ArrayList<Devices>)
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
        val intent = Intent(this@NotificationActivity, Dashboard::class.java);
        intent.putExtra("NewApp", "false")
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_CREATE_DEVICE && resultCode == Activity.RESULT_OK) {
            loadDevices()
        }
    }
}
