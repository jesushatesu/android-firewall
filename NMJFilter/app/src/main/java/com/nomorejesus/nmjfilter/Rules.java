package com.nomorejesus.nmjfilter;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
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

    @ColumnInfo(name = "isInKernel")
    public int isIn;

    public int getId() {
        return uid;
    }

    public static final DiffUtil.ItemCallback<Rules> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Rules>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Rules oldRule, @NonNull Rules newRule) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldRule.getId() == newRule.getId();
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull Rules oldRule, @NonNull Rules newRule) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldRule.str.equals(newRule.str);
                }
            };
}