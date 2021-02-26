package com.salzerproduct.database.model;

import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface DeviceNameDAO {

    @Insert
    public void insert(DeviceName... device);

    @Update
    public void update(DeviceName... device);

    @Delete
    public void delete(DeviceName device);

    @Query("SELECT * FROM DeviceName")
    public List<DeviceName> getDevices();

    @Query("SELECT * FROM devicename WHERE Devicename = :type")
    public DeviceName getDevicebyName(String type);

    @Query("DELETE FROM devicename")
    void DeleteDevices();
}
