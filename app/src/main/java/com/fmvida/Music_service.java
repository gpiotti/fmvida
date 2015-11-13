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
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;


public class Music_service extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener {


    private boolean prepared =false;
    private  MediaPlayer mediaPlayer;
    private boolean auto_reconnect = false;
    private WifiManager.WifiLock wifiLock = null;
    private float volumen;
    private boolean conectado=false;
    private boolean muted = false;
    private AudioManager audioManager = null;

    String url = "rtsp://iptv.cybertap.com.ar:1935/fmvida/fmvida.stream";
    //String url = "http://72.13.93.91:80";

    @Override
    public IBinder onBind(Intent arg0) {
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


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            Log.i("Service", "could not get audio focus. ");
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
            startForeground(mId, mBuilder.build());

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            intentFilter.addAction("android.net.wifi.CONNECTIVITY_ACTION");
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            registerReceiver(mConnReceiver, new IntentFilter(intentFilter));
            Log.i("Service", "Registrado mConnReceiver");


            new StartAsync().execute(intent);
        }
        else if(intent.getAction().equals((MainActivity.ACTION_STOP))){
            Log.i("Service", "MediaPlayer empe<zando el stop " );
            if(this.mediaPlayer!=null){
                stopMediaPlayer();

            }
            Log.i("Service", "MediaPlayer stop " );
        }
        else if(intent.getAction().equals((MainActivity.MUTE))){
            if(this.mediaPlayer!=null) {
                this.mediaPlayer.setVolume(0f, 0f);
                this.muted = true;
            }
        }
        else if(intent.getAction().equals((MainActivity.UNMUTE))){
            if(this.mediaPlayer!=null) {
                this.mediaPlayer.setVolume(intent.getFloatExtra("vol", 0.7f), intent.getFloatExtra("vol", 0.7f));
                volumen = intent.getFloatExtra("vol", 0.7f);
                this.muted=false;
            }
        }
        else if(intent.getAction().equals((MainActivity.VOLUME_CHANGE))){
            if(this.mediaPlayer!=null) {
                this.mediaPlayer.setVolume(intent.getFloatExtra("vol", 0.7f), intent.getFloatExtra("vol", 0.7f));
                volumen = intent.getFloatExtra("vol", 0.7f);
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
                             
                new StartAsync().execute(intent);
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
       if (this.mediaPlayer != null){
           this.mediaPlayer.reset();
           this.mediaPlayer.release();
           this.mediaPlayer = null;
           Log.i("Service", "entro release ");
       }
       this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        this.wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        this.wifiLock.acquire();

       this.mediaPlayer.setOnErrorListener(this);
       this.mediaPlayer.setOnInfoListener(this);

       Log.i("Service", "Iniciado Player ");
       this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
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

       try {
           this.mediaPlayer.setDataSource(url);
           this.mediaPlayer.prepareAsync();
       } catch (IOException e) {
           Log.i("Service", "error en el setdatasrouce " + e.getMessage() + e.getLocalizedMessage());
           this.mediaPlayer.reset();
           sendToActivity((MainActivity.STATUS_STOP));
       }
    }

    public void stopMediaPlayer() {
        if (prepared == true) {
            Log.i("Service", "STATUS_STOP sended");
            new StopAsync().execute();

        }
    }


    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.i("Service", "Audio Focus GAIN");
                try {
                    if (this.mediaPlayer != null && prepared && !muted) {

                        this.mediaPlayer.setVolume(volumen, volumen);
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

                    this.mediaPlayer.setVolume(0f, 0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.i("Service", "Audio Focus Duck");
                if (prepared  && !muted ) {
                    this.mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == 701 && prepared == true) {
            Toast.makeText(getApplicationContext(), "Buffering...",
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i("Service", "Error: " + what + " Extra: " + extra);
        if (what == 1 && extra == -2147483648) {
            Toast.makeText(getApplicationContext(), "Sin conexion con streaming",
                    Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Error: " + what + " " + extra,
                    Toast.LENGTH_LONG).show();
        }
        this.mediaPlayer.reset();

        sendToActivity((MainActivity.STATUS_STOP));
        prepared=false;
       return false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Service", "Service On destroy ");
        if (audioManager != null) {
            audioManager.abandonAudioFocus(this);
        }
        if (mConnReceiver != null) {
            unregisterReceiver(mConnReceiver);
        }
        wifiLock.release();
        this.mediaPlayer.stop();
        this.mediaPlayer.reset();
        this.mediaPlayer.release();
        //this.mediaPlayer = null;
    }

    private class StartAsync extends AsyncTask<Intent, Void, Integer> {
        @Override
        protected Integer doInBackground(Intent... intents) {
            volumen = intents[0].getFloatExtra("vol", 0.7f);
            initMediaPlayer(volumen);
            Log.i("Service", "Iniciando mediaPlayer " + volumen);
            auto_reconnect = true;

            return 0;
        }

        protected void onPreExecute() {
            sendToActivity(MainActivity.STATUS_CONNECTING);
        }

        protected void onPostExecute(Integer result) {

        }

    }

    private class StopAsync extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            prepared = false;
            stopSelf();

            return 0;
        }

        protected void onPreExecute() {
            sendToActivity(MainActivity.STATUS_STOPPING);
        }

        protected void onPostExecute(Integer result) {
            sendToActivity(MainActivity.STATUS_STOP);
        }

    }
}