package com.nomorejesus.nmjfilter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class MyListAdapter extends ListAdapter<SkbInfo, MyListAdapter.SkbViewHolder> {
    public MyListAdapter() {
        super(SkbInfo.DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public SkbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new MyListAdapter.SkbViewHolder(itemView);
    }

    public static class SkbViewHolder extends RecyclerView.ViewHolder {
        @NotNull
        public TextView largeTextView;

        public SkbViewHolder(View itemView) {
            super(itemView);
            View tv = itemView.findViewById(R.id.skb_info);
            this.largeTextView = (TextView)tv;
        }

        public void bindTo(SkbInfo item) {
            String pid = (item.pid.equals("0"))? "" : " pid: " + item.pid;
            String str = "proto: " + item.proto + " netdev: " + item.netdev + pid
                    + "\nsaddr: " + item.saddr + "\ndaddr: " + item.daddr;
            largeTextView.setText(str);
            if (item.drop.equals("1"))
                largeTextView.setTextColor(Color.rgb(230, 99, 164));
            else
                largeTextView.setTextColor(Color.rgb(64, 215, 94));
        }
    }

    @Override
    public void onBindViewHolder(SkbViewHolder holder, int position) {
        holder.bindTo(getItem(position));
    }

}
