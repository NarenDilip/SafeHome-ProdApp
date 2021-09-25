package com.salzerproduct.database.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ProfileSetDAO {
    @Insert
    public void insert(ProfileSet... pset);

    @Update
    public void update(ProfileSet... pset);

    @Delete
    public void delete(ProfileSet pset);

    @Query("SELECT * FROM userProfileSet")
    public List<ProfileSet> getDevices();

    @Query("SELECT * FROM userProfileSet WHERE profile = :type")
    public ProfileSet getprofile(String type);

    @Query("DELETE FROM userProfileSet WHERE profile= :type")
    void Deleteprfoile(String type);

    @Query("DELETE FROM userProfileSet")
    void Deletepr();
}
