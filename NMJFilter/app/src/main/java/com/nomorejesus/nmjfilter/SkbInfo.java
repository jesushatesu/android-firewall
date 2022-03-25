package com.nomorejesus.nmjfilter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SkbInfo {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    public int uid;

    @ColumnInfo(name = "saddr")
    public String saddr;

    @ColumnInfo(name = "daddr")
    public String daddr;

    @ColumnInfo(name = "pid")
    public String pid;

    @ColumnInfo(name = "proto")
    public String proto;

    @ColumnInfo(name = "netdev")
    public String netdev;

    @ColumnInfo(name = "drop")
    public String drop;

    @ColumnInfo(name = "type")
    public int type;

    public int getId() {
        return uid;
    }

    public static final DiffUtil.ItemCallback<SkbInfo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SkbInfo>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull SkbInfo oldSkb, @NonNull SkbInfo newSkb) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldSkb.getId() == newSkb.getId();
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull SkbInfo oldSkb, @NonNull SkbInfo newSkb) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldSkb.saddr.equals(newSkb.saddr) && oldSkb.daddr.equals(newSkb.daddr);
                }
            };
}
