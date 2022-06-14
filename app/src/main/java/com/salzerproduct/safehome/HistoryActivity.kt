package com.salzerproduct.safehome

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.persistence.room.Room
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.Toast
import com.salzerproduct.database.model.AddDeviceDAO
import com.salzerproduct.database.model.AppDatabase
import com.salzerproduct.http.Response
import com.salzerproduct.http.ResponseListener
import com.salzerproduct.safehome.dialog.DeviceDialog
import com.salzerproduct.safehome.model.History
import com.salzerproduct.safehome.webservice.ThingsManager
import com.salzerproduct.widget.AppDialogs
import kotlinx.android.synthetic.main.activty_history_view.*
import kotlinx.android.synthetic.main.item_recycler_contact.*
import java.util.*
import java.text.SimpleDateFormat

// Device History, In this page we need to fetch the device details based on the user selected
// from time and to time , we need to request the device details to the server based on the request
// time period and fetched details are displayed in the ui design, ui design like list view.


class HistoryActivity : AppCompatActivity(), ResponseListener, DeviceDialog.CallBack {

    private var mYear: Int = 0
    private var secYear: Int = 0
    private var mMonth: Int = 0
    private var secMonth: Int = 0
    private var mDay: Int = 0
    private var secDay: Int = 0
    private var mHour: Int = 0
    private var mMinute: Int = 0
    private var dDialog: DeviceDialog? = null
    private var mAddDeviceDAO: AddDeviceDAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_history_view)

        mAddDeviceDAO = Room.databaseBuilder(this, AppDatabase::class.java, "db-devices")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries() //Allows room to do operation on main thread
            .build()
            .addDeviceDAO

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        from_date.setOnClickListener {

            val c = Calendar.getInstance()
            mYear = c.get(Calendar.YEAR)
            mMonth = c.get(Calendar.MONTH)
            mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    from_date.setText(dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    secYear = year
                    secMonth = monthOfYear
                    secDay = dayOfMonth
                },
                mYear,
                mMonth,
                mDay
            )
            datePickerDialog.show()
        }

        from_time.setOnClickListener {
            val c = Calendar.getInstance()
            mHour = c.get(Calendar.HOUR_OF_DAY)
            mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute -> from_time.setText("$hourOfDay:$minute") },
                mHour,
                mMinute,
                false
            )
            timePickerDialog.show()
        }

        to_date.setOnClickListener {
            //            val c = Calendar.getInstance()
            mYear = secYear
            mMonth = secMonth
            mDay = secDay

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth -> to_date.setText(dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year) },
                mYear,
                mMonth,
                mDay
            )
            datePickerDialog.show()
            val l = secDay
        }

        to_time.setOnClickListener {
            val c = Calendar.getInstance()
            mHour = c.get(Calendar.HOUR_OF_DAY)
            mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute -> to_time.setText("$hourOfDay:$minute") },
                mHour,
                mMinute,
                false
            )
            timePickerDialog.show()
        }

        selectdevice.setOnClickListener {
            dDialog = DeviceDialog(this)
            dDialog!!.setCallBack(this)
            dDialog!!.show()
        }

        submitbtn.setOnClickListener {
            if (!from_date.text.toString().isEmpty()) {
                if (!to_date.text.toString().isEmpty()) {
                    if (!selectdevice.text.toString().equals("Select Device")) {

                        val fromdate = from_date.text.toString() + " " + from_time.text.toString()
                        val todate = to_date.text.toString() + " " + to_time.text.toString()

                        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")

                        val fdate = formatter.parse("$fromdate:00")
                        val tdate = formatter.parse("$todate:00")

                        var devicelist = mAddDeviceDAO!!.getDeviceinfo(selectdevice.text.toString())
                        if (!devicelist.deviceid.isEmpty()) {
                            AppDialogs.showProgressDialog(this, "Please wait")
                            ThingsManager.telemetryHistory(
                                applicationContext!!,
                                l = this!!,
                                entityType = "DEVICE",
                                entityId = devicelist.deviceid,
                                Keys = "alert",
                                fromDate = fdate.time.toString(),
                                toDate = tdate.time.toString()
                            )
                        }
                    } else {
                        Toast.makeText(this, "Please select device", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Select to Date", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Select from date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun GetDevices(DeviceName: String?) {
        selectdevice.setText(DeviceName)
        dDialog!!.dismiss()
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
            AppDialogs.hideProgressDialog()
            if (r.message == "Token has expired" || r.errorCode == 11 && r.status == 401) {
                AppPreference.clearAll(c = applicationContext!!)
                Snackbar.make(view!!, "Session expired. Please login again", Snackbar.LENGTH_LONG).show()
                Handler().postDelayed({
                    finish()
                }, 2000)
            } else when (r.requestType) {
                ThingsManager.API.telemetryData.hashCode() -> {
                    if (r is History) {
                        //creating our adapter
                        if (r.historyList != null) {
                            val adapter = MyHistoryAdapter(r.historyList)
                            recyclerview.adapter = adapter
                        } else {
                            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show()
                        }
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