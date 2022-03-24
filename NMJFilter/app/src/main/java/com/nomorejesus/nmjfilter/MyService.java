package com.nomorejesus.nmjfilter;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service implements NativeCallListener {

    public MyService() {
    }

    static List<String> rcvData;
    static ArrayList<String> sndData;
    PendingIntent pi1;
    PendingIntent pi2;
    int activeOne;
    int activeTwo;
    int lastSkbId;
    SkbDao skbDao;

    static List<String> getRcvData() {
        return rcvData;
    }

    static ArrayList<String> getSndData() {
        return sndData;
    }

    // Used to load the 'nmjfilter' library on application startup.
    static {
        System.loadLibrary("nmjfilter");
    }

    private void skbInsert(String str, int type){
        lastSkbId++;
        SkbInfo skb = new SkbInfo();
        skb.uid = lastSkbId;
        skb.str = str;
        skb.type = type;
        skbDao.insertAll(skb);
    }

    @Override
    public void onNativeRcvCall(@NonNull String arg) {
        try {
            if (arg.substring(0, 3).equals("nmj")) {
                if (activeTwo == 1){
                    pi2.send(MainActivity2.NEED_UPDATE);
                }
                skbInsert(arg.substring(4), 0);
                sndData.add(arg.substring(4));
            }
            else {
                if (activeOne == 1)
                    pi1.send(MainActivity.NEED_UPDATE);

                skbInsert(arg, 1);
                rcvData.add(arg);
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
    public void onCreate() {
        rcvData = new ArrayList<>(100);
        sndData = new ArrayList<>(100);
        AppDatabase db = App.getInstance().getDatabase();
        skbDao = db.skbDao();
        lastSkbId = skbDao.getNum();

        stringFromJNI(this);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        MyRun mr = new MyRun();
        executorService.execute(mr);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LOG", "start service startId = " + startId);
        if ((flags&START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY)
            Log.d("LOG", "START_FLAG_REDELIVERY");
        if ((flags&START_FLAG_RETRY) == START_FLAG_RETRY)
            Log.d("LOG", "START_FLAG_RETRY");

        pi1 = intent.getParcelableExtra(MainActivity.PARAM_PINTENT);
        pi2 = intent.getParcelableExtra(MainActivity2.PARAM_PINTENT);
        activeOne = intent.getIntExtra(MainActivity.PARAM_ACTIVEONE, 0);
        activeTwo = intent.getIntExtra(MainActivity2.PARAM_ACTIVETWO, 0);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activeOne = 0;
        activeTwo = 0;
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

    class MyRun implements Runnable {

        public MyRun() {
        }

        public void run() {
            try {
                Log.d("LOG", "start new thread");
                while (true){
                    recvData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}