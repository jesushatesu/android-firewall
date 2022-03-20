package com.nomorejesus.nmjfilter;

import androidx.appcompat.app.AppCompatActivity;

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
    Button btn2;
    static ArrayList<String> sndData;
    ArrayAdapter<String> adapter;
    TextView tv;
    Intent mainIntent;

    public static void addSndData(String data)
    {
        sndData.add(data);
        //tv.setText("have appeared new data");
    }

    public void setMainIntent(MainActivity activity)
    {
        //mainIntent = Intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        lvmain = findViewById(R.id.lvmain);
        btn = findViewById(R.id.btn);
        btn2 = findViewById(R.id.btn2);
        TextView tv = findViewById(R.id.needUpdate);

        sndData = new ArrayList<>(100);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, sndData);
        lvmain.setAdapter(adapter);
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
                //tv.setText("");
                break;
            case R.id.btn2:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

        }
    }
}