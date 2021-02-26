package com.salzerproduct.database.model;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.*;

import java.util.List;

@Dao
public interface SosNumbersDAO {
    @Insert
    public void insert(SosNumbers... sosNumbers);

    @Update
    public void update(SosNumbers... sosNumbers);

    @Delete
    public void delete(SosNumbers sosNumbers);

    @Query("SELECT * FROM sosnumbers")
    public List<SosNumbers> getusernumbers();

    //    @Query("SELECT * FROM AddDevice")
    @Query("SELECT * FROM sosnumbers ORDER BY Sno DESC")
    public LiveData<List<SosNumbers>> getallusernumbers();

    @Query("DELETE FROM sosnumbers")
    void Deletesosnumbers();

}
