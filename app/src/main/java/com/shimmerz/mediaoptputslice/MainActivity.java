package com.shimmerz.mediaoptputslice;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    private MyBroadcastReceiver myBroadcastReceiver;
    private BluetoothDisconnect bluetoothDisconnect;
    private AudioManager audioManager;
    private ImageView blueTooth_imageView;
    int blueToothConnectState;
    public KeepSpeakerOn.ServiceBinder binder;
    private SeekBar volumeSeekBar;
    private LinearLayout unClickable;
    private CardView select_speaker, select_blueTooth;
    private int MaxVolume, nowVolume;
    public ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (KeepSpeakerOn.ServiceBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        CircleImageView imageView = findViewById(R.id.title_image);
        blueToothConnectState = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEADSET);
        select_blueTooth = findViewById(R.id.select_blueTooth);
        select_speaker = findViewById(R.id.select_speaker);
        volumeSeekBar = findViewById(R.id.volume);
        blueTooth_imageView = findViewById(R.id.image_blueTooth);
        unClickable = findViewById(R.id.unClickable);
        View emptyView = findViewById(R.id.touch_to_finish);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        MaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        nowVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);


        //初始化控件文本
        volumeSeekBar.setMax(MaxVolume);
        volumeSeekBar.setProgress(nowVolume);
        if(blueToothConnectState == BluetoothProfile.STATE_CONNECTED){
            select_blueTooth.setCardBackgroundColor(getColor(R.color.select));
            unClickable.setVisibility(View.GONE);
        }
        else {
            select_speaker.setCardBackgroundColor(getColor(R.color.select));
            blueTooth_imageView.setImageResource(R.drawable.bluetoothpiece_gray);
        }

        //监听通知栏点击事件
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.shimmerz.mediaoptputslice.SWITCH_STATE");
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, filter);
        //监听蓝牙耳机连接状态
        bluetoothDisconnect= new BluetoothDisconnect();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(bluetoothDisconnect,intentFilter);
        //打开服务
        Intent intent = new Intent(MainActivity.this,KeepSpeakerOn.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);

        select_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select_speaker.getCardBackgroundColor() == getColorStateList(R.color.select)){

                }
                else{
                    binder.switchState();
                    select_blueTooth.setCardBackgroundColor(getColor(R.color.white));
                    select_speaker.setCardBackgroundColor(getColor(R.color.select));
                    nowVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    volumeSeekBar.setProgress(nowVolume);
                }
            }
        });

        select_blueTooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select_blueTooth.getCardBackgroundColor() == getColorStateList(R.color.select)){
                }
                else{
                   binder.switchState();
                   select_blueTooth.setCardBackgroundColor(getColor(R.color.select));
                   select_speaker.setCardBackgroundColor(getColor(R.color.white));
                   nowVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                   volumeSeekBar.setProgress(nowVolume);
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.setCancelable(true);
                dialog.show();  //注意：必须在window.setContentView之前show
                Window window = dialog.getWindow();
                window.setContentView(R.layout.dialog_layout);
                TextView yesButton = (TextView) window.findViewById(R.id.alert_dialog_yes);
                //点击确定按钮让对话框消失
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                TextView noButton = window.findViewById(R.id.alert_dialog_no);
                noButton.setOnClickListener(new View.OnClickListener() {
                    //跳转应用市场评分
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                            startActivity(intent);
                            dialog.dismiss();
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, "抱歉，你没有安装应用市场", Toast.LENGTH_LONG);
                        }

                    }
                });
            }
        });
        
        unClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "蓝牙设备未连接", Toast.LENGTH_SHORT).show();
            }
        });


        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,AudioManager.FLAG_PLAY_SOUND);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //点击上部分变成后台
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
            }
        });
    }

    //点击返回后变成后台
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this,KeepSpeakerOn.class);
        unbindService(connection);
        stopService(intent);
        unregisterReceiver(myBroadcastReceiver);
        unregisterReceiver(bluetoothDisconnect);
        super.onDestroy();
    }

    private void createNotificationChannel(){
        CharSequence name = "setSpeakerOn";
        String description = "保持服务在后台进行";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel =new NotificationChannel("setSpeakerOn",name,importance);
        channel.setDescription(description);
        NotificationManager manager = getSystemService(NotificationManager.class);
        assert manager != null;
        manager.createNotificationChannel(channel);
    }


    //两个广播接收器
    class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String state;
            state =  binder.switchState();
            Toast.makeText(context, "已切换至蓝牙设备输出", Toast.LENGTH_SHORT).show();
            select_blueTooth.setCardBackgroundColor(getColor(R.color.select));
            select_speaker.setCardBackgroundColor(getColor(R.color.white));
        }
    }


    class BluetoothDisconnect extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            blueToothConnectState = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEADSET);
            if(blueToothConnectState == BluetoothProfile.STATE_DISCONNECTED){
                binder.stopService();
                Toast.makeText(context, "蓝牙已断开连接", Toast.LENGTH_SHORT).show();
                select_blueTooth.setCardBackgroundColor(getColor(R.color.white));
                select_speaker.setCardBackgroundColor(getColor(R.color.select));
                unClickable.setVisibility(View.VISIBLE);
                blueTooth_imageView.setImageResource(R.drawable.bluetoothpiece_gray);
            }
            else if(blueToothConnectState == BluetoothProfile.STATE_CONNECTED){
                unClickable.setVisibility(View.GONE);
                blueTooth_imageView.setImageResource(R.drawable.bluetoothpiece);
                select_speaker.setCardBackgroundColor(getColor(R.color.white));
                select_blueTooth.setCardBackgroundColor(getColor(R.color.select));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nowVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setProgress(nowVolume);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                if(nowVolume+1<=MaxVolume){
                    volumeSeekBar.setProgress(++nowVolume);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                if(nowVolume-1>=0){
                    volumeSeekBar.setProgress(--nowVolume);
                }
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
