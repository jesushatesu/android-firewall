package com.nomorejesus.nmjfilter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RulesDao {
    @Query("SELECT * FROM rules")
    List<Rules> getAll();

    @Query("SELECT * FROM rules")
    public abstract LiveData<List<Rules>> allToLiveData();

    @Query("SELECT COUNT(*) FROM rules")
    int getNum();

    @Query("SELECT * FROM rules WHERE rowid IN (:rules)")
    List<Rules> loadAllByIds(int[] rules);

    @Query("SELECT * FROM rules WHERE rule LIKE :first LIMIT 1")
    Rules findByName(String first);

    @Insert
    void insertAll(Rules... rules);

    @Delete
    void delete(Rules rule);

}
