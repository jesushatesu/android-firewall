package com.nomorejesus.nmjfilter;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SkbInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SkbDao skbDao();
}

