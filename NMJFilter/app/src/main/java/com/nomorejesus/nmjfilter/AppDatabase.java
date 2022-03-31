package com.nomorejesus.nmjfilter;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SkbInfo.class, Rules.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SkbDao skbDao();
    public abstract RulesDao rulesDao();
}

