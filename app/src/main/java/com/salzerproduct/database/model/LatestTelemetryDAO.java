package com.salzerproduct.database.model;

import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface LatestTelemetryDAO {

    @Insert
    public void insert(LatestTelemetry... latestTelemetries);

    @Update
    public void update(LatestTelemetry... latestTelemetries);

    @Delete
    public void delete(LatestTelemetry latestTelemetries);

    @Query("SELECT * FROM latesttelemetry")
    public List<LatestTelemetry> getDevices();

    @Query("SELECT * FROM latesttelemetry WHERE Deviceid = :type")
    public LatestTelemetry getDevicebyUid(String type);

    @Query("SELECT * FROM latesttelemetry WHERE Deviceid = :type")
    public List<LatestTelemetry> getEntityGroup(String type);

    @Query("DELETE FROM latesttelemetry WHERE Deviceid = :type")
    void DeleteSensor(String type);

    @Query("DELETE FROM latesttelemetry")
    void DeleteTelemetry();

    @Query("DELETE FROM latesttelemetry WHERE Deviceid= :type")
    void DeleteTelemetryDevice(String type);
}
