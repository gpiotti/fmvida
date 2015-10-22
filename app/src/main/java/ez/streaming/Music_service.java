package ez.streaming;

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
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;


public class Music_service extends Service implements MediaPlayer.OnErrorListener {

    private  MediaPlayer mediaPlayer;
    private boolean auto_reconnect = false;

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

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)

                        .setContentTitle("Fm Vida 103.5")
                        .setContentText("Reproduciendo...");


        // Creates an explicit intent for an Activity in your app
        final Intent notificationIntent = new Intent(this, MainActivity.class);

        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.

        // Adds the back stack for the Intent (but not the Intent itself)
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        int mId = 1;
        // mId allows you to update the notification later on.




        if (intent.getAction().equals(MainActivity.ACTION_START)) {
            startForeground(mId, mBuilder.build());
            initMediaPlayer(intent.getFloatExtra("vol", 10f));
            Log.i("Service", "Iniciando mediaPlayer");
            auto_reconnect = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            intentFilter.addAction("android.net.wifi.CONNECTIVITY_ACTION");
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");


            registerReceiver(mConnReceiver,
                    new IntentFilter(intentFilter));


        }
        else if(intent.getAction().equals((MainActivity.ACTION_STOP))){
            auto_reconnect=false;
            sendToActivity(MainActivity.STATUS_STOP);
            mediaPlayer.stop();

            stopSelf();


            Log.i("Service", "MediaPlayer stop " + mediaPlayer.isPlaying());
        }

        else if(intent.getAction().equals((MainActivity.MUTE))){

            mediaPlayer.setVolume(0f,0f);

        }
        else if(intent.getAction().equals((MainActivity.UNMUTE))){

            mediaPlayer.setVolume(intent.getFloatExtra("lastVolume",10f),intent.getFloatExtra("lastVolume",10f));

        }
        else if(intent.getAction().equals((MainActivity.VOLUME_CHANGE))){


            mediaPlayer.setVolume(intent.getFloatExtra("vol",10f),intent.getFloatExtra("vol",10f));
        }



        return Service.START_NOT_STICKY;

    }


    private void handleConnectivity(Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected() && netInfo.isConnectedOrConnecting()) {
            Log.i("Service", "esta conectado " + intent.getStringExtra("extraInfo") + " mas datos: " + intent.getParcelableExtra("otherNetwork"));
            if (auto_reconnect) {

                mediaPlayer.reset();
                initMediaPlayer(15f);

            }


        } else if (netInfo != null && !netInfo.isConnected() && !netInfo.isConnectedOrConnecting()) {
            Log.i("Service", "esta desconectado " + intent.getStringExtra("extraInfo"));
        }

     else if(netInfo != null && !netInfo.isConnected() && netInfo.isConnectedOrConnecting()){
        Log.i("Service", "esta conectando " + intent.getStringExtra("extraInfo"))  ;
    }
        else  if(netInfo != null){
            Log.i("Service", "otro estado  " + intent.getStringExtra("extraInfo") + netInfo.isConnected() + netInfo.isConnectedOrConnecting())  ;
        }

    }

    private void sendToActivity(String command) {
        Intent intent = new Intent("sendMessage");
        intent.putExtra("command", command);

                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i("Service", "sended algo " + command );

    }

   public void initMediaPlayer(final Float init_volume) {
        Log.i("Service", "Init Player ");
       sendToActivity(MainActivity.STATUS_CONNECTING);
        this.mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            Log.i("Service", "error en el setdatasrouce " + e.getMessage() + e.getLocalizedMessage());
        }
        mediaPlayer.prepareAsync();
        Toast.makeText(getApplicationContext(), "Conectando...", Toast.LENGTH_LONG).show();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setVolume(init_volume, init_volume);
                if (auto_reconnect) {
                    Log.i("Service", "Por start");
                    mediaPlayer.start();
                    sendToActivity(MainActivity.STATUS_PLAYING);
                }
                Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_SHORT).show();
            }
        });
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                Log.i("Service", "MediaPlayer.OnInfoListener: " + what + " Extra: " + extra);
                return false;
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        Log.i("Service", "Error: " + what + " Extra: " + extra);
        mediaPlayer.reset();
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
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Service", "Service On destroy ");
        unregisterReceiver(mConnReceiver);
        mediaPlayer.release();
        mediaPlayer = null;
    }
}