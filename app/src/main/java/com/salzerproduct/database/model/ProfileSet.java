package com.salzerproduct.database.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "userProfileSet")
public class ProfileSet {

    @PrimaryKey(autoGenerate = true)
    private int Sno;

    @NonNull
    private String profile;
    private String response;


    public int getSno() {
        return Sno;
    }

    public void setSno(int sno) {
        Sno = sno;
    }

    @NonNull
    public String getProfile() {
        return profile;
    }

    public void setProfile(@NonNull String profile) {
        this.profile = profile;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
