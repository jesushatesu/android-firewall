package com.nomorejesus.nmjfilter;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service implements NativeCallListener{
    public MyService() {
    }

    static ArrayList<String> rcvData;
    static ArrayList<String> sndData;
    PendingIntent pi;
    int stopSending;
    ExecutorService es;
    int lastStartId;

    static ArrayList<String> getRcvData()
    {
        return rcvData;
    }

    // Used to load the 'nmjfilter' library on application startup.
    static {
        System.loadLibrary("nmjfilter");
    }

    @Override
    public void onNativeRcvCall(String arg)
    {
        try {
            if (stopSending == 0)
                pi.send(MainActivity.NEED_UPDATE);

            if(arg.substring(0, 3).equals("nmj".toString()))
            {
                sndData.add(arg.substring(4));
                if (stopSending == 0)
                {
                    Intent intent = new Intent().putExtra(MainActivity.PARAM_RESULT, sndData);
                    pi.send(MyService.this, MainActivity.UPDATE_DATA, intent);
                }
            }
            else
            {
                rcvData.add(arg);
                if(stopSending == 0)
                {
                    Intent intent = new Intent().putExtra(MainActivity.PARAM_RESULT, rcvData);
                    pi.send(MyService.this, MainActivity.UPDATE_DATA, intent);
                }
            }
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNativeSndCall(String arg) {
        //sndData.add(arg);
    }

    @Override
    public void onCreate()
    {
        rcvData = new ArrayList<>(100);
        sndData = new ArrayList<>(100);
        lastStartId = 0;
        stringFromJNI(this);
        es = Executors.newFixedThreadPool(1);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LOG", "start service startId = " + startId);
        if ((flags&START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY)
            Log.d("LOG", "START_FLAG_REDELIVERY");
        if ((flags&START_FLAG_RETRY) == START_FLAG_RETRY)
            Log.d("LOG", "START_FLAG_RETRY");
        if (startId > lastStartId)
            lastStartId = startId;
        pi = intent.getParcelableExtra(MainActivity.PARAM_PINTENT);
        stopSending = intent.getIntExtra(MainActivity.PARAM_STOP, 0);

        if (startId == 1){
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("LOG", "start new thread");
                    while (true){
                        recvData();
                    }
                }
            }).start();*/
            MyRun mr = new MyRun();
            es.execute(mr);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSending = 0;
        /*
        for (int i = 2; i < lastStartId; i++)
            stopSelf(i);*/
        Log.d("LOG", "stop service ");
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

    class MyRun implements Runnable{

        public MyRun()
        {

        }

        public void run()
        {
            try
            {
                Log.d("LOG", "start new thread");
                while (true){
                    recvData();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}