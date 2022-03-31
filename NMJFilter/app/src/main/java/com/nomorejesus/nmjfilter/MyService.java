package com.nomorejesus.nmjfilter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service implements NativeCallListener {

    public MyService() {
    }
    SkbDao skbDao;

    // Used to load the 'nmjfilter' library on application startup.
    static {
        System.loadLibrary("nmjfilter");
    }

    private void skbInsert(String str, int type){
        String[] dump = str.split(",");
        if (dump.length != 6)
            return;
        SkbInfo skb = new SkbInfo();
        skb.pid = dump[0];
        skb.saddr = dump[1];
        skb.daddr = dump[2];
        skb.netdev = dump[3];
        skb.proto = dump[4];
        skb.drop = dump[5];
        skb.type = type;
        skbDao.insertAll(skb);
    }

    @Override
    public void onNativeRcvCall(@NonNull String arg) {
        try {
            if (arg.length() > 4 && arg.substring(0, 3).equals("nmj")) {
                skbInsert(arg.substring(4), 0);
            }
            else {
                skbInsert(arg, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNativeSndCall(String arg) {
        int i = 0;
        //sndData.add(arg);
    }

    @Override
    public void onCreate() {
        AppDatabase db = App.getInstance().getDatabase();
        skbDao = db.skbDao();

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
        if (startId == 1){
            String NOTIFICATION_CHANNEL_ID = "com.nomorejesus.nmjfilter";
            String channelName = "Background Monitoring Network";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("NMJFilter")
                    .setContentText("Monitoring")
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setTicker("1234")
                    .build();
            startForeground(123, notification);
        }


        Log.d("LOG", "start service startId = " + startId);

        String rule = intent.getStringExtra(MainActivity.PARAM_RULE);
        if (rule != null){
            Thread thread = new Thread(new Runnable() {
                int sended = 0;
                public void run() {
                    try {
                        //int i = 0;
                        //while (sended != 1){
                            //Thread.sleep(1000);
                            //if (i == 5){
                                Log.d("LOG", "rule is " + rule);
                                int err = sendData(rule);
                                Log.d("LOG", "sendData returned - " + err);
                                sended = 1;
                            //}
                        //}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LOG", "stop service ");
    }

    public void recvData()
    {
        recvmsg();
    }
    public int sendData(String str)
    {
        int ret = sndmsg(str);
        return ret;
    }

    /**
     * A native method that is implemented by the 'nmjfilter' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI(NativeCallListener nativeCallListener);
    public native String recvmsg();
    public native int sndmsg(String str);

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