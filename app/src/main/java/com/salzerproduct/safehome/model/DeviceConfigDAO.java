package com.salzerproduct.safehome.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DeviceConfigDAO {

    @Insert
    public void insert(DeviceConfig... deviceConfigs);

    @Update
    public void update(DeviceConfig... deviceConfigs);

    @Delete
    public void delete(DeviceConfig deviceConfigs);

    @Query("SELECT * FROM DeviceConfig")
    public List<DeviceConfig> getallDevices();

    @Query("SELECT * FROM DeviceConfig WHERE gatewayid = :type")
    public DeviceConfig getDevicebygwid(String type);

    @Query("SELECT * FROM DeviceConfig WHERE gatewayid = :type and deviceid = :deviceid")
    public DeviceConfig getDevice(String type, String deviceid);

    @Query("SELECT * FROM DeviceConfig WHERE deviceid = :deviceid")
    public DeviceConfig getSDevice(String deviceid);

    @Query("SELECT * FROM DeviceConfig WHERE deviceloader = :data and gatewayid = :type")
    public List<DeviceConfig> fetchdevices(String data, String type);

    @Query("SELECT * FROM DeviceConfig WHERE deviceloader = :data and deviceid = :type")
    public List<DeviceConfig> fetchonedevice(String data, String type);

    @Query("UPDATE DeviceConfig SET deviceindex = :type WHERE gatewayid = :gid")
    void changedeviceindex(String type, String gid);

    @Query("SELECT * FROM DeviceConfig WHERE deviceindex = :type")
    public DeviceConfig getdeviceindex(String type);

    @Query("UPDATE DeviceConfig SET Deviceindex = :deviceindex, Devicetype= :devicetype  WHERE deviceid = :type")
    void updatedevicetype(String deviceindex, String devicetype, String type);

    @Query("UPDATE DeviceConfig SET Deviceindex = :deviceindex  WHERE deviceid = :type")
    void updatedeviceindex(String deviceindex, String type);

    @Query("UPDATE DeviceConfig SET deviceloader = :deviceloader WHERE deviceid = :type")
    void updatedeviceloader(String deviceloader, String type);

    @Query("UPDATE DeviceConfig SET deviceid = :deviceid WHERE deviceindex = :type")
    void updatedeviceid(String deviceid, String type);

    @Query("DELETE FROM DeviceConfig WHERE Deviceid = :type")
    void DeleteDevices(String type);

    @Query("DELETE FROM DeviceConfig WHERE deviceindex = :type")
    void DeleteindexDevices(String type);

    @Query("DELETE FROM DeviceConfig")
    void Deletedeviceconfigdao();
}
