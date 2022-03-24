package com.nomorejesus.nmjfilter;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SkbInfo {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    public int uid;

    @ColumnInfo(name = "str_info")
    public String str;

    @ColumnInfo(name = "type")
    public int type;

    public int getId() {
        return uid;
    }
}

