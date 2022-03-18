package com.nomorejesus.nmjfilter;

import static android.view.Gravity.CENTER_HORIZONTAL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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


public class MainActivity extends AppCompatActivity implements NativeCallListener{


    // Used to load the 'nmjfilter' library on application startup.
    static {
        System.loadLibrary("nmjfilter");
        //System.load("/home/jeez/AndroidStudioProjects/NMJFilter/app/src/main/jniLibs/arm64-v8a/libnmjfilter.so");
    }

    private ActivityMainBinding binding;
    ListView lvmain;
    Button btn;
    ArrayList<String> rcvData;
    ArrayList<String> sndData;
    ArrayAdapter<String> adapter;

    @Override
    public void onNativeRcvCall(String arg)
    {
        if(arg.substring(0, 3).equals("nmj".toString()))
        {
            sndData.add(arg.substring(4));
        }
        else
        {
            rcvData.add(arg);
        }

        TextView tv = binding.needUpdate;
        tv.setText("have appeared new data");
    }

    @Override
    public void onNativeSndCall(String arg) {
        //sndData.add(arg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Thread th = new Thread(() -> {
            try {
                Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        th.start();*/
        //ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS},100);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        //TextView tv = binding.sampleText;
        //tv.setText(stringFromJNI());

        //TextView tv1 = binding.textView;
        //tv1.setText(recvmsg());
        rcvData = new ArrayList<>(100);
        sndData = new ArrayList<>(100);
        lvmain = binding.lvmain;
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, rcvData);
        lvmain.setAdapter(adapter);
        btn = binding.btn;
        btn.setText("Update");
        btn.setOnClickListener(this::onClick);
        stringFromJNI(this);
        Thread th = new Thread(() -> {
            try {
                while (true){
                    recvData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        th.start();
    }

    public void onClick(View v)
    {
        adapter.notifyDataSetChanged();
        TextView tv = binding.needUpdate;
        tv.setText("");
    }

    public void recvData()
    {
        recvmsg();
    }

    /**
     * A native method that is implemented by the 'nmjfilter' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI(NativeCallListener nativeCallListener);
    public native String recvmsg();
}