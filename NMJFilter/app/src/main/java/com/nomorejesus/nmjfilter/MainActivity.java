package com.nomorejesus.nmjfilter;

import static android.view.Gravity.CENTER_HORIZONTAL;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nomorejesus.nmjfilter.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;
    ListView lvmain;
    Button btn;
    Button btn2;
    static ArrayList<String> rcvData;
    ArrayAdapter<String> adapter;
    public final static String PARAM_PINTENT = "pendingIntent";
    public final static String PARAM_STOP = "stop";
    public final static String PARAM_RESULT = "result";
    final int TASK1_CODE = 1;
    public final static int NEED_UPDATE = 100;
    public final static int UPDATE_DATA = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_STOP, 0);
        startService(intent);

        rcvData = new ArrayList<>(100);
        lvmain = binding.lvmain;
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, rcvData);
        lvmain.setAdapter(adapter);

        btn = binding.btn;
        btn2 = binding.btn2;
        btn.setText("Update");
        btn.setOnClickListener(this::onClick);
        btn2.setOnClickListener(this::onClick);
    }

    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn:
                adapter.notifyDataSetChanged();

                TextView tv = binding.needUpdate;
                tv.setText("");
                break;
            case R.id.btn2:
                stopService(new Intent(this, MyService.class));
                break;

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_STOP, 1);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_STOP, 0);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NEED_UPDATE)
        {
            if (requestCode == TASK1_CODE)
            {
                TextView tv = binding.needUpdate;
                tv.setText("new data, need update");
            }
        }
        if (resultCode == UPDATE_DATA)
        {
            String result = data.getStringExtra(PARAM_RESULT);
            //ArrayList<String> rcvData = data.getStringArrayListExtra(PARAM_RESULT);
            if (requestCode == TASK1_CODE)
            {
                rcvData.add(result);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi).putExtra(PARAM_STOP, 1);
        startService(intent);
    }
}