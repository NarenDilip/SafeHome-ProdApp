package com.schnell.database.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "devices")
public class Devices implements Comparable, Cloneable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String Name;
    private String Type;
    private String Alert;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    private Date createdDate;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getAlert() {
        return Alert;
    }

    public void setAlert(String alert) {
        Alert = alert;
    }

    @NonNull
    public Date getCreatedDate() {
        return createdDate;
    }

    @NonNull
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public int compareTo(Object o) {
        Devices compare = (Devices) o;

        if (compare.id == this.id && compare.Name.equals(this.Name) && compare.Type == (this.Type)) {
            return 0;
        }
        return 1;
    }

}
