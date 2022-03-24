package com.nomorejesus.nmjfilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nomorejesus.nmjfilter.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static String PARAM_PINTENT = "pendingIntent";
    public final static String PARAM_ACTIVEONE = "activityOneRdy";
    public final static String PARAM_RESULT = "result";
    public final static int NEED_UPDATE = 100;
    public final static int UPDATE_DATA = 200;

    private ActivityMainBinding binding;

    //ListView lvmain;
    Button btn;
    Button btn2;
    List<SkbInfo> rData;
    List<SkbInfo> sData;

    RecyclerView recyclerView;
    MyAdapter listAdapter;
    RecyclerView.LayoutManager rLayoutManager;
    SkbDao skbDao;


    final int TASK1_CODE = 1;
    int wasStopped;
    Intent intentTwo;
    Intent intentOne;

    class MyViewModel extends ViewModel {
        public final LiveData<List<SkbInfo>> usersList;
        public MyViewModel(SkbDao userDao) {
            usersList = userDao.allToLiveData();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Database
        AppDatabase db = App.getInstance().getDatabase();
        skbDao = db.skbDao();
        rData = skbDao.loadAllByType(0);
        sData = skbDao.loadAllByType(1);

        //RecyclerView
        recyclerView = binding.myRecyclerView;
        rLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rLayoutManager);
        listAdapter = new MyAdapter(rData);
        recyclerView.setAdapter(listAdapter);


        wasStopped = 0;
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intentOne = new Intent(this, this.getClass());
        intentTwo = new Intent(this, MainActivity2.class);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_ACTIVEONE, 1);
        startService(intent);

        btn = binding.btn;
        btn2 = binding.btn2;
        btn.setText("Update");
        btn.setOnClickListener(this::onClick);
        btn2.setOnClickListener(this::onClick);
    }

    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btn:
                rData = skbDao.loadAllByType(0);
                sData = skbDao.loadAllByType(1);
                listAdapter.notifyDataSetChanged();

                TextView tv = binding.needUpdate;
                tv.setText("");
                break;
            case R.id.btn2:
                startActivity(intentTwo);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        wasStopped = 1;
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_ACTIVEONE, 0);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasStopped == 1) {
            PendingIntent pi;
            Intent intent;
            pi = createPendingResult(TASK1_CODE, new Intent(), 0);
            intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_ACTIVEONE, 1);
            startService(intent);
            wasStopped = 0;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NEED_UPDATE) {
            if (requestCode == TASK1_CODE) {
                TextView tv = binding.needUpdate;
                tv.setText("new data, need update");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wasStopped = 1;
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_ACTIVEONE, 0);
        stopService(intent);
    }
}