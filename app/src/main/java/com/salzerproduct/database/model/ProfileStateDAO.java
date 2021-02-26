package com.salzerproduct.database.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ProfileStateDAO {
    @Insert
    public void insert(ProfileState... profileStates);

    @Update
    public void update(ProfileState... profileStates);

    @Delete
    public void delete(ProfileState profileStates);

    @Query("SELECT * FROM profileState")
    public List<ProfileState> getDevices();

    @Query("SELECT * FROM profileState WHERE gateway = :type")
    public ProfileState getstatebyGw(String type);

    @Query("UPDATE profileState SET P1state= :home, P2state= :sleep,P3state= :away WHERE gateway = :type")
    void updateall(String home, String sleep, String away, String type);

    @Query("DELETE FROM profileState")
    void DeleteprofileState();

}
