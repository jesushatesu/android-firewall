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

    Button btn;
    static ArrayList<String> sndData;
    ArrayAdapter<String> adapter;
    public final static String PARAM_PINTENT = "pendingIntent";
    final int TASK1_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        PendingIntent pi;
        Intent intent;
        pi = createPendingResult(TASK1_CODE, new Intent(), 0);
        intent = new Intent(this, MyService.class).putExtra(PARAM_PINTENT, pi);
        startService(intent);

        btn = findViewById(R.id.btn);
        btn.setText("Update");
        btn.setOnClickListener(this::onClick);
    }

    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btn:
                break;

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}