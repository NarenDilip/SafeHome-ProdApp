package com.salzerproduct.database.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "profileState")
public class ProfileState {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    private String gateway;
    private String P1state;
    private String P2state;
    private String P3state;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getP1state() {
        return P1state;
    }

    public void setP1state(String p1state) {
        P1state = p1state;
    }

    public String getP2state() {
        return P2state;
    }

    public void setP2state(String p2state) {
        P2state = p2state;
    }

    public String getP3state() {
        return P3state;
    }

    public void setP3state(String p3state) {
        P3state = p3state;
    }
}
