package com.schnell.database.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import com.schnell.database.model.typeconverters.DateTypeConverter;


@Database(entities = {Devices.class, AddDevice.class, SosNumbers.class, LatestAttribute.class, LatestTelemetry.class}, version = 2)
@TypeConverters({DateTypeConverter.class})

public abstract class AppDatabase extends RoomDatabase {

    public abstract DeviceDAO getDeviceDAO();

    public abstract AddDeviceDAO getAddDeviceDAO();

    public abstract SosNumbersDAO getSosNumbersDAO();

    public abstract LatestAttributesDAO geAttributesDAO();

    public abstract LatestTelemetryDAO getTelemetryDAO();
}
