package com.nomorejesus.nmjfilter;

import static android.view.Gravity.CENTER_HORIZONTAL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
    LinearLayout llmain;
    Button btn;
    ArrayList<String> rcvData;

    @Override
    public void onNativeStringCall(String arg)
    {
        rcvData.add(arg);
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
        llmain = binding.llmain;
        btn = binding.btn;
        btn.setText("go");
        btn.setOnClickListener(this::onClick);
        stringFromJNI(this);
    }

    public void onClick(View v)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        int i = 0;
        while (i < 40){
            recvData(layoutParams, i);
            i++;
        }

    }

    public void recvData(LinearLayout.LayoutParams layoutParams, int i)
    {
        TextView tv = new TextView(this);
        recvmsg();
        tv.setText(rcvData.get(i));
        tv.setLayoutParams(layoutParams);
        tv.setGravity(CENTER_HORIZONTAL);
        llmain.addView(tv);
    }

    /**
     * A native method that is implemented by the 'nmjfilter' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI(NativeCallListener nativeCallListener);
    public native String recvmsg();
}