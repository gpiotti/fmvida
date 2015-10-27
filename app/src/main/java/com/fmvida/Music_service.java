package com.fmvida;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;



public class Music_service extends Service implements MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {


    private boolean prepared =false;
    private  MediaPlayer mediaPlayer;
    private boolean auto_reconnect = false;
    private WifiManager.WifiLock wifiLock = null;
    private float volumen;
    private boolean conectado=false;
    private boolean muted = false;

    String url = "rtsp://iptv.cybertap.com.ar:1935/fmvida/fmvida.stream";
    //String url = "http://72.13.93.91:80";

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.i("Service", "Service Bind ");
        return null;
    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            handleConnectivity(intent);
        }
    };

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Service", "Service onStartCommand ");


        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // could not get audio focus.
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_favorite_outline_white_48dp)
                        .setColor(0xff123456)
                        .setContentTitle("Fm Vida 103.5")
                        .setContentText("Reproduciendo...");

        // Creates an explicit intent for an Activity in your app
        final Intent notificationIntent = new Intent(this, MainActivity.class);

        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        int mId = 1;
        // mId allows you to update the notification later on.

        if (intent.getAction().equals(MainActivity.ACTION_START)) {
            volumen = intent.getFloatExtra("vol", 0.7f);
            startForeground(mId, mBuilder.build());
            initMediaPlayer(volumen);
            Log.i("Service", "Iniciando mediaPlayer " + volumen);
            auto_reconnect = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            intentFilter.addAction("android.net.wifi.CONNECTIVITY_ACTION");
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            registerReceiver(mConnReceiver, new IntentFilter(intentFilter));
            Log.i("Service", "Registrado mConnReceiver");
        }
        else if(intent.getAction().equals((MainActivity.ACTION_STOP))){
            if(mediaPlayer!=null){
                stopMediaPlayer();
            }
            Log.i("Service", "MediaPlayer stop " + mediaPlayer.isPlaying());
        }
        else if(intent.getAction().equals((MainActivity.MUTE))){
            if(mediaPlayer!=null) {
                mediaPlayer.setVolume(0f, 0f);
                this.muted = true;
            }
        }
        else if(intent.getAction().equals((MainActivity.UNMUTE))){
            if(mediaPlayer!=null) {
                mediaPlayer.setVolume(intent.getFloatExtra("lastVolume", 0.7f), intent.getFloatExtra("lastVolume", 0.7f));
                volumen = intent.getFloatExtra("lastVolume", 0.7f);
                this.muted=false;
            }
        }
        else if(intent.getAction().equals((MainActivity.VOLUME_CHANGE))){
            if(mediaPlayer!=null) {
                mediaPlayer.setVolume(intent.getFloatExtra("vol", 0.7f), intent.getFloatExtra("vol", 0.7f));
                volumen = intent.getFloatExtra("lastVolume", 0.7f);
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void handleConnectivity(Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected() && netInfo.isConnectedOrConnecting()) {

            if (auto_reconnect && conectado == false) {
                Log.i("Service", "esta conectado " + intent.getStringExtra("extraInfo") + " mas datos: " + intent.getParcelableExtra("otherNetwork"));
                conectado=true;
                mediaPlayer.reset();
                initMediaPlayer(volumen);
            }
        } else if (netInfo != null && !netInfo.isConnected() && !netInfo.isConnectedOrConnecting()) {
            if (prepared == true) {
                Log.i("Service", "esta desconectado " + intent.getStringExtra("extraInfo"));
                stopMediaPlayer();
                conectado=false;
                Toast.makeText(getApplicationContext(), "Se perdió la conexión",
                        Toast.LENGTH_LONG).show();
            }
        }
            else if(netInfo != null && !netInfo.isConnected() && netInfo.isConnectedOrConnecting()){
            Log.i("Service", "esta conectando " + intent.getStringExtra("extraInfo"))  ;
            conectado=true;
        }
            else  if(netInfo != null){
            Log.i("Service", "otro estado  " + intent.getStringExtra("extraInfo") + netInfo.isConnected() + netInfo.isConnectedOrConnecting())  ;
            conectado=false;
        }
    }

    private void sendToActivity(String command) {
        Intent intent = new Intent("sendMessage");
        intent.putExtra("command", command);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

   public void initMediaPlayer(final Float init_volume) {
        Log.i("Service", "Init Player ");
        sendToActivity(MainActivity.STATUS_CONNECTING);
        this.mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        this.wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        this.wifiLock.acquire();
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            Log.i("Service", "error en el setdatasrouce " + e.getMessage() + e.getLocalizedMessage());
        }
        mediaPlayer.prepareAsync();
       Log.i("Service", "Iniciado Player ");
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i("Service", "Preparado con volumen: " + init_volume);
                mediaPlayer.setVolume(init_volume, init_volume);
                if (auto_reconnect) {
                    mediaPlayer.start();
                    sendToActivity(MainActivity.STATUS_PLAYING);
                    prepared = true;
                }
            }
        });
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                return false;
            }
        });
    }

    public void stopMediaPlayer() {
        if (prepared==true) {
            sendToActivity(MainActivity.STATUS_STOP);
            Log.i("Service", "STATUS_STOP sended");
            mediaPlayer.stop();


            prepared=false;
            stopSelf();
        } else {
            sendToActivity(MainActivity.STATUS_STOP);
            Log.i("Service", "STATUS_PREPARE CANCEL");

            mediaPlayer.reset();
            stopSelf();
        }
        }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.i("Service", "Audio Focus GAIN");
                try {
                    if (mediaPlayer != null && prepared && !muted) {

                        mediaPlayer.setVolume(volumen, volumen);
                        Log.i("Service", "seteado volumen");
                    }
                } catch (Exception e) {
                    Log.i("Service", "error: " + e.getMessage());
                }


                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                Log.i("Service", "Audio Focus LOSS");
                if (prepared) {
                    stopMediaPlayer();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.i("Service", "Audio Focus TRANSIENT");
                if (prepared==true) {

                    mediaPlayer.setVolume(0f, 0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.i("Service", "Audio Focus Duck");
                if (prepared  && !muted ) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i("Service", "Error: " + what + " Extra: " + extra);
        mediaPlayer.reset();
        sendToActivity((MainActivity.STATUS_STOP));
        prepared=false;

        if (what == 100 || what == -110) {
            new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    initMediaPlayer(10f);
                    sendToActivity((MainActivity.STATUS_LOST_STREAM));
                }
            }.start();
        }
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Service", "Service On destroy ");
        unregisterReceiver(mConnReceiver);
        wifiLock.release();
        mediaPlayer.stop();
        mediaPlayer.release();
        //mediaPlayer = null;
    }
}