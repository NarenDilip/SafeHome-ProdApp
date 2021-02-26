package com.salzerproduct.database.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "latesttelemetry")
public class LatestTelemetry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String Deviceid;
    private String Devicetelemetry;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getDeviceid() {
        return Deviceid;
    }

    public void setDeviceid(@NonNull String deviceid) {
        Deviceid = deviceid;
    }

    public String getDevicetelemetry() {
        return Devicetelemetry;
    }

    public void setDevicetelemetry(String devicetelemetry) {
        Devicetelemetry = devicetelemetry;
    }
}
