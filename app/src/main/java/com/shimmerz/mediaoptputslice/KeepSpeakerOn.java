package com.shimmerz.mediaoptputslice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.LinearLayout;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeepSpeakerOn extends Service {
    private AudioManager audioManager;
    private ScheduledExecutorService scheduledExecutorService;
    private Runnable runnable;


    @Override
    public void onDestroy() {
        endLastSchedule();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    audioManager.setBluetoothScoOn(false);
                    audioManager.stopBluetoothSco();
                    audioManager.setSpeakerphoneOn(true);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

    }

    private ServiceBinder mBinder = new ServiceBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void endLastSchedule(){
        if(scheduledExecutorService!=null){
            scheduledExecutorService.shutdownNow();
        }
        scheduledExecutorService = null;
    }

    class ServiceBinder extends Binder{
        public String switchState(){
            if(scheduledExecutorService==null){
                startForeground(1,getNotification());
                endLastSchedule();
                scheduledExecutorService = Executors.newScheduledThreadPool(1);
                scheduledExecutorService.scheduleAtFixedRate(runnable,1,1,TimeUnit.SECONDS);
                return "以蓝牙设备输出";
            }else {
                getNotificationManager().cancel(1);
                stopForeground(true );
                endLastSchedule();
                return "以扬声器输出";
            }
        }
        public void stopService(){
            getNotificationManager().cancel(1);
            stopForeground(true);
            endLastSchedule();
        }
    }


    private NotificationManager getNotificationManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification(){
        Intent intent = new Intent("com.shimmerz.mediaoptputslice.SWITCH_STATE");
        PendingIntent pendingIntent  =PendingIntent.getBroadcast(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"setSpeakerOn")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("正以扬声器输出，点击通知切换至蓝牙设备")
                .setContentIntent(pendingIntent);
        return builder.build();
    }

}