package com.nomorejesus.nmjfilter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SkbDao {
    @Query("SELECT * FROM skbinfo WHERE type LIKE :type ORDER BY rowid DESC")
    public abstract LiveData<List<SkbInfo>> getAll(int type);

    @Query("SELECT * FROM skbinfo")
    public abstract LiveData<List<SkbInfo>> allToLiveData();

    @Query("SELECT COUNT(*) FROM skbinfo")
    int getNum();

    @Query("SELECT * FROM skbinfo WHERE rowid IN (:skbIds)")
    List<SkbInfo> findByIds(int[] skbIds);

    @Query("SELECT * FROM skbinfo WHERE type = :type")
    List<SkbInfo> findByType(int type);

    @Query("SELECT * FROM skbinfo WHERE saddr LIKE :first")
    List<SkbInfo> findBySaddr(String first);

    @Query("SELECT * FROM skbinfo WHERE daddr LIKE :first")
    List<SkbInfo> findByDaddr(String first);

    @Query("SELECT * FROM skbinfo WHERE pid LIKE :first")
    List<SkbInfo> findByPid(String first);

    @Query("SELECT * FROM skbinfo WHERE proto LIKE :first")
    List<SkbInfo> findByProto(String first);

    @Query("SELECT * FROM skbinfo WHERE netdev LIKE :first")
    List<SkbInfo> findByNetdev(String first);

    //@Query("SELECT * FROM skbinfo WHERE drop LIKE :first")
    //List<SkbInfo> findByDrop(String first);

    @Insert
    void insertAll(SkbInfo... skbs);

    @Delete
    void delete(SkbInfo skb);

    @Query("DELETE FROM skbinfo")
    void nukeTable();
}
