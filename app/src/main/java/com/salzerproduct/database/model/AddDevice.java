package com.salzerproduct.database.model;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "adddevices")
public class AddDevice {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String Devicename;
    private String Deviceuid;
    private String Devicetype;
    @NonNull
    private String Deviceid;
    private String Deviceindex;
    private String entitygroupid;
    private String gatewayDeviceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDevicename() {
        return Devicename;
    }

    public void setDevicename(String devicename) {
        Devicename = devicename;
    }

    public String getDeviceuid() {
        return Deviceuid;
    }

    public void setDeviceuid(String deviceuid) {
        Deviceuid = deviceuid;
    }

    public String getDevicetype() {
        return Devicetype;
    }

    public void setDevicetype(String devicetype) {
        Devicetype = devicetype;
    }

    @NonNull
    public String getDeviceid() {
        return Deviceid;
    }

    public void setDeviceid(@NonNull String deviceid) {
        Deviceid = deviceid;
    }

    public String getDeviceindex() {
        return Deviceindex;
    }

    public void setDeviceindex(String deviceindex) {
        Deviceindex = deviceindex;
    }

    public String getEntitygroupid() {
        return entitygroupid;
    }

    public void setEntitygroupid(String entitygroupid) {
        this.entitygroupid = entitygroupid;
    }

    public String getGatewayDeviceId() {
        return gatewayDeviceId;
    }

    public void setGatewayDeviceId(String gatewayDeviceId) {
        this.gatewayDeviceId = gatewayDeviceId;
    }
}


