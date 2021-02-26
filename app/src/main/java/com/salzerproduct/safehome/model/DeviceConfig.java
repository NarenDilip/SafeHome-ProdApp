package com.salzerproduct.safehome.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "DeviceConfig")
public class DeviceConfig {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String deviceid;
    private String devicetype;
    private String deviceindex;
    private String devicestatus;
    private String gatewayid;
    private String deviceloader;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }

    public String getDeviceindex() {
        return deviceindex;
    }

    public void setDeviceindex(String deviceindex) {
        this.deviceindex = deviceindex;
    }

    public String getDevicestatus() {
        return devicestatus;
    }

    public void setDevicestatus(String devicestatus) {
        this.devicestatus = devicestatus;
    }

    public String getGatewayid() {
        return gatewayid;
    }

    public void setGatewayid(String gatewayid) {
        this.gatewayid = gatewayid;
    }


    public String getDeviceloader() {
        return deviceloader;
    }

    public void setDeviceloader(String deviceloader) {
        this.deviceloader = deviceloader;
    }
}
