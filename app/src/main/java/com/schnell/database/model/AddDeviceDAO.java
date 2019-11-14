package com.schnell.database.model;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface AddDeviceDAO {

    @Insert
    public void insert(AddDevice... adddevice);

    @Update
    public void update(AddDevice... adddevice);

    @Delete
    public void delete(AddDevice adddevice);

    @Query("SELECT * FROM adddevices")
    public List<AddDevice> getDevices();

    @Query("SELECT * FROM adddevices WHERE Deviceuid = :type")
    public AddDevice getDevicebyUid(String type);


    @Query("SELECT * FROM adddevices WHERE Deviceid = :type")
    public AddDevice getDeviceid(String type);

    @Query("SELECT * FROM adddevices WHERE entitygroupid = :type")
    public List<AddDevice> getEntityGroup(String type);

    @Query("SELECT * FROM adddevices WHERE Deviceindex = :type")
    public AddDevice getDeviceindex(String type);


    @Query("SELECT * FROM adddevices WHERE entitygroupid = :type AND Devicename='Gateway'" )
    public AddDevice getOnlyEntityGroup(String type);

    //    @Query("SELECT * FROM AddDevice")
    @Query("SELECT * FROM adddevices ORDER BY id DESC")
    public LiveData<List<AddDevice>> getAllDevices();

    @Query("UPDATE adddevices SET Devicename= :devicename, Deviceindex= :deviceindex  WHERE Deviceid = :type")
    void updateall(String devicename, String deviceindex, String type);

    @Query("DELETE FROM adddevices WHERE Deviceid = :type")
    void DeleteSensor(String type);

}
