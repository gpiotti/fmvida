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

    private MediaPlayer mediaPlayer;
    private boolean auto_reconnect = false;

    String url = "rtsp://iptv.cybertap.com.ar:1935/fmvida/fmvida.stream";
    //String url = "http://72.13.93.91:80";


    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.i("MyActivity", "Service Bind ");
        return null;
    }


    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            handleConnectivity(intent);

        }
    };

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyActivity", "Service onStartCommand ");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_media_play)
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
        startForeground(mId, mBuilder.build());



        if (intent.getAction().equals(MainActivity.START_SERVICE)) {
            initMediaPlayer(intent.getFloatExtra("vol", 10f));
            Log.i("MyActivity", "Iniciando mediaPlayer");

            registerReceiver(mConnReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            auto_reconnect = true;



        }
        else if(intent.getAction().equals(MainActivity.ACTION_RESUME)){
            Log.i("MyActivity", "resumiendo mediaPlayer");
            mediaPlayer.start();

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
        else if(intent.getAction().equals((MainActivity.ACTION_PAUSE))){
            auto_reconnect=false;
            mediaPlayer.pause();
            Log.i("MyActivity", "MediaPlayer Pausado " + mediaPlayer.isPlaying());
        }


        return Service.START_NOT_STICKY;

    }


    private void handleConnectivity(Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo != null && netInfo.isConnected()) {
            Log.i("MyActivity", "esta conectado " + intent.getStringExtra("extraInfo") + " mas datos: " + intent.getParcelableExtra("otherNetwork"))  ;
            if (auto_reconnect) {

                mediaPlayer.reset();
                initMediaPlayer(15f);

            }
        } else {
            Log.i("MyActivity", "esta desconectado " + intent.getStringExtra("extraInfo"))  ;
        }
    }

    private void sendToActivity(String command) {
        Intent intent = new Intent("sendMessage");
        intent.putExtra("command", command);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

   public void initMediaPlayer(final Float init_volume) {
        Log.i("MyActivity", "Init Player ");
       sendToActivity(MainActivity.STATUS_CONNECTING);
        this.mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            Log.i("MyActivity", "error en el setdatasrouce " + e.getMessage() + e.getLocalizedMessage());
        }
        mediaPlayer.prepareAsync();
        Toast.makeText(getApplicationContext(), "Conectando...", Toast.LENGTH_LONG).show();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setVolume(init_volume, init_volume);
                if (auto_reconnect) {
                    Log.i("MyActivity", "Por start");
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
                Log.i("myActivity", "MediaPlayer.OnInfoListener: " + what + " Extra: " + extra);
                return false;
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        Log.i("MyApplication", "Error: " + what + " Extra: " + extra);
        mediaPlayer.reset();
        if (what == 100 || what == -110) {
            new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    initMediaPlayer(10f);
                }
            }.start();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MyActivity", "Service On destroy ");
        unregisterReceiver(mConnReceiver);
        mediaPlayer.release();
        mediaPlayer = null;
    }
}