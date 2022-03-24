package com.nomorejesus.nmjfilter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SkbDao {
    @Query("SELECT * FROM skbinfo")
    LiveData<SkbInfo> getAll();

    @Query("SELECT * FROM skbinfo")
     public abstract LiveData<List<SkbInfo>> allToLiveData();

    @Query("SELECT COUNT(*) FROM skbinfo")
    int getNum();

    @Query("SELECT * FROM skbinfo WHERE rowid IN (:skbIds)")
    List<SkbInfo> loadAllByIds(int[] skbIds);

    @Query("SELECT * FROM skbinfo WHERE type = :type")
    List<SkbInfo> loadAllByType(int type);

    @Query("SELECT * FROM skbinfo WHERE str_info LIKE :first LIMIT 1")
    SkbInfo findByName(String first);

    @Insert
    void insertAll(SkbInfo... skbs);

    @Delete
    void delete(SkbInfo skb);

}
