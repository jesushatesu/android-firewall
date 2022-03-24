package com.nomorejesus.nmjfilter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public final class MyAdapter extends Adapter<MyAdapter.ViewHolder> {
    public final List<SkbInfo> list;

    public MyAdapter(List<SkbInfo> list) {
        super();
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @NotNull
        public TextView largeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            View tv = itemView.findViewById(R.id.skb_info);
            this.largeTextView = (TextView)tv;
        }
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.largeTextView.setText((CharSequence)this.list.get(position).str);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
