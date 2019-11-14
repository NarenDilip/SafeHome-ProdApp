package com.schnell.database.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "latestattribute")
public class LatestAttribute {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String Deviceid;
    private String Devicename;

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

    @NonNull
    public void setDeviceid(String deviceid) {
        Deviceid = deviceid;
    }

    public String getDevicename() {
        return Devicename;
    }

    public void setDevicename(String devicename) {
        Devicename = devicename;
    }
}
