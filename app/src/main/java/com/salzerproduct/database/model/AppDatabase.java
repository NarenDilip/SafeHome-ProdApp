package com.salzerproduct.database.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import com.salzerproduct.database.model.typeconverters.DateTypeConverter;
import com.salzerproduct.safehome.model.DeviceConfig;
import com.salzerproduct.safehome.model.DeviceConfigDAO;
import com.salzerproduct.safehome.model.DeviceListDAO;
import com.salzerproduct.safehome.model.DevicesList;

@Database(entities = {Devices.class, AddDevice.class, SosNumbers.class, LatestAttribute.class, LatestTelemetry.class,DeviceName.class,ProfileState.class,ProfileSet.class, DeviceConfig.class, DevicesList.class}, version = 1)
@TypeConverters({DateTypeConverter.class})

public abstract class AppDatabase extends RoomDatabase {

    public abstract DeviceDAO getDeviceDAO();

    public abstract AddDeviceDAO getAddDeviceDAO();

    public abstract SosNumbersDAO getSosNumbersDAO();

    public abstract LatestAttributesDAO geAttributesDAO();

    public abstract LatestTelemetryDAO getTelemetryDAO();

    public abstract DeviceNameDAO getDeviceNameDAO();

    public abstract ProfileSetDAO getprofileDao();

    public abstract ProfileStateDAO getProfileState();

    public abstract DeviceConfigDAO getDeviceindex();

    public abstract DeviceListDAO getAllDevices();
}
