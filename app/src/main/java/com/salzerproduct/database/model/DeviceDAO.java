package com.salzerproduct.database.model;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;

import java.util.List;


@Dao
public interface DeviceDAO {
    @Insert
    public void insert(Devices... device);

    @Update
    public void update(Devices... device);

    @Delete
    public void delete(Devices device);

    @Query("SELECT * FROM devices")
    public List<Devices> getDevices();

    @Query("SELECT * FROM devices WHERE Type = :type")
    public Devices getDevicebyType(String type);

    //    @Query("SELECT * FROM devices")
    @Query("SELECT * FROM devices ORDER BY createdDate DESC")
    public LiveData<List<Devices>> getAllDevices();

    //    @Query("SELECT * FROM devices")
    @Query("SELECT * FROM devices ORDER BY id DESC limit 10")
    public List<Devices> getcompleteDevices();

    @Query("DELETE FROM devices")
    void Deletedevdevices();

}
