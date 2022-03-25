package com.nomorejesus.nmjfilter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MyViewModel extends ViewModel {
    public final LiveData<List<SkbInfo>> skbListRcv;
    public final LiveData<List<SkbInfo>> skbListSnd;
    public MyViewModel(SkbDao skbDao) {
        skbListSnd = skbDao.getAll(0);
        skbListRcv = skbDao.getAll(1);
    }
}
