package com.nomorejesus.nmjfilter;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

public class MyViewModelProvider implements ViewModelProvider.Factory {
    public SkbDao skb;

    public MyViewModelProvider(SkbDao skbDao) {
        skb = skbDao;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MyViewModel(skb);
    }
}
