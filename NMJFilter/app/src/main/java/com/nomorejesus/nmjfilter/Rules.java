package com.nomorejesus.nmjfilter;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Rules {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    public int uid;

    @ColumnInfo(name = "rule")
    public String str;

    public int getId() {
        return uid;
    }
}