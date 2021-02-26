package com.salzerproduct.safehome.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DeviceListDAO {
    @Insert
    public void insert(DevicesList... device);

    @Update
    public void update(DevicesList... device);

    @Delete
    public void delete(DevicesList device);

    @Query("SELECT * FROM DevicesList")
    public List<DevicesList> getDevices();

    @Query("SELECT * FROM DevicesList WHERE Name = :type")
    public List<DevicesList> getNameDevices(String type);

    @Query("UPDATE devicesList SET Deviceindex = :deviceindex  WHERE DeviceId = :type")
    void updatedeviceindex(String deviceindex, String type);

    @Query("DELETE FROM devicesList WHERE Deviceindex = :type")
    void DeleteindexDevices(String type);

    @Query("DELETE FROM devicesList")
    void Deletedevices();

}
