package com.salzerproduct.safehome.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "devicesList")
public class DevicesList{

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String Name;
    private String Type;
    private String DeviceId;
    private String DeviceIndex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    public String getDeviceIndex() {
        return DeviceIndex;
    }

    public void setDeviceIndex(String deviceIndex) {
        DeviceIndex = deviceIndex;
    }
}
