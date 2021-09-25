package com.salzerproduct.database.model;

import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface LatestAttributesDAO {

    @Insert
    public void insert(LatestAttribute... latestAttributes);

    @Update
    public void update(LatestAttribute... latestAttributes);

    @Delete
    public void delete(LatestAttribute latestAttributes);

    @Query("SELECT * FROM latestattribute")
    public List<LatestAttribute> getDevices();

    @Query("SELECT * FROM latestattribute WHERE Deviceid = :type")
    public LatestAttribute getDevicebyUid(String type);

    @Query("SELECT * FROM latestattribute WHERE Deviceid = :type")
    public List<LatestAttribute> getEntityGroup(String type);

    @Query("DELETE FROM latestattribute")
    void DeleteSensor();

    @Query("DELETE FROM latestattribute WHERE Deviceid= :type")
    void DeletedeviceSensor(String type);

}
