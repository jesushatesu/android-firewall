package com.nomorejesus.nmjfilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nomorejesus.nmjfilter.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    ListView lvmain;
    Button btn;
    static ArrayList<String> sndData;
    ArrayAdapter<String> adapter;
    public final static String PARAM_PINTENT = "pendingIntent";
    public final static String PARAM_ACTIVETWO = "activityTwoRdy";
    public final static String PARAM_RESULT = "result";
    final int TASK1_CODE = 1;
    public final static int NEED_UPDATE = 100;
    public final static int UPDATE_DATA = 200;
    int wasStopped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        wasStopped = 0;
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_ACTIVETWO, 1);
        startService(intent);

        sndData = new ArrayList<String>();
        ArrayList<String> tmp = MyService.getSndData();
        sndData.addAll(tmp);
        lvmain = findViewById(R.id.lvmain);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, sndData);
        lvmain.setAdapter(adapter);

        btn = findViewById(R.id.btn);
        btn.setText("Update");
        btn.setOnClickListener(this::onClick);
    }

    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btn:
                ArrayList<String> tmp = MyService.getSndData();
                sndData.clear();
                sndData.addAll(tmp);
                adapter.notifyDataSetChanged();
                TextView tv = findViewById(R.id.needUpdate);
                tv.setText("");
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
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_ACTIVETWO, 0);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasStopped == 1)
        {
            PendingIntent pi;
            Intent intent;
            pi = createPendingResult(TASK1_CODE, new Intent(), 0);
            intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_ACTIVETWO, 1);
            startService(intent);
            wasStopped = 0;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NEED_UPDATE)
        {
            if (requestCode == TASK1_CODE)
            {
                TextView tv = findViewById(R.id.needUpdate);
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
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_ACTIVETWO, 0);
        stopService(intent);
    }



}