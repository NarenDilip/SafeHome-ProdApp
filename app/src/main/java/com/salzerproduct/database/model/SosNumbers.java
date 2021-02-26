package com.salzerproduct.database.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "sosnumbers")
public class SosNumbers {

    @PrimaryKey(autoGenerate = true)
    private int Sno;
    @NonNull
    private String firstnumber;
    private String Secondnumber;
    private String thirdnumber;
    private String fourthnumber;
    private String fifthnumber;

    public int getSno() {
        return Sno;
    }

    public void setSno(int sno) {
        Sno = sno;
    }

    @NonNull
    public String getFirstnumber() {
        return firstnumber;
    }

    public void setFirstnumber(@NonNull String firstnumber) {
        this.firstnumber = firstnumber;
    }

    public String getSecondnumber() {
        return Secondnumber;
    }

    public void setSecondnumber(String secondnumber) {
        Secondnumber = secondnumber;
    }

    public String getThirdnumber() {
        return thirdnumber;
    }

    public void setThirdnumber(String thirdnumber) {
        this.thirdnumber = thirdnumber;
    }

    public String getFourthnumber() {
        return fourthnumber;
    }

    public void setFourthnumber(String fourthnumber) {
        this.fourthnumber = fourthnumber;
    }


    public String getFifthnumber() {
        return fifthnumber;
    }

    public void setFifthnumber(String fifthnumber) {
        this.fifthnumber = fifthnumber;
    }
}
